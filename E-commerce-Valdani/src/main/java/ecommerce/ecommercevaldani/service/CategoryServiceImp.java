package ecommerce.ecommercevaldani.service;

import ecommerce.ecommercevaldani.model.Category;
import ecommerce.ecommercevaldani.repository.CategoryRepository;
import ecommerce.ecommercevaldani.request.CreateCategoryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImp implements CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    @Override
    public Category createCategory(CreateCategoryRequest categoryRequest) {
        Category category = new Category();
        category.setName(categoryRequest.getName());
        category.setDescription(categoryRequest.getDescription());
        category.setCreatedAt(LocalDateTime.now());
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Long categoryId, CreateCategoryRequest updatedCategory) {
        Category category = getCategoryById(categoryId);
        if (updatedCategory.getName() != null) {
            category.setName(updatedCategory.getName());
        }
        if (updatedCategory.getDescription() != null) {
            category.setDescription(updatedCategory.getDescription());
        }

        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        categoryRepository.delete(category);

    }

    @Override
    public Category getCategoryById(Long categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        if (category.isEmpty()) {
            throw new RuntimeException("Category not found with ID: " + categoryId);
        }
        return category.get();
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public boolean ping() {
        categoryRepository.count();
        return true;
    }

}
