package ecommerce.ecommercevaldani.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    private Cart cart;


    @ManyToOne
    private Product product;

    @ManyToOne
    private ProductBranchStock productBranchStock;

    private int quantity;
    private Double priceAtTime;
    private LocalDateTime createdAt;
}

