package com.userService.repository;

import com.userService.model.EmailVerificationTokens;
import com.userService.model.RefreshToken;
import com.userService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface EmailVerificationTokensRepository extends JpaRepository<EmailVerificationTokens, Long> {
    Optional<EmailVerificationTokens> findByToken(String token);

}