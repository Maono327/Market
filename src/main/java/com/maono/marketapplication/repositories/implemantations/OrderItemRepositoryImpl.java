package com.maono.marketapplication.repositories.implemantations;

import com.maono.marketapplication.models.OrderItem;
import com.maono.marketapplication.repositories.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepository {
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Void> saveOrderItems(List<OrderItem> orderItems) {
        String SQL = "INSERT INTO order_items(order_id, product_id, count) VALUES (:orderId, :productId, :count)";
        return Flux.fromIterable(orderItems)
                .flatMap(orderItem ->
                        databaseClient.sql(SQL)
                                .bind("orderId", orderItem.getOrderId())
                                .bind("productId", orderItem.getProductId())
                                .bind("count", orderItem.getCount())
                                .fetch().rowsUpdated()).then();
    };
}
