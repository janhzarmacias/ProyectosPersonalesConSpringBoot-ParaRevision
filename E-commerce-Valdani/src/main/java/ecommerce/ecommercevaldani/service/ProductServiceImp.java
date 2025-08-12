package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.DTO.BranchInfoDTO;
import ecommerce.ecommercevaldani.DTO.ProductResponseDTO;
import ecommerce.ecommercevaldani.model.*;
import ecommerce.ecommercevaldani.repository.*;
import ecommerce.ecommercevaldani.request.AssignProductToBranchRequest;
import ecommerce.ecommercevaldani.request.CreateProductRequest;
import ecommerce.ecommercevaldani.request.UpdateStockRequest;
import ecommerce.ecommercevaldani.util.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImp implements ProductService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductBranchStockRepository productBranchStockRepository;

    @Override
    public Product createProduct(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setImageUrl(request.getImageUrl());
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setCategory(category);
        product.setBrand(brand);

        return productRepository.save(product);
    }

    @Override
    public ProductResponseDTO updateProduct(Long productId, CreateProductRequest updateRequest) {
        Product product = getProductById(productId);

        if (updateRequest.getName() != null) product.setName(updateRequest.getName());
        if (updateRequest.getDescription() != null) product.setDescription(updateRequest.getDescription());
        if (updateRequest.getPrice() != null) product.setPrice(updateRequest.getPrice());
        if (updateRequest.getImageUrl() != null) product.setImageUrl(updateRequest.getImageUrl());

        if (updateRequest.getCategoryId() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        if (updateRequest.getBrandId() != null) {
            Brand brand = brandRepository.findById(updateRequest.getBrandId())
                    .orElseThrow(() -> new RuntimeException("Brand not found"));
            product.setBrand(brand);
        }

        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        return getProductInfoById(productId);
    }

    @Override
    public ProductResponseDTO getProductInfoById(long productId) {
        Product product = getProductById(productId);
        return ProductMapper.toDTO(product);
    }

    @Override
    public ProductResponseDTO getProductInfoByIdAndBranch(long productId, long branchId) {
        Product product = getProductById(productId);
        ProductBranchStock stock = productBranchStockRepository
                .findByProductIdAndBranchId(productId, branchId)
                .orElseThrow(() -> new RuntimeException("Product not found in this branch"));

        return ProductMapper.toDTO(product, stock);
    }


    @Override
    public Product getProductById(long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
    }

    @Override
    public ProductResponseDTO getProductByName(String name) {
        Product product = productRepository.findByName(name);
        if (product == null) {
            throw new RuntimeException("Product not found with name: " + name);
        }
        return getProductInfoById(product.getId());
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(p -> getProductInfoById(p.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProduct(long productId) {
        Product product = getProductById(productId);
        productRepository.delete(product);
    }

    @Override
    public Product updateStatus(Long productId) {
        Product product = getProductById(productId);
        product.setActive(!product.isActive());
        return productRepository.save(product);
    }

    @Override
    public void assignProductToBranch(AssignProductToBranchRequest request) {
        assignProductToBranch(request.getProductId(), request.getBranchId(), request.getStockQuantity());
    }

    @Override
    public ProductBranchStock assignProductToBranch(Long productId, Long branchId, int stockQuantity) {
        Product product = getProductById(productId);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        ProductBranchKey key = new ProductBranchKey(productId, branchId);

        // Si ya existe, actualiza stock
        ProductBranchStock stock = productBranchStockRepository.findById(key)
                .orElse(new ProductBranchStock());

        stock.setId(key);
        stock.setProduct(product);
        stock.setBranch(branch);
        stock.setStockQuantity(stockQuantity);
        stock.setCreatedAt(LocalDateTime.now());

        return productBranchStockRepository.save(stock);
    }

    @Override
    public void updateStockByBranch(UpdateStockRequest request) {
        updateStockByBranch(request.getProductId(), request.getBranchId(), request.getNewStockQuantity());
    }

    @Override
    public ProductBranchStock updateStockByBranch(Long productId, Long branchId, int newStock) {
        ProductBranchKey key = new ProductBranchKey(productId, branchId);
        ProductBranchStock stock = productBranchStockRepository.findById(key)
                .orElseThrow(() -> new RuntimeException("Product not found in this branch"));

        stock.setStockQuantity(newStock);
        return productBranchStockRepository.save(stock);
    }

    @Override
    public void deleteProductFromBranch(Long productId, Long branchId) {
        ProductBranchKey key = new ProductBranchKey(productId, branchId);
        ProductBranchStock stock = productBranchStockRepository.findById(key)
                .orElseThrow(() -> new RuntimeException("Product is not assigned to this branch"));

        productBranchStockRepository.delete(stock);
    }

    @Override
    public List<ProductResponseDTO> getProductsByBranch(Long branchId) {
        // Validar que la sucursal exista
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found with ID: " + branchId));

        // Obtener todos los ProductBranchStock para esa sucursal
        List<ProductBranchStock> stocks = productBranchStockRepository.findByBranchId(branchId);

        // Mapear cada stock a ProductResponseDTO combinando producto + stock de sucursal
        return stocks.stream()
                .map(stock -> ProductMapper.toDTO(stock.getProduct(), stock))
                .collect(Collectors.toList());
    }


    public boolean ping() {
        productRepository.count();
        return true;
    }


}
