package com.x00179223.librarybackend;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.x00179223.librarybackend.client.StripeClient;
import com.x00179223.librarybackend.dto.ChargeResponse;
import com.x00179223.librarybackend.exception.ResourceNotFoundException;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StripeClientTest {

    @InjectMocks
    private StripeClient stripeClient;

    @Mock
    private UserService userService;

    @Mock
    private Charge charge;

    private User testUser;
    private ChargeResponse testChargeResponse;

    @BeforeEach
    public void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstname("firstnameTest")
                .lastname("lastnameTest")
                .email("test@example.com")
                .password("password")
                .fine(0.0)
                .build();

        testChargeResponse = ChargeResponse.builder()
                .id("ch_1JMKj5I5a5S5S5S5")
                .amount(1000L)
                .currency("eur")
                .status("succeeded")
                .build();
    }

    @Test
    public void testChargeNewCard() throws Exception {
        String token = "tok_visa";
        double amount = 10.0;
        Long userId = 1L;

        when(userService.findById(userId)).thenReturn(Optional.of(testUser));

        ChargeResponse response = stripeClient.chargeNewCard(token, amount, userId);

        assertEquals(testChargeResponse.getAmount(), response.getAmount());
        assertEquals(testChargeResponse.getCurrency(), response.getCurrency());
        assertEquals(testChargeResponse.getStatus(), response.getStatus());

        verify(userService).findById(userId);
        verify(userService).addFine(testUser);
        verifyNoMoreInteractions(userService);
    }

    @Test
    public void testChargeNewCardFailure() {
        // TODO: Implement this test
    }

    @Test
    public void testChargeNewCardUserNotFound() throws Exception {
        String token = "tok_visa";
        double amount = 10.0;
        Long userId = 1L;

        when(userService.findById(userId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> stripeClient.chargeNewCard(token, amount, userId));
        assertEquals("User not found", exception.getMessage());

        verify(userService).findById(userId);
        verifyNoMoreInteractions(userService);
    }

}
