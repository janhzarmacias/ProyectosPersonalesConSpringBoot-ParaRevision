package ecommerce.ecommercevaldani.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponseDTO {
    private String requestId;
    private String processUrl;
}