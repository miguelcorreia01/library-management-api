package org.library.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.library.dto.borrow.BorrowRequest;
import org.library.dto.borrow.BorrowResponse;
import org.library.service.BorrowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
@Tag(name = "Borrows", description = "Endpoints for managing borrows and returns")
public class BorrowController {
    private final BorrowService borrowService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Borrow a book", description = "User can borrow a book (max 5 books at a time)")
    public ResponseEntity<BorrowResponse> borrowBook(@Valid @RequestBody BorrowRequest borrowRequest) {
        BorrowResponse response = borrowService.borrowBook(borrowRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{borrowRecordId}/return")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Return a book", description = "User can return a borrowed book")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long borrowRecordId) {
        BorrowResponse response = borrowService.returnBook(borrowRecordId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get my active borrows", description = "Returns all currently borrowed books by the user")
    public ResponseEntity<List<BorrowResponse>> getMyBorrowedBooks() {
        return ResponseEntity.ok(borrowService.getMyBorrowedBooks());
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get borrow history", description = "Returns complete borrow history for the user")
    public ResponseEntity<List<BorrowResponse>> getMyBorrowHistory() {
        return ResponseEntity.ok(borrowService.getMyBorrowHistory());
    }

    @GetMapping("/{borrowRecordId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get borrow record by ID", description = "Admin can retrieve any borrow record by its ID")
    public ResponseEntity<BorrowResponse> getBorrowRecordById(@PathVariable Long borrowRecordId) {
        BorrowResponse response = borrowService.getBorrowRecordById(borrowRecordId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all active borrows", description = "Admin can retrieve all currently active borrows")
    public ResponseEntity<List<BorrowResponse>> getAllActiveBorrows() {
        return ResponseEntity.ok(borrowService.getAllActiveBorrows());
    }

    @GetMapping("/all/returned")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all returned borrows", description = "Admin can retrieve all returned borrows")
    public ResponseEntity<List<BorrowResponse>> getAllReturnedBorrows() {
        return ResponseEntity.ok(borrowService.getAllReturnedBorrows());
    }

    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get borrows by book", description = "Admin can retrieve all borrows for a specific book")
    public ResponseEntity<List<BorrowResponse>> getBorrowsByBook(@PathVariable Long bookId) {
        return ResponseEntity.ok(borrowService.getBorrowsByBook(bookId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get borrows by user", description = "Admin can retrieve all borrows for a specific user")
    public ResponseEntity<List<BorrowResponse>> getBorrowsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(borrowService.getBorrowsByUser(userId));
    }


}
