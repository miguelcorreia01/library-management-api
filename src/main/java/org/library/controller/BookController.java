package org.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.library.dto.book.BookRequest;
import org.library.dto.book.BookResponse;
import org.library.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Endpoints for managing books")
public class BookController {
    private final BookService bookService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new book", description = "Admin only - Creates a new book")
    public ResponseEntity<BookResponse>createBook(
            @Valid @RequestBody BookRequest bookRequest) {
        BookResponse createdBook = bookService.createBook(bookRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }


    @GetMapping("/{bookId}")
    @Operation(summary = "Get book by ID", description = "Retrieves a book by its ID")
    public ResponseEntity<BookResponse>findBookById(@PathVariable Long bookId) {
        BookResponse bookResponse = bookService.findBookById(bookId);
        return ResponseEntity.ok(bookResponse);
    }

    @GetMapping("/title/{title}")
    @Operation(summary = "Get book by title", description = "Retrieves a book by its title")
    public ResponseEntity<BookResponse>findBookByTitle(@PathVariable String title) {
        BookResponse bookResponse = bookService.findBookByTitle(title);
        return ResponseEntity.ok(bookResponse);
    }

    @GetMapping("/author/{authorId}")
    @Operation(summary = "Get books by author", description = "Retrieves books by a specific author")
    public ResponseEntity<List<BookResponse>>findBooksByAuthor(@PathVariable Long authorId) {
        return ResponseEntity.ok(bookService.findBooksByAuthor(authorId));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get books by category", description = "Retrieves books by a specific category")
    public ResponseEntity<List<BookResponse>>findBooksByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(bookService.findBooksByCategory(categoryId));
    }

    @GetMapping("/release-year/{releaseYear}")
    @Operation(summary = "Get books by release year", description = "Retrieves books released in a specific year")
    public ResponseEntity<List<BookResponse>>findBooksByReleaseYear(@PathVariable int releaseYear) {
        return ResponseEntity.ok(bookService.findBooksByReleaseYear(releaseYear));
    }

    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Searches for books based on multiple criteria")
    public ResponseEntity<List<BookResponse>>searchBooks(
            @RequestParam (required = false) String title,
            @RequestParam (required = false) String authorName,
            @RequestParam (required = false) String categoryName,
            @RequestParam (required = false) Integer releaseYear) {

        List<BookResponse> books = bookService.searchBooks(title, authorName, categoryName, releaseYear);
        return ResponseEntity.ok(books);

    }

    @GetMapping("/borrowed")
    @Operation(summary = "Get borrowed books", description = "Retrieves all borrowed books")
    public ResponseEntity<List<BookResponse>>findBorrowedBooks() {
        return ResponseEntity.ok(bookService.findBorrowedBooks());
    }

    @GetMapping("/available")
    @Operation(summary = "Get available books", description = "Retrieves all available books")
    public ResponseEntity<List<BookResponse>>findAvailableBooks() {
        return ResponseEntity.ok(bookService.findAvailableBooks());
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieves all books in the library")
    public ResponseEntity<List<BookResponse>>findAllBooks() {
        return ResponseEntity.ok(bookService.findAllBooks());
    }


    @PutMapping("/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a book", description = "Admin only - Updates an existing book")
    public ResponseEntity<BookResponse>updateBook(
            @PathVariable Long bookId,
            @Valid @RequestBody BookRequest bookRequest) {
        BookResponse updatedBook = bookService.updateBook(bookId, bookRequest);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a book", description = "Admin only - Deletes a book by its ID")
    public ResponseEntity<BookResponse>deleteBook(@PathVariable Long bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

}
