package org.library.service;


import org.library.dto.category.CategoryRequest;
import org.library.dto.category.CategoryResponse;
import org.library.entities.Category;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository){
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse createCategory(CategoryRequest categoryRequest){
        if(categoryRepository.findByName(categoryRequest.getName()).isPresent()){
            throw new ConflictException("Category with the same name already exists");
        }
        Category category = new Category();
        category.setName(categoryRequest.getName());

        Category saved = categoryRepository.save(category);
        return toCategoryResponse(saved);
    }

    public CategoryResponse findCategoryById(Long categoryId){
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        return toCategoryResponse(category);
    }

    public List<CategoryResponse> getAllCategories(){
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    private CategoryResponse toCategoryResponse(Category category){
        return new CategoryResponse(category.getId(), category.getName());
    }
}
