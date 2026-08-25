package ru.otus.hw.service;

import org.springframework.stereotype.Service;
import ru.otus.hw.domain.OrderItem;

@Service
public class AvailabilityService {
    private final CatalogService catalogService;

    public AvailabilityService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public OrderItem check(OrderItem item) {
        var menu = catalogService.get(item.getMenuItemId());
        item.setAvailable(menu.available());
        return item;
    }
}
