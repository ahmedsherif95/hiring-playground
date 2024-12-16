package com.celfocus.hiring.kickstarter;

import com.celfocus.hiring.kickstarter.api.UserService;
import com.celfocus.hiring.kickstarter.db.entity.UserEntity;
import com.celfocus.hiring.kickstarter.db.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        String username = "user1";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword("password123");
        userEntity.setRoles(List.of("USER"));

        when(userRepository.findByUsername(username)).thenReturn(userEntity);

        // Act
        UserDetails userDetails = userService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        // Convert authorities to String array for comparison
        String[] expectedAuthorities = {"ROLE_USER"};
        String[] actualAuthorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)  // Extract the authority string
                .toArray(String[]::new);

        assertArrayEquals(expectedAuthorities, actualAuthorities);  // Compare authority strings
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        String username = "nonExistentUser";

        when(userRepository.findByUsername(username)).thenReturn(null);

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(username);
        });
        assertEquals("User not found!", exception.getMessage());
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldLogWarning_WhenUserHasNoRoles() {
        // Arrange
        String username = "userNoRoles";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword("password123");
        userEntity.setRoles(List.of());

        when(userRepository.findByUsername(username)).thenReturn(userEntity);

        // Act
        UserDetails userDetails = userService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().isEmpty());
        verify(userRepository, times(1)).findByUsername(username);
        // We can also check the logger if needed, e.g., verify logging was called.
        // In real tests, you could use a mocking framework for the logger.
    }

    @Test
    void loadUserByUsername_ShouldReturnUserWithMultipleRoles() {
        // Arrange
        String username = "userWithRoles";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword("password123");
        userEntity.setRoles(List.of("USER", "ADMIN"));

        when(userRepository.findByUsername(username)).thenReturn(userEntity);

        // Act
        UserDetails userDetails = userService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);

        // Convert authorities to String array for comparison
        String[] expectedAuthorities = {"ROLE_USER", "ROLE_ADMIN"};
        String[] actualAuthorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)  // Extract the authority string
                .toArray(String[]::new);

        assertTrue(actualAuthorities.length == expectedAuthorities.length
                && Arrays.asList(actualAuthorities).containsAll(Arrays.asList(expectedAuthorities))
                && Arrays.asList(expectedAuthorities).containsAll(Arrays.asList(actualAuthorities)));


        verify(userRepository, times(1)).findByUsername(username);
    }

}
