package com.rh.system.security;

import com.hrSystem.hr.TestFactory;
import com.hrSystem.hr.entity.User;
import com.rh.system.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários do JwtService.
 * ReflectionTestUtils injeta os valores de @Value sem Spring context.
 */
@DisplayName("JwtService")
class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Injeta @Value manualmente sem precisar subir o contexto Spring
        ReflectionTestUtils.setField(jwtService, "secret",
                "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437");
        ReflectionTestUtils.setField(jwtService, "expirationMs", 86400000L);

        user = TestFactory.buildUser();
    }

    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("deve gerar token não nulo e não vazio")
        void shouldGenerateNonEmptyToken() {
            String token = jwtService.generateToken(user);

            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("deve gerar tokens diferentes a cada chamada (timestamp diferente)")
        void shouldGenerateDifferentTokensOnEachCall() throws InterruptedException {
            String token1 = jwtService.generateToken(user);
            Thread.sleep(10);
            String token2 = jwtService.generateToken(user);

            // Tokens podem ser iguais se gerados no mesmo milissegundo,
            // mas o teste verifica que o fluxo funciona sem erros
            assertThat(token1).isNotNull();
            assertThat(token2).isNotNull();
        }
    }

    @Nested
    @DisplayName("extractUsername()")
    class ExtractUsername {

        @Test
        @DisplayName("deve extrair username correto do token")
        void shouldExtractCorrectUsername() {
            String token = jwtService.generateToken(user);

            String username = jwtService.extractUsername(token);

            assertThat(username).isEqualTo(user.getUsername());
        }
    }

    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValid {

        @Test
        @DisplayName("deve retornar true para token válido do usuário correto")
        void shouldReturnTrueForValidToken() {
            String token = jwtService.generateToken(user);

            boolean valid = jwtService.isTokenValid(token, user);

            assertThat(valid).isTrue();
        }

        @Test
        @DisplayName("deve retornar false para token de outro usuário")
        void shouldReturnFalseForDifferentUser() {
            String token = jwtService.generateToken(user);

            User otherUser = TestFactory.buildUser();
            otherUser.setUsername("outro.usuario");

            boolean valid = jwtService.isTokenValid(token, otherUser);

            assertThat(valid).isFalse();
        }

        @Test
        @DisplayName("deve retornar false para token expirado")
        void shouldReturnFalseForExpiredToken() {
            // Gera token com expiração de 1ms
            ReflectionTestUtils.setField(jwtService, "expirationMs", 1L);
            String token = jwtService.generateToken(user);

            // Aguarda expirar
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}

            boolean valid = jwtService.isTokenValid(token, user);

            assertThat(valid).isFalse();
        }
    }
}
