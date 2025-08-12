package ecommerce.ecommercevaldani.repository;

import ecommerce.ecommercevaldani.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByStripePaymentId(String stripePaymentId);
}
