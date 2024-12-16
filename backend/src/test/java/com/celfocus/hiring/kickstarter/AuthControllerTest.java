package com.celfocus.hiring.kickstarter;

import com.celfocus.hiring.kickstarter.api.AuthenticationRequest;
import com.celfocus.hiring.kickstarter.api.AuthenticationResponse;
import com.celfocus.hiring.kickstarter.api.UserService;
import com.celfocus.hiring.kickstarter.controllers.AuthController;
import com.celfocus.hiring.kickstarter.db.repo.UserRepository;
import com.celfocus.hiring.kickstarter.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(authenticationManager, jwtTokenUtil, userService, userRepository);
    }

    @Test
    void createAuthenticationToken_Success() throws Exception {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("user", "password");
        UserDetails userDetails = mock(UserDetails.class);
        when(userService.loadUserByUsername("user")).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(jwtTokenUtil.generateToken(any(), any())).thenReturn("mockJwtToken");

        // Act
        ResponseEntity<?> response = authController.createAuthenticationToken(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof AuthenticationResponse);
        AuthenticationResponse authResponse = (AuthenticationResponse) response.getBody();
        assertEquals("mockJwtToken", authResponse.getJwt());
    }

    @Test
    void createAuthenticationToken_InvalidCredentials() throws Exception {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("user", "wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act
        ResponseEntity<?> response = authController.createAuthenticationToken(request);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Incorrect username or password", response.getBody());
    }

    @Test
    void createAuthenticationToken_InternalServerError() throws Exception {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("user", "password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<?> response = authController.createAuthenticationToken(request);

        // Assert
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("Internal Server Error", response.getBody());
    }

    @Test
    void createAuthenticationToken_GenerateJwt() throws Exception {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("user", "password");
        UserDetails userDetails = mock(UserDetails.class);
        when(userService.loadUserByUsername("user")).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(jwtTokenUtil.generateToken(any(), any())).thenReturn("mockJwtToken");

        // Act
        ResponseEntity<?> response = authController.createAuthenticationToken(request);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        AuthenticationResponse authResponse = (AuthenticationResponse) response.getBody();
        assertNotNull(authResponse);
        assertEquals("mockJwtToken", authResponse.getJwt());
    }

    @Test
    void createAuthenticationToken_InvalidUsername() throws Exception {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("invaliduser", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act
        ResponseEntity<?> response = authController.createAuthenticationToken(request);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Incorrect username or password", response.getBody());
    }
}

