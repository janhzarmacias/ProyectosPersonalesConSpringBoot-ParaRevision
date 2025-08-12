package ecommerce.ecommercevaldani.request;

import lombok.Data;

@Data
public class CartUpdateRequest {
    private Long branchId;
    private int quantity;

}
