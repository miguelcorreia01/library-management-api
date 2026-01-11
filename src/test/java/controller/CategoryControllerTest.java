package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.library.LibraryApplication;
import org.library.dto.category.CategoryRequest;
import org.library.dto.category.CategoryResponse;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LibraryApplication.class)
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCategorySuccess() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Fantasy");

        CategoryResponse response = new CategoryResponse(1L, "Fantasy");

        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Fantasy"));

        verify(categoryService).createCategory(any(CategoryRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCategoryDuplicateName() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Fantasy");

        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenThrow(new ConflictException("Category with the same name already exists"));

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());

        verify(categoryService).createCategory(any(CategoryRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCategoryInvalidRequest_BlankName() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName(""); // Blank name - invalid

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any(CategoryRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCategoryInvalidRequest_NullName() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName(null);

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any(CategoryRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCategoryInvalidRequest_MissingName() throws Exception {
        String emptyRequest = "{}";

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(emptyRequest)
                )
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any(CategoryRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateCategoryForbidden() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("Fantasy");

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());

        verify(categoryService, never()).createCategory(any(CategoryRequest.class));
    }

    @Test
    @WithMockUser
    void testGetCategoryByIdSuccess() throws Exception {
        Long categoryId = 1L;
        CategoryResponse response = new CategoryResponse(categoryId, "Fantasy");

        when(categoryService.findCategoryById(categoryId)).thenReturn(response);

        mockMvc.perform(
                        get("/api/categories/{categoryId}", categoryId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Fantasy"));

        verify(categoryService).findCategoryById(categoryId);
    }

    @Test
    @WithMockUser
    void testGetCategoryByIdNotFound() throws Exception {
        Long categoryId = 999L;

        when(categoryService.findCategoryById(categoryId))
                .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(
                        get("/api/categories/{categoryId}", categoryId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(categoryService).findCategoryById(categoryId);
    }

    @Test
    @WithMockUser
    void testGetAllCategoriesSuccess() throws Exception {
        CategoryResponse category1 = new CategoryResponse(1L, "Fantasy");
        CategoryResponse category2 = new CategoryResponse(2L, "Science Fiction");
        CategoryResponse category3 = new CategoryResponse(3L, "Mystery");

        List<CategoryResponse> categories = Arrays.asList(category1, category2, category3);

        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(
                        get("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Fantasy"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Science Fiction"))
                .andExpect(jsonPath("$[2].id").value(3L))
                .andExpect(jsonPath("$[2].name").value("Mystery"));

        verify(categoryService).getAllCategories();
    }

    @Test
    @WithMockUser
    void testGetAllCategoriesEmpty() throws Exception {
        when(categoryService.getAllCategories()).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(categoryService).getAllCategories();
    }

    @Test
    @WithMockUser
    void testGetAllCategoriesWithSingleCategory() throws Exception {
        CategoryResponse category = new CategoryResponse(1L, "Fantasy");
        List<CategoryResponse> categories = List.of(category);

        when(categoryService.getAllCategories()).thenReturn(categories);

        mockMvc.perform(
                        get("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Fantasy"));

        verify(categoryService).getAllCategories();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateCategoryWithWhitespaceOnly() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setName("   "); // Whitespace only - should be treated as blank

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any(CategoryRequest.class));
    }
}