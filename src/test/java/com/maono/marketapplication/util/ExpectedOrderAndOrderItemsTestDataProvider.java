package com.maono.marketapplication.util;

import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.models.OrderItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.models.dto.OrderDto;
import com.maono.marketapplication.models.dto.OrderItemDto;
import com.maono.marketapplication.models.util.OrderItemId;
import org.springframework.data.util.Pair;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.buildProductById;

public class ExpectedOrderAndOrderItemsTestDataProvider {

    public static List<Order> buildOrderList(Long... orders) {
        return Arrays.stream(orders).map(ExpectedOrderAndOrderItemsTestDataProvider::buildOrderById).toList();
    }

    public static Order buildOrderById(long orderId) {
        if (orderId == 1L) return buildOrder(1L, Pair.of(1L, 2), Pair.of(4L, 1));
        if (orderId == 2L) return buildOrder(2L, Pair.of(1L, 3), Pair.of(3L, 1), Pair.of(4L, 1), Pair.of(5L, 2));
        return null;
    }

    public static Order buildOrder(Pair<Long, Integer>... products) {
        Order order = new Order();

        List<OrderItem> orderItems = Arrays.stream(products)
                .map(product ->
                        buildOrderItem(order, buildProductById(product.getFirst()), product.getSecond()))
                .toList();
        order.setItems(orderItems);

        order.setTotalSum(orderItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return order;
    }

    public static Order buildOrder(Long orderId, Pair<Long, Integer>... products) {
        Order order = new Order();
        order.setId(orderId);

        List<OrderItem> orderItems = Arrays.stream(products)
                .map(product -> buildOrderItem(order, product.getFirst(), product.getSecond()))
                .toList();

        order.setItems(orderItems);
        order.setTotalSum(orderItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return order;
    }

    public static OrderItem buildOrderItem(Order order, Long productId, int count) {
        OrderItemId orderItemId = new OrderItemId(order.getId(), productId);
        return OrderItem.builder()
                .id(orderItemId)
                .order(order)
                .product(buildProductById(productId))
                .count(count)
                .build();
    }

    public static OrderItem buildOrderItem(Order order, Product product, int count) {
        OrderItemId orderItemId = new OrderItemId();
        return OrderItem.builder()
                .id(orderItemId)
                .order(order)
                .product(product)
                .count(count)
                .build();
    }

    public static Order buildOrderWithManagedEntities(Pair<Product, Integer>... products) {
        Order order = new Order();

        List<OrderItem> orderItems = Arrays.stream(products)
                .map(product ->
                        buildOrderItemWithManagedEntities(order, product.getFirst(), product.getSecond()))
                .toList();
        order.setItems(orderItems);

        order.setTotalSum(orderItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return order;
    }

    public static OrderItem buildOrderItemWithManagedEntities(Order order, Product product, int count) {
        OrderItemId orderItemId = new OrderItemId();
        return OrderItem.builder()
                .id(orderItemId)
                .order(order)
                .product(product)
                .count(count)
                .build();
    }

    public static OrderDto mapToOrderDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .items(order.getItems().stream()
                        .map(ExpectedOrderAndOrderItemsTestDataProvider::mapToOrderItemDto)
                        .toList())
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
