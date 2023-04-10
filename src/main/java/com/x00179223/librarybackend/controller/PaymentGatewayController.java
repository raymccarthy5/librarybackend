package com.x00179223.librarybackend.controller;

import com.x00179223.librarybackend.client.StripeClient;
import com.x00179223.librarybackend.dto.ChargeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
@RestController
@CrossOrigin("http://localhost:3000")
@RequestMapping("/api/v1/payment")
public class PaymentGatewayController {

    private StripeClient stripeClient;

    @Autowired
    PaymentGatewayController(StripeClient stripeClient) {
        this.stripeClient = stripeClient;
    }
    @PostMapping("/charge")
    public ChargeResponse chargeCard(@RequestHeader(value="token") String token, @RequestHeader(value="amount") Double amount, @RequestHeader(value="userId") Long userId) throws Exception {
        return this.stripeClient.chargeNewCard(token, amount, userId);
    }
}