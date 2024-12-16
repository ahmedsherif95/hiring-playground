package com.celfocus.hiring.kickstarter;

import com.celfocus.hiring.kickstarter.api.CartService;
import com.celfocus.hiring.kickstarter.api.dto.CartItemInput;
import com.celfocus.hiring.kickstarter.api.dto.CartResponse;
import com.celfocus.hiring.kickstarter.controllers.CartAPIController;
import com.celfocus.hiring.kickstarter.db.entity.CartItemEntity;
import com.celfocus.hiring.kickstarter.db.repo.CartRepository;
import com.celfocus.hiring.kickstarter.domain.Cart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartAPIControllerTest {

    @Mock
    private CartRepository cartRepository;  // Mock the repository

    @Mock
    private CartService cartService;  // Mock the service

    @InjectMocks
    private CartAPIController cartAPIController;

    @Test
    void addItemToCart_ShouldReturn201() {
        // Arrange
        String username = "user1";
        CartItemInput itemInput = new CartItemInput("ITEM001");

        // Mock the Authentication object
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username); // Mock the username
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);  // Set the mocked SecurityContext

        // Act
        ResponseEntity<Void> response = cartAPIController.addItemToCart(username, itemInput);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(cartService, times(1)).addItemToCart(username, itemInput);
    }

    @Test
    void clearCart_ShouldReturn204() {
        // Arrange
        String username = "user1";

        // Mock the Authentication object
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username); // Mock the username
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);  // Set the mocked SecurityContext

        // Act
        ResponseEntity<Void> response = cartAPIController.clearCart(username);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(cartService, times(1)).clearCart(username);
    }


    @Test
    void getCart_ShouldReturnCart() {
        // Arrange
        String username = "user1";

        // Mock the Authentication object
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(username); // Mock the username
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);  // Set the mocked SecurityContext

        // Create a mock Cart<CartItemEntity>
        Cart<CartItemEntity> mockCart = new Cart<>();
        mockCart.setUserId(username);

        // Mock the cartRepository to return an Optional containing the mockCart
        when(cartRepository.findByUserId(username)).thenReturn(Optional.of(mockCart));

        // Mock the cartService to return the mockCart when getCart is called
        when(cartService.getCart(username)).thenReturn(mockCart);

        // Act
        ResponseEntity<CartResponse> response = cartAPIController.getCart(username);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cartService, times(1)).getCart(username);
        verify(cartRepository, times(1)).findByUserId(username);  // Verify repository call
    }
}
