package org.library.dto.book;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookResponse {
    private long id;
    private String title;
    private String authorName;
    private String categoryName;
    private int releaseYear;
    private boolean isBorrowed;

}
