package ecommerce.ecommercevaldani.request;

import lombok.Data;

@Data
public class UpdateStockRequest {
    private Long productId;
    private Long branchId;
    private int newStockQuantity;
}