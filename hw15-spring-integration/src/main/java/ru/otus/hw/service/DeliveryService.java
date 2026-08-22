package ru.otus.hw.service;

import org.springframework.stereotype.Service;
import ru.otus.hw.domain.DeliveryTask;
import ru.otus.hw.domain.Order;

@Service
public class DeliveryService {
    public void createTask(Order order) {
        new DeliveryTask(order.getOrderId(), order.getAddress());
    }
}
