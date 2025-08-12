package ecommerce.ecommercevaldani.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import ecommerce.ecommercevaldani.model.*;
import ecommerce.ecommercevaldani.repository.OrderRepository;
import ecommerce.ecommercevaldani.repository.PaymentRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class StripeServiceImp implements StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeServiceImp.class);

    @Autowired
    private CartServiceImp cartService;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public StripeServiceImp(OrderRepository orderRepository, PaymentRepository paymentRepository, CartServiceImp cartServiceImp) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public String createPaymentIntent(Long orderId) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new Exception("Order not found"));

        if (order.getPaymentIntentId() != null) {
            PaymentIntent existing = PaymentIntent.retrieve(order.getPaymentIntentId());
            String status = existing.getStatus();
            if (!"succeeded".equals(status) && !"canceled".equals(status)) {
                logger.info("Reusing existing PaymentIntent for Order {}: {}", orderId, existing.getId());
                return existing.getClientSecret();
            }
        }

        long amount = (long) (Double.parseDouble(order.getTotalPrice()) * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency("usd")
                .addPaymentMethodType("card")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        order.setPaymentIntentId(intent.getId());
        orderRepository.save(order);

        return intent.getClientSecret();
    }

    @Override
    public void handleChargeDataManually(String paymentIntentId, String chargeId, double amount,
                                         String currency, String paymentMethod, String last4) {
        try {
            logger.info("Manually processing charge.succeeded for paymentIntentId: {}", paymentIntentId);

            Order order = orderRepository.findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new IllegalStateException("Order not found"));


            User user = order.getUser();
            Cart cart = user.getCart();
            cartService.purchaseCart(cart.getId(),order.getBranch());
            

            if (paymentRepository.existsByStripePaymentId(paymentIntentId)) {
                logger.info("Payment already registered previously");
                return;
            }

            Payment payment = new Payment();
            payment.setOrder(order);
            payment.setStripePaymentId(paymentIntentId);
            payment.setTransactionId(chargeId);
            payment.setAmount(amount);
            payment.setCurrency(currency);
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentMethodLast4(last4);
            payment.setPaymentDate(LocalDateTime.now());

            Payment saved = paymentRepository.save(payment);

            order.setStatus(OrderStatus.WAITINGFORDELIVERING);
            order.setPayment(saved);
            orderRepository.save(order);

            logger.info("Order and payment updated correctly");

        } catch (Exception e) {
            logger.error("Error manually handling the charge.succeeded webhook", e);
        }
    }

    @Override
    public void handleChargeSucceeded(JsonNode dataNode) {
        try {
            String paymentIntentId = dataNode.path("payment_intent").asText();
            String chargeId = dataNode.path("id").asText();
            double amount = dataNode.path("amount").asDouble() / 100.0;
            String currency = dataNode.path("currency").asText();
            String paymentMethod = dataNode.path("payment_method_details").path("type").asText("unknown");
            String last4 = dataNode.path("payment_method_details").path("card").path("last4").asText(null);

            handleChargeDataManually(paymentIntentId, chargeId, amount, currency, paymentMethod, last4);


        } catch (Exception e) {
            logger.error("Error handling event automatically charge.succeeded", e);
        }
    }

    @Override
    public void handlePaymentFailed(String paymentIntentId) {
        Order order = orderRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new IllegalStateException("Order not found"));

        Payment payment = order.getPayment();

        if (payment == null) {
            payment = new Payment();
            payment.setOrder(order);
            payment.setStripePaymentId(paymentIntentId);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setPaymentDate(LocalDateTime.now());

            // Valores por defecto para evitar nulls peligrosos
            payment.setAmount(0.0); // evita el doubleValue() null
            payment.setCurrency("usd");
            payment.setPaymentMethod("card");
            payment.setPaymentMethodLast4("----");

            paymentRepository.save(payment);
            order.setPayment(payment);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }


    public boolean ping() {
        try {
            // Verifica si el API Key fue correctamente inicializado
            if (Stripe.apiKey == null || Stripe.apiKey.isEmpty()) {
                return false;
            }

            // Verifica conectividad mínima con una operación local
            paymentRepository.count();  // validación de base de datos

            return true;
        } catch (Exception e) {
            return false;
        }
    }



}
