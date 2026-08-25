package ru.otus.hw.domain;

import java.math.BigDecimal;

public record MenuItem(String id, String title, BigDecimal price, boolean available) {

}
