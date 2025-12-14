package org.library.dto.author;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthorRequest {

    @NotBlank(message = "Author name is required")
    private String name;
}
