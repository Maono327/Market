package com.maono.marketapplication.util;

import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.models.OrderItem;
import com.maono.marketapplication.models.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.maono.marketapplication.util.ExpectedProductsTestDataProvider.productByIdTemplate;


public class ExpectedOrderAndOrderItemsTestDataProvider {

    public static class StagedOrderTestDataBuilder {
        private final Order order;
        private final boolean autoCalculateTotalSum;

        public StagedOrderTestDataBuilder(Order order, boolean autoCalculateTotalSum) {
            this.order = order;
            this.autoCalculateTotalSum = autoCalculateTotalSum;
        }

        public StagedOrderAndOrderItemsTestDataBuilder withOrderItems() {
            return new StagedOrderAndOrderItemsTestDataBuilder(this, order);
        }

        public Order get() {
            if (autoCalculateTotalSum && !order.getItems().isEmpty()) {
                BigDecimal totalSum = order.getItems().stream()
                        .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getCount())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                order.setTotalSum(totalSum);
            }
            return order;
        }
    }

    public static StagedOrderTestDataBuilder order(Long id, BigDecimal totalSum) {
        Order order = Order.builder()
                .id(id)
                .totalSum(totalSum)
                .build();

        return new StagedOrderTestDataBuilder(order, false);
    }

    public static StagedOrderTestDataBuilder order(Long id) {
        Order order = Order.builder()
                .id(id)
                .build();

        return new StagedOrderTestDataBuilder(order, true);
    }

    public static class StagedOrderAndOrderItemsTestDataBuilder {
        private final StagedOrderTestDataBuilder orderTestDataBuilder;
        private final Order order;
        private final List<OrderItem> orderItems;

        public StagedOrderAndOrderItemsTestDataBuilder(StagedOrderTestDataBuilder orderTestDataBuilder, Order order) {
            this.orderTestDataBuilder = orderTestDataBuilder;
            this.order = order;
            orderItems = new ArrayList<>();
        }

        public StagedOrderAndOrderItemsTestDataBuilder orderItem(Long productId, int count) {
            Product p = productByIdTemplate(productId).get();
            OrderItem item = OrderItem.builder()
                    .orderId(order.getId())
                    .order(order)
                    .productId(productId)
                    .product(p)
                    .count(count)
                    .build();
            orderItems.add(item);
            return this;
        }

        public StagedOrderAndOrderItemsTestDataBuilder orderItem(Product product, int count) {
            OrderItem item = OrderItem.builder()
                    .orderId(order.getId())
                    .order(order)
                    .productId(product.getId())
                    .product(product)
                    .count(count)
                    .build();
            orderItems.add(item);
            return this;
        }

        public StagedOrderTestDataBuilder getItems() {
            order.setItems(orderItems);
            return orderTestDataBuilder;
        }
    }
}
