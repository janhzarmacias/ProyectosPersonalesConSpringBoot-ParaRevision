package ecommerce.ecommercevaldani.DTO;

import lombok.Data;

@Data
public class BranchInfoDTO {
    private Long id;
    private String name;
    private int stock;

    public BranchInfoDTO(Long id, String name, int stock) {
        this.id = id;
        this.name = name;
        this.stock = stock;
    }
}