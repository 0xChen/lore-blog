package com.developerchen.core.auth.config;

import com.developerchen.core.auth.converter.OpaqueTokenAuthenticationToken;
import com.developerchen.core.auth.service.TokenService;
import com.developerchen.core.auth.service.TokenValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * AuthenticationManagerConfig 测试类
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationManagerConfigTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    private AuthenticationManagerConfig config;
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        config = new AuthenticationManagerConfig(tokenService, userDetailsService, passwordEncoder);
        authenticationManager = config.authenticationManager();
    }

    @Test
    void shouldCreateAuthenticationManagerWithCorrectProviders() {
        // Given & When
        List<String> providers = config.getConfiguredProviders();

        // Then
        assertThat(providers).containsExactly(
            "DaoAuthenticationProvider",
            "OpaqueTokenAuthenticationProvider"
        );
    }

    @Test
    void shouldAuthenticateUsernamePasswordSuccessfully() {
        // Given
        String username = "testuser";
        String password = "password";
        String encodedPassword = "$2a$10$encoded";

        UserDetails userDetails = User.builder()
            .username(username)
            .password(encodedPassword)
            .authorities("ROLE_USER")
            .build();

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        UsernamePasswordAuthenticationToken authRequest = 
            UsernamePasswordAuthenticationToken.unauthenticated(username, password);

        // When
        Authentication result = authenticationManager.authenticate(authRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getName()).isEqualTo(username);
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void shouldFailUsernamePasswordAuthenticationWithWrongPassword() {
        // Given
        String username = "testuser";
        String password = "wrongpassword";
        String encodedPassword = "$2a$10$encoded";

        UserDetails userDetails = User.builder()
            .username(username)
            .password(encodedPassword)
            .authorities("ROLE_USER")
            .build();

        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        UsernamePasswordAuthenticationToken authRequest = 
            UsernamePasswordAuthenticationToken.unauthenticated(username, password);

        // When & Then
        assertThatThrownBy(() -> authenticationManager.authenticate(authRequest))
            .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldFailUsernamePasswordAuthenticationWithUnknownUser() {
        // Given
        String username = "unknownuser";
        String password = "password";

        when(userDetailsService.loadUserByUsername(username))
            .thenThrow(new UsernameNotFoundException("User not found"));

        UsernamePasswordAuthenticationToken authRequest = 
            UsernamePasswordAuthenticationToken.unauthenticated(username, password);

        // When & Then
        assertThatThrownBy(() -> authenticationManager.authenticate(authRequest))
            .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldAuthenticateOpaqueTokenSuccessfully() {
        // Given
        String token = "opaque-token-123";
        String username = "testuser";

        TokenValidationResult validationResult = new TokenValidationResult(
            true, username, Instant.now().plusSeconds(3600), null);

        UserDetails userDetails = User.builder()
            .username(username)
            .password("encoded")
            .authorities("ROLE_USER")
            .build();

        when(tokenService.validateToken(token)).thenReturn(validationResult);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        OpaqueTokenAuthenticationToken authRequest = 
            new OpaqueTokenAuthenticationToken(token);

        // When
        Authentication result = authenticationManager.authenticate(authRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getPrincipal()).isEqualTo(userDetails);
        assertThat(result.getAuthorities()).hasSize(1);
    }

    @Test
    void shouldFailOpaqueTokenAuthenticationWithInvalidToken() {
        // Given
        String token = "invalid-token";

        TokenValidationResult validationResult = new TokenValidationResult(
            false, null, null, "Token expired");

        when(tokenService.validateToken(token)).thenReturn(validationResult);

        OpaqueTokenAuthenticationToken authRequest = 
            new OpaqueTokenAuthenticationToken(token);

        // When & Then
        assertThatThrownBy(() -> authenticationManager.authenticate(authRequest))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("Invalid token");
    }

    @Test
    void shouldFailOpaqueTokenAuthenticationWithUnknownUser() {
        // Given
        String token = "valid-token";
        String username = "unknownuser";

        TokenValidationResult validationResult = new TokenValidationResult(
            true, username, Instant.now().plusSeconds(3600), null);

        when(tokenService.validateToken(token)).thenReturn(validationResult);
        when(userDetailsService.loadUserByUsername(username))
            .thenThrow(new UsernameNotFoundException("User not found"));

        OpaqueTokenAuthenticationToken authRequest = 
            new OpaqueTokenAuthenticationToken(token);

        // When & Then
        assertThatThrownBy(() -> authenticationManager.authenticate(authRequest))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("User not found");
    }

    @Test
    void shouldIncludeJwtProviderWhenJwtDecoderIsAvailable() {
        // Given
        AuthenticationManagerConfig configWithJwt = new AuthenticationManagerConfig(
            tokenService, userDetailsService, passwordEncoder);
        
        // 使用反射设置 jwtDecoder
        try {
            var field = AuthenticationManagerConfig.class.getDeclaredField("jwtDecoder");
            field.setAccessible(true);
            field.set(configWithJwt, jwtDecoder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // When
        List<String> providers = configWithJwt.getConfiguredProviders();

        // Then
        assertThat(providers).containsExactly(
            "DaoAuthenticationProvider",
            "OpaqueTokenAuthenticationProvider",
            "JwtAuthenticationProvider"
        );
    }

    @Test
    void shouldAuthenticateJwtTokenWhenJwtDecoderIsAvailable() {
        // Given
        AuthenticationManagerConfig configWithJwt = new AuthenticationManagerConfig(
            tokenService, userDetailsService, passwordEncoder);
        
        // 使用反射设置 jwtDecoder
        try {
            var field = AuthenticationManagerConfig.class.getDeclaredField("jwtDecoder");
            field.setAccessible(true);
            field.set(configWithJwt, jwtDecoder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        AuthenticationManager managerWithJwt = configWithJwt.authenticationManager();

        String jwtToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...";
        
        Jwt jwt = Jwt.withTokenValue(jwtToken)
            .header("alg", "HS256")
            .claim("sub", "testuser")
            .claim("exp", Instant.now().plusSeconds(3600))
            .build();

        when(jwtDecoder.decode(jwtToken)).thenReturn(jwt);

        BearerTokenAuthenticationToken authRequest = 
            new BearerTokenAuthenticationToken(jwtToken);

        // When
        Authentication result = managerWithJwt.authenticate(authRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getName()).isEqualTo("testuser");
    }

    @Test
    void shouldHandleMultipleAuthenticationTypes() {
        // Given
        AuthenticationManagerConfig configWithJwt = new AuthenticationManagerConfig(
            tokenService, userDetailsService, passwordEncoder);
        
        // 使用反射设置 jwtDecoder
        try {
            var field = AuthenticationManagerConfig.class.getDeclaredField("jwtDecoder");
            field.setAccessible(true);
            field.set(configWithJwt, jwtDecoder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        AuthenticationManager managerWithJwt = configWithJwt.authenticationManager();

        // When & Then - 应该支持多种认证类型
        assertThat(managerWithJwt).isNotNull();
        
        // 验证配置的提供者数量
        List<String> providers = configWithJwt.getConfiguredProviders();
        assertThat(providers).hasSize(3);
    }
}