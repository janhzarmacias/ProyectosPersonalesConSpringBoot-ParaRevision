package ecommerce.ecommercevaldani.request;

import lombok.Data;

@Data
public class AssignProductToBranchRequest {
    private Long productId;
    private Long branchId;
    private Integer stockQuantity;
}