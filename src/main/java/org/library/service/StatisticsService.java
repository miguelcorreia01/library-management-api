package org.library.service;

import org.library.dto.statistics.*;
import org.library.entities.Book;
import org.library.entities.BorrowRecord;
import org.library.repository.BookRepository;
import org.library.repository.BorrowRecordRepository;
import org.library.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;

    public StatisticsService(BookRepository bookRepository,
                             BorrowRecordRepository borrowRecordRepository,
                             UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.userRepository = userRepository;
    }

    public StatisticsResponse getStatistics() {
        long totalBooks = bookRepository.count();
        long totalUsers = userRepository.count();
        long totalBorrowedBooks = bookRepository.findByIsBorrowedTrue().size();
        long totalAvailableBooks = bookRepository.findByIsBorrowedFalse().size();
        long totalBorrowRecords = borrowRecordRepository.count();
        long totalActiveBorrows = borrowRecordRepository.findByIsReturnedFalse().size();

        // Get all borrow records for statistics
        List<BorrowRecord> allBorrowRecords = borrowRecordRepository.findAll();

        // Popular Categories Statistics
        Map<Long, Long> categoryBorrowCount = allBorrowRecords.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getBook().getCategory().getId(),
                        Collectors.counting()
                ));

        Map<Long, Long> categoryBookCount = bookRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        book -> book.getCategory().getId(),
                        Collectors.counting()
                ));

        List<CategoryStatistics> popularCategories = categoryBorrowCount.entrySet().stream()
                .map(entry -> {
                    Book sampleBook = allBorrowRecords.stream()
                            .filter(record -> record.getBook().getCategory().getId().equals(entry.getKey()))
                            .findFirst()
                            .map(BorrowRecord::getBook)
                            .orElse(null);

                    String categoryName = sampleBook != null ? sampleBook.getCategory().getName() : "Unknown";

                    return new CategoryStatistics(
                            entry.getKey(),
                            categoryName,
                            categoryBookCount.getOrDefault(entry.getKey(), 0L),
                            entry.getValue()
                    );
                })
                .sorted(Comparator.comparing(CategoryStatistics::getBorrowCount).reversed())
                .limit(10)
                .collect(Collectors.toList());

        // Popular Authors Statistics
        Map<Long, Long> authorBorrowCount = allBorrowRecords.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getBook().getAuthor().getId(),
                        Collectors.counting()
                ));

        Map<Long, Long> authorBookCount = bookRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        book -> book.getAuthor().getId(),
                        Collectors.counting()
                ));

        List<AuthorStatistics> popularAuthors = authorBorrowCount.entrySet().stream()
                .map(entry -> {
                    Book sampleBook = allBorrowRecords.stream()
                            .filter(record -> record.getBook().getAuthor().getId().equals(entry.getKey()))
                            .findFirst()
                            .map(BorrowRecord::getBook)
                            .orElse(null);

                    String authorName = sampleBook != null ? sampleBook.getAuthor().getName() : "Unknown";

                    return new AuthorStatistics(
                            entry.getKey(),
                            authorName,
                            authorBookCount.getOrDefault(entry.getKey(), 0L),
                            entry.getValue()
                    );
                })
                .sorted(Comparator.comparing(AuthorStatistics::getBorrowCount).reversed())
                .limit(10)
                .collect(Collectors.toList());

        // Most Borrowed Books Statistics
        Map<Long, Long> bookBorrowCount = allBorrowRecords.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getBook().getId(),
                        Collectors.counting()
                ));

        List<BookStatistics> mostBorrowedBooks = bookBorrowCount.entrySet().stream()
                .map(entry -> {
                    Book book = bookRepository.findById(entry.getKey())
                            .orElse(null);

                    if (book == null) {
                        return null;
                    }

                    return new BookStatistics(
                            book.getId(),
                            book.getTitle(),
                            book.getAuthor().getName(),
                            entry.getValue()
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(BookStatistics::getBorrowCount).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return new StatisticsResponse(
                totalBooks,
                totalUsers,
                totalBorrowedBooks,
                totalAvailableBooks,
                totalBorrowRecords,
                totalActiveBorrows,
                popularCategories,
                popularAuthors,
                mostBorrowedBooks
        );
    }
}
