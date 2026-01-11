package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.dto.statistics.*;
import org.library.entities.Author;
import org.library.entities.Book;
import org.library.entities.BorrowRecord;
import org.library.entities.Category;
import org.library.entities.User;
import org.library.repository.BookRepository;
import org.library.repository.BorrowRecordRepository;
import org.library.repository.UserRepository;
import org.library.service.StatisticsService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatisticsServiceTest {

    private StatisticsService statisticsService;
    private BookRepository bookRepository;
    private BorrowRecordRepository borrowRecordRepository;
    private UserRepository userRepository;

    private Author author1;
    private Author author2;
    private Category category1;
    private Category category2;
    private Book book1;
    private Book book2;
    private Book book3;
    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        bookRepository = mock(BookRepository.class);
        borrowRecordRepository = mock(BorrowRecordRepository.class);
        userRepository = mock(UserRepository.class);

        statisticsService = new StatisticsService(bookRepository, borrowRecordRepository, userRepository);

        // Setup test data
        author1 = new Author();
        author1.setId(1L);
        author1.setName("J.K. Rowling");

        author2 = new Author();
        author2.setId(2L);
        author2.setName("George R.R. Martin");

        category1 = new Category();
        category1.setId(1L);
        category1.setName("Fantasy");

        category2 = new Category();
        category2.setId(2L);
        category2.setName("Science Fiction");

        book1 = new Book();
        book1.setId(1L);
        book1.setTitle("Harry Potter");
        book1.setAuthor(author1);
        book1.setCategory(category1);
        book1.setBorrowed(true);

        book2 = new Book();
        book2.setId(2L);
        book2.setTitle("A Game of Thrones");
        book2.setAuthor(author2);
        book2.setCategory(category1);
        book2.setBorrowed(false);

        book3 = new Book();
        book3.setId(3L);
        book3.setTitle("Foundation");
        book3.setAuthor(author2);
        book3.setCategory(category2);
        book3.setBorrowed(true);

        user1 = new User();
        user1.setId(1L);
        user1.setName("John Doe");
        user1.setEmail("john@email.com");

        user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Doe");
        user2.setEmail("jane@email.com");
    }

    @Test
    void testGetStatisticsWithData() {
        // Setup books
        List<Book> allBooks = Arrays.asList(book1, book2, book3);
        List<Book> borrowedBooks = Arrays.asList(book1, book3);
        List<Book> availableBooks = Arrays.asList(book2);

        // Setup borrow records
        BorrowRecord record1 = createBorrowRecord(1L, user1, book1, false);
        BorrowRecord record2 = createBorrowRecord(2L, user1, book1, true); // Returned
        BorrowRecord record3 = createBorrowRecord(3L, user2, book2, false);
        BorrowRecord record4 = createBorrowRecord(4L, user1, book3, false);
        List<BorrowRecord> allBorrowRecords = Arrays.asList(record1, record2, record3, record4);
        List<BorrowRecord> activeBorrows = Arrays.asList(record1, record3, record4);

        // Mock repository calls
        when(bookRepository.count()).thenReturn(3L);
        when(userRepository.count()).thenReturn(2L);
        when(bookRepository.findByIsBorrowedTrue()).thenReturn(borrowedBooks);
        when(bookRepository.findByIsBorrowedFalse()).thenReturn(availableBooks);
        when(borrowRecordRepository.count()).thenReturn(4L);
        when(borrowRecordRepository.findByIsReturnedFalse()).thenReturn(activeBorrows);
        when(borrowRecordRepository.findAll()).thenReturn(allBorrowRecords);
        when(bookRepository.findAll()).thenReturn(allBooks);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book2));
        when(bookRepository.findById(3L)).thenReturn(Optional.of(book3));

        StatisticsResponse response = statisticsService.getStatistics();

        assertNotNull(response);
        assertEquals(3L, response.getTotalBooks());
        assertEquals(2L, response.getTotalUsers());
        assertEquals(2L, response.getTotalBorrowedBooks());
        assertEquals(1L, response.getTotalAvailableBooks());
        assertEquals(4L, response.getTotalBorrowRecords());
        assertEquals(3L, response.getTotalActiveBorrows());

        // Verify popular categories
        assertNotNull(response.getPopularCategories());
        assertFalse(response.getPopularCategories().isEmpty());
        assertTrue(response.getPopularCategories().get(0).getBorrowCount() > 0);

        // Verify popular authors
        assertNotNull(response.getPopularAuthors());
        assertFalse(response.getPopularAuthors().isEmpty());

        // Verify most borrowed books
        assertNotNull(response.getMostBorrowedBooks());
        assertFalse(response.getMostBorrowedBooks().isEmpty());

        verify(bookRepository).count();
        verify(userRepository).count();
        verify(bookRepository).findByIsBorrowedTrue();
        verify(bookRepository).findByIsBorrowedFalse();
        verify(borrowRecordRepository).count();
        verify(borrowRecordRepository).findByIsReturnedFalse();
        verify(borrowRecordRepository).findAll();
        verify(bookRepository, times(2)).findAll();
    }

    @Test
    void testGetStatisticsEmptyData() {
        // Mock empty data
        when(bookRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(bookRepository.findByIsBorrowedTrue()).thenReturn(List.of());
        when(bookRepository.findByIsBorrowedFalse()).thenReturn(List.of());
        when(borrowRecordRepository.count()).thenReturn(0L);
        when(borrowRecordRepository.findByIsReturnedFalse()).thenReturn(List.of());
        when(borrowRecordRepository.findAll()).thenReturn(List.of());
        when(bookRepository.findAll()).thenReturn(List.of());

        StatisticsResponse response = statisticsService.getStatistics();

        assertNotNull(response);
        assertEquals(0L, response.getTotalBooks());
        assertEquals(0L, response.getTotalUsers());
        assertEquals(0L, response.getTotalBorrowedBooks());
        assertEquals(0L, response.getTotalAvailableBooks());
        assertEquals(0L, response.getTotalBorrowRecords());
        assertEquals(0L, response.getTotalActiveBorrows());
        assertNotNull(response.getPopularCategories());
        assertTrue(response.getPopularCategories().isEmpty());
        assertNotNull(response.getPopularAuthors());
        assertTrue(response.getPopularAuthors().isEmpty());
        assertNotNull(response.getMostBorrowedBooks());
        assertTrue(response.getMostBorrowedBooks().isEmpty());
    }

    @Test
    void testGetStatisticsPopularCategoriesSorted() {
        // Setup books
        List<Book> allBooks = Arrays.asList(book1, book2, book3);

        // Create borrow records
        BorrowRecord record1 = createBorrowRecord(1L, user1, book1, false); // Category1
        BorrowRecord record2 = createBorrowRecord(2L, user1, book1, false); // Category1
        BorrowRecord record3 = createBorrowRecord(3L, user2, book2, false); // Category1
        BorrowRecord record4 = createBorrowRecord(4L, user1, book3, false); // Category2
        List<BorrowRecord> allBorrowRecords = Arrays.asList(record1, record2, record3, record4);

        when(bookRepository.count()).thenReturn(3L);
        when(userRepository.count()).thenReturn(2L);
        when(bookRepository.findByIsBorrowedTrue()).thenReturn(Arrays.asList(book1, book3));
        when(bookRepository.findByIsBorrowedFalse()).thenReturn(Arrays.asList(book2));
        when(borrowRecordRepository.count()).thenReturn(4L);
        when(borrowRecordRepository.findByIsReturnedFalse()).thenReturn(allBorrowRecords);
        when(borrowRecordRepository.findAll()).thenReturn(allBorrowRecords);
        when(bookRepository.findAll()).thenReturn(allBooks);

        StatisticsResponse response = statisticsService.getStatistics();

        assertNotNull(response.getPopularCategories());
        assertFalse(response.getPopularCategories().isEmpty());

        assertTrue(response.getPopularCategories().get(0).getBorrowCount() >=
                response.getPopularCategories().get(1).getBorrowCount());
    }

    @Test
    void testGetStatisticsPopularAuthorsSorted() {
        // Setup books
        List<Book> allBooks = Arrays.asList(book1, book2, book3);

        // Create borrow records
        BorrowRecord record1 = createBorrowRecord(1L, user1, book1, false); // Author1
        BorrowRecord record2 = createBorrowRecord(2L, user1, book2, false); // Author2
        BorrowRecord record3 = createBorrowRecord(3L, user2, book2, false); // Author2
        BorrowRecord record4 = createBorrowRecord(4L, user1, book3, false); // Author2
        List<BorrowRecord> allBorrowRecords = Arrays.asList(record1, record2, record3, record4);

        when(bookRepository.count()).thenReturn(3L);
        when(userRepository.count()).thenReturn(2L);
        when(bookRepository.findByIsBorrowedTrue()).thenReturn(Arrays.asList(book1, book3));
        when(bookRepository.findByIsBorrowedFalse()).thenReturn(Arrays.asList(book2));
        when(borrowRecordRepository.count()).thenReturn(4L);
        when(borrowRecordRepository.findByIsReturnedFalse()).thenReturn(allBorrowRecords);
        when(borrowRecordRepository.findAll()).thenReturn(allBorrowRecords);
        when(bookRepository.findAll()).thenReturn(allBooks);

        StatisticsResponse response = statisticsService.getStatistics();

        assertNotNull(response.getPopularAuthors());
        assertFalse(response.getPopularAuthors().isEmpty());

        assertTrue(response.getPopularAuthors().get(0).getBorrowCount() >=
                response.getPopularAuthors().get(1).getBorrowCount());
    }

    @Test
    void testGetStatisticsMostBorrowedBooksSorted() {
        // Setup books
        List<Book> allBooks = Arrays.asList(book1, book2, book3);

        // Create borrow records
        BorrowRecord record1 = createBorrowRecord(1L, user1, book1, false); // Book1
        BorrowRecord record2 = createBorrowRecord(2L, user2, book1, false); // Book1
        BorrowRecord record3 = createBorrowRecord(3L, user1, book1, true); // Book1 - returned
        BorrowRecord record4 = createBorrowRecord(4L, user1, book2, false); // Book2
        List<BorrowRecord> allBorrowRecords = Arrays.asList(record1, record2, record3, record4);

        when(bookRepository.count()).thenReturn(3L);
        when(userRepository.count()).thenReturn(2L);
        when(bookRepository.findByIsBorrowedTrue()).thenReturn(Arrays.asList(book1, book3));
        when(bookRepository.findByIsBorrowedFalse()).thenReturn(Arrays.asList(book2));
        when(borrowRecordRepository.count()).thenReturn(4L);
        when(borrowRecordRepository.findByIsReturnedFalse()).thenReturn(Arrays.asList(record1, record2, record4));
        when(borrowRecordRepository.findAll()).thenReturn(allBorrowRecords);
        when(bookRepository.findAll()).thenReturn(allBooks);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book2));

        StatisticsResponse response = statisticsService.getStatistics();

        assertNotNull(response.getMostBorrowedBooks());
        assertFalse(response.getMostBorrowedBooks().isEmpty());

        // First book should have more borrows (book1 has 3, book2 has 1)
        assertTrue(response.getMostBorrowedBooks().get(0).getBorrowCount() >=
                response.getMostBorrowedBooks().get(1).getBorrowCount());

        // Verify book1 is the most borrowed
        assertEquals(1L, response.getMostBorrowedBooks().get(0).getBookId());
        assertEquals(3L, response.getMostBorrowedBooks().get(0).getBorrowCount());
    }

    @Test
    void testGetStatisticsLimitsToTen() {
        // Create 15 categories worth of data
        List<Book> allBooks = Arrays.asList(book1, book2, book3);

        // Create many borrow records
        List<BorrowRecord> allBorrowRecords = Arrays.asList(
                createBorrowRecord(1L, user1, book1, false),
                createBorrowRecord(2L, user1, book2, false),
                createBorrowRecord(3L, user1, book3, false)
        );

        when(bookRepository.count()).thenReturn(3L);
        when(userRepository.count()).thenReturn(2L);
        when(bookRepository.findByIsBorrowedTrue()).thenReturn(Arrays.asList(book1, book3));
        when(bookRepository.findByIsBorrowedFalse()).thenReturn(Arrays.asList(book2));
        when(borrowRecordRepository.count()).thenReturn(3L);
        when(borrowRecordRepository.findByIsReturnedFalse()).thenReturn(allBorrowRecords);
        when(borrowRecordRepository.findAll()).thenReturn(allBorrowRecords);
        when(bookRepository.findAll()).thenReturn(allBooks);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book2));
        when(bookRepository.findById(3L)).thenReturn(Optional.of(book3));

        StatisticsResponse response = statisticsService.getStatistics();

        assertNotNull(response);
        // Popular categories, authors, and books should be limited to 10
        assertTrue(response.getPopularCategories().size() <= 10);
        assertTrue(response.getPopularAuthors().size() <= 10);
        assertTrue(response.getMostBorrowedBooks().size() <= 10);
    }

    @Test
    void testGetStatisticsWithNullBook() {
        // Setup - book might not be found in repository
        List<Book> allBooks = Arrays.asList(book1, book2);
        List<BorrowRecord> allBorrowRecords = Arrays.asList(
                createBorrowRecord(1L, user1, book1, false),
                createBorrowRecord(2L, user1, book2, false)
        );

        when(bookRepository.count()).thenReturn(2L);
        when(userRepository.count()).thenReturn(1L);
        when(bookRepository.findByIsBorrowedTrue()).thenReturn(Arrays.asList(book1));
        when(bookRepository.findByIsBorrowedFalse()).thenReturn(Arrays.asList(book2));
        when(borrowRecordRepository.count()).thenReturn(2L);
        when(borrowRecordRepository.findByIsReturnedFalse()).thenReturn(allBorrowRecords);
        when(borrowRecordRepository.findAll()).thenReturn(allBorrowRecords);
        when(bookRepository.findAll()).thenReturn(allBooks);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.findById(2L)).thenReturn(Optional.of(book2));
        when(bookRepository.findById(999L)).thenReturn(Optional.empty());

        StatisticsResponse response = statisticsService.getStatistics();

        assertNotNull(response);
        assertNotNull(response.getMostBorrowedBooks());
    }

    // Helper method to create borrow records
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
}