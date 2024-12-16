package com.celfocus.hiring.kickstarter.api;

import com.celfocus.hiring.kickstarter.api.dto.CartItemInput;
import com.celfocus.hiring.kickstarter.db.entity.CartEntity;
import com.celfocus.hiring.kickstarter.db.entity.CartItemEntity;
import com.celfocus.hiring.kickstarter.db.entity.CartItemPK;
import com.celfocus.hiring.kickstarter.db.repo.CartItemRepository;
import com.celfocus.hiring.kickstarter.db.repo.CartRepository;
import com.celfocus.hiring.kickstarter.db.repo.ProductRepository;
import com.celfocus.hiring.kickstarter.domain.Cart;
import com.celfocus.hiring.kickstarter.domain.CartItem;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ConcurrentModificationException;

@Service
@Transactional
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public void addItemToCart(String username, CartItemInput itemInput) {
        var cart = cartRepository.findByUserId(username).orElseGet(() -> {
            var newCart = new CartEntity();
            newCart.setUserId(username);
            return cartRepository.save(newCart);
        });

        cartItemRepository.findById(new CartItemPK(itemInput.itemId(), cart.getId()))
                .ifPresentOrElse((item) -> updateItemQuantity(item, 1), () -> {
                    addNewItemToCart(itemInput, cart);
                });
    }

    private void addNewItemToCart(CartItemInput itemInput, CartEntity cart) {
        var product = productRepository.findBySku(itemInput.itemId())
                .orElseThrow(() -> new RuntimeException("Cart Item not found"));
        var cartItem = new CartItemEntity();
        cartItem.setQuantity(1);
        cartItem.setItemId(itemInput.itemId());
        cartItem.setCartId(cart.getId());
        cartItem.setCart(cart);
        cartItem.setPrice(product.getPrice());
        try {
            cartItemRepository.save(cartItem);
        } catch (OptimisticLockException e) {
            throw new ConcurrentModificationException("The item you are trying to add has been modified by another user.");
        }
    }

    private void updateItemQuantity(CartItemEntity item, int byCount) {
        try {
            setItemQuantity(item, item.getQuantity() + byCount);
        } catch (OptimisticLockException e) {
            throw new ConcurrentModificationException("The cart item was modified by another user.");
        }    }

    private void setItemQuantity(CartItemEntity item, int quantity) {
        item.setQuantity(quantity);
        cartItemRepository.save(item);
    }

    public void clearCart(String username) {
        try {
            cartRepository.deleteByUserId(username);
        } catch (OptimisticLockException e) {
            throw new ConcurrentModificationException("The cart was modified by another user while attempting to clear it.");
        }
    }

    public Cart<? extends CartItem> getCart(String username) {
        return cartRepository.findByUserId(username)
                .map(this::mapToCart)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }

    public void removeItemFromCart(String username, String itemId) {
        try {
            cartRepository.findByUserId(username)
                    .ifPresent(cart -> cartItemRepository.deleteById(new CartItemPK(itemId, cart.getId())));
        } catch (OptimisticLockException e) {
            throw new ConcurrentModificationException("The cart item was modified by another user while attempting to remove it.");
        }
    }


    private Cart<? extends CartItem> mapToCart(CartEntity cartEntity) {
        Cart<CartItemEntity> cart = new Cart<>();
        cart.setUserId(cartEntity.getUserId());
        cart.setItems(cartEntity.getItems());
        return cart;
    }
}
