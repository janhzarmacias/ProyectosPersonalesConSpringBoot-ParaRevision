package ecommerce.ecommercevaldani.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBranchStock {

    @EmbeddedId
    private ProductBranchKey id = new ProductBranchKey();

    @ManyToOne
    @MapsId("productId")
    private Product product;

    @ManyToOne
    @MapsId("branchId")
    private Branch branch;

    private int stockQuantity;
    private LocalDateTime createdAt;
}


