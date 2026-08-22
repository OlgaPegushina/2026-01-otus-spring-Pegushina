package ru.otus.hw.service;

import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Order;
import ru.otus.hw.domain.OrderItem;
import ru.otus.hw.domain.OrderRequest;
import ru.otus.hw.domain.OrderResult;

import java.math.BigDecimal;

@Service
public class OrderService {
    private final CatalogService catalogService;

    public OrderService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public Order createDraft(OrderRequest req) {
        var items = req.items().stream()
                .map(r -> {
                    var menu = catalogService.get(r.menuItemId());
                    return new OrderItem(menu.id(), menu.title(), r.qty(), menu.price());
                })
                .toList();

        return Order.builder()
                .customerId(req.customerId())
                .restaurantId(req.restaurantId())
                .paymentMethod(req.paymentMethod())
                .address(req.address())
                .items(items)
                .build();
    }

    public Order calculateTotal(Order order) {
        var total = order.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return order.withTotal(total);
    }

    public OrderResult toResult(Order order, String message) {
        return new OrderResult(order.getOrderId(), order.getStatus(), order.getTotal(), message);
    }

    public boolean validateRequest(OrderRequest req) {
        return req.items() != null && !req.items().isEmpty()
               && req.address() != null && !req.address().isBlank();
    }
}