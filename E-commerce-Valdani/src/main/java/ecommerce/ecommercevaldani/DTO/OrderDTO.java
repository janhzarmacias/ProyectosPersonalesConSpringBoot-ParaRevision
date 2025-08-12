package ecommerce.ecommercevaldani.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String status;
    private String paymentIntentId;
    private LocalDateTime createdAt;

    private UserDTO user;
    private AddressDTO address;
    private BranchOrderDTO branch;
    private List<OrderItemDTO> items;
    private PaymentDTO payment;
}