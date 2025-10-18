package com.maono.marketapplication.models;

import com.maono.marketapplication.models.util.OrderItemId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "order_items")
@Getter @Setter
@ToString(exclude = {"order", "product"})
@EqualsAndHashCode(exclude = "order")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @EmbeddedId
    @Builder.Default
    private OrderItemId id = new OrderItemId();

    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "count", nullable = false)
    private int count;

    public void setOrder(Order order) {
        this.order = order;
        if (order != null) this.id.setOrderId(order.getId());
    }
    public void setProduct(Product product) {
        this.product = product;
        if (product != null) this.id.setProductId(product.getId());
    }
}
