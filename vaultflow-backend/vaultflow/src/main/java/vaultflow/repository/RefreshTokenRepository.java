package vaultflow.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import vaultflow.entity.RefreshToken;
import vaultflow.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUser(User user);

    void deleteByUser(User user);

}