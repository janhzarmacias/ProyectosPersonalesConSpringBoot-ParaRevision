package ecommerce.ecommercevaldani.service;


import ecommerce.ecommercevaldani.model.CartItem;
import ecommerce.ecommercevaldani.model.Category;
import ecommerce.ecommercevaldani.request.CreateCategoryRequest;


import java.util.List;
import java.util.Optional;

public interface CategoryService {

    public Category createCategory(CreateCategoryRequest categoryRequest);
    public Category updateCategory(Long categoryId, CreateCategoryRequest updatedCategory);
    public void deleteCategory(Long categoryId);
    public Category getCategoryById(Long categoryId);
    public List<Category> getAllCategories();

}
