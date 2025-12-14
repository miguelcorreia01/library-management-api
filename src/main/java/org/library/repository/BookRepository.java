package org.library.repository;

import org.library.entities.Book;
import org.library.entities.Category;
import org.library.entities.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT b FROM Book b WHERE " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:authorName IS NULL OR LOWER(b.author.name) LIKE LOWER(CONCAT('%', :authorName, '%'))) AND " +
            "(:categoryName IS NULL OR LOWER(b.category.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))) AND " +
            "(:releaseYear IS NULL OR b.releaseYear = :releaseYear)")
    List<Book> searchBooks(@Param("title") String title,
                           @Param("authorName") String authorName,
                           @Param("categoryName") String categoryName,
                           @Param("releaseYear") Integer releaseYear);
}

