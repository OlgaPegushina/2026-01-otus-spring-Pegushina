package ru.otus.hw.service;

import org.springframework.stereotype.Service;
import ru.otus.hw.domain.Order;
import ru.otus.hw.domain.OrderStatus;

@Service
public class PaymentService {
    public Order payByCard(Order order) {
        return order.withStatus(OrderStatus.PAID);
    }

    public Order payCash(Order order) {
        return order.withStatus(OrderStatus.PAID);
    }
}
