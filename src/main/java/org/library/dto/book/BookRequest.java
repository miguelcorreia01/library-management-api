package org.library.dto.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookRequest {
    @NotBlank(message = "Book title is required")
    private String title;

    @NotNull(message = "Author ID is required")
    private Long authorId;
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Release year is required")
    @Min(value = 0 , message = "Release year must be a valid year")
    @Max(value = 2026 , message = "Release year must be a valid year")
    private int releaseYear;
}
