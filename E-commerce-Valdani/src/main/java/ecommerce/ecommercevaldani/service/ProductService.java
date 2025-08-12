package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.DTO.ProductResponseDTO;
import ecommerce.ecommercevaldani.model.Product;
import ecommerce.ecommercevaldani.model.ProductBranchStock;
import ecommerce.ecommercevaldani.request.CreateProductRequest;
import ecommerce.ecommercevaldani.request.AssignProductToBranchRequest;
import ecommerce.ecommercevaldani.request.UpdateStockRequest;

import java.util.List;

public interface ProductService {

    Product createProduct(CreateProductRequest createProductRequest);
    ProductResponseDTO updateProduct(Long productId, CreateProductRequest updateProductRequest);
    ProductResponseDTO getProductInfoById(long productId);
    ProductResponseDTO getProductInfoByIdAndBranch(long productId, long branchId);
    Product getProductById(long productId);
    ProductResponseDTO getProductByName(String name);
    List<ProductResponseDTO> getAllProducts();
    public void deleteProduct(long productId);
    Product updateStatus(Long productId);
    public void assignProductToBranch(AssignProductToBranchRequest request);
    public void updateStockByBranch(UpdateStockRequest request);
    public ProductBranchStock assignProductToBranch(Long productId, Long branchId, int stockQuantity);
    public ProductBranchStock updateStockByBranch(Long productId, Long branchId, int newStock);
    public void deleteProductFromBranch(Long productId, Long branchId);
    public List<ProductResponseDTO> getProductsByBranch(Long branchId);
}
