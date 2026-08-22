package ru.otus.hw.integration;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.store.MessageGroup;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.messaging.support.MessageBuilder;
import ru.otus.hw.domain.Order;
import ru.otus.hw.domain.OrderItem;
import ru.otus.hw.domain.OrderRequest;
import ru.otus.hw.domain.OrderResult;
import ru.otus.hw.domain.OrderStatus;
import ru.otus.hw.domain.PaymentMethod;
import ru.otus.hw.service.AvailabilityService;
import ru.otus.hw.service.DeliveryService;
import ru.otus.hw.service.KitchenService;
import ru.otus.hw.service.OrderService;
import ru.otus.hw.service.PaymentService;

@Configuration
public class FoodIntegrationConfig {

    private static final String HDR_ORDER = "order";

    @Bean
    public MessageChannel ordersIn() {
        return MessageChannels.direct("orders.in").getObject();
    }

    @Bean
    public MessageChannel ordersOut() {
        return MessageChannels.direct("orders.out").getObject();
    }

    @Bean
    public MessageChannel ordersValid() {
        return MessageChannels.direct("orders.valid").getObject();
    }

    @Bean
    public MessageChannel ordersPayment() {
        return MessageChannels.direct("orders.payment").getObject();
    }

    @Bean
    public MessageChannel ordersAfterPayment() {
        return MessageChannels.direct("orders.afterPayment").getObject();
    }

    @Bean
    public MessageChannel ordersInvalid() {
        return MessageChannels.direct("orders.invalid").getObject();
    }

    @Bean
    public MessageChannel ordersUnavailable() {
        return MessageChannels.direct("orders.unavailable").getObject();
    }

    @Bean
    public MessageChannel ordersErrors() {
        return MessageChannels.direct("orders.errors").getObject();
    }

    @Bean
    public IntegrationFlow orderFlow(OrderService orderService) {
        return IntegrationFlow.from("orders.in")
                .route(OrderRequest.class,
                        req -> orderService.validateRequest(req) ? "VALID" : "INVALID",
                        mapping -> mapping
                                .subFlowMapping("INVALID", sf -> sf
                                        .transform(req -> new OrderResult(
                                                "unknown",
                                                OrderStatus.REJECTED,
                                                null,
                                                "Заказ невалиден"
                                        ))
                                        .channel("orders.out")
                                )
                                .subFlowMapping("VALID", sf -> sf
                                        .handle(OrderRequest.class, (p, h) -> orderService.createDraft(p))
                                        .enrichHeaders(h -> h
                                                .headerFunction(IntegrationMessageHeaderAccessor.CORRELATION_ID,
                                                        m -> ((Order) m.getPayload()).getOrderId())
                                                .headerFunction(HDR_ORDER, Message::getPayload))
                                        .channel("orders.valid")
                                )
                )
                .get();
    }

    @Bean
    public IntegrationFlow itemsFlow(AvailabilityService availabilityService, OrderService orderService) {
        return IntegrationFlow.from("orders.valid")
                .split(Order.class, Order::getItems)
                .handle(OrderItem.class, (i, h) -> availabilityService.check(i))
                .aggregate(a -> a
                        .outputProcessor(this::aggregateBackToSameOrder)
                        .expireGroupsUponCompletion(true))
                .filter(Order.class, o -> o.getStatus() != OrderStatus.REJECTED,
                        f -> f.discardChannel("orders.unavailable"))
                .handle(Order.class, (o, h) -> orderService.calculateTotal(o))
                .channel("orders.payment")
                .get();
    }

    @Bean
    public IntegrationFlow unavailableItemsFlow(OrderService orderService) {
        return IntegrationFlow.from("orders.unavailable")
                .handle(Order.class, (o, h) -> orderService.toResult(o, "Нет в наличии"))
                .channel("orders.out")
                .get();
    }

    @Bean
    public IntegrationFlow paymentFlow(PaymentService paymentService) {
        return IntegrationFlow.from("orders.payment")
                .route(Order.class, Order::getPaymentMethod, r -> r
                        .subFlowMapping(PaymentMethod.CARD, sf -> sf
                                .handle(Order.class, (o, h) -> paymentService.payByCard(o))
                                .channel("orders.afterPayment"))
                        .subFlowMapping(PaymentMethod.CASH, sf -> sf
                                .handle(Order.class, (o, h) -> paymentService.payCash(o))
                                .channel("orders.afterPayment")))
                .get();
    }

    @Bean
    public IntegrationFlow postPaymentFlow(KitchenService kitchenService,
                                           DeliveryService deliveryService,
                                           OrderService orderService) {
        return IntegrationFlow.from("orders.afterPayment")
                .publishSubscribeChannel(ps -> ps
                        .subscribe(sf -> sf.handle(Order.class, (o, h) -> {
                            kitchenService.sendToKitchen(o);
                            return o;
                        }))
                        .subscribe(sf -> sf.handle(Order.class, (o, h) -> {
                            deliveryService.createTask(o);
                            return o;
                        })))
                .handle(Order.class, (o, h) -> {
                    Order finalOrder = o.withStatus(OrderStatus.DELIVERY_CREATED);
                    return orderService.toResult(finalOrder, "Заказ обработан");
                })
                .channel("orders.out")
                .get();
    }

    @Bean
    public IntegrationFlow errorsFlow(OrderService orderService) {
        return IntegrationFlow.from("orders.errors")
                .handle(ErrorMessage.class, (em, h) -> errorToResultMessage(em, orderService))
                .get();
    }

    private Order aggregateBackToSameOrder(MessageGroup g) {
        // --Исходный заказ из хедера
        Message<?> first = g.getOne();
        Order originalOrder = (Order) first.getHeaders().get(HDR_ORDER);

        // -- обработанные items из сообщений группы
        List<OrderItem> processedItems = g.getMessages().stream()
                .map(m -> (OrderItem) m.getPayload())
                .toList();

        // --проверяем доступность
        boolean allAvailable = processedItems.stream().allMatch(OrderItem::isAvailable);
        OrderStatus newStatus = allAvailable ? OrderStatus.NEW : OrderStatus.REJECTED;

        // -- Собираем новый заказ на основе старого, но с новыми items и статусом
        assert originalOrder != null;
        return originalOrder.toBuilder()
                .items(processedItems)
                .status(newStatus)
                .build();
    }

    private Message<OrderResult> errorToResultMessage(ErrorMessage em, OrderService orderService) {
        MessagingException me = (MessagingException) em.getPayload();
        Message<?> failed = me.getFailedMessage();
        Order order = failed != null ? (Order) failed.getHeaders().get(HDR_ORDER) : null;
        if (order == null && failed != null && failed.getPayload() instanceof Order o) {
            order = o;
        }

        String text = me.getCause() != null ? me.getCause().getMessage() : me.getMessage();
        String msg = "Ошибка обработки: " + (text == null ? "unknown" : text);

        OrderResult res = (order == null)
                ? new OrderResult("unknown", OrderStatus.REJECTED, null, msg)
                : orderService.toResult(order.withStatus(OrderStatus.REJECTED), msg);

        var builder = MessageBuilder.withPayload(res);
        if (failed != null) {
            builder.copyHeaders(failed.getHeaders());
        }
        return builder.build();
    }
}