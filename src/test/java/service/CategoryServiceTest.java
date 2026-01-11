package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.dto.category.CategoryRequest;
import org.library.dto.category.CategoryResponse;
import org.library.entities.Category;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.repository.CategoryRepository;
import org.library.service.CategoryService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    private CategoryService categoryService;
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setup() {
        categoryRepository = mock(CategoryRepository.class);
        categoryService = new CategoryService(categoryRepository);
    }

    @Test
    void testCreateCategorySuccess() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Fantasy");

        when(categoryRepository.findByName(request.getName())).thenReturn(Optional.empty());

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(1L);
            return category;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Fantasy", response.getName());

        verify(categoryRepository).findByName(request.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testCreateCategoryDuplicateName() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Fantasy");

        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Fantasy");

        when(categoryRepository.findByName(request.getName())).thenReturn(Optional.of(existingCategory));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> categoryService.createCategory(request));

        assertEquals("Category with the same name already exists", exception.getMessage());

        verify(categoryRepository).findByName(request.getName());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void testFindCategoryByIdSuccess() {
        Long categoryId = 1L;
        Category category = new Category();
        category.setId(categoryId);
        category.setName("Fantasy");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.findCategoryById(categoryId);

        assertNotNull(response);
        assertEquals(categoryId, response.getId());
        assertEquals("Fantasy", response.getName());

        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void testFindCategoryByIdNotFound() {
        Long categoryId = 999L;

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> categoryService.findCategoryById(categoryId));

        assertEquals("Category not found", exception.getMessage());

        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void testGetAllCategoriesSuccess() {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Fantasy");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Science Fiction");

        Category category3 = new Category();
        category3.setId(3L);
        category3.setName("Mystery");

        List<Category> categories = Arrays.asList(category1, category2, category3);

        when(categoryRepository.findAll()).thenReturn(categories);

        List<CategoryResponse> responses = categoryService.getAllCategories();

        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals("Fantasy", responses.get(0).getName());
        assertEquals(2L, responses.get(1).getId());
        assertEquals("Science Fiction", responses.get(1).getName());
        assertEquals(3L, responses.get(2).getId());
        assertEquals("Mystery", responses.get(2).getName());

        verify(categoryRepository).findAll();
    }

    @Test
    void testGetAllCategoriesEmpty() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponse> responses = categoryService.getAllCategories();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(categoryRepository).findAll();
    }

    @Test
    void testCreateCategoryWithEmptyName() {
        CategoryRequest request = new CategoryRequest();
        request.setName("");

        when(categoryRepository.findByName("")).thenReturn(Optional.empty());

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(1L);
            return category;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("", response.getName());

        verify(categoryRepository).findByName("");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void testCreateCategoryWithLongName() {
        CategoryRequest request = new CategoryRequest();
        String longName = "A".repeat(100);
        request.setName(longName);

        when(categoryRepository.findByName(longName)).thenReturn(Optional.empty());

        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(1L);
            return category;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals(longName, response.getName());

        verify(categoryRepository).findByName(longName);
        verify(categoryRepository).save(any(Category.class));
    }

}