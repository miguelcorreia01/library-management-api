package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.library.LibraryApplication;
import org.library.dto.book.BookRequest;
import org.library.dto.book.BookResponse;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.service.BookService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LibraryApplication.class)
@AutoConfigureMockMvc
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    //CREATE BOOK TESTS

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateBookSuccess() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("Harry Potter and the Philosopher's Stone");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(1997);

        BookResponse response = new BookResponse(1L, "Harry Potter and the Philosopher's Stone",
                "J.K. Rowling", "Fantasy", 1997, false);

        when(bookService.createBook(any(BookRequest.class))).thenReturn(response);

        mockMvc.perform(
                        post("/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Harry Potter and the Philosopher's Stone"))
                .andExpect(jsonPath("$.authorName").value("J.K. Rowling"))
                .andExpect(jsonPath("$.categoryName").value("Fantasy"))
                .andExpect(jsonPath("$.releaseYear").value(1997));

        verify(bookService).createBook(any(BookRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateBookDuplicateTitle() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("Harry Potter and the Philosopher's Stone");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(1997);

        when(bookService.createBook(any(BookRequest.class)))
                .thenThrow(new ConflictException("Book with the same title already exists"));

        mockMvc.perform(
                        post("/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());

        verify(bookService).createBook(any(BookRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateBookInvalidRequest_BlankTitle() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(1997);

        mockMvc.perform(
                        post("/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(bookService, never()).createBook(any(BookRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateBookInvalidRequest_NullAuthorId() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("Test Book");
        request.setAuthorId(null);
        request.setCategoryId(1L);
        request.setReleaseYear(1997);

        mockMvc.perform(
                        post("/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(bookService, never()).createBook(any(BookRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateBookInvalidRequest_InvalidReleaseYear() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("Test Book");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(2030); // Exceeds max value

        mockMvc.perform(
                        post("/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(bookService, never()).createBook(any(BookRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER") // Non-admin user
    void testCreateBookForbidden() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("Test Book");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(1997);

        mockMvc.perform(
                        post("/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());

        verify(bookService, never()).createBook(any(BookRequest.class));
    }

    //GET BOOK BY ID TESTS

    @Test
    @WithMockUser
    void testGetBookByIdSuccess() throws Exception {
        Long bookId = 1L;
        BookResponse response = new BookResponse(bookId, "Harry Potter and the Philosopher's Stone",
                "J.K. Rowling", "Fantasy", 1997, false);

        when(bookService.findBookById(bookId)).thenReturn(response);

        mockMvc.perform(
                        get("/api/books/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.title").value("Harry Potter and the Philosopher's Stone"));

        verify(bookService).findBookById(bookId);
    }

    @Test
    @WithMockUser
    void testGetBookByIdNotFound() throws Exception {
        Long bookId = 999L;

        when(bookService.findBookById(bookId))
                .thenThrow(new ResourceNotFoundException("Book not found"));

        mockMvc.perform(
                        get("/api/books/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(bookService).findBookById(bookId);
    }

    //GET BOOK BY TITLE TESTS

    @Test
    @WithMockUser
    void testGetBookByTitleSuccess() throws Exception {
        String title = "Harry Potter and the Philosopher's Stone";
        BookResponse response = new BookResponse(1L, title, "J.K. Rowling", "Fantasy", 1997, false);

        when(bookService.findBookByTitle(title)).thenReturn(response);

        mockMvc.perform(
                        get("/api/books/title/{title}", title)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(title));

        verify(bookService).findBookByTitle(title);
    }

    @Test
    @WithMockUser
    void testGetBookByTitleNotFound() throws Exception {
        String title = "Non-existent Book";

        when(bookService.findBookByTitle(title))
                .thenThrow(new ResourceNotFoundException("Book not found"));

        mockMvc.perform(
                        get("/api/books/title/{title}", title)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(bookService).findBookByTitle(title);
    }

    //GET BOOKS BY AUTHOR TESTS

    @Test
    @WithMockUser
    void testGetBooksByAuthorSuccess() throws Exception {
        Long authorId = 1L;
        BookResponse book1 = new BookResponse(1L, "Harry Potter and the Philosopher's Stone",
                "J.K. Rowling", "Fantasy", 1997, false);
        BookResponse book2 = new BookResponse(2L, "Harry Potter and the Chamber of Secrets",
                "J.K. Rowling", "Fantasy", 1998, false);

        List<BookResponse> books = Arrays.asList(book1, book2);

        when(bookService.findBooksByAuthor(authorId)).thenReturn(books);

        mockMvc.perform(
                        get("/api/books/author/{authorId}", authorId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].authorName").value("J.K. Rowling"))
                .andExpect(jsonPath("$[1].authorName").value("J.K. Rowling"));

        verify(bookService).findBooksByAuthor(authorId);
    }

    @Test
    @WithMockUser
    void testGetBooksByAuthorEmpty() throws Exception {
        Long authorId = 999L;

        when(bookService.findBooksByAuthor(authorId)).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/books/author/{authorId}", authorId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookService).findBooksByAuthor(authorId);
    }

    //GET BOOKS BY CATEGORY TESTS

    @Test
    @WithMockUser
    void testGetBooksByCategorySuccess() throws Exception {
        Long categoryId = 1L;
        BookResponse book1 = new BookResponse(1L, "Book 1", "Author 1", "Fantasy", 1997, false);
        BookResponse book2 = new BookResponse(2L, "Book 2", "Author 2", "Fantasy", 1998, false);

        List<BookResponse> books = Arrays.asList(book1, book2);

        when(bookService.findBooksByCategory(categoryId)).thenReturn(books);

        mockMvc.perform(
                        get("/api/books/category/{categoryId}", categoryId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoryName").value("Fantasy"))
                .andExpect(jsonPath("$[1].categoryName").value("Fantasy"));

        verify(bookService).findBooksByCategory(categoryId);
    }

    //GET BOOKS BY RELEASE YEAR TESTS

    @Test
    @WithMockUser
    void testGetBooksByReleaseYearSuccess() throws Exception {
        int releaseYear = 1997;
        BookResponse book1 = new BookResponse(1L, "Book 1", "Author 1", "Category 1", releaseYear, false);
        BookResponse book2 = new BookResponse(2L, "Book 2", "Author 2", "Category 2", releaseYear, false);

        List<BookResponse> books = Arrays.asList(book1, book2);

        when(bookService.findBooksByReleaseYear(releaseYear)).thenReturn(books);

        mockMvc.perform(
                        get("/api/books/release-year/{releaseYear}", releaseYear)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].releaseYear").value(releaseYear))
                .andExpect(jsonPath("$[1].releaseYear").value(releaseYear));

        verify(bookService).findBooksByReleaseYear(releaseYear);
    }

    //SEARCH BOOKS TESTS

    @Test
    @WithMockUser
    void testSearchBooksByTitle() throws Exception {
        BookResponse book1 = new BookResponse(1L, "Harry Potter", "J.K. Rowling", "Fantasy", 1997, false);
        List<BookResponse> books = List.of(book1);

        when(bookService.searchBooks("Harry Potter", null, null, null)).thenReturn(books);

        mockMvc.perform(
                        get("/api/books/search")
                                .param("title", "Harry Potter")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Harry Potter"));

        verify(bookService).searchBooks("Harry Potter", null, null, null);
    }

    @Test
    @WithMockUser
    void testSearchBooksByAuthorName() throws Exception {
        BookResponse book1 = new BookResponse(1L, "Book 1", "J.K. Rowling", "Fantasy", 1997, false);
        List<BookResponse> books = List.of(book1);

        when(bookService.searchBooks(null, "J.K. Rowling", null, null)).thenReturn(books);

        mockMvc.perform(
                        get("/api/books/search")
                                .param("authorName", "J.K. Rowling")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookService).searchBooks(null, "J.K. Rowling", null, null);
    }

    @Test
    @WithMockUser
    void testSearchBooksByCategoryName() throws Exception {
        BookResponse book1 = new BookResponse(1L, "Book 1", "Author 1", "Fantasy", 1997, false);
        List<BookResponse> books = List.of(book1);

        when(bookService.searchBooks(null, null, "Fantasy", null)).thenReturn(books);

        mockMvc.perform(
                        get("/api/books/search")
                                .param("categoryName", "Fantasy")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookService).searchBooks(null, null, "Fantasy", null);
    }

    @Test
    @WithMockUser
    void testSearchBooksByReleaseYear() throws Exception {
        BookResponse book1 = new BookResponse(1L, "Book 1", "Author 1", "Category 1", 1997, false);
        List<BookResponse> books = List.of(book1);

        when(bookService.searchBooks(null, null, null, 1997)).thenReturn(books);

        mockMvc.perform(
                        get("/api/books/search")
                                .param("releaseYear", "1997")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookService).searchBooks(null, null, null, 1997);
    }

    @Test
    @WithMockUser
    void testSearchBooksMultipleCriteria() throws Exception {
        BookResponse book1 = new BookResponse(1L, "Harry Potter", "J.K. Rowling", "Fantasy", 1997, false);
        List<BookResponse> books = List.of(book1);

        when(bookService.searchBooks("Harry Potter", "J.K. Rowling", "Fantasy", 1997))
                .thenReturn(books);

        mockMvc.perform(
                        get("/api/books/search")
                                .param("title", "Harry Potter")
                                .param("authorName", "J.K. Rowling")
                                .param("categoryName", "Fantasy")
                                .param("releaseYear", "1997")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookService).searchBooks("Harry Potter", "J.K. Rowling", "Fantasy", 1997);
    }

    @Test
    @WithMockUser
    void testSearchBooksNoResults() throws Exception {
        when(bookService.searchBooks(anyString(), anyString(), anyString(), any()))
                .thenReturn(List.of());

        mockMvc.perform(
                        get("/api/books/search")
                                .param("title", "Non-existent")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookService).searchBooks("Non-existent", null, null, null);
    }

    //GET BORROWED BOOKS TESTS

    @Test
    @WithMockUser
    void testGetBorrowedBooksSuccess() throws Exception {
        BookResponse book1 = new BookResponse(1L, "Book 1", "Author 1", "Category 1", 1997, true);
        BookResponse book2 = new BookResponse(2L, "Book 2", "Author 2", "Category 2", 1998, true);
        List<BookResponse> books = Arrays.asList(book1, book2);

        when(bookService.findBorrowedBooks()).thenReturn(books);

        mockMvc.perform(
                        get("/api/books/borrowed")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Book 1"))
                .andExpect(jsonPath("$[1].title").value("Book 2"));



        verify(bookService).findBorrowedBooks();
    }

    @Test
    @WithMockUser
    void testGetBorrowedBooksEmpty() throws Exception {
        when(bookService.findBorrowedBooks()).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/books/borrowed")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookService).findBorrowedBooks();
    }

    //GET AVAILABLE BOOKS TESTS

    @Test
    @WithMockUser
    void testGetAvailableBooksSuccess() throws Exception {
        BookResponse book1 = new BookResponse(1L, "Book 1", "Author 1", "Category 1", 1997, false);
        BookResponse book2 = new BookResponse(2L, "Book 2", "Author 2", "Category 2", 1998, false);
        List<BookResponse> books = Arrays.asList(book1, book2);

        when(bookService.findAvailableBooks()).thenReturn(books);

        mockMvc.perform(
                        get("/api/books/available")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Book 1"))
                .andExpect(jsonPath("$[1].title").value("Book 2"));

        verify(bookService).findAvailableBooks();
    }

    //GET ALL BOOKS TESTS

    @Test
    @WithMockUser
    void testGetAllBooksSuccess() throws Exception {
        BookResponse book1 = new BookResponse(1L, "Book 1", "Author 1", "Category 1", 1997, false);
        BookResponse book2 = new BookResponse(2L, "Book 2", "Author 2", "Category 2", 1998, true);
        List<BookResponse> books = Arrays.asList(book1, book2);

        when(bookService.findAllBooks()).thenReturn(books);

        mockMvc.perform(
                        get("/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(bookService).findAllBooks();
    }

    @Test
    @WithMockUser
    void testGetAllBooksEmpty() throws Exception {
        when(bookService.findAllBooks()).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/books")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(bookService).findAllBooks();
    }

    //UPDATE BOOK TESTS

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateBookSuccess() throws Exception {
        Long bookId = 1L;
        BookRequest request = new BookRequest();
        request.setTitle("Updated Title");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(2000);

        BookResponse response = new BookResponse(bookId, "Updated Title", "J.K. Rowling", "Fantasy", 2000, false);

        when(bookService.updateBook(bookId, request)).thenReturn(response);

        mockMvc.perform(
                        put("/api/books/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.releaseYear").value(2000));

        verify(bookService).updateBook(bookId, request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateBookNotFound() throws Exception {
        Long bookId = 999L;
        BookRequest request = new BookRequest();
        request.setTitle("Updated Title");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(2000);

        when(bookService.updateBook(bookId, request))
                .thenThrow(new ResourceNotFoundException("Book not found"));

        mockMvc.perform(
                        put("/api/books/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(bookService).updateBook(bookId, request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateBookInvalidRequest() throws Exception {
        Long bookId = 1L;
        BookRequest request = new BookRequest();
        request.setTitle(""); // Blank title
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(2000);

        mockMvc.perform(
                        put("/api/books/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(bookService, never()).updateBook(anyLong(), any(BookRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateBookForbidden() throws Exception {
        Long bookId = 1L;
        BookRequest request = new BookRequest();
        request.setTitle("Updated Title");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(2000);

        mockMvc.perform(
                        put("/api/books/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden());

        verify(bookService, never()).updateBook(anyLong(), any(BookRequest.class));
    }

    //DELETE BOOK TESTS

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteBookSuccess() throws Exception {
        Long bookId = 1L;

        doNothing().when(bookService).deleteBook(bookId);

        mockMvc.perform(
                        delete("/api/books/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(bookId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteBookNotFound() throws Exception {
        Long bookId = 999L;

        doThrow(new ResourceNotFoundException("Book not found"))
                .when(bookService).deleteBook(bookId);

        mockMvc.perform(
                        delete("/api/books/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(bookService).deleteBook(bookId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testDeleteBookForbidden() throws Exception {
        Long bookId = 1L;

        mockMvc.perform(
                        delete("/api/books/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(bookService, never()).deleteBook(anyLong());
    }
}