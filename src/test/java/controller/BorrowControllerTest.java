package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.library.LibraryApplication;
import org.library.dto.borrow.BorrowRequest;
import org.library.dto.borrow.BorrowResponse;
import org.library.exception.BadRequestException;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LibraryApplication.class)
@AutoConfigureMockMvc
class BorrowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowService borrowService;

    @Autowired
    private ObjectMapper objectMapper;

    // ========== BORROW BOOK TESTS ==========

    @Test
    @WithMockUser(roles = "USER")
    void testBorrowBookSuccess() throws Exception {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);

        BorrowResponse response = new BorrowResponse(1L, 1L, "Harry Potter", 1L, "John Doe",
                LocalDate.now(), null, false);

        when(borrowService.borrowBook(any(BorrowRequest.class))).thenReturn(response);

        mockMvc.perform(
                        post("/api/borrows")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookId").value(1L))
                .andExpect(jsonPath("$.bookTitle").value("Harry Potter"))
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.userName").value("John Doe"));

        verify(borrowService).borrowBook(any(BorrowRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testBorrowBookMaxLimitReached() throws Exception {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);

        when(borrowService.borrowBook(any(BorrowRequest.class)))
                .thenThrow(new BadRequestException("Maximum active borrow limit of 5 books reached"));

        mockMvc.perform(
                        post("/api/borrows")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(borrowService).borrowBook(any(BorrowRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testBorrowBookNotFound() throws Exception {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(999L);

        when(borrowService.borrowBook(any(BorrowRequest.class)))
                .thenThrow(new ResourceNotFoundException("Book not found"));

        mockMvc.perform(
                        post("/api/borrows")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());

        verify(borrowService).borrowBook(any(BorrowRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testBorrowBookAlreadyBorrowed() throws Exception {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);

        when(borrowService.borrowBook(any(BorrowRequest.class)))
                .thenThrow(new ConflictException("Book is already borrowed"));

        mockMvc.perform(
                        post("/api/borrows")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isConflict());

        verify(borrowService).borrowBook(any(BorrowRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testBorrowBookInvalidRequest_NullBookId() throws Exception {
        String emptyRequest = "{}";

        mockMvc.perform(
                        post("/api/borrows")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(emptyRequest)
                )
                .andExpect(status().isBadRequest());

        verify(borrowService, never()).borrowBook(any(BorrowRequest.class));
    }


    // ========== RETURN BOOK TESTS ==========

    @Test
    @WithMockUser(roles = "USER")
    void testReturnBookSuccess() throws Exception {
        Long borrowRecordId = 1L;
        BorrowResponse response = new BorrowResponse(1L, 1L, "Harry Potter", 1L, "John Doe",
                LocalDate.now().minusDays(5), LocalDate.now(), true);

        when(borrowService.returnBook(borrowRecordId)).thenReturn(response);

        mockMvc.perform(
                        put("/api/borrows/{borrowRecordId}/return", borrowRecordId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.returnDate").exists());

        verify(borrowService).returnBook(borrowRecordId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testReturnBookNotFound() throws Exception {
        Long borrowRecordId = 999L;

        when(borrowService.returnBook(borrowRecordId))
                .thenThrow(new ResourceNotFoundException("Borrow record not found"));

        mockMvc.perform(
                        put("/api/borrows/{borrowRecordId}/return", borrowRecordId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(borrowService).returnBook(borrowRecordId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testReturnBookNotOwned() throws Exception {
        Long borrowRecordId = 1L;

        when(borrowService.returnBook(borrowRecordId))
                .thenThrow(new BadRequestException("You can only return your own borrowed books"));

        mockMvc.perform(
                        put("/api/borrows/{borrowRecordId}/return", borrowRecordId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());

        verify(borrowService).returnBook(borrowRecordId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testReturnBookAlreadyReturned() throws Exception {
        Long borrowRecordId = 1L;

        when(borrowService.returnBook(borrowRecordId))
                .thenThrow(new ConflictException("Book is already returned"));

        mockMvc.perform(
                        put("/api/borrows/{borrowRecordId}/return", borrowRecordId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict());

        verify(borrowService).returnBook(borrowRecordId);
    }

    // ========== GET MY ACTIVE BORROWS TESTS ==========

    @Test
    @WithMockUser(roles = "USER")
    void testGetMyBorrowedBooksSuccess() throws Exception {
        BorrowResponse borrow1 = new BorrowResponse(1L, 1L, "Book 1", 1L, "John Doe",
                LocalDate.now().minusDays(5), null, false);
        BorrowResponse borrow2 = new BorrowResponse(2L, 2L, "Book 2", 1L, "John Doe",
                LocalDate.now().minusDays(3), null, false);
        List<BorrowResponse> borrows = Arrays.asList(borrow1, borrow2);

        when(borrowService.getMyBorrowedBooks()).thenReturn(borrows);

        mockMvc.perform(
                        get("/api/borrows/active")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bookTitle").value("Book 1"))
                .andExpect(jsonPath("$[1].bookTitle").value("Book 2"));

        verify(borrowService).getMyBorrowedBooks();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetMyBorrowedBooksEmpty() throws Exception {
        when(borrowService.getMyBorrowedBooks()).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/borrows/active")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(borrowService).getMyBorrowedBooks();
    }

    // ========== GET MY BORROW HISTORY TESTS ==========

    @Test
    @WithMockUser(roles = "USER")
    void testGetMyBorrowHistorySuccess() throws Exception {
        BorrowResponse borrow1 = new BorrowResponse(1L, 1L, "Book 1", 1L, "John Doe",
                LocalDate.now().minusDays(30), LocalDate.now().minusDays(20), true);
        BorrowResponse borrow2 = new BorrowResponse(2L, 2L, "Book 2", 1L, "John Doe",
                LocalDate.now().minusDays(15), LocalDate.now().minusDays(10), true);
        List<BorrowResponse> borrows = Arrays.asList(borrow1, borrow2);

        when(borrowService.getMyBorrowHistory()).thenReturn(borrows);

        mockMvc.perform(
                        get("/api/borrows/history")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bookTitle").value("Book 1"))
                .andExpect(jsonPath("$[1].bookTitle").value("Book 2"));

        verify(borrowService).getMyBorrowHistory();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetMyBorrowHistoryEmpty() throws Exception {
        when(borrowService.getMyBorrowHistory()).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/borrows/history")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(borrowService).getMyBorrowHistory();
    }

    // ========== GET BORROW RECORD BY ID (ADMIN) TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBorrowRecordByIdSuccess() throws Exception {
        Long borrowRecordId = 1L;
        BorrowResponse response = new BorrowResponse(1L, 1L, "Harry Potter", 1L, "John Doe",
                LocalDate.now().minusDays(5), null, false);

        when(borrowService.getBorrowRecordById(borrowRecordId)).thenReturn(response);

        mockMvc.perform(
                        get("/api/borrows/{borrowRecordId}", borrowRecordId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.bookTitle").value("Harry Potter"));

        verify(borrowService).getBorrowRecordById(borrowRecordId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBorrowRecordByIdNotFound() throws Exception {
        Long borrowRecordId = 999L;

        when(borrowService.getBorrowRecordById(borrowRecordId))
                .thenThrow(new ResourceNotFoundException("Borrow record not found"));

        mockMvc.perform(
                        get("/api/borrows/{borrowRecordId}", borrowRecordId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(borrowService).getBorrowRecordById(borrowRecordId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetBorrowRecordByIdForbidden() throws Exception {
        Long borrowRecordId = 1L;

        mockMvc.perform(
                        get("/api/borrows/{borrowRecordId}", borrowRecordId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(borrowService, never()).getBorrowRecordById(anyLong());
    }

    // ========== GET ALL ACTIVE BORROWS (ADMIN) TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllActiveBorrowsSuccess() throws Exception {
        BorrowResponse borrow1 = new BorrowResponse(1L, 1L, "Book 1", 1L, "User 1",
                LocalDate.now().minusDays(5), null, false);
        BorrowResponse borrow2 = new BorrowResponse(2L, 2L, "Book 2", 2L, "User 2",
                LocalDate.now().minusDays(3), null, false);
        List<BorrowResponse> borrows = Arrays.asList(borrow1, borrow2);

        when(borrowService.getAllActiveBorrows()).thenReturn(borrows);

        mockMvc.perform(
                        get("/api/borrows/all/active")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bookTitle").value("Book 1"))
                .andExpect(jsonPath("$[1].bookTitle").value("Book 2"));

        verify(borrowService).getAllActiveBorrows();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllActiveBorrowsEmpty() throws Exception {
        when(borrowService.getAllActiveBorrows()).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/borrows/all/active")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(borrowService).getAllActiveBorrows();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllActiveBorrowsForbidden() throws Exception {
        mockMvc.perform(
                        get("/api/borrows/all/active")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(borrowService, never()).getAllActiveBorrows();
    }

    // ========== GET ALL RETURNED BORROWS (ADMIN) TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllReturnedBorrowsSuccess() throws Exception {
        BorrowResponse borrow1 = new BorrowResponse(1L, 1L, "Book 1", 1L, "User 1",
                LocalDate.now().minusDays(30), LocalDate.now().minusDays(20), true);
        BorrowResponse borrow2 = new BorrowResponse(2L, 2L, "Book 2", 2L, "User 2",
                LocalDate.now().minusDays(15), LocalDate.now().minusDays(10), true);
        List<BorrowResponse> borrows = Arrays.asList(borrow1, borrow2);

        when(borrowService.getAllReturnedBorrows()).thenReturn(borrows);

        mockMvc.perform(
                        get("/api/borrows/all/returned")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bookTitle").value("Book 1"))
                .andExpect(jsonPath("$[1].bookTitle").value("Book 2"));

        verify(borrowService).getAllReturnedBorrows();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllReturnedBorrowsEmpty() throws Exception {
        when(borrowService.getAllReturnedBorrows()).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/borrows/all/returned")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(borrowService).getAllReturnedBorrows();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllReturnedBorrowsForbidden() throws Exception {
        mockMvc.perform(
                        get("/api/borrows/all/returned")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(borrowService, never()).getAllReturnedBorrows();
    }

    // ========== GET BORROWS BY BOOK (ADMIN) TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBorrowsByBookSuccess() throws Exception {
        Long bookId = 1L;
        BorrowResponse borrow1 = new BorrowResponse(1L, bookId, "Book 1", 1L, "User 1",
                LocalDate.now().minusDays(30), LocalDate.now().minusDays(20), true);
        BorrowResponse borrow2 = new BorrowResponse(2L, bookId, "Book 1", 2L, "User 2",
                LocalDate.now().minusDays(15), LocalDate.now().minusDays(10), true);
        List<BorrowResponse> borrows = Arrays.asList(borrow1, borrow2);

        when(borrowService.getBorrowsByBook(bookId)).thenReturn(borrows);

        mockMvc.perform(
                        get("/api/borrows/book/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].bookId").value(bookId))
                .andExpect(jsonPath("$[1].bookId").value(bookId));

        verify(borrowService).getBorrowsByBook(bookId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBorrowsByBookNotFound() throws Exception {
        Long bookId = 999L;

        when(borrowService.getBorrowsByBook(bookId))
                .thenThrow(new ResourceNotFoundException("Book not found"));

        mockMvc.perform(
                        get("/api/borrows/book/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(borrowService).getBorrowsByBook(bookId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBorrowsByBookEmpty() throws Exception {
        Long bookId = 1L;

        when(borrowService.getBorrowsByBook(bookId)).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/borrows/book/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(borrowService).getBorrowsByBook(bookId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetBorrowsByBookForbidden() throws Exception {
        Long bookId = 1L;

        mockMvc.perform(
                        get("/api/borrows/book/{bookId}", bookId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(borrowService, never()).getBorrowsByBook(anyLong());
    }

    // ========== GET BORROWS BY USER (ADMIN) TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBorrowsByUserSuccess() throws Exception {
        Long userId = 1L;
        BorrowResponse borrow1 = new BorrowResponse(1L, 1L, "Book 1", userId, "User 1",
                LocalDate.now().minusDays(30), LocalDate.now().minusDays(20), true);
        BorrowResponse borrow2 = new BorrowResponse(2L, 2L, "Book 2", userId, "User 1",
                LocalDate.now().minusDays(15), LocalDate.now().minusDays(10), true);
        List<BorrowResponse> borrows = Arrays.asList(borrow1, borrow2);

        when(borrowService.getBorrowsByUser(userId)).thenReturn(borrows);

        mockMvc.perform(
                        get("/api/borrows/user/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userId").value(userId))
                .andExpect(jsonPath("$[1].userId").value(userId));

        verify(borrowService).getBorrowsByUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBorrowsByUserNotFound() throws Exception {
        Long userId = 999L;

        when(borrowService.getBorrowsByUser(userId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(
                        get("/api/borrows/user/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        verify(borrowService).getBorrowsByUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBorrowsByUserEmpty() throws Exception {
        Long userId = 1L;

        when(borrowService.getBorrowsByUser(userId)).thenReturn(List.of());

        mockMvc.perform(
                        get("/api/borrows/user/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(borrowService).getBorrowsByUser(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetBorrowsByUserForbidden() throws Exception {
        Long userId = 1L;

        mockMvc.perform(
                        get("/api/borrows/user/{userId}", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(borrowService, never()).getBorrowsByUser(anyLong());
    }
}