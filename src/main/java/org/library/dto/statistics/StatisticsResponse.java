package org.library.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private long totalBooks;
    private long totalUsers;
    private long totalBorrowedBooks;
    private long totalAvailableBooks;
    private long totalBorrowRecords;
    private long totalActiveBorrows;
    private List<CategoryStatistics> popularCategories;
    private List<AuthorStatistics> popularAuthors;
    private List<BookStatistics> mostBorrowedBooks;
}

