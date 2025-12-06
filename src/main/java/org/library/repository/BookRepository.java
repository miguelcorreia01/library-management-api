package org.library.repository;

import org.library.entities.Book;
import org.library.entities.Category;
import org.library.entities.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByTitle(String title);

    List<Book> findByCategory(Category categoryName);

    List<Book> findByAuthor(Author author);

    List<Book> findByReleaseYear(int releaseYear);

    List<Book> findByIsBorrowedTrue();
    List<Book> findByIsBorrowedFalse();

}
