package org.library.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorStatistics {
    private Long authorId;
    private String authorName;
    private long bookCount;
    private long borrowCount;
}
