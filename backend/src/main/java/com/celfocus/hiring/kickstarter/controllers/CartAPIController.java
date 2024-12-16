package com.celfocus.hiring.kickstarter.api;

import com.celfocus.hiring.kickstarter.api.CartAPI;
import com.celfocus.hiring.kickstarter.api.CartService;
import com.celfocus.hiring.kickstarter.api.ProductService;
import com.celfocus.hiring.kickstarter.api.dto.CartItemInput;
import com.celfocus.hiring.kickstarter.api.dto.CartItemResponse;
import com.celfocus.hiring.kickstarter.api.dto.CartResponse;
import com.celfocus.hiring.kickstarter.domain.Cart;
import com.celfocus.hiring.kickstarter.domain.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping(CartAPIController.CARTS_PATH)
public class CartAPIController implements CartAPI {

    static final String CARTS_PATH = "/api/v1/carts";

    private final CartService cartService;
    private final ProductService productService;

    @Autowired
    public CartAPIController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @GetMapping("/")
    public String index() {
        return "Greetings from Celfocus!";
    }

    @Override
    public ResponseEntity<Void> addItemToCart(String username, CartItemInput itemInput) {
        cartService.addItemToCart(username, itemInput);
        return ResponseEntity.status(201).build();
    }

    @Override
    public ResponseEntity<Void> clearCart(String username) {
        cartService.clearCart(username);
        return ResponseEntity.status(204).build();
    }

    @Override
    @PreAuthorize("isAuthenticated() and hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CartResponse> getCart(String username) {
        validateUserAccess(username);
        var cart = cartService.getCart(username);
        return ResponseEntity.ok(mapToCartResponse(cart));
    }

    @Override
    public ResponseEntity<Void> removeItemFromCart(String username, String itemId) {
        cartService.removeItemFromCart(username, itemId);
        return ResponseEntity.status(204).build();
    }

    private CartResponse mapToCartResponse(Cart<? extends CartItem> cart) {
        return new CartResponse(cart.getItems().stream().map(this::mapToCartItemResponse).collect(Collectors.toList()));
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        var product = productService.getProduct(item.getItemId());
        return new CartItemResponse(item.getItemId(), item.getQuantity(), product.orElseThrow().getPrice(), product.orElseThrow().getName());
    }

    public void validateUserAccess(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInUsername = authentication.getName();

        if (!loggedInUsername.equals(username)) {
            throw new SecurityException("You are not authorized to access this cart.");
        }
    }
}
