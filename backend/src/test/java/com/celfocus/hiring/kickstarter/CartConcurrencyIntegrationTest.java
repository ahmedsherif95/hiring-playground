package com.celfocus.hiring.kickstarter;

import com.celfocus.hiring.kickstarter.api.dto.CartItemInput;
import com.celfocus.hiring.kickstarter.db.repo.CartItemRepository;
import com.celfocus.hiring.kickstarter.db.repo.CartRepository;
import com.celfocus.hiring.kickstarter.security.JwtRequestFilter;
import com.celfocus.hiring.kickstarter.security.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CartConcurrencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @MockBean
    private JwtRequestFilter jwtRequestFilter;

    @BeforeEach
    void setUp() throws Exception {
        doNothing().when(jwtRequestFilter).doFilterInternal(any(), any(), any());
    }


    @Test
    void addItemToCart_ConcurrentRequests_ShouldHandleGracefully() throws Exception {
        // Generate or mock a valid JWT
        String validJwt = "Bearer " + jwtTokenUtil.generateToken("testUser", new String[]{"ROLE_USER"});

        // Prepare the cart item input
        CartItemInput cartItemInput = new CartItemInput("SKUTEST1");

        // Concurrent tasks to perform the requests
        Callable<ResultActions> task = () -> mockMvc.perform(post("/api/v1/carts/items")
                .header("Authorization", validJwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartItemInput)));

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Future<ResultActions>> futures = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            futures.add(executorService.submit(task));
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // Verify all responses are successful
        for (Future<ResultActions> future : futures) {
            ResultActions result = future.get();
            result.andExpect(status().isOk());
        }
    }

}

