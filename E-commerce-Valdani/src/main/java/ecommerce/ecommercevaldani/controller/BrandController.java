package ecommerce.ecommercevaldani.controller;

import ecommerce.ecommercevaldani.model.Brand;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.service.BrandServiceImp;
import ecommerce.ecommercevaldani.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/brands")
public class BrandController {


    @Autowired
    private BrandServiceImp brandService;
    @Autowired
    private UserServiceImp userService;

    @GetMapping("/search/{id}")
    public ResponseEntity<Brand> findBrandById(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Brand brand = brandService.getBrandById(id);
        return new ResponseEntity<>(brand, HttpStatus.OK);
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
