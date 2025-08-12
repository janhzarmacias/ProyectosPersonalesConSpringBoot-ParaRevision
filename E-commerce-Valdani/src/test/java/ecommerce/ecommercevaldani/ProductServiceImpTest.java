package ecommerce.ecommercevaldani;

import ecommerce.ecommercevaldani.DTO.ProductResponseDTO;
import ecommerce.ecommercevaldani.model.*;
import ecommerce.ecommercevaldani.repository.*;
import ecommerce.ecommercevaldani.request.AssignProductToBranchRequest;
import ecommerce.ecommercevaldani.request.CreateProductRequest;
import ecommerce.ecommercevaldani.request.UpdateStockRequest;
import ecommerce.ecommercevaldani.util.ProductMapper;
import ecommerce.ecommercevaldani.service.ProductServiceImp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceImpTest {

    @InjectMocks
    private ProductServiceImp productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private ProductBranchStockRepository productBranchStockRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProduct_success() {
        // Datos simulados
        Category category = new Category();
        category.setId(1L);

        Brand brand = new Brand();
        brand.setId(1L);

        CreateProductRequest req = new CreateProductRequest();
        req.setName("Producto Test");
        req.setDescription("Desc");
        req.setPrice(100.00);
        req.setImageUrl("http://imagen.jpg");
        req.setCategoryId(1L);
        req.setBrandId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        Product created = productService.createProduct(req);

        assertNotNull(created);
        assertEquals("Producto Test", created.getName());
        assertEquals(category, created.getCategory());
        assertEquals(brand, created.getBrand());
        assertTrue(created.isActive());

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_updatesFields() {
        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setName("Old Name");
        existingProduct.setDescription("Old Desc");
        existingProduct.setPrice(50.00);
        existingProduct.setImageUrl("old.jpg");
        existingProduct.setCategory(new Category());
        existingProduct.setBrand(new Brand());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        CreateProductRequest updateReq = new CreateProductRequest();
        updateReq.setName("New Name");
        updateReq.setDescription("New Desc");
        updateReq.setPrice(150.00);
        updateReq.setImageUrl("new.jpg");

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArguments()[0]);

        ProductResponseDTO dto = productService.updateProduct(1L, updateReq);

        assertEquals("New Name", dto.getName());
        assertEquals("New Desc", dto.getDescription());
        assertEquals(150.00, dto.getPrice());
        assertEquals("new.jpg", dto.getImageUrl());

        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void getProductInfoById_returnsDTO() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Prod");
        product.setPrice(100.00);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponseDTO dto = productService.getProductInfoById(1L);

        assertEquals("Test Prod", dto.getName());
    }

    @Test
    void assignProductToBranch_createsOrUpdatesStock() {
        Product product = new Product();
        product.setId(1L);
        Branch branch = new Branch();
        branch.setId(2L);

        ProductBranchKey key = new ProductBranchKey(1L, 2L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(branchRepository.findById(2L)).thenReturn(Optional.of(branch));
        when(productBranchStockRepository.findById(key)).thenReturn(Optional.empty());
        when(productBranchStockRepository.save(any(ProductBranchStock.class))).thenAnswer(i -> i.getArguments()[0]);

        ProductBranchStock stock = productService.assignProductToBranch(1L, 2L, 10);

        assertEquals(10, stock.getStockQuantity());
        assertEquals(product, stock.getProduct());
        assertEquals(branch, stock.getBranch());
    }

    @Test
    void updateStockByBranch_updatesExistingStock() {
        ProductBranchKey key = new ProductBranchKey(1L, 2L);
        ProductBranchStock stock = new ProductBranchStock();
        stock.setId(key);
        stock.setStockQuantity(5);

        when(productBranchStockRepository.findById(key)).thenReturn(Optional.of(stock));
        when(productBranchStockRepository.save(any(ProductBranchStock.class))).thenAnswer(i -> i.getArguments()[0]);

        ProductBranchStock updated = productService.updateStockByBranch(1L, 2L, 20);

        assertEquals(20, updated.getStockQuantity());
    }
}