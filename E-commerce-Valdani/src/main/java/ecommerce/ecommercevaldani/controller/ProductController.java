package ecommerce.ecommercevaldani.controller;

import ecommerce.ecommercevaldani.DTO.ProductResponseDTO;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.service.ProductServiceImp;
import ecommerce.ecommercevaldani.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductServiceImp productService;

    @Autowired
    private UserServiceImp userService;

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

    @GetMapping("/{productId}/branch/{branchId}")
    public ResponseEntity<ProductResponseDTO> getProductByBranch(
            @PathVariable Long productId,
            @PathVariable Long branchId
    ) {
        ProductResponseDTO dto = productService.getProductInfoByIdAndBranch(productId, branchId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByBranch(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long branchId
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        List<ProductResponseDTO> products = productService.getProductsByBranch(branchId);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }



}
