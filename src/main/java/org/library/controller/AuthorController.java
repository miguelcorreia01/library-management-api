package org.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.library.dto.author.AuthorRequest;
import org.library.dto.author.AuthorResponse;
import org.library.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "Endpoints for managing authors")
public class AuthorController {
    private final AuthorService authorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new author", description = "Admin only - Creates a new author")
    public ResponseEntity<AuthorResponse>createAuthor(@Valid @RequestBody AuthorRequest authorRequest) {
        AuthorResponse createdAuthor = authorService.createAuthor(authorRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAuthor);
    }

    @GetMapping("/{authorId}")
    @Operation(summary = "Get author by ID", description = "Retrieves an author by its ID")
    public ResponseEntity<AuthorResponse> getAuthorById(@PathVariable Long authorId) {
        AuthorResponse authorResponse = authorService.findAuthorById(authorId);
        return ResponseEntity.ok(authorResponse);
    }

    @GetMapping
    @Operation(summary = "Get all authors", description = "Retrieves a list of all authors")
    public ResponseEntity<List<AuthorResponse>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }
}
