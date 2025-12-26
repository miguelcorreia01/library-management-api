package org.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.library.dto.statistics.StatisticsResponse;
import org.library.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Statistics", description = "Admin endpoints for viewing library statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping
    @Operation(
            summary = "Get library statistics",
            description = "Returns comprehensive statistics including popular categories, authors, and most borrowed books"
    )
    public ResponseEntity<StatisticsResponse> getStatistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
}