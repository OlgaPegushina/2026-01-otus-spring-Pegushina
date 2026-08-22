package ru.otus.hw.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class OrderItem {
    private final String menuItemId;

    private final String title;

    private final int qty;

    private final BigDecimal price;

    @Setter
    private boolean available = true;
}
