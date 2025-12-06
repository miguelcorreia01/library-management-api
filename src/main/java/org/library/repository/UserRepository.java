package org.library.repository;

import org.library.entities.Role;
import org.library.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String Name);

    Optional<User> findByEmail(String email);

    List<User> findByRole(Role role);

    boolean existsByEmail(String email);

    List<User> findByIsActiveTrue();
    List<User> findByIsActiveFalse();


}
