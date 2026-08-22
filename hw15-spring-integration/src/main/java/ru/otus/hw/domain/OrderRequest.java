package ru.otus.hw.domain;

import java.util.List;

public record OrderRequest(
    String customerId,
    String restaurantId,
    List<OrderItemRequest> items,
    PaymentMethod paymentMethod,
    String address
) {

}
