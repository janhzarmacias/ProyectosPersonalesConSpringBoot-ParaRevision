package ecommerce.ecommercevaldani.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import ecommerce.ecommercevaldani.model.Brand;
import ecommerce.ecommercevaldani.model.Category;
import lombok.Data;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Category category;
    private Brand brand;
    private BranchInfoDTO branch;
    private boolean active;

}
