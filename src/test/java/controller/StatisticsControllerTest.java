package controller;

import org.junit.jupiter.api.Test;
import org.library.LibraryApplication;
import org.library.dto.statistics.*;
import org.library.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = LibraryApplication.class)
@AutoConfigureMockMvc
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetStatisticsSuccess() throws Exception {
        // Setup statistics data
        CategoryStatistics category1 = new CategoryStatistics(1L, "Fantasy", 10L, 25L);
        CategoryStatistics category2 = new CategoryStatistics(2L, "Science Fiction", 8L, 15L);
        List<CategoryStatistics> popularCategories = Arrays.asList(category1, category2);

        AuthorStatistics author1 = new AuthorStatistics(1L, "J.K. Rowling", 7L, 30L);
        AuthorStatistics author2 = new AuthorStatistics(2L, "George R.R. Martin", 5L, 20L);
        List<AuthorStatistics> popularAuthors = Arrays.asList(author1, author2);

        BookStatistics book1 = new BookStatistics(1L, "Harry Potter", "J.K. Rowling", 15L);
        BookStatistics book2 = new BookStatistics(2L, "A Game of Thrones", "George R.R. Martin", 12L);
        List<BookStatistics> mostBorrowedBooks = Arrays.asList(book1, book2);

        StatisticsResponse response = new StatisticsResponse(
                50L,  // totalBooks
                25L,  // totalUsers
                20L,  // totalBorrowedBooks
                30L,  // totalAvailableBooks
                100L, // totalBorrowRecords
                15L,  // totalActiveBorrows
                popularCategories,
                popularAuthors,
                mostBorrowedBooks
        );

        when(statisticsService.getStatistics()).thenReturn(response);

        mockMvc.perform(
                        get("/api/admin/statistics")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").value(50L))
                .andExpect(jsonPath("$.totalUsers").value(25L))
                .andExpect(jsonPath("$.totalBorrowedBooks").value(20L))
                .andExpect(jsonPath("$.totalAvailableBooks").value(30L))
                .andExpect(jsonPath("$.totalBorrowRecords").value(100L))
                .andExpect(jsonPath("$.totalActiveBorrows").value(15L))
                .andExpect(jsonPath("$.popularCategories.length()").value(2))
                .andExpect(jsonPath("$.popularCategories[0].categoryId").value(1L))
                .andExpect(jsonPath("$.popularCategories[0].categoryName").value("Fantasy"))
                .andExpect(jsonPath("$.popularCategories[0].bookCount").value(10L))
                .andExpect(jsonPath("$.popularCategories[0].borrowCount").value(25L))
                .andExpect(jsonPath("$.popularCategories[1].categoryId").value(2L))
                .andExpect(jsonPath("$.popularCategories[1].categoryName").value("Science Fiction"))
                .andExpect(jsonPath("$.popularCategories[1].bookCount").value(8L))
                .andExpect(jsonPath("$.popularCategories[1].borrowCount").value(15L))
                .andExpect(jsonPath("$.popularAuthors.length()").value(2))
                .andExpect(jsonPath("$.popularAuthors[0].authorId").value(1L))
                .andExpect(jsonPath("$.popularAuthors[0].authorName").value("J.K. Rowling"))
                .andExpect(jsonPath("$.popularAuthors[0].bookCount").value(7L))
                .andExpect(jsonPath("$.popularAuthors[0].borrowCount").value(30L))
                .andExpect(jsonPath("$.popularAuthors[1].authorId").value(2L))
                .andExpect(jsonPath("$.popularAuthors[1].authorName").value("George R.R. Martin"))
                .andExpect(jsonPath("$.popularAuthors[1].bookCount").value(5L))
                .andExpect(jsonPath("$.popularAuthors[1].borrowCount").value(20L))
                .andExpect(jsonPath("$.mostBorrowedBooks.length()").value(2))
                .andExpect(jsonPath("$.mostBorrowedBooks[0].bookId").value(1L))
                .andExpect(jsonPath("$.mostBorrowedBooks[0].bookTitle").value("Harry Potter"))
                .andExpect(jsonPath("$.mostBorrowedBooks[0].authorName").value("J.K. Rowling"))
                .andExpect(jsonPath("$.mostBorrowedBooks[0].borrowCount").value(15L))
                .andExpect(jsonPath("$.mostBorrowedBooks[1].bookId").value(2L))
                .andExpect(jsonPath("$.mostBorrowedBooks[1].bookTitle").value("A Game of Thrones"))
                .andExpect(jsonPath("$.mostBorrowedBooks[1].authorName").value("George R.R. Martin"))
                .andExpect(jsonPath("$.mostBorrowedBooks[1].borrowCount").value(12L));

        verify(statisticsService).getStatistics();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetStatisticsWithEmptyLists() throws Exception {
        StatisticsResponse response = new StatisticsResponse(
                0L,   // totalBooks
                0L,   // totalUsers
                0L,   // totalBorrowedBooks
                0L,   // totalAvailableBooks
                0L,   // totalBorrowRecords
                0L,   // totalActiveBorrows
                List.of(), // popularCategories
                List.of(), // popularAuthors
                List.of()  // mostBorrowedBooks
        );

        when(statisticsService.getStatistics()).thenReturn(response);

        mockMvc.perform(
                        get("/api/admin/statistics")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").value(0L))
                .andExpect(jsonPath("$.totalUsers").value(0L))
                .andExpect(jsonPath("$.totalBorrowedBooks").value(0L))
                .andExpect(jsonPath("$.totalAvailableBooks").value(0L))
                .andExpect(jsonPath("$.totalBorrowRecords").value(0L))
                .andExpect(jsonPath("$.totalActiveBorrows").value(0L))
                .andExpect(jsonPath("$.popularCategories.length()").value(0))
                .andExpect(jsonPath("$.popularAuthors.length()").value(0))
                .andExpect(jsonPath("$.mostBorrowedBooks.length()").value(0));

        verify(statisticsService).getStatistics();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetStatisticsWithSingleItemInLists() throws Exception {
        CategoryStatistics category = new CategoryStatistics(1L, "Fantasy", 5L, 10L);
        AuthorStatistics author = new AuthorStatistics(1L, "J.K. Rowling", 3L, 15L);
        BookStatistics book = new BookStatistics(1L, "Harry Potter", "J.K. Rowling", 20L);

        StatisticsResponse response = new StatisticsResponse(
                10L,
                5L,
                3L,
                7L,
                25L,
                2L,
                List.of(category),
                List.of(author),
                List.of(book)
        );

        when(statisticsService.getStatistics()).thenReturn(response);

        mockMvc.perform(
                        get("/api/admin/statistics")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.popularCategories.length()").value(1))
                .andExpect(jsonPath("$.popularCategories[0].categoryName").value("Fantasy"))
                .andExpect(jsonPath("$.popularAuthors.length()").value(1))
                .andExpect(jsonPath("$.popularAuthors[0].authorName").value("J.K. Rowling"))
                .andExpect(jsonPath("$.mostBorrowedBooks.length()").value(1))
                .andExpect(jsonPath("$.mostBorrowedBooks[0].bookTitle").value("Harry Potter"));

        verify(statisticsService).getStatistics();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetStatisticsForbidden() throws Exception {
        mockMvc.perform(
                        get("/api/admin/statistics")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());

        verify(statisticsService, never()).getStatistics();
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetStatisticsWithLargeNumbers() throws Exception {
        StatisticsResponse response = new StatisticsResponse(
                1000L,
                500L,
                300L,
                700L,
                5000L,
                250L,
                List.of(),
                List.of(),
                List.of()
        );

        when(statisticsService.getStatistics()).thenReturn(response);

        mockMvc.perform(
                        get("/api/admin/statistics")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBooks").value(1000L))
                .andExpect(jsonPath("$.totalUsers").value(500L))
                .andExpect(jsonPath("$.totalBorrowedBooks").value(300L))
                .andExpect(jsonPath("$.totalAvailableBooks").value(700L))
                .andExpect(jsonPath("$.totalBorrowRecords").value(5000L))
                .andExpect(jsonPath("$.totalActiveBorrows").value(250L));

        verify(statisticsService).getStatistics();
    }
}