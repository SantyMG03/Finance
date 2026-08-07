package com.santy.finances.services;

import com.santy.finances.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JWTServiceTest {

    private static final String TEST_SECRET = "ThisIsAReallyLongSecretAndSecureKeyForThisFinanceAppThatIsBeingDevelop";

    private JWTService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
    }

    private User buildUser(String username) {
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setEmail(username + "@test.com");
        user.setPassword("encodedPassword");
        return user;
    }

    @Test
    void generateToken_containsUsernameAsSubject() {
        User user = buildUser("santy");

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("santy");
    }

    @Test
    void extractClaim_returnsRequestedClaim() {
        User user = buildUser("santy");
        String token = jwtService.generateToken(user);

        String username = jwtService.extractClaim(token, Claims::getSubject);

        assertThat(username).isEqualTo("santy");
    }

    @Test
    void isTokenValid_returnsTrueForTokenOfSameUser() {
        User user = buildUser("santy");
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForDifferentUser() {
        User user = buildUser("santy");
        User otherUser = buildUser("other");
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() {
        User user = buildUser("santy");
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
        String expiredToken = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 25))
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60))
                .signWith(key)
                .compact();

        assertThat(jwtService.isTokenValid(expiredToken, user)).isFalse();
    }
}
