package com.rh.system.security;


import com.rh.system.repository.UserRepository;
import com.rh.system.service.JwtService;
import com.rh.system.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT — intercepta cada requisição e valida o token Bearer.
 *
 * Fluxo:
 * 1. Extrai o header Authorization
 * 2. Valida o token com JwtService
 * 3. Carrega o User do banco (com Employee via JOIN FETCH)
 * 4. Registra a autenticação no SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String token    = authHeader.substring(7);
            final String username = jwtService.extractUsername(token);

            // Autentica apenas se ainda não há autenticação no contexto
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                User user = userRepository.findActiveByUsernameWithEmployee(username)
                        .orElse(null);

                if (user != null && jwtService.isTokenValid(token, user)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    user.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            log.warn("Falha na validação do token JWT: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}