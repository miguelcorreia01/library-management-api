package org.library.dto.book;

import lombok.Data;

@Data
public class BookRequest {
    private String title;
    private long authorId;
    private long categoryId;
    private int releaseYear;
}
