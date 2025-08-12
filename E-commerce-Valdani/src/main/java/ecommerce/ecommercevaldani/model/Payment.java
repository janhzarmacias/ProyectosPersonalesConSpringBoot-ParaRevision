package ecommerce.ecommercevaldani.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Order order;

    private String stripePaymentId;
    private String transactionId;
    private Double amount;
    private String currency;
    private PaymentStatus status;
    private String paymentMethod;
    private String paymentMethodLast4;
    private LocalDateTime paymentDate;
}



