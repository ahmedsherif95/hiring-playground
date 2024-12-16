package com.celfocus.hiring.kickstarter;

import com.celfocus.hiring.kickstarter.api.CartService;
import com.celfocus.hiring.kickstarter.api.dto.CartItemInput;
import com.celfocus.hiring.kickstarter.db.entity.CartEntity;
import com.celfocus.hiring.kickstarter.db.entity.CartItemEntity;
import com.celfocus.hiring.kickstarter.db.entity.CartItemPK;
import com.celfocus.hiring.kickstarter.db.entity.ProductEntity;
import com.celfocus.hiring.kickstarter.db.repo.CartItemRepository;
import com.celfocus.hiring.kickstarter.db.repo.CartRepository;
import com.celfocus.hiring.kickstarter.db.repo.ProductRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addItemToCart_NewItem_ShouldAddItem() {
        // Arrange
        String username = "user1";
        CartItemInput itemInput = new CartItemInput("ITEM001");
        CartEntity cartEntity = new CartEntity();
        cartEntity.setId(1L);
        cartEntity.setUserId(username);

        ProductEntity product = new ProductEntity();
        product.setSku(itemInput.itemId());
        product.setPrice(BigDecimal.valueOf(100));

        when(cartRepository.findByUserId(username)).thenReturn(Optional.of(cartEntity));
        when(cartItemRepository.findById(any(CartItemPK.class))).thenReturn(Optional.empty());
        when(productRepository.findBySku(itemInput.itemId())).thenReturn(Optional.of(product));

        // Act
        cartService.addItemToCart(username, itemInput);

        // Assert
        verify(cartItemRepository, times(1)).save(argThat(item ->
                item.getItemId().equals(itemInput.itemId()) &&
                        item.getCartId().equals(cartEntity.getId())
        ));
    }

    @Test
    void addItemToCart_ExistingItem_ShouldUpdateQuantity() {
        // Arrange
        String username = "user1";
        CartItemInput itemInput = new CartItemInput("ITEM001");
        CartEntity cartEntity = new CartEntity();
        cartEntity.setId(1L);
        cartEntity.setUserId(username);

        CartItemEntity existingItem = new CartItemEntity();
        existingItem.setItemId(itemInput.itemId());
        existingItem.setQuantity(2);
        existingItem.setCartId(cartEntity.getId());

        when(cartRepository.findByUserId(username)).thenReturn(Optional.of(cartEntity));
        when(cartItemRepository.findById(any(CartItemPK.class))).thenReturn(Optional.of(existingItem));

        // Act
        cartService.addItemToCart(username, itemInput);

        // Assert
        verify(cartItemRepository, times(1)).save(argThat(item ->
                item.getQuantity() == 3
        ));
    }

    @Test
    void clearCart_ShouldDeleteCart() {
        // Arrange
        String username = "user1";

        // Act
        cartService.clearCart(username);

        // Assert
        verify(cartRepository, times(1)).deleteByUserId(username);
    }

    @Test
    void removeItemFromCart_ShouldDeleteItem() {
        // Arrange
        String username = "testUser";
        String itemId = "item123";
        Long cartId = 1L;

        CartEntity mockCart = new CartEntity();
        mockCart.setId(cartId);
        mockCart.setUserId(username);

        when(cartRepository.findByUserId(username)).thenReturn(Optional.of(mockCart));

        // Act
        cartService.removeItemFromCart(username, itemId);

        // Assert
        verify(cartItemRepository).deleteById(eq(new CartItemPK(itemId, cartId)));
    }


    @Test
    void addItemToCart_ConcurrentModification_ShouldThrowException() {
        // Arrange
        String username = "user1";
        CartItemInput itemInput = new CartItemInput("ITEM001");
        CartEntity cartEntity = new CartEntity();
        cartEntity.setId(1L);
        cartEntity.setUserId(username);

        ProductEntity product = new ProductEntity();
        product.setSku(itemInput.itemId());
        product.setPrice(BigDecimal.valueOf(100));

        when(cartRepository.findByUserId(username)).thenReturn(Optional.of(cartEntity));
        when(cartItemRepository.findById(any(CartItemPK.class))).thenReturn(Optional.empty());
        when(productRepository.findBySku(itemInput.itemId())).thenReturn(Optional.of(product));
        doThrow(OptimisticLockException.class).when(cartItemRepository).save(any(CartItemEntity.class));

        // Act & Assert
        assertThrows(ConcurrentModificationException.class, () -> cartService.addItemToCart(username, itemInput));
    }
}
