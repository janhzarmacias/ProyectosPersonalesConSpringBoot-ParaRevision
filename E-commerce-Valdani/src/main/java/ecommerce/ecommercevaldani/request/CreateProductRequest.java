package ecommerce.ecommercevaldani.request;


import lombok.Data;


@Data
public class CreateProductRequest {

    private String name;
    private String description;
    private Double price;
    private String imageUrl;

    private Long categoryId;
    private Long brandId;

    private Long branchId;
    private int stockQuantity;

}
