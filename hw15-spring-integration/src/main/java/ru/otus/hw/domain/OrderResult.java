package ru.otus.hw.domain;

import java.math.BigDecimal;

public record OrderResult(String orderId, OrderStatus status, BigDecimal total, String message) {

}
