package com.rh.system.repository;

import com.rh.system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    /**
     * Busca o usuário com o Employee já carregado (JOIN FETCH).
     * Usado no JwtFilter para evitar lazy loading fora de sessão.
     */
    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.employee
            WHERE u.username = :username
            AND u.active = true
            """)
    Optional<User> findActiveByUsernameWithEmployee(@Param("username") String username);
}