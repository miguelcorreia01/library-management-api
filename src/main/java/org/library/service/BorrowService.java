package org.library.service;

import org.library.dto.borrow.BorrowRequest;
import org.library.dto.borrow.BorrowResponse;
import org.library.entities.Book;
import org.library.entities.BorrowRecord;
import org.library.entities.User;
import org.library.exception.BadRequestException;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.repository.BookRepository;
import org.library.repository.BorrowRecordRepository;
import org.library.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BorrowService {
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private static final int MAX_BORROWED_BOOKS = 5;

    public BorrowService(BorrowRecordRepository borrowRecordRepository, BookRepository bookRepository, UserRepository userRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public BorrowResponse borrowBook(BorrowRequest borrowRequest) {

        User user = getCurrentUser();

        // Check if user has reached max borrow limit
        int activeBorrows = borrowRecordRepository.countByUserAndIsReturnedFalse(user);
        if (activeBorrows >= MAX_BORROWED_BOOKS) {
            throw new BadRequestException("Maximum active borrow limit of " + MAX_BORROWED_BOOKS + " books reached");
        }

        // Get book
        Book book = bookRepository.findById(borrowRequest.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        // Check if book is already borrowed
        if (book.isBorrowed()) {
            throw new ConflictException("Book is already borrowed");
        }

        // Create borrow record
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setBook(book);
        borrowRecord.setUser(user);
        borrowRecord.setBorrowDate(LocalDate.now());
        borrowRecord.setReturned(false);

        // Update book status
        book.setBorrowed(true);
        bookRepository.save(book);

        BorrowRecord saved = borrowRecordRepository.save(borrowRecord);
        return toBorrowResponse(saved);
    }

    public BorrowResponse returnBook(Long borrowRecordId) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));

        User user = getCurrentUser();

        // Check if user owns this borrow record
        if (!borrowRecord.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("You can only return your own borrowed books");
        }

        // Check if already returned
        if (borrowRecord.isReturned()) {
            throw new ConflictException("Book is already returned");
        }

        // Update borrow record
        borrowRecord.setReturned(true);
        borrowRecord.setReturnDate(LocalDate.now());

        // Update book status
        Book book = borrowRecord.getBook();
        book.setBorrowed(false);
        bookRepository.save(book);

        BorrowRecord saved = borrowRecordRepository.save(borrowRecord);
        return toBorrowResponse(saved);
    }

    public BorrowResponse getBorrowRecordById(Long borrowRecordId) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(borrowRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found"));

        return toBorrowResponse(borrowRecord);
    }

    public List<BorrowResponse> getMyBorrowedBooks() {
        User user = getCurrentUser();

        List<BorrowRecord> borrowRecords = borrowRecordRepository.findByUserAndIsReturnedFalse(user);
        return borrowRecords.stream()
                .map(this::toBorrowResponse)
                .toList();
    }

    public List<BorrowResponse> getMyBorrowHistory() {
        User user = getCurrentUser();

        List<BorrowRecord> borrowRecords = borrowRecordRepository.findByUser(user);
        return borrowRecords.stream()
                .map(this::toBorrowResponse)
                .toList();
    }

    public List<BorrowResponse> getAllActiveBorrows() {
        List<BorrowRecord> borrowRecords = borrowRecordRepository.findByIsReturnedFalse();
        return borrowRecords.stream()
                .map(this::toBorrowResponse)
                .toList();
    }

    public List<BorrowResponse> getAllReturnedBorrows() {
        List<BorrowRecord> borrowRecords = borrowRecordRepository.findByIsReturnedTrue();
        return borrowRecords.stream()
                .map(this::toBorrowResponse)
                .toList();
    }

    public List<BorrowResponse> getBorrowsByBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        List<BorrowRecord> borrowRecords = borrowRecordRepository.findByBook(book);
        return borrowRecords.stream()
                .map(this::toBorrowResponse)
                .toList();
    }

    public List<BorrowResponse> getBorrowsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<BorrowRecord> borrowRecords = borrowRecordRepository.findByUser(user);
        return borrowRecords.stream()
                .map(this::toBorrowResponse)
                .toList();
    }


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private BorrowResponse toBorrowResponse(BorrowRecord borrowRecord) {
        return new BorrowResponse(
                borrowRecord.getId(),
                borrowRecord.getBook().getId(),
                borrowRecord.getBook().getTitle(),
                borrowRecord.getUser().getId(),
                borrowRecord.getUser().getName(),
                borrowRecord.getBorrowDate(),
                borrowRecord.getReturnDate(),
                borrowRecord.isReturned()
        );
    }

}
