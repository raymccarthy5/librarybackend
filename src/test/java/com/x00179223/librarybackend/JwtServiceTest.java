package com.x00179223.librarybackend;

import com.x00179223.librarybackend.config.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        String username = "TestUser";
        String password = "TestPassword";
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        userDetails = new User(username, password, authorities);
    }

    @Test
    void testExtractUsername() {
        String token = jwtService.generateToken(userDetails);

        String extractedUsername = jwtService.extractUsername(token);

        assertEquals(userDetails.getUsername(), extractedUsername);
    }


    @Test
    void testExtractClaim() {
        String token = jwtService.generateToken(userDetails);

        Function<Claims, String> subjectClaimResolver = Claims::getSubject;

        String extractedClaim = jwtService.extractClaim(token, subjectClaimResolver);

        assertEquals(userDetails.getUsername(), extractedClaim);
    }


    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken(userDetails);

        String extractedUsername = jwtService.extractUsername(token);

        assertEquals(userDetails.getUsername(), extractedUsername);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }


    @Test
    void testGenerateTokenWithExtraClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("extraClaimKey", "extraClaimValue");

        String token = jwtService.generateToken(extraClaims, userDetails);

        String extractedUsername = jwtService.extractUsername(token);

        Function<Claims, String> extraClaimResolver = claims -> claims.get("extraClaimKey", String.class);
        String extractedExtraClaim = jwtService.extractClaim(token, extraClaimResolver);

        assertEquals(userDetails.getUsername(), extractedUsername);

        assertEquals("extraClaimValue", extractedExtraClaim);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }


    @Test
    void testIsTokenValid() {
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);

        UserDetails invalidUserDetails = new User("InvalidUsername", userDetails.getPassword(), userDetails.getAuthorities());

        boolean isInvalid = jwtService.isTokenValid(token, invalidUserDetails);

        assertFalse(isInvalid);
    }
}
