package org.library.dto.book;

import lombok.Data;

@Data
public class BookResponse {
    private long id;
    private String title;
    private String authorName;
    private String categoryName;
    private int releaseYear;
    private boolean isBorrowed;

}
