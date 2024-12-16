package com.celfocus.hiring.kickstarter.db.entity;

import com.celfocus.hiring.kickstarter.domain.CartItem;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "TB_CART_ITEM")
@IdClass(CartItemPK.class)
public class CartItemEntity extends CartItem implements Serializable {

    private CartEntity cart;

    @Version
    private Integer version;

    public CartItemEntity() {
    }

    @Id
    @Column(name = "CART_ITEM_ID")
    @Override
    public String getItemId() {
        return super.getItemId();
    }

    @Id
    @Column(name = "CART_ID")
    @Override
    public Long getCartId() {
        return super.getCartId();
    }

    @Column(name = "PRICE")
    @Override
    public BigDecimal getPrice() {
        return super.getPrice();
    }

    @Column(name = "QUANTITY")
    @Override
    public Integer getQuantity() {
        return super.getQuantity();
    }

    @JoinColumn(name = "CART_ID", insertable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    public CartEntity getCart() {
        return cart;
    }

    public void setCart(CartEntity cart) {
        this.cart = cart;
    }
}
