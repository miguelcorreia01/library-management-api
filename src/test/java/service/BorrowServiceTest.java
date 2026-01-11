package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.dto.borrow.BorrowRequest;
import org.library.dto.borrow.BorrowResponse;
import org.library.entities.Author;
import org.library.entities.Book;
import org.library.entities.BorrowRecord;
import org.library.entities.Category;
import org.library.entities.Role;
import org.library.entities.User;
import org.library.exception.BadRequestException;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.repository.BookRepository;
import org.library.repository.BorrowRecordRepository;
import org.library.repository.UserRepository;
import org.library.service.BorrowService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BorrowServiceTest {

    private BorrowService borrowService;
    private BorrowRecordRepository borrowRecordRepository;
    private BookRepository bookRepository;
    private UserRepository userRepository;

    private User testUser;
    private Book testBook;
    private Author testAuthor;
    private Category testCategory;

    @BeforeEach
    void setup() {
        borrowRecordRepository = mock(BorrowRecordRepository.class);
        bookRepository = mock(BookRepository.class);
        userRepository = mock(UserRepository.class);

        borrowService = new BorrowService(borrowRecordRepository, bookRepository, userRepository);

        // Setup test data
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@email.com");
        testUser.setRole(Role.USER);
        testUser.setActive(true);

        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setName("J.K. Rowling");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Fantasy");

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Harry Potter");
        testBook.setAuthor(testAuthor);
        testBook.setCategory(testCategory);
        testBook.setReleaseYear(1997);
        testBook.setBorrowed(false);

        // Mock SecurityContext
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("john@email.com");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testBorrowBookSuccess() {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);

        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(testUser));
        when(borrowRecordRepository.countByUserAndIsReturnedFalse(testUser)).thenReturn(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return book;
        });

        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(invocation -> {
            BorrowRecord record = invocation.getArgument(0);
            record.setId(1L);
            return record;
        });

        BorrowResponse response = borrowService.borrowBook(request);

        assertNotNull(response);
        assertEquals(1L, response.getBookId());
        assertEquals("Harry Potter", response.getBookTitle());
        assertEquals(1L, response.getUserId());
        assertEquals("John Doe", response.getUserName());
        assertFalse(response.isReturned());
        assertNotNull(response.getBorrowDate());

        verify(userRepository).findByEmail("john@email.com");
        verify(borrowRecordRepository).countByUserAndIsReturnedFalse(testUser);
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    @Test
    void testBorrowBookMaxLimitReached() {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);

        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(testUser));
        when(borrowRecordRepository.countByUserAndIsReturnedFalse(testUser)).thenReturn(5);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> borrowService.borrowBook(request));

        assertEquals("Maximum active borrow limit of 5 books reached", exception.getMessage());

        verify(userRepository).findByEmail("john@email.com");
        verify(borrowRecordRepository).countByUserAndIsReturnedFalse(testUser);
        verify(bookRepository, never()).findById(anyLong());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testBorrowBookNotFound() {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(999L);

        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(testUser));
        when(borrowRecordRepository.countByUserAndIsReturnedFalse(testUser)).thenReturn(0);
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> borrowService.borrowBook(request));

        assertEquals("Book not found", exception.getMessage());

        verify(userRepository).findByEmail("john@email.com");
        verify(borrowRecordRepository).countByUserAndIsReturnedFalse(testUser);
        verify(bookRepository).findById(999L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testBorrowBookAlreadyBorrowed() {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);

        Book borrowedBook = new Book();
        borrowedBook.setId(1L);
        borrowedBook.setTitle("Harry Potter");
        borrowedBook.setBorrowed(true);

        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(testUser));
        when(borrowRecordRepository.countByUserAndIsReturnedFalse(testUser)).thenReturn(0);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(borrowedBook));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> borrowService.borrowBook(request));

        assertEquals("Book is already borrowed", exception.getMessage());

        verify(userRepository).findByEmail("john@email.com");
        verify(borrowRecordRepository).countByUserAndIsReturnedFalse(testUser);
        verify(bookRepository).findById(1L);
        verify(bookRepository, never()).save(any(Book.class));
        verify(borrowRecordRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    void testReturnBookSuccess() {
        Long borrowRecordId = 1L;
        BorrowRecord borrowRecord = createBorrowRecord(borrowRecordId, testUser, testBook, false);

        when(borrowRecordRepository.findById(borrowRecordId)).thenReturn(Optional.of(borrowRecord));
        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(testUser));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(borrowRecordRepository.save(any(BorrowRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BorrowResponse response = borrowService.returnBook(borrowRecordId);

        assertNotNull(response);
        assertTrue(response.isReturned());
        assertNotNull(response.getReturnDate());
        assertFalse(testBook.isBorrowed());

        verify(borrowRecordRepository).findById(borrowRecordId);
        verify(userRepository).findByEmail("john@email.com");
        verify(bookRepository).save(any(Book.class));
        verify(borrowRecordRepository).save(any(BorrowRecord.class));
    }

    @Test
    void testReturnBookNotFound() {
        Long borrowRecordId = 999L;
        when(borrowRecordRepository.findById(borrowRecordId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> borrowService.returnBook(borrowRecordId));

        assertEquals("Borrow record not found", exception.getMessage());

        verify(borrowRecordRepository).findById(borrowRecordId);
        verify(borrowRecordRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    void testReturnBookNotOwned() {
        Long borrowRecordId = 1L;
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setName("Jane Doe");
        otherUser.setEmail("jane@email.com");

        BorrowRecord borrowRecord = createBorrowRecord(borrowRecordId, otherUser, testBook, false);

        when(borrowRecordRepository.findById(borrowRecordId)).thenReturn(Optional.of(borrowRecord));
        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(testUser));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> borrowService.returnBook(borrowRecordId));

        assertEquals("You can only return your own borrowed books", exception.getMessage());

        verify(borrowRecordRepository).findById(borrowRecordId);
        verify(userRepository).findByEmail("john@email.com");
        verify(borrowRecordRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    void testReturnBookAlreadyReturned() {
        Long borrowRecordId = 1L;
        BorrowRecord borrowRecord = createBorrowRecord(borrowRecordId, testUser, testBook, true); // Already returned

        when(borrowRecordRepository.findById(borrowRecordId)).thenReturn(Optional.of(borrowRecord));
        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(testUser));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> borrowService.returnBook(borrowRecordId));

        assertEquals("Book is already returned", exception.getMessage());

        verify(borrowRecordRepository).findById(borrowRecordId);
        verify(userRepository).findByEmail("john@email.com");
        verify(borrowRecordRepository, never()).save(any(BorrowRecord.class));
    }

    @Test
    void testGetBorrowRecordByIdSuccess() {
        Long borrowRecordId = 1L;
        BorrowRecord borrowRecord = createBorrowRecord(borrowRecordId, testUser, testBook, false);

        when(borrowRecordRepository.findById(borrowRecordId)).thenReturn(Optional.of(borrowRecord));

        BorrowResponse response = borrowService.getBorrowRecordById(borrowRecordId);

        assertNotNull(response);
        assertEquals(borrowRecordId, response.getId());
        assertEquals(1L, response.getBookId());
        assertEquals(1L, response.getUserId());

        verify(borrowRecordRepository).findById(borrowRecordId);
    }

    @Test
    void testGetBorrowRecordByIdNotFound() {
        Long borrowRecordId = 999L;
        when(borrowRecordRepository.findById(borrowRecordId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> borrowService.getBorrowRecordById(borrowRecordId));

        assertEquals("Borrow record not found", exception.getMessage());
        verify(borrowRecordRepository).findById(borrowRecordId);
    }

    @Test
    void testGetMyBorrowedBooks() {
        BorrowRecord record1 = createBorrowRecord(1L, testUser, testBook, false);
        Book book2 = createTestBook(2L, "Book 2");
        BorrowRecord record2 = createBorrowRecord(2L, testUser, book2, false);

        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(testUser));
        when(borrowRecordRepository.findByUserAndIsReturnedFalse(testUser))
                .thenReturn(Arrays.asList(record1, record2));

        List<BorrowResponse> responses = borrowService.getMyBorrowedBooks();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertFalse(responses.get(0).isReturned());
        assertFalse(responses.get(1).isReturned());

        verify(userRepository).findByEmail("john@email.com");
        verify(borrowRecordRepository).findByUserAndIsReturnedFalse(testUser);
    }

    @Test
    void testGetMyBorrowHistory() {
        BorrowRecord record1 = createBorrowRecord(1L, testUser, testBook, false);
        BorrowRecord record2 = createBorrowRecord(2L, testUser, testBook, true);

        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(testUser));
        when(borrowRecordRepository.findByUser(testUser))
                .thenReturn(Arrays.asList(record1, record2));

        List<BorrowResponse> responses = borrowService.getMyBorrowHistory();

        assertNotNull(responses);
        assertEquals(2, responses.size());

        verify(userRepository).findByEmail("john@email.com");
        verify(borrowRecordRepository).findByUser(testUser);
    }

    @Test
    void testGetAllActiveBorrows() {
        BorrowRecord record1 = createBorrowRecord(1L, testUser, testBook, false);
        User user2 = new User();
        user2.setId(2L);
        Book book2 = createTestBook(2L, "Book 2");
        BorrowRecord record2 = createBorrowRecord(2L, user2, book2, false);

        when(borrowRecordRepository.findByIsReturnedFalse())
                .thenReturn(Arrays.asList(record1, record2));

        List<BorrowResponse> responses = borrowService.getAllActiveBorrows();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertFalse(responses.get(0).isReturned());
        assertFalse(responses.get(1).isReturned());

        verify(borrowRecordRepository).findByIsReturnedFalse();
    }

    @Test
    void testGetAllReturnedBorrows() {
        BorrowRecord record1 = createBorrowRecord(1L, testUser, testBook, true);
        BorrowRecord record2 = createBorrowRecord(2L, testUser, testBook, true);

        when(borrowRecordRepository.findByIsReturnedTrue())
                .thenReturn(Arrays.asList(record1, record2));

        List<BorrowResponse> responses = borrowService.getAllReturnedBorrows();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue(responses.get(0).isReturned());
        assertTrue(responses.get(1).isReturned());

        verify(borrowRecordRepository).findByIsReturnedTrue();
    }

    @Test
    void testGetBorrowsByBook() {
        BorrowRecord record1 = createBorrowRecord(1L, testUser, testBook, false);
        BorrowRecord record2 = createBorrowRecord(2L, testUser, testBook, true);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(borrowRecordRepository.findByBook(testBook))
                .thenReturn(Arrays.asList(record1, record2));

        List<BorrowResponse> responses = borrowService.getBorrowsByBook(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getBookId());
        assertEquals(1L, responses.get(1).getBookId());

        verify(bookRepository).findById(1L);
        verify(borrowRecordRepository).findByBook(testBook);
    }

    @Test
    void testGetBorrowsByBookNotFound() {
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> borrowService.getBorrowsByBook(999L));

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository).findById(999L);
        verify(borrowRecordRepository, never()).findByBook(any(Book.class));
    }

    @Test
    void testGetBorrowsByUser() {
        BorrowRecord record1 = createBorrowRecord(1L, testUser, testBook, false);
        BorrowRecord record2 = createBorrowRecord(2L, testUser, testBook, true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(borrowRecordRepository.findByUser(testUser))
                .thenReturn(Arrays.asList(record1, record2));

        List<BorrowResponse> responses = borrowService.getBorrowsByUser(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getUserId());
        assertEquals(1L, responses.get(1).getUserId());

        verify(userRepository).findById(1L);
        verify(borrowRecordRepository).findByUser(testUser);
    }

    @Test
    void testGetBorrowsByUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> borrowService.getBorrowsByUser(999L));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(999L);
        verify(borrowRecordRepository, never()).findByUser(any(User.class));
    }

    // Helper methods
    private BorrowRecord createBorrowRecord(Long id, User user, Book book, boolean isReturned) {
        BorrowRecord record = new BorrowRecord();
        record.setId(id);
        record.setUser(user);
        record.setBook(book);
        record.setBorrowDate(LocalDate.now());
        record.setReturned(isReturned);
        if (isReturned) {
            record.setReturnDate(LocalDate.now());
        }
        return record;
    }

    private Book createTestBook(Long id, String title) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor(testAuthor);
        book.setCategory(testCategory);
        book.setReleaseYear(2000);
        book.setBorrowed(false);
        return book;
    }
}