package com.maono.marketapplication.repositories.implemantations;

import com.maono.marketapplication.models.Order;
import com.maono.marketapplication.models.OrderItem;
import com.maono.marketapplication.models.Product;
import com.maono.marketapplication.repositories.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final DatabaseClient databaseClient;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    /**
     * This class describes the strings of the combined order, orderItems, and products.
     */
    @Getter @Setter
    private static final class OrderItemsOrderRow {
        // Order Data
        private Long orderId;
        private BigDecimal totalSum;

        // Product Data
        private Long productId;
        private String title;
        private String description;
        private String imageName;
        private BigDecimal price;

        // OrderItem Data
        private Integer count;
    }

    @Override
    public Flux<Order> findAllWithRelations() {
        String SQL = """
                     SELECT o.id AS order_id,
                         o.total_sum,
                         p.id AS product_id,
                         p.title,
                         p.description,
                         p.image_name,
                         p.price,
                         oi.count
                      FROM orders AS o
                      JOIN order_items AS oi
                          ON o.id=oi.order_id
                      JOIN products AS p
                          ON oi.product_id=p.id;
                     """;
        return getRows(databaseClient.sql(SQL))
                .collectList()
                .flatMapMany(orderItemsOrderRows -> {
                    if (orderItemsOrderRows.isEmpty()) {
                        return Flux.empty();
                    } else {
                        List<Order> orders = new ArrayList<>();
                        Map<Long, List<OrderItemsOrderRow>> grouped =
                                orderItemsOrderRows
                                        .stream()
                                        .collect(Collectors.groupingBy(OrderItemsOrderRow::getOrderId));

                        for (var group : grouped.entrySet()) {
                            orders.add(mapOrder(group.getValue()));
                        }
                        return Flux.fromIterable(orders);
                    }
                });
    }

    @Override
    public Mono<Order> findByIdWithRelations(Long id) {
        String SQL = """
                     SELECT o.id AS order_id,
                         o.total_sum,
                         p.id AS product_id,
                         p.title,
                         p.description,
                         p.image_name,
                         p.price,
                         oi.count
                      FROM orders AS o
                      JOIN order_items AS oi
                          ON o.id=oi.order_id
                      JOIN products AS p
                          ON oi.product_id=p.id
                      WHERE o.id=:id
                     """;
        return getRows(databaseClient.sql(SQL).bind("id", id))
                .collectList()
                .flatMap(rows -> {
                    if (rows.isEmpty()) {
                        return Mono.empty();
                    }
                    return Mono.just(mapOrder(rows));
                });
    }

    @Override
    public Mono<Order> save(Order order) {
        return r2dbcEntityTemplate.insert(order);
    }

    private Order mapOrder(List<OrderItemsOrderRow> rows) {
        Order order = new Order();

        order.setId(rows.getFirst().getOrderId());
        order.setTotalSum(rows.getFirst().getTotalSum());
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemsOrderRow row : rows) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(row.getOrderId());
            orderItem.setProductId(row.getProductId());
            orderItem.setCount(row.getCount());
            orderItem.setProduct(Product
                    .builder()
                    .id(row.getProductId())
                    .title(row.getTitle())
                    .description(row.getDescription())
                    .price(row.getPrice())
                    .imageName(row.getImageName())
                    .build());
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);
        return order;
    }

    private Flux<OrderItemsOrderRow> getRows(DatabaseClient.GenericExecuteSpec spec) {
        return spec
                .map((row, rowMetadata) -> {
                    OrderItemsOrderRow orderItemsOrderRow = new OrderItemsOrderRow();
                    orderItemsOrderRow.setOrderId(row.get("order_id", Long.class));
                    orderItemsOrderRow.setTotalSum(row.get("total_sum", BigDecimal.class));
                    orderItemsOrderRow.setProductId(row.get("product_id", Long.class));
                    orderItemsOrderRow.setTitle(row.get("title", String.class));
                    orderItemsOrderRow.setDescription(row.get("description", String.class));
                    orderItemsOrderRow.setImageName(row.get("image_name", String.class));
                    orderItemsOrderRow.setPrice(row.get("price", BigDecimal.class));
                    orderItemsOrderRow.setCount(row.get("count", Integer.class));
                    return orderItemsOrderRow;
                })
                .all();
    }
}
