package ru.otus.hw.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import ru.otus.hw.domain.MenuItem;
import ru.otus.hw.domain.Order;
import ru.otus.hw.domain.OrderItemRequest;
import ru.otus.hw.domain.OrderRequest;
import ru.otus.hw.domain.OrderResult;
import ru.otus.hw.domain.OrderStatus;
import ru.otus.hw.domain.PaymentMethod;
import ru.otus.hw.service.CatalogService;
import ru.otus.hw.service.DeliveryService;
import ru.otus.hw.service.KitchenService;
import ru.otus.hw.service.PaymentService;

@SpringBootTest
class FoodIntegrationTest {

    @Autowired
    private OrderGateway orderGateway;

    @MockitoBean
    private CatalogService catalogService;

    @MockitoSpyBean
    private PaymentService paymentService;

    @MockitoSpyBean
    private KitchenService kitchenService;

    @MockitoSpyBean
    private DeliveryService deliveryService;

    @BeforeEach
    void setup() {
        doAnswer(inv -> {
            String id = inv.getArgument(0, String.class);
            return switch (id) {
                case "p1" -> new MenuItem("p1", "Пицца Маргарита", new BigDecimal("550.00"), true);
                case "d1" -> new MenuItem("d1", "Кола 0.5", new BigDecimal("120.00"), true);
                case "x1" -> new MenuItem("x1", "Редкий соус", new BigDecimal("999.00"), false);
                default -> throw new IllegalArgumentException("Неизвестное блюдо: " + id);
            };
        }).when(catalogService).get(anyString());
    }

    @Test
    void shouldProcessValidOrderCardDeliveryCreatedTotalCalculated() {
        OrderRequest req = new OrderRequest(
                "cust-1",
                "rest-1",
                List.of(
                        new OrderItemRequest("p1", 2), // --1100.00
                        new OrderItemRequest("d1", 1)  // --120.00
                ),
                PaymentMethod.CARD,
                "Санкт-Петербург, Невский 10"
        );

        OrderResult result = orderGateway.placeOrder(req);

        assertAll(
                () -> assertNotNull(result),
                () -> assertNotNull(result.orderId()),
                () -> assertFalse(result.orderId().isBlank()),

                () -> assertEquals(OrderStatus.DELIVERY_CREATED, result.status()),
                () -> assertEquals(new BigDecimal("1220.00"), result.total()),
                () -> assertEquals("Заказ обработан", result.message())
        );

        verify(catalogService, atLeastOnce()).get("p1");
        verify(catalogService, atLeastOnce()).get("d1");

        verify(paymentService, times(1)).payByCard(any(Order.class));
        verify(kitchenService, times(1)).sendToKitchen(any(Order.class));
        verify(deliveryService, times(1)).createTask(any(Order.class));
    }

    @Test
    void shouldProcessValidOrderCashDeliveryCreated() {
        OrderRequest req = new OrderRequest(
                "cust-2",
                "rest-1",
                List.of(new OrderItemRequest("p1", 1)),
                PaymentMethod.CASH,
                "Санкт-Петербург, Невский 10"
        );

        OrderResult result = orderGateway.placeOrder(req);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(OrderStatus.DELIVERY_CREATED, result.status()),
                () -> assertEquals(new BigDecimal("550.00"), result.total()),
                () -> assertEquals("Заказ обработан", result.message())
        );

        verify(paymentService, times(1)).payCash(any(Order.class));
        verify(kitchenService, times(1)).sendToKitchen(any(Order.class));
        verify(deliveryService, times(1)).createTask(any(Order.class));
    }

    @Test
    void shouldRejectInvalidOrderWhenNoItems() {
        OrderRequest req = new OrderRequest(
                "cust-3",
                "rest-1",
                List.of(),
                PaymentMethod.CARD,
                "Санкт-Петербург, Невский 10"
        );

        OrderResult result = orderGateway.placeOrder(req);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(OrderStatus.REJECTED, result.status()),
                () -> assertEquals("Заказ невалиден", result.message())
        );

        verifyNoInteractions(catalogService);
        verifyNoInteractions(paymentService);
        verify(kitchenService, never()).sendToKitchen(any());
        verify(deliveryService, never()).createTask(any());
    }

    @Test
    void shouldRejectOrderWhenItemUnavailable() {
        OrderRequest req = new OrderRequest(
                "cust-4",
                "rest-1",
                List.of(
                        new OrderItemRequest("p1", 1),
                        new OrderItemRequest("x1", 1)
                ),
                PaymentMethod.CARD,
                "Санкт-Петербург, Невский 10"
        );

        OrderResult result = orderGateway.placeOrder(req);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(OrderStatus.REJECTED, result.status()),
                () -> assertEquals("Нет в наличии", result.message()),
                () -> assertEquals(BigDecimal.ZERO, result.total())
        );

        verify(catalogService, atLeastOnce()).get("p1");
        verify(catalogService, atLeastOnce()).get("x1");

        verify(paymentService, never()).payByCard(any());
        verify(kitchenService, never()).sendToKitchen(any());
        verify(deliveryService, never()).createTask(any());
    }

    @Test
    void shouldReturnRejectedResultWhenCatalogThrowsUnknownMenuItem() {
        doThrow(new IllegalArgumentException("Неизвестное блюдо: zz"))
                .when(catalogService).get("zz");

        OrderRequest req = new OrderRequest(
                "cust-5",
                "rest-1",
                List.of(new OrderItemRequest("zz", 1)),
                PaymentMethod.CARD,
                "Санкт-Петербург, Невский 10"
        );

        OrderResult result = orderGateway.placeOrder(req);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(OrderStatus.REJECTED, result.status()),
                () -> assertNotNull(result.message()),
                () -> assertTrue(result.message().contains("Неизвестное блюдо"),
                        "message должно содержать причину, было: " + result.message())
        );

        verifyNoInteractions(paymentService);
        verify(kitchenService, never()).sendToKitchen(any());
        verify(deliveryService, never()).createTask(any());
    }

    @Test
    void shouldReturnRejectedResultWhenPaymentThrowsGoesToErrorsFlow() {
        doThrow(new RuntimeException("банк не отвечает"))
                .when(paymentService).payByCard(any(Order.class));

        OrderRequest req = new OrderRequest(
                "cust-6",
                "rest-1",
                List.of(new OrderItemRequest("p1", 1)),
                PaymentMethod.CARD,
                "Санкт-Петербург, Невский 10"
        );

        OrderResult result = orderGateway.placeOrder(req);

        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(OrderStatus.REJECTED, result.status()),
                () -> assertNotNull(result.message()),
                () -> assertTrue(result.message().contains("банк не отвечает"),
                        "message должно содержать причину, было: " + result.message())
        );

        verify(kitchenService, never()).sendToKitchen(any());
        verify(deliveryService, never()).createTask(any());
    }
}