package ecommerce.ecommercevaldani.controller;


import ecommerce.ecommercevaldani.model.Brand;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.request.CreateBrandRequest;
import ecommerce.ecommercevaldani.response.MessageResponse;
import ecommerce.ecommercevaldani.service.BrandServiceImp;
import ecommerce.ecommercevaldani.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/brands")
public class AdminBrandController {

    @Autowired
    private BrandServiceImp brandService;

    @Autowired
    private UserServiceImp userService;

    @PostMapping
    public ResponseEntity<Brand> createBrandRequest(
            @RequestBody CreateBrandRequest req,
            @RequestHeader ("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Brand brand = brandService.createBrand(req);

        return new ResponseEntity<>(brand,HttpStatus.CREATED);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Brand> updateBrandRequest(
            @RequestBody CreateBrandRequest req,
            @RequestHeader ("Authorization") String jwt,
            @PathVariable long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Brand brand = brandService.updateBrand(id,req);

        return new ResponseEntity<>(brand,HttpStatus.CREATED);

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteBrandRequest(
            @RequestHeader ("Authorization") String jwt,
            @PathVariable long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        brandService.deleteBrand(id);

        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("Brand successfully deleted ");

        return new ResponseEntity<>(messageResponse,HttpStatus.CREATED);
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<Brand> findBrandById(
            @RequestHeader ("Authorization") String jwt,
            @PathVariable Long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Brand brand = brandService.getBrandById(id);
        return new ResponseEntity<>(brand,HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<Brand>> getAllBrands(
            @RequestHeader ("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        List<Brand> brands = brandService.getAllBrands();
        return new ResponseEntity<>(brands,HttpStatus.OK);
    }

}
