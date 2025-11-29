package com.maono.marketapplication.models.mappers;

import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.models.OrderItem;
import com.maono.marketapplication.models.dto.responses.OrderDto;
import com.maono.marketapplication.models.dto.responses.OrderItemDto;

public class OrderDtoMapper {
    public static OrderDto mapToOrderDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .items(order.getItems().stream().map(OrderDtoMapper::mapToOrderItemDto).toList())
                .totalSum(order.getTotalSum())
                .build();
    }

    public static OrderItemDto mapToOrderItemDto(OrderItem orderItem) {
        return OrderItemDto.builder()
                .id(orderItem.getProduct().getId())
                .title(orderItem.getProduct().getTitle())
                .price(orderItem.getProduct().getPrice())
                .count(orderItem.getCount())
                .build();
    }
}
