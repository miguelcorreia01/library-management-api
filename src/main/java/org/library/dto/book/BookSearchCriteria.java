package org.library.dto.book;

import lombok.Data;

@Data
public class BookSearchCriteria {
    private String title;
    private String authorName;
    private String categoryName;
    private int releaseYear;
}
