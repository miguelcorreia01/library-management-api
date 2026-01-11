package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.dto.book.BookRequest;
import org.library.dto.book.BookResponse;
import org.library.entities.Author;
import org.library.entities.Book;
import org.library.entities.Category;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.repository.AuthorRepository;
import org.library.repository.BookRepository;
import org.library.repository.CategoryRepository;
import org.library.service.BookService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookServiceTest {

    private BookService bookService;
    private BookRepository bookRepository;
    private AuthorRepository authorRepository;
    private CategoryRepository categoryRepository;

    private Author testAuthor;
    private Category testCategory;

    @BeforeEach
    void setup() {
        bookRepository = mock(BookRepository.class);
        authorRepository = mock(AuthorRepository.class);
        categoryRepository = mock(CategoryRepository.class);

        bookService = new BookService(bookRepository, authorRepository, categoryRepository);

        // Setup test data
        testAuthor = new Author();
        testAuthor.setId(1L);
        testAuthor.setName("J.K. Rowling");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Fantasy");
    }

    @Test
    void testCreateBookSuccess() {
        BookRequest request = new BookRequest();
        request.setTitle("Harry Potter");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(1997);

        when(bookRepository.findByTitle(request.getTitle())).thenReturn(Optional.empty());
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return book;
        });

        BookResponse response = bookService.createBook(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Harry Potter", response.getTitle());
        assertEquals("J.K. Rowling", response.getAuthorName());
        assertEquals("Fantasy", response.getCategoryName());
        assertEquals(1997, response.getReleaseYear());
        assertFalse(response.isBorrowed());

        verify(bookRepository).findByTitle(request.getTitle());
        verify(authorRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void testCreateBookDuplicateTitle() {
        BookRequest request = new BookRequest();
        request.setTitle("Harry Potter");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(1997);

        Book existingBook = new Book();
        when(bookRepository.findByTitle(request.getTitle())).thenReturn(Optional.of(existingBook));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> bookService.createBook(request));

        assertEquals("Book with the same title already exists", exception.getMessage());

        verify(bookRepository).findByTitle(request.getTitle());
        verify(authorRepository, never()).findById(anyLong());
        verify(categoryRepository, never()).findById(anyLong());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testCreateBookAuthorNotFound() {
        BookRequest request = new BookRequest();
        request.setTitle("Harry Potter");
        request.setAuthorId(999L);
        request.setCategoryId(1L);
        request.setReleaseYear(1997);

        when(bookRepository.findByTitle(request.getTitle())).thenReturn(Optional.empty());
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookService.createBook(request));

        assertEquals("Author not found", exception.getMessage());

        verify(bookRepository).findByTitle(request.getTitle());
        verify(authorRepository).findById(999L);
        verify(categoryRepository, never()).findById(anyLong());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testCreateBookCategoryNotFound() {
        BookRequest request = new BookRequest();
        request.setTitle("Harry Potter");
        request.setAuthorId(1L);
        request.setCategoryId(999L);
        request.setReleaseYear(1997);

        when(bookRepository.findByTitle(request.getTitle())).thenReturn(Optional.empty());
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookService.createBook(request));

        assertEquals("Category not found", exception.getMessage());

        verify(bookRepository).findByTitle(request.getTitle());
        verify(authorRepository).findById(1L);
        verify(categoryRepository).findById(999L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testFindBookByIdSuccess() {
        Long bookId = 1L;
        Book book = createTestBook(bookId, "Harry Potter", testAuthor, testCategory, 1997, false);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        BookResponse response = bookService.findBookById(bookId);

        assertNotNull(response);
        assertEquals(bookId, response.getId());
        assertEquals("Harry Potter", response.getTitle());
        assertEquals("J.K. Rowling", response.getAuthorName());
        assertEquals("Fantasy", response.getCategoryName());
        assertEquals(1997, response.getReleaseYear());

        verify(bookRepository).findById(bookId);
    }

    @Test
    void testFindBookByIdNotFound() {
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookService.findBookById(bookId));

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository).findById(bookId);
    }

    @Test
    void testFindBookByTitleSuccess() {
        Book book = createTestBook(1L, "Harry Potter", testAuthor, testCategory, 1997, false);

        when(bookRepository.findByTitle("Harry Potter")).thenReturn(Optional.of(book));

        BookResponse response = bookService.findBookByTitle("Harry Potter");

        assertNotNull(response);
        assertEquals("Harry Potter", response.getTitle());
        verify(bookRepository).findByTitle("Harry Potter");
    }

    @Test
    void testFindBookByTitleNotFound() {
        when(bookRepository.findByTitle("NonExistent")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookService.findBookByTitle("NonExistent"));

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository).findByTitle("NonExistent");
    }

    @Test
    void testFindBooksByAuthorSuccess() {
        Book book1 = createTestBook(1L, "Harry Potter", testAuthor, testCategory, 1997, false);
        Book book2 = createTestBook(2L, "Chamber of Secrets", testAuthor, testCategory, 1998, false);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(bookRepository.findByAuthor(testAuthor)).thenReturn(Arrays.asList(book1, book2));

        List<BookResponse> responses = bookService.findBooksByAuthor(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Harry Potter", responses.get(0).getTitle());
        assertEquals("Chamber of Secrets", responses.get(1).getTitle());

        verify(authorRepository).findById(1L);
        verify(bookRepository).findByAuthor(testAuthor);
    }

    @Test
    void testFindBooksByAuthorNotFound() {
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookService.findBooksByAuthor(999L));

        assertEquals("Author not found", exception.getMessage());
        verify(authorRepository).findById(999L);
        verify(bookRepository, never()).findByAuthor(any(Author.class));
    }

    @Test
    void testFindBooksByCategorySuccess() {
        Book book1 = createTestBook(1L, "Harry Potter", testAuthor, testCategory, 1997, false);
        Book book2 = createTestBook(2L, "Lord of the Rings", testAuthor, testCategory, 1954, false);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(bookRepository.findByCategory(testCategory)).thenReturn(Arrays.asList(book1, book2));

        List<BookResponse> responses = bookService.findBooksByCategory(1L);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Fantasy", responses.get(0).getCategoryName());
        assertEquals("Fantasy", responses.get(1).getCategoryName());

        verify(categoryRepository).findById(1L);
        verify(bookRepository).findByCategory(testCategory);
    }

    @Test
    void testFindBooksByCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookService.findBooksByCategory(999L));

        assertEquals("Category not found", exception.getMessage());
        verify(categoryRepository).findById(999L);
        verify(bookRepository, never()).findByCategory(any(Category.class));
    }

    @Test
    void testFindBooksByReleaseYear() {
        Book book1 = createTestBook(1L, "Book 1", testAuthor, testCategory, 1997, false);
        Book book2 = createTestBook(2L, "Book 2", testAuthor, testCategory, 1997, false);

        when(bookRepository.findByReleaseYear(1997)).thenReturn(Arrays.asList(book1, book2));

        List<BookResponse> responses = bookService.findBooksByReleaseYear(1997);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(1997, responses.get(0).getReleaseYear());
        assertEquals(1997, responses.get(1).getReleaseYear());

        verify(bookRepository).findByReleaseYear(1997);
    }

    @Test
    void testSearchBooks() {
        Book book1 = createTestBook(1L, "Harry Potter", testAuthor, testCategory, 1997, false);
        Book book2 = createTestBook(2L, "Harry Potter 2", testAuthor, testCategory, 1998, false);

        when(bookRepository.searchBooks("Harry", null, null, null))
                .thenReturn(Arrays.asList(book1, book2));

        List<BookResponse> responses = bookService.searchBooks("Harry", null, null, null);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        verify(bookRepository).searchBooks("Harry", null, null, null);
    }

    @Test
    void testFindBorrowedBooks() {
        Book book1 = createTestBook(1L, "Book 1", testAuthor, testCategory, 1997, true);
        Book book2 = createTestBook(2L, "Book 2", testAuthor, testCategory, 1998, true);

        when(bookRepository.findByIsBorrowedTrue()).thenReturn(Arrays.asList(book1, book2));

        List<BookResponse> responses = bookService.findBorrowedBooks();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertTrue(responses.get(0).isBorrowed());
        assertTrue(responses.get(1).isBorrowed());

        verify(bookRepository).findByIsBorrowedTrue();
    }

    @Test
    void testFindAvailableBooks() {
        Book book1 = createTestBook(1L, "Book 1", testAuthor, testCategory, 1997, false);
        Book book2 = createTestBook(2L, "Book 2", testAuthor, testCategory, 1998, false);

        when(bookRepository.findByIsBorrowedFalse()).thenReturn(Arrays.asList(book1, book2));

        List<BookResponse> responses = bookService.findAvailableBooks();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertFalse(responses.get(0).isBorrowed());
        assertFalse(responses.get(1).isBorrowed());

        verify(bookRepository).findByIsBorrowedFalse();
    }

    @Test
    void testFindAllBooks() {
        Book book1 = createTestBook(1L, "Book 1", testAuthor, testCategory, 1997, false);
        Book book2 = createTestBook(2L, "Book 2", testAuthor, testCategory, 1998, true);

        when(bookRepository.findAll()).thenReturn(Arrays.asList(book1, book2));

        List<BookResponse> responses = bookService.findAllBooks();

        assertNotNull(responses);
        assertEquals(2, responses.size());

        verify(bookRepository).findAll();
    }

    @Test
    void testUpdateBookSuccess() {
        Long bookId = 1L;
        Book existingBook = createTestBook(bookId, "Old Title", testAuthor, testCategory, 1997, false);

        BookRequest request = new BookRequest();
        request.setTitle("New Title");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(2000);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByTitle("New Title")).thenReturn(Optional.empty());
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return book;
        });

        BookResponse response = bookService.updateBook(bookId, request);

        assertNotNull(response);
        assertEquals("New Title", response.getTitle());
        assertEquals(2000, response.getReleaseYear());

        verify(bookRepository).findById(bookId);
        verify(bookRepository).findByTitle("New Title");
        verify(authorRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void testUpdateBookNotFound() {
        Long bookId = 999L;
        BookRequest request = new BookRequest();
        request.setTitle("New Title");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(2000);

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookService.updateBook(bookId, request));

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository).findById(bookId);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testUpdateBookDuplicateTitle() {
        Long bookId = 1L;
        Book existingBook = createTestBook(bookId, "Old Title", testAuthor, testCategory, 1997, false);
        Book duplicateBook = createTestBook(2L, "New Title", testAuthor, testCategory, 1998, false);

        BookRequest request = new BookRequest();
        request.setTitle("New Title");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(2000);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByTitle("New Title")).thenReturn(Optional.of(duplicateBook));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> bookService.updateBook(bookId, request));

        assertEquals("Book with the same title already exists", exception.getMessage());
        verify(bookRepository).findById(bookId);
        verify(bookRepository).findByTitle("New Title");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testUpdateBookSameTitle() {
        Long bookId = 1L;
        Book existingBook = createTestBook(bookId, "Same Title", testAuthor, testCategory, 1997, false);

        BookRequest request = new BookRequest();
        request.setTitle("Same Title");
        request.setAuthorId(1L);
        request.setCategoryId(1L);
        request.setReleaseYear(2000);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(authorRepository.findById(1L)).thenReturn(Optional.of(testAuthor));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return book;
        });

        BookResponse response = bookService.updateBook(bookId, request);

        assertNotNull(response);
        assertEquals("Same Title", response.getTitle());
        verify(bookRepository, never()).findByTitle("Same Title");
    }

    @Test
    void testDeleteBookSuccess() {
        Long bookId = 1L;
        Book book = createTestBook(bookId, "Harry Potter", testAuthor, testCategory, 1997, false);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        doNothing().when(bookRepository).delete(book);

        bookService.deleteBook(bookId);

        verify(bookRepository).findById(bookId);
        verify(bookRepository).delete(book);
    }

    @Test
    void testDeleteBookNotFound() {
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> bookService.deleteBook(bookId));

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository).findById(bookId);
        verify(bookRepository, never()).delete(any(Book.class));
    }

    // Helper method to create test books
    private Book createTestBook(Long id, String title, Author author, Category category,
                                int releaseYear, boolean isBorrowed) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setReleaseYear(releaseYear);
        book.setBorrowed(isBorrowed);
        return book;
    }
}