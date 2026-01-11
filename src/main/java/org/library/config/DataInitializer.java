package org.library.config;

import org.library.entities.*;
import org.library.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           AuthorRepository authorRepository,
                           CategoryRepository categoryRepository,
                           BookRepository bookRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        logger.info("Starting data initialization...");

        initializeAdmin();
        initializeAuthors();
        initializeCategories();
        initializeBooks();

        logger.info("Data initialization completed!");
    }

    private void initializeAdmin() {
        if (userRepository.findByEmail("admin@library.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@library.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
            logger.info("Default admin user created: admin@library.com / admin123");
        } else {
            logger.info("Admin user already exists, skipping creation");
        }
    }

    private void initializeAuthors() {
        String[] authorNames = {
                "J.K. Rowling",
                "George R.R. Martin",
                "J.R.R. Tolkien",
                "Stephen King",
                "Agatha Christie",
                "Isaac Asimov",
                "Jane Austen",
                "Ernest Hemingway"
        };

        for (String authorName : authorNames) {
            if (authorRepository.findByName(authorName).isEmpty()) {
                Author author = new Author();
                author.setName(authorName);
                authorRepository.save(author);
                logger.info("Created author: {}", authorName);
            }
        }
    }

    private void initializeCategories() {
        String[] categoryNames = {
                "Fantasy",
                "Science Fiction",
                "Mystery",
                "Romance",
                "Thriller",
                "Horror",
                "Classic Literature",
                "Adventure"
        };

        for (String categoryName : categoryNames) {
            if (categoryRepository.findByName(categoryName).isEmpty()) {
                Category category = new Category();
                category.setName(categoryName);
                categoryRepository.save(category);
                logger.info("Created category: {}", categoryName);
            }
        }
    }

    private void initializeBooks() {
        // Get authors
        Author rowling = authorRepository.findByName("J.K. Rowling").orElse(null);
        Author martin = authorRepository.findByName("George R.R. Martin").orElse(null);
        Author tolkien = authorRepository.findByName("J.R.R. Tolkien").orElse(null);
        Author king = authorRepository.findByName("Stephen King").orElse(null);
        Author christie = authorRepository.findByName("Agatha Christie").orElse(null);
        Author asimov = authorRepository.findByName("Isaac Asimov").orElse(null);
        Author austen = authorRepository.findByName("Jane Austen").orElse(null);
        Author hemingway = authorRepository.findByName("Ernest Hemingway").orElse(null);

        // Get categories
        Category fantasy = categoryRepository.findByName("Fantasy").orElse(null);
        Category sciFi = categoryRepository.findByName("Science Fiction").orElse(null);
        Category mystery = categoryRepository.findByName("Mystery").orElse(null);
        Category romance = categoryRepository.findByName("Romance").orElse(null);
        Category thriller = categoryRepository.findByName("Thriller").orElse(null);
        Category horror = categoryRepository.findByName("Horror").orElse(null);
        Category classic = categoryRepository.findByName("Classic Literature").orElse(null);
        Category adventure = categoryRepository.findByName("Adventure").orElse(null);

        // Sample books data
        BookData[] books = {
                new BookData("Harry Potter and the Philosopher's Stone", rowling, fantasy, 1997),
                new BookData("Harry Potter and the Chamber of Secrets", rowling, fantasy, 1998),
                new BookData("A Game of Thrones", martin, fantasy, 1996),
                new BookData("A Clash of Kings", martin, fantasy, 1998),
                new BookData("The Hobbit", tolkien, fantasy, 1937),
                new BookData("The Lord of the Rings", tolkien, fantasy, 1954),
                new BookData("The Shining", king, horror, 1977),
                new BookData("It", king, horror, 1986),
                new BookData("Murder on the Orient Express", christie, mystery, 1934),
                new BookData("And Then There Were None", christie, mystery, 1939),
                new BookData("Foundation", asimov, sciFi, 1951),
                new BookData("I, Robot", asimov, sciFi, 1950),
                new BookData("Pride and Prejudice", austen, romance, 1813),
                new BookData("Sense and Sensibility", austen, romance, 1811),
                new BookData("The Old Man and the Sea", hemingway, classic, 1952),
                new BookData("For Whom the Bell Tolls", hemingway, adventure, 1940)
        };

        for (BookData bookData : books) {
            if (bookData.author != null && bookData.category != null) {
                if (bookRepository.findByTitle(bookData.title).isEmpty()) {
                    Book book = new Book();
                    book.setTitle(bookData.title);
                    book.setAuthor(bookData.author);
                    book.setCategory(bookData.category);
                    book.setReleaseYear(bookData.releaseYear);
                    book.setBorrowed(false);

                    bookRepository.save(book);
                    logger.info("Created book: {}", bookData.title);
                }
            }
        }
    }

    // Helper class for book data
    private static class BookData {
        String title;
        Author author;
        Category category;
        int releaseYear;

        BookData(String title, Author author, Category category, int releaseYear) {
            this.title = title;
            this.author = author;
            this.category = category;
            this.releaseYear = releaseYear;
        }
    }
}
