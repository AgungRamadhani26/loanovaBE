package com.example.loanova.repository;

import com.example.loanova.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {


    @Query(value = "select count(*) from users where username = :username", nativeQuery = true)
    long countUsernameNative(@Param("username") String username);

    @Query(value = "select count(*) from users where email = :email", nativeQuery = true)
    long countEmailNative(@Param("email") String email);

    default boolean existsByUsername(String username) {
        return countUsernameNative(username) > 0;
    }

    default boolean existsByEmail(String email) {
        return countEmailNative(email) > 0;
    }

    Optional<User> findByUsername(String username);

}
