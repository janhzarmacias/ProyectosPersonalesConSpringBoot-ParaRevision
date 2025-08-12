package ecommerce.ecommercevaldani.controller;

import ecommerce.ecommercevaldani.DTO.ProductResponseDTO;
import ecommerce.ecommercevaldani.model.Product;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.request.AssignProductToBranchRequest;
import ecommerce.ecommercevaldani.request.CreateProductRequest;
import ecommerce.ecommercevaldani.request.UpdateStockRequest;
import ecommerce.ecommercevaldani.response.MessageResponse;
import ecommerce.ecommercevaldani.service.ProductServiceImp;
import ecommerce.ecommercevaldani.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    @Autowired
    private ProductServiceImp productService;

    @Autowired
    private UserServiceImp userService;

    @PostMapping
    public ResponseEntity<Product> createProductRequest(
            @RequestBody CreateProductRequest req,
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Product product = productService.createProduct(req);
        return new ResponseEntity<>(product, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProductRequest(
            @RequestBody CreateProductRequest req,
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        ProductResponseDTO product = productService.updateProduct(id, req);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteProductRequest(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        productService.deleteProduct(id);

        MessageResponse response = new MessageResponse();
        response.setMessage("Product successfully deleted");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Product> updateProductStatusRequest(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Product product = productService.updateStatus(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductInfoById(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        ProductResponseDTO product = productService.getProductInfoById(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts(
            @RequestHeader("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        List<ProductResponseDTO> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<ProductResponseDTO> getProductByName(
            @RequestHeader("Authorization") String jwt,
            @PathVariable String name
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        ProductResponseDTO product = productService.getProductByName(name);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @PostMapping("/assign-to-branch")
    public ResponseEntity<MessageResponse> assignProductToBranch(
            @RequestHeader("Authorization") String jwt,
            @RequestBody AssignProductToBranchRequest request
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        productService.assignProductToBranch(request);
        MessageResponse response = new MessageResponse();
        response.setMessage("Product successfully assigned to branch");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/update-stock")
    public ResponseEntity<MessageResponse> updateStockByBranch(
            @RequestHeader("Authorization") String jwt,
            @RequestBody UpdateStockRequest request
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        productService.updateStockByBranch(request);
        MessageResponse response = new MessageResponse();
        response.setMessage("Stock successfully updated for branch");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{productId}/branch/{branchId}")
    public ResponseEntity<MessageResponse> deleteProductFromBranch(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long productId,
            @PathVariable Long branchId
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        productService.deleteProductFromBranch(productId, branchId);

        MessageResponse response = new MessageResponse();
        response.setMessage("Product removed from branch successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}

