package ecommerce.ecommercevaldani.request;

import lombok.Data;

@Data
public class AddCartItemRequest {

    private Long productId;
    private int quantity;
    private Long branchId;

}
