package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.library.dto.author.AuthorRequest;
import org.library.dto.author.AuthorResponse;
import org.library.entities.Author;
import org.library.exception.ConflictException;
import org.library.exception.ResourceNotFoundException;
import org.library.repository.AuthorRepository;
import org.library.service.AuthorService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthorServiceTest {

    private AuthorService authorService;
    private AuthorRepository authorRepository;

    @BeforeEach
    void setup() {
        authorRepository = mock(AuthorRepository.class);
        authorService = new AuthorService(authorRepository);
    }

    @Test
    void testCreateAuthorSuccess() {
        AuthorRequest request = new AuthorRequest();
        request.setName("J.K. Rowling");

        when(authorRepository.findByName(request.getName())).thenReturn(Optional.empty());

        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(1L);
            return author;
        });

        AuthorResponse response = authorService.createAuthor(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("J.K. Rowling", response.getName());

        verify(authorRepository).findByName(request.getName());
        verify(authorRepository).save(any(Author.class));
    }

    @Test
    void testCreateAuthorDuplicateName() {
        AuthorRequest request = new AuthorRequest();
        request.setName("J.K. Rowling");

        Author existingAuthor = new Author();
        existingAuthor.setId(1L);
        existingAuthor.setName("J.K. Rowling");

        when(authorRepository.findByName(request.getName())).thenReturn(Optional.of(existingAuthor));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> authorService.createAuthor(request));

        assertEquals("Author with the same name already exists", exception.getMessage());

        verify(authorRepository).findByName(request.getName());
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void testFindAuthorByIdSuccess() {
        Long authorId = 1L;
        Author author = new Author();
        author.setId(authorId);
        author.setName("J.K. Rowling");

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));

        AuthorResponse response = authorService.findAuthorById(authorId);

        assertNotNull(response);
        assertEquals(authorId, response.getId());
        assertEquals("J.K. Rowling", response.getName());

        verify(authorRepository).findById(authorId);
    }

    @Test
    void testFindAuthorByIdNotFound() {
        Long authorId = 999L;

        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> authorService.findAuthorById(authorId));

        assertEquals("Author not found", exception.getMessage());

        verify(authorRepository).findById(authorId);
    }

    @Test
    void testGetAllAuthorsSuccess() {
        Author author1 = new Author();
        author1.setId(1L);
        author1.setName("J.K. Rowling");

        Author author2 = new Author();
        author2.setId(2L);
        author2.setName("George R.R. Martin");

        Author author3 = new Author();
        author3.setId(3L);
        author3.setName("J.R.R. Tolkien");

        List<Author> authors = Arrays.asList(author1, author2, author3);

        when(authorRepository.findAll()).thenReturn(authors);

        List<AuthorResponse> responses = authorService.getAllAuthors();

        assertNotNull(responses);
        assertEquals(3, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals("J.K. Rowling", responses.get(0).getName());
        assertEquals(2L, responses.get(1).getId());
        assertEquals("George R.R. Martin", responses.get(1).getName());
        assertEquals(3L, responses.get(2).getId());
        assertEquals("J.R.R. Tolkien", responses.get(2).getName());

        verify(authorRepository).findAll();
    }

    @Test
    void testGetAllAuthorsEmpty() {
        when(authorRepository.findAll()).thenReturn(List.of());

        List<AuthorResponse> responses = authorService.getAllAuthors();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(authorRepository).findAll();
    }

    @Test
    void testCreateAuthorWithEmptyName() {
        AuthorRequest request = new AuthorRequest();
        request.setName("");

        when(authorRepository.findByName("")).thenReturn(Optional.empty());

        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            author.setId(1L);
            return author;
        });

        AuthorResponse response = authorService.createAuthor(request);

        assertNotNull(response);
        assertEquals("", response.getName());

        verify(authorRepository).findByName("");
        verify(authorRepository).save(any(Author.class));
    }
}