package org.library.dto.borrow;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnRequest {

    @NotNull(message = "Borrow record ID is required")
    private Long borrowRecordId;
}
