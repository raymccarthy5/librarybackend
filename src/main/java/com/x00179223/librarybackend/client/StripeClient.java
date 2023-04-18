package com.x00179223.librarybackend.client;

import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.x00179223.librarybackend.dto.ChargeResponse;
import com.x00179223.librarybackend.exception.ResourceNotFoundException;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
@Component
public class StripeClient {

    private final UserService userService;
    @Autowired
    StripeClient(UserService userService) {
        this.userService = userService;
        Stripe.apiKey = "sk_test_51Hs4PpDNHqlXLssuIwTZg1Vv3aINzw5nqHjByuVDMDPilK2JvAVgL0I5z0pM6Brmtqu9BypbFfT8tA4bGK6aUsei006I07Vs0S";
    }
    public ChargeResponse chargeNewCard(String token, double amount, Long userId) throws Exception {
        Map<String, Object> chargeParams = new HashMap<String, Object>();
        chargeParams.put("amount", (int)(amount * 100));
        chargeParams.put("currency", "EUR");
        chargeParams.put("source", token);
        Charge charge = Charge.create(chargeParams);

         if (charge.getStatus().equals("succeeded")){
            User existingUser = userService.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
            existingUser.setFine(0.0);
            userService.addFine(existingUser);
         }
        return convertToStripeResponse(charge);
    }
    private ChargeResponse convertToStripeResponse(Charge charge) {
        ChargeResponse chargeResponse = new ChargeResponse();
        chargeResponse.setId(charge.getId());
        chargeResponse.setAmount(charge.getAmount());
        chargeResponse.setCurrency(charge.getCurrency());
        chargeResponse.setStatus(charge.getStatus());

        return chargeResponse;
    }
}