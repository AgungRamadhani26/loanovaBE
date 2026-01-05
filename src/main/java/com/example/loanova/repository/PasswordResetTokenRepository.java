package com.example.loanova.repository;

import com.example.loanova.entity.PasswordResetToken;
import com.example.loanova.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  Optional<PasswordResetToken> findByToken(String token);

  Optional<PasswordResetToken> findByUserAndIsUsedFalse(User user);
}
