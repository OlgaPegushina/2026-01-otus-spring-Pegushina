package ru.otus.hw.service;

import org.springframework.stereotype.Service;
import ru.otus.hw.domain.MenuItem;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class CatalogService {
    private final Map<String, MenuItem> menu = Map.of(
            "p1", new MenuItem("p1", "Пицца Маргарита", new BigDecimal("550.00"), true),
            "d1", new MenuItem("d1", "Кола 0.5", new BigDecimal("120.00"), true),
            "x1", new MenuItem("x1", "Редкий соус (нет в наличии)", new BigDecimal("999.00"), false)
    );

    public MenuItem get(String menuItemId) {
        var item = menu.get(menuItemId);
        if (item == null) {
            throw new IllegalArgumentException("Неизвестное блюдо: " + menuItemId);
        }
        return item;
    }
}
