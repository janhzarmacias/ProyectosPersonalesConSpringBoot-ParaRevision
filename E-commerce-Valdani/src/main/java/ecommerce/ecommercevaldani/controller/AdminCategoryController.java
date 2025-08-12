package ecommerce.ecommercevaldani.controller;


import ecommerce.ecommercevaldani.model.Category;
import ecommerce.ecommercevaldani.model.User;
import ecommerce.ecommercevaldani.request.CreateCategoryRequest;
import ecommerce.ecommercevaldani.response.MessageResponse;
import ecommerce.ecommercevaldani.service.CategoryServiceImp;
import ecommerce.ecommercevaldani.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryServiceImp categoryService;

    @Autowired
    private UserServiceImp userService;

    @PostMapping
    public ResponseEntity<Category> createCategoryRequest(
            @RequestBody CreateCategoryRequest req,
            @RequestHeader ("Authorization") String jwt
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Category category = categoryService.createCategory(req);

        return new ResponseEntity<>(category,HttpStatus.CREATED);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategoryRequest(
            @RequestBody CreateCategoryRequest req,
            @RequestHeader ("Authorization") String jwt,
            @PathVariable long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Category category = categoryService.updateCategory(id,req);

        return new ResponseEntity<>(category,HttpStatus.CREATED);

    }
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteCategoryRequest(
            @RequestHeader ("Authorization") String jwt,
            @PathVariable long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        categoryService.deleteCategory(id);

        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setMessage("Category successfully deleted ");

        return new ResponseEntity<>(messageResponse,HttpStatus.CREATED);
    }

    @GetMapping("/search/{id}")
    public ResponseEntity<Category> findCategoryById(
            @RequestHeader ("Authorization") String jwt,
            @PathVariable Long id
    ) throws Exception {
        User user = userService.findUserByjwtToken(jwt);
        Category category = categoryService.getCategoryById(id);
        return new ResponseEntity<>(category,HttpStatus.OK);
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