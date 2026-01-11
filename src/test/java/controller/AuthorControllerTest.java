package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.library.LibraryApplication;
import org.library.dto.author.AuthorRequest;
import org.library.dto.author.AuthorResponse;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.service.AuthorService;
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
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")  // Mock admin user for POST
    void testCreateAuthorSuccess() throws Exception {
        AuthorRequest request = new AuthorRequest();
        request.setName("J.K. Rowling");

        AuthorResponse response = new AuthorResponse(1L, "J.K. Rowling");

        when(authorService.createAuthor(any(AuthorRequest.class))).thenReturn(response);

        mockMvc.perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("J.K. Rowling"));

        verify(authorService).createAuthor(any(AuthorRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAuthorDuplicateName() throws Exception {
        AuthorRequest request = new AuthorRequest();
        request.setName("J.K. Rowling");

        when(authorService.createAuthor(any(AuthorRequest.class)))
                .thenThrow(new ConflictException("Author with the same name already exists"));

        mockMvc.perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());

        verify(authorService).createAuthor(any(AuthorRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAuthorInvalidRequest() throws Exception {
        AuthorRequest request = new AuthorRequest();
        request.setName(""); // Blank name - invalid

        mockMvc.perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(authorService, never()).createAuthor(any(AuthorRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAuthorMissingName() throws Exception {
        String emptyRequest = "{}";

        mockMvc.perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(emptyRequest)
                )
                .andExpect(status().isBadRequest());

        verify(authorService, never()).createAuthor(any(AuthorRequest.class));
    }

    @Test
    @WithMockUser  // Mock authenticated user for GET
    void testGetAuthorByIdSuccess() throws Exception {
        Long authorId = 1L;
        AuthorResponse response = new AuthorResponse(authorId, "J.K. Rowling");

        when(authorService.findAuthorById(authorId)).thenReturn(response);

        mockMvc.perform(
                        get("/api/authors/{authorId}", authorId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(authorId))
                .andExpect(jsonPath("$.name").value("J.K. Rowling"));

        verify(authorService).findAuthorById(authorId);
    }

    @Test
    @WithMockUser
    void testGetAuthorByIdNotFound() throws Exception {
        Long authorId = 999L;

        when(authorService.findAuthorById(authorId))
                .thenThrow(new ResourceNotFoundException("Author not found"));

        mockMvc.perform(
                        get("/api/authors/{authorId}", authorId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(authorService).findAuthorById(authorId);
    }

    @Test
    @WithMockUser
    void testGetAllAuthorsSuccess() throws Exception {
        AuthorResponse author1 = new AuthorResponse(1L, "J.K. Rowling");
        AuthorResponse author2 = new AuthorResponse(2L, "George R.R. Martin");
        AuthorResponse author3 = new AuthorResponse(3L, "J.R.R. Tolkien");

        List<AuthorResponse> authors = Arrays.asList(author1, author2, author3);

        when(authorService.getAllAuthors()).thenReturn(authors);

        mockMvc.perform(
                        get("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("J.K. Rowling"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("George R.R. Martin"))
                .andExpect(jsonPath("$[2].id").value(3L))
                .andExpect(jsonPath("$[2].name").value("J.R.R. Tolkien"));

        verify(authorService).getAllAuthors();
    }

    @Test
    @WithMockUser
    void testGetAllAuthorsEmpty() throws Exception {
        when(authorService.getAllAuthors()).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(authorService).getAllAuthors();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAuthorWithNullName() throws Exception {
        AuthorRequest request = new AuthorRequest();
        request.setName(null);

        mockMvc.perform(
                        post("/api/authors")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(authorService, never()).createAuthor(any(AuthorRequest.class));
    }
}