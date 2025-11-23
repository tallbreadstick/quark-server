package com.darauy.quark.repository;

import com.darauy.quark.entity.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query(value = """
        SELECT u FROM User u
        WHERE u.username LIKE %:identifier% OR u.email LIKE %:identifier%
        ORDER BY u.username ASC
    """)
    List<User> searchTop10ByIdentifier(@Param("identifier") String identifier);
}
