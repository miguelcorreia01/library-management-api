package org.library.repository;

import org.library.entities.Book;
import org.library.entities.BorrowRecord;
import org.library.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    // Find all borrow records for a specific user
    List<BorrowRecord> findByUser(User user);

    // Find all active borrow records for a specific user
    List<BorrowRecord> findByUserAndIsReturnedFalse(User user);

    // Count active borrow records for a specific user
    int countByUserAndIsReturnedFalse(User user);

    // Find all borrow records for a specific book
    List<BorrowRecord> findByBook(Book book);

    // Find all returned borrow records
    List<BorrowRecord> findByIsReturnedTrue();

    // Find all active borrow records
    List<BorrowRecord> findByIsReturnedFalse();
}
