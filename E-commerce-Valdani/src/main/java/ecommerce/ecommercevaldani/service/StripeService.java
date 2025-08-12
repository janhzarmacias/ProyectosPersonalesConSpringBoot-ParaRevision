package ecommerce.ecommercevaldani.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import ecommerce.ecommercevaldani.model.Order;
import ecommerce.ecommercevaldani.model.OrderStatus;
import ecommerce.ecommercevaldani.model.Payment;
import ecommerce.ecommercevaldani.model.PaymentStatus;
import ecommerce.ecommercevaldani.response.PaymentResponse;

import java.time.LocalDateTime;

public interface StripeService {

    public String createPaymentIntent(Long orderId) throws Exception;
    public void handleChargeDataManually(String paymentIntentId, String chargeId, double amount, String currency, String paymentMethod, String last4);
    public void handleChargeSucceeded(JsonNode dataNode);
    public void handlePaymentFailed(String paymentIntentId);

}
