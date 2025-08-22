package com.developerchen.core.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * JWT 配置集成测试
 * 
 * @author syc
 */
@SpringBootTest(classes = {SecurityAutoConfiguration.class, JwtConfigurationTest.TestConfig.class})
@TestPropertySource(properties = {
    "security.token.jwt-secret=test-secret-key-for-jwt-signing-with-sufficient-length",
    "security.token.jwt-issuer=lore-core-test",
    "security.token.access-token-expiration=PT30M"
})
class JwtConfigurationTest {
    
    @Autowired
    private JwtEncoder jwtEncoder;
    
    @Autowired
    private JwtDecoder jwtDecoder;
    
    @Autowired
    private SecurityProperties securityProperties;
    
    @Test
    void jwtEncoder_ShouldBeConfigured() {
        assertThat(jwtEncoder).isNotNull();
        assertThat(jwtEncoder).isInstanceOf(NimbusJwtEncoder.class);
    }
    
    @Test
    void jwtDecoder_ShouldBeConfigured() {
        assertThat(jwtDecoder).isNotNull();
        assertThat(jwtDecoder).isInstanceOf(NimbusJwtDecoder.class);
    }
    
    @Test
    void jwtEncodingAndDecoding_ShouldWorkCorrectly() {
        // Given
        String subject = "testuser";
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiration = now.plus(securityProperties.getToken().getAccessTokenExpiration());
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(securityProperties.getToken().getJwtIssuer())
            .subject(subject)
            .issuedAt(now)
            .expiresAt(expiration)
            .claim("scope", "read write")
            .claim("authorities", "ROLE_USER")
            .build();
        
        JwtEncoderParameters parameters = JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).build(),
            claims
        );
        
        // When - Encode JWT
        Jwt encodedJwt = jwtEncoder.encode(parameters);
        
        // Then - JWT should be encoded
        assertThat(encodedJwt).isNotNull();
        assertThat(encodedJwt.getTokenValue()).isNotBlank();
        assertThat(encodedJwt.getSubject()).isEqualTo(subject);
        assertThat(encodedJwt.getClaimAsString("iss")).isEqualTo(securityProperties.getToken().getJwtIssuer());
        
        // When - Decode JWT
        Jwt decodedJwt = jwtDecoder.decode(encodedJwt.getTokenValue());
        
        // Then - JWT should be decoded correctly
        assertThat(decodedJwt).isNotNull();
        assertThat(decodedJwt.getSubject()).isEqualTo(subject);
        assertThat(decodedJwt.getClaimAsString("iss")).isEqualTo(securityProperties.getToken().getJwtIssuer());
        assertThat(decodedJwt.getClaimAsString("scope")).isEqualTo("read write");
        assertThat(decodedJwt.getClaimAsString("authorities")).isEqualTo("ROLE_USER");
        assertThat(decodedJwt.getExpiresAt()).isEqualTo(expiration);
    }
    
    @Test
    void jwtDecoding_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.jwt.token";
        
        // When & Then
        assertThatThrownBy(() -> jwtDecoder.decode(invalidToken))
            .isInstanceOf(JwtException.class);
    }
    
    @Test
    void jwtDecoding_WithExpiredToken_ShouldThrowException() {
        // Given - Create an expired token using the same encoder/decoder pair
        String subject = "testuser";
        Instant past = Instant.now().minus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.SECONDS);
        Instant expiration = past.plus(30, ChronoUnit.MINUTES); // Still in the past
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(securityProperties.getToken().getJwtIssuer())
            .subject(subject)
            .issuedAt(past)
            .expiresAt(expiration)
            .build();
        
        JwtEncoderParameters parameters = JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).build(),
            claims
        );
        
        Jwt expiredJwt = jwtEncoder.encode(parameters);
        
        // When & Then - The decoder should reject the expired token
        assertThatThrownBy(() -> jwtDecoder.decode(expiredJwt.getTokenValue()))
            .isInstanceOf(JwtException.class)
            .hasMessageContaining("expired");
    }
    
    @Test
    void jwtConfiguration_WithSameSecret_ShouldGenerateConsistentKeyPair() {
        // Given - Two JWT configurations with the same secret
        SecurityProperties props1 = new SecurityProperties();
        props1.getToken().setJwtSecret("same-secret-key");
        
        SecurityProperties props2 = new SecurityProperties();
        props2.getToken().setJwtSecret("same-secret-key");
        
        JwtConfiguration config1 = new JwtConfiguration();
        JwtConfiguration config2 = new JwtConfiguration();
        
        // When - Create encoders with same secret
        JwtEncoder encoder1 = config1.jwtEncoder(props1);
        JwtEncoder encoder2 = config2.jwtEncoder(props2);
        
        JwtDecoder decoder1 = config1.jwtDecoder(props1);
        JwtDecoder decoder2 = config2.jwtDecoder(props2);
        
        // Then - Tokens encoded by one should be decodable by the other
        String subject = "testuser";
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiration = now.plus(30, ChronoUnit.MINUTES);
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("test-issuer")
            .subject(subject)
            .issuedAt(now)
            .expiresAt(expiration)
            .build();
        
        JwtEncoderParameters parameters = JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).build(),
            claims
        );
        
        Jwt jwt1 = encoder1.encode(parameters);
        Jwt jwt2 = encoder2.encode(parameters);
        
        // Both decoders should be able to decode both tokens
        assertThatCode(() -> decoder1.decode(jwt1.getTokenValue())).doesNotThrowAnyException();
        assertThatCode(() -> decoder1.decode(jwt2.getTokenValue())).doesNotThrowAnyException();
        assertThatCode(() -> decoder2.decode(jwt1.getTokenValue())).doesNotThrowAnyException();
        assertThatCode(() -> decoder2.decode(jwt2.getTokenValue())).doesNotThrowAnyException();
    }
    
    @Test
    void jwtConfiguration_WithDifferentSecrets_ShouldGenerateDifferentKeyPairs() {
        // Given - Two JWT configurations with different secrets
        SecurityProperties props1 = new SecurityProperties();
        props1.getToken().setJwtSecret("secret-key-one");
        
        SecurityProperties props2 = new SecurityProperties();
        props2.getToken().setJwtSecret("secret-key-two");
        
        JwtConfiguration config1 = new JwtConfiguration();
        JwtConfiguration config2 = new JwtConfiguration();
        
        // When - Create encoders and decoders with different secrets
        JwtEncoder encoder1 = config1.jwtEncoder(props1);
        JwtDecoder decoder2 = config2.jwtDecoder(props2);
        
        // Then - Token encoded by one should not be decodable by the other
        String subject = "testuser";
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiration = now.plus(30, ChronoUnit.MINUTES);
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("test-issuer")
            .subject(subject)
            .issuedAt(now)
            .expiresAt(expiration)
            .build();
        
        JwtEncoderParameters parameters = JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).build(),
            claims
        );
        
        Jwt jwt1 = encoder1.encode(parameters);
        
        // Decoder with different secret should reject the token
        assertThatThrownBy(() -> decoder2.decode(jwt1.getTokenValue()))
            .isInstanceOf(JwtException.class);
    }
    
    @Test
    void jwtConfiguration_ShouldUseConfiguredAlgorithm() {
        // Given
        String subject = "testuser";
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiration = now.plus(30, ChronoUnit.MINUTES);
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(securityProperties.getToken().getJwtIssuer())
            .subject(subject)
            .issuedAt(now)
            .expiresAt(expiration)
            .build();
        
        JwtEncoderParameters parameters = JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).build(),
            claims
        );
        
        // When
        Jwt encodedJwt = jwtEncoder.encode(parameters);
        
        // Then - JWT header should contain RS256 algorithm
        assertThat(encodedJwt.getHeaders()).containsKey("alg");
        assertThat(encodedJwt.getHeaders().get("alg").toString()).isEqualTo("RS256");
    }
    
    @TestConfiguration
    static class TestConfig {
        
        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails user = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
            
            return username -> {
                if ("testuser".equals(username)) {
                    return user;
                }
                throw new RuntimeException("User not found: " + username);
            };
        }
    }
}