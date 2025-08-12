package ecommerce.ecommercevaldani.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Double unitPrice;
    private int quantity;
    private Double totalPrice;
    private Long branchId;
    private String branchName;
}
