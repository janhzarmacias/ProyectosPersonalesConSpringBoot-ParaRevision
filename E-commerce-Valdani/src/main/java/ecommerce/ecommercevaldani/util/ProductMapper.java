package ecommerce.ecommercevaldani.util;

import ecommerce.ecommercevaldani.DTO.BranchInfoDTO;
import ecommerce.ecommercevaldani.DTO.ProductResponseDTO;
import ecommerce.ecommercevaldani.model.Product;
import ecommerce.ecommercevaldani.model.ProductBranchStock;

public class ProductMapper {

    public static ProductResponseDTO toDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setCategory(product.getCategory());
        dto.setBrand(product.getBrand());
        dto.setActive(product.isActive());
        dto.setBranch(null);
        return dto;
    }

    public static ProductResponseDTO toDTO(Product product, ProductBranchStock stock) {
        ProductResponseDTO dto = toDTO(product);
        BranchInfoDTO branchDTO = new BranchInfoDTO(
                stock.getBranch().getId(),
                stock.getBranch().getName(),
                stock.getStockQuantity()
        );
        dto.setBranch(branchDTO);
        return dto;
    }
}
