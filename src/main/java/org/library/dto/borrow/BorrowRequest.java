package org.library.dto.borrow;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequest {

    @NotNull(message = "Book ID is required")
    private Long bookId;
}
