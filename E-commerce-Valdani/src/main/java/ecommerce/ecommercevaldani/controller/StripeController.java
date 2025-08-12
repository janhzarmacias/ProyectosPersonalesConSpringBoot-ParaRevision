package ecommerce.ecommercevaldani.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import ecommerce.ecommercevaldani.service.StripeServiceImp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    private static final Logger logger = LoggerFactory.getLogger(StripeController.class);

    private final ObjectMapper objectMapper;
    private final StripeServiceImp stripeServiceImp;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    public StripeController(ObjectMapper objectMapper, StripeServiceImp stripeServiceImp) {
        this.objectMapper = objectMapper;
        this.stripeServiceImp = stripeServiceImp;
    }

    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Object> data) {
        try {
            Long orderId = Long.parseLong(data.get("orderId").toString());
            String clientSecret = stripeServiceImp.createPaymentIntent(orderId);
            return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            logger.info("Webhook received with signature: {}", sigHeader);
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);

            String eventType = event.getType();
            logger.info("Event Stripe received: {}", eventType);

            if ("charge.succeeded".equals(eventType)) {
                JsonNode rootNode = objectMapper.readTree(payload);
                JsonNode dataNode = rootNode.path("data").path("object");
                stripeServiceImp.handleChargeSucceeded(dataNode);
            }
            if ("charge.failed".equals(eventType)) {
                JsonNode rootNode = objectMapper.readTree(payload);
                JsonNode dataNode = rootNode.path("data").path("object");
                String paymentIntentId = dataNode.path("payment_intent").asText();
                stripeServiceImp.handlePaymentFailed(paymentIntentId);
            }
            if ("payment_intent.canceled".equals(eventType)) {
                JsonNode rootNode = objectMapper.readTree(payload);
                JsonNode dataNode = rootNode.path("data").path("object");
                String paymentIntentId = dataNode.path("payment_intent").asText();
                stripeServiceImp.handlePaymentFailed(paymentIntentId);
            }

            return ResponseEntity.ok("Webhook recibido");

        } catch (SignatureVerificationException e) {
            logger.error("Invalid signature webhook", e);
            return ResponseEntity.status(401).body("Invalid signature");
        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.internalServerError().body("Error processing event");
        }
    }
}

