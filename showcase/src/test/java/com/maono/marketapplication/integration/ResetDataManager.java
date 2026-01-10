package com.maono.marketapplication.integration;

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

public class ResetDataManager {
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    public ResetDataManager(R2dbcEntityTemplate r2dbcEntityTemplate) {
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
    }

    public void resetOrders() {
        String SQL =
                """
                TRUNCATE TABLE orders, order_items RESTART IDENTITY;
                
                INSERT INTO orders(total_sum) VALUES (3399.97);
                INSERT INTO orders(total_sum) VALUES (36699.93);
                
                INSERT INTO order_items(order_id, product_id, count) VALUES (1, 1, 2);
                INSERT INTO order_items(order_id, product_id, count) VALUES (1, 4, 1);
                
                INSERT INTO order_items(order_id, product_id, count) VALUES (2, 1, 3);
                INSERT INTO order_items(order_id, product_id, count) VALUES (2, 3, 1);
                INSERT INTO order_items(order_id, product_id, count) VALUES (2, 4, 1);
                INSERT INTO order_items(order_id, product_id, count) VALUES (2, 5, 2);
                """;

        r2dbcEntityTemplate.getDatabaseClient().sql(SQL).then().block();
    }

    public void resetCartItems() {
        String SQL =
                """
                TRUNCATE TABLE cart_items RESTART IDENTITY;
                
                INSERT INTO cart_items(product_id, count) VALUES (1, 3);
                INSERT INTO cart_items(product_id, count) VALUES (2, 1);
                INSERT INTO cart_items(product_id, count) VALUES (5, 2);
                """;

        r2dbcEntityTemplate.getDatabaseClient().sql(SQL).then().block();
    }

    public void resetAll() {
        String SQL =
                """
                TRUNCATE TABLE cart_items, orders, order_items, products RESTART IDENTITY;
                
                INSERT INTO products(title, description, image_name, price) VALUES ('Книга', 'Интересная книга', 'book.png', 699.99);
                INSERT INTO products(title, description, image_name, price) VALUES ('Портфель', 'Удобный и красивый портфель', 'briefcase.png', 4000.00);
                INSERT INTO products(title, description, image_name, price) VALUES ('Polaroid', 'Для крутых фотографий', 'polaroid.png', 6599.99);
                INSERT INTO products(title, description, image_name, price) VALUES ('Зонтик', 'Красивый и прозрачный зонт', 'umbrella.png', 1999.99);
                INSERT INTO products(title, description, image_name, price) VALUES ('Ваза', 'Дизайнерская ваза', 'vase.png', 12999.99);
                
                INSERT INTO cart_items(product_id, count) VALUES (1, 3);
                INSERT INTO cart_items(product_id, count) VALUES (2, 1);
                INSERT INTO cart_items(product_id, count) VALUES (5, 2);
                
                INSERT INTO orders(total_sum) VALUES (3399.97);
                INSERT INTO orders(total_sum) VALUES (36699.93);
                
                INSERT INTO order_items(order_id, product_id, count) VALUES (1, 1, 2);
                INSERT INTO order_items(order_id, product_id, count) VALUES (1, 4, 1);
                
                INSERT INTO order_items(order_id, product_id, count) VALUES (2, 1, 3);
                INSERT INTO order_items(order_id, product_id, count) VALUES (2, 3, 1);
                INSERT INTO order_items(order_id, product_id, count) VALUES (2, 4, 1);
                INSERT INTO order_items(order_id, product_id, count) VALUES (2, 5, 2);
                """;

        r2dbcEntityTemplate.getDatabaseClient().sql(SQL).then().block();
    }
}
