package ecommerce.ecommercevaldani.controller;

import ecommerce.ecommercevaldani.model.Category;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.service.CategoryServiceImp;
import ecommerce.ecommercevaldani.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {


    @Autowired
    private CategoryServiceImp categoryService;
    @Autowired
    private UserServiceImp userService;


    @GetMapping("/search/{id}")
    public ResponseEntity<Category> findCategoryById(
            @RequestHeader("Authorization") String jwt,
            @PathVariable Long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Category category = categoryService.getCategoryById(id);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<Category>> getAllCategories(
            @RequestHeader ("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        List<Category> categories = categoryService.getAllCategories();
        return new ResponseEntity<>(categories,HttpStatus.OK);
    }
}
