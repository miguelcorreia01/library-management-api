package org.library.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookStatistics {
    private Long bookId;
    private String bookTitle;
    private String authorName;
    private long borrowCount;
}
