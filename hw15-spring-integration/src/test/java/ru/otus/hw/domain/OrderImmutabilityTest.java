package ru.otus.hw.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;

class OrderImmutabilityTest {

    @Test
    void withStatusShouldCreateNewInstanceAndNotAffectOriginal() {
        Order originalOrder = Order.builder()
                .customerId("cust-1")
                .status(OrderStatus.NEW)
                .items(List.of(new OrderItem("p1", "Пицца", 1, BigDecimal.TEN)))
                .build();

        Order paidOrder = originalOrder.withStatus(OrderStatus.PAID);

        assertAll(
                () -> assertEquals(OrderStatus.NEW, originalOrder.getStatus(),
                        "Статус оригинального заказа не должен меняться"),

                () -> assertEquals(OrderStatus.PAID, paidOrder.getStatus(),
                        "Новый заказ должен иметь обновлённый статус"),

                () -> assertNotSame(originalOrder, paidOrder,
                        "withStatus() должен возвращать новый экземпляр объекта"),

                () -> assertEquals(originalOrder.getCustomerId(), paidOrder.getCustomerId())
        );
    }
}
