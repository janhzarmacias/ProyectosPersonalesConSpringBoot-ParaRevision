package ecommerce.ecommercevaldani.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private Long id;
    private double amount;
    private String currency;
    private String paymentMethod;
    private String paymentMethodLast4;
    private String status;
    private String stripePaymentId;
    private String transactionId;
    private LocalDateTime paymentDate;
}
