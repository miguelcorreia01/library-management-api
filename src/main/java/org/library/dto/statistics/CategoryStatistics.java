package org.library.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatistics {
    private Long categoryId;
    private String categoryName;
    private long bookCount;
    private long borrowCount;
}
