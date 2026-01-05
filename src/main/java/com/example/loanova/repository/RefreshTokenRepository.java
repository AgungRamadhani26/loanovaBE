package com.example.loanova.repository;

import com.example.loanova.entity.RefreshToken;
import com.example.loanova.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByToken(String token);

  void deleteByUser(User user);
}
