package org.library.service;


import org.library.dto.author.AuthorRequest;
import org.library.dto.author.AuthorResponse;
import org.library.entities.Author;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.repository.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public AuthorResponse createAuthor(AuthorRequest authorRequest) {
        if(authorRepository.findByName(authorRequest.getName()).isPresent()){
            throw new ConflictException("Author with the same name already exists");
        }

        var author = new Author();
        author.setName(authorRequest.getName());

        Author saved = authorRepository.save(author);
        return toAuthorResponse(saved);
    }

    public AuthorResponse findAuthorById(Long authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        return toAuthorResponse(author);
    }

    public List<AuthorResponse> getAllAuthors() {
        List<Author> authors = authorRepository.findAll();
        return authors.stream()
                .map(this::toAuthorResponse)
                .toList();
    }

    private AuthorResponse toAuthorResponse(Author author) {
        return new AuthorResponse(author.getId(), author.getName());
    }
}
