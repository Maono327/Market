package com.maono.marketapplication.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("order_items")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
@EqualsAndHashCode(exclude = {"order", "product"})
@ToString(exclude = "order")
public class OrderItem {
    @Column("order_id")
    private Long orderId;
    @Column("product_id")
    private Long productId;

    @Transient
    private Order order;

    @Transient
    private Product product;

    @Column("count")
    private int count;

}
