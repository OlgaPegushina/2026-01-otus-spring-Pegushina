package ru.otus.hw.integration;

import org.springframework.integration.annotation.MessagingGateway;
import ru.otus.hw.domain.OrderRequest;
import ru.otus.hw.domain.OrderResult;

@MessagingGateway(
        defaultRequestChannel = "orders.in",
        defaultReplyChannel = "orders.out",
        errorChannel = "orders.errors"
)
public interface OrderGateway {
    OrderResult placeOrder(OrderRequest request);
}
