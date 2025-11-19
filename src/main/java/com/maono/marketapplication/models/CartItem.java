package com.maono.marketapplication.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("cart_items")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
@EqualsAndHashCode(exclude = "isNew")
public class CartItem implements Persistable<Long> {
    @Id
    @Column("product_id")
    private Long id;

    @Transient
    private Product product;

    @Column("count")
    private int count;

    @Transient
    private boolean isNew;

    public CartItem(Long id, int count) {
        this.id = id;
        this.count = count;
    }

    public CartItem(Product product, int count, boolean isNew) {
        this.id = product.getId();
        this.product = product;
        this.count = count;
        this.isNew = isNew;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

}
