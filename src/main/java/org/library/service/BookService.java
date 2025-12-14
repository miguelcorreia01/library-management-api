package org.library.service;

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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    public BookResponse createBook(BookRequest bookRequest) {
        if(bookRepository.findByTitle(bookRequest.getTitle()).isPresent()){
            throw new ConflictException("Book with the same title already exists");
        }
        Author author = authorRepository.findById(bookRequest.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        Category category = categoryRepository.findById(bookRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Book book = new Book();
        book.setTitle(bookRequest.getTitle());
        book.setReleaseYear(bookRequest.getReleaseYear());
        book.setAuthor(author);
        book.setCategory(category);
        book.setBorrowed(false);

        Book saved = bookRepository.save(book);
        return toBookResponse(saved);

    }

    public BookResponse findBookById(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        return toBookResponse(book);
    }

    public BookResponse findBookByTitle(String title) {
        Book book = bookRepository.findByTitle(title)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        return toBookResponse(book);
    }

    public List<BookResponse> findBooksByAuthor(Long authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        List<Book> books = bookRepository.findByAuthor(author);

        return books.stream()
                .map(this::toBookResponse)
                .toList();
    }

    public List<BookResponse> findBooksByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        List<Book> books = bookRepository.findByCategory(category);

        return books.stream()
                .map(this::toBookResponse)
                .toList();
    }

    public List<BookResponse> findBooksByReleaseYear(int releaseYear) {
        List<Book> books = bookRepository.findByReleaseYear(releaseYear);

        return books.stream()
                .map(this::toBookResponse)
                .toList();
    }

    public List<BookResponse> searchBooks(String title, String authorName, String categoryName, Integer releaseYear) {
        List<Book> books = bookRepository.searchBooks(title, authorName, categoryName, releaseYear);
        return books.stream()
                .map(this::toBookResponse)
                .toList();
    }

    public List<BookResponse> findBorrowedBooks() {
        List<Book> books = bookRepository.findByIsBorrowedTrue();

        return books.stream()
                .map(this::toBookResponse)
                .toList();
    }

    public List<BookResponse> findAvailableBooks() {
        List<Book> books = bookRepository.findByIsBorrowedFalse();

        return books.stream()
                .map(this::toBookResponse)
                .toList();
    }

    public List<BookResponse> findAllBooks() {
        List<Book> books = bookRepository.findAll();

        return books.stream()
                .map(this::toBookResponse)
                .toList();
    }

    public BookResponse updateBook(Long bookId, BookRequest bookRequest) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (!book.getTitle().equals(bookRequest.getTitle())) {
            if (bookRepository.findByTitle(bookRequest.getTitle()).isPresent()) {
                throw new ConflictException("Book with the same title already exists");
            }
        }

        Author author = authorRepository.findById(bookRequest.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        Category category = categoryRepository.findById(bookRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        book.setTitle(bookRequest.getTitle());
        book.setReleaseYear(bookRequest.getReleaseYear());
        book.setAuthor(author);
        book.setCategory(category);

        Book saved = bookRepository.save(book);
        return toBookResponse(saved);


    }

    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        bookRepository.delete(book);
    }

    private BookResponse toBookResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor().getName(),
                book.getCategory().getName(),
                book.getReleaseYear(),
                book.isBorrowed()
        );
    }
}
