package com.celfocus.hiring.kickstarter.controllers;

import com.celfocus.hiring.kickstarter.api.AuthenticationRequest;
import com.celfocus.hiring.kickstarter.api.AuthenticationResponse;
import com.celfocus.hiring.kickstarter.api.UserService;
import com.celfocus.hiring.kickstarter.db.repo.UserRepository;
import com.celfocus.hiring.kickstarter.security.JwtTokenUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private JwtTokenUtil jwtTokenUtil;
    private UserService userService;
    private UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UserService userService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        System.out.println("Attempting login for username: " + authenticationRequest.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );

        } catch (BadCredentialsException e) {
            System.out.println("Authentication failed: Invalid credentials");
            return ResponseEntity.status(401).body("Incorrect username or password");
        }
        catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error");
        }

        final UserDetails userDetails = userService.loadUserByUsername(authenticationRequest.getUsername());

        String[] roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);
        final String jwt = jwtTokenUtil.generateToken(userDetails.getUsername(), roles);
        System.out.println("Generated JWT: " + jwt);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
}
