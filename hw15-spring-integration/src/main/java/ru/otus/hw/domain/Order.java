package ru.otus.hw.domain;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Value //--оригинальный заказ не будет меняться для безопасности использования в потоках
@Builder(toBuilder = true)
public class Order {
    @Builder.Default
    private final String orderId = UUID.randomUUID().toString();

    @Builder.Default
    @With
    private final OrderStatus status = OrderStatus.NEW;

    private final String customerId;

    private final String restaurantId;

    private final PaymentMethod paymentMethod;

    private final String address;

    @Builder.Default
    private final List<OrderItem> items = new ArrayList<>();

    @With
    @Builder.Default
    private final BigDecimal total = BigDecimal.ZERO;
}
