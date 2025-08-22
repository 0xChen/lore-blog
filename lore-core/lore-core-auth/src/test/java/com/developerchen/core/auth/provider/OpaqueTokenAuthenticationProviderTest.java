package com.developerchen.core.auth.provider;

import com.developerchen.core.auth.converter.OpaqueTokenAuthenticationToken;
import com.developerchen.core.auth.service.TokenService;
import com.developerchen.core.auth.service.TokenValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpaqueTokenAuthenticationProviderTest {
    
    @Mock
    private TokenService tokenService;
    
    @Mock
    private UserDetailsService userDetailsService;
    
    private OpaqueTokenAuthenticationProvider provider;
    
    @BeforeEach
    void setUp() {
        provider = new OpaqueTokenAuthenticationProvider(tokenService, userDetailsService);
    }
    
    @Test
    void constructor_WithNullTokenService_ShouldThrowException() {
        assertThatThrownBy(() -> new OpaqueTokenAuthenticationProvider(null, userDetailsService))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("TokenService cannot be null");
    }
    
    @Test
    void constructor_WithNullUserDetailsService_ShouldThrowException() {
        assertThatThrownBy(() -> new OpaqueTokenAuthenticationProvider(tokenService, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("UserDetailsService cannot be null");
    }
    
    @Test
    void authenticate_WithValidToken_ShouldReturnAuthenticatedToken() {
        // Given
        String token = "valid-uuid-token";
        String username = "testuser";
        Instant expiration = Instant.now().plusSeconds(3600);
        
        TokenValidationResult validResult = TokenValidationResult.valid(username, expiration);
        UserDetails userDetails = User.builder()
            .username(username)
            .password("password")
            .authorities("ROLE_USER")
            .build();
        
        when(tokenService.validateToken(token)).thenReturn(validResult);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        
        OpaqueTokenAuthenticationToken authToken = new OpaqueTokenAuthenticationToken(token);
        
        // When
        Authentication result = provider.authenticate(authToken);
        
        // Then
        assertThat(result).isInstanceOf(OpaqueTokenAuthenticationToken.class);
        OpaqueTokenAuthenticationToken resultToken = (OpaqueTokenAuthenticationToken) result;
        
        assertThat(resultToken.isAuthenticated()).isTrue();
        assertThat(resultToken.getToken()).isEqualTo(token);
        assertThat(resultToken.getPrincipal()).isEqualTo(userDetails);
        assertThat(resultToken.getAuthorities()).containsExactly(new SimpleGrantedAuthority("ROLE_USER"));
        
        verify(tokenService).validateToken(token);
        verify(userDetailsService).loadUserByUsername(username);
    }
    
    @Test
    void authenticate_WithInvalidToken_ShouldThrowBadCredentialsException() {
        // Given
        String token = "invalid-token";
        String errorMessage = "Token expired";
        
        TokenValidationResult invalidResult = TokenValidationResult.invalid(errorMessage);
        when(tokenService.validateToken(token)).thenReturn(invalidResult);
        
        OpaqueTokenAuthenticationToken authToken = new OpaqueTokenAuthenticationToken(token);
        
        // When & Then
        assertThatThrownBy(() -> provider.authenticate(authToken))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("Invalid token: " + errorMessage);
        
        verify(tokenService).validateToken(token);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }
    
    @Test
    void authenticate_WithValidTokenButUserNotFound_ShouldThrowBadCredentialsException() {
        // Given
        String token = "valid-token";
        String username = "nonexistent";
        Instant expiration = Instant.now().plusSeconds(3600);
        
        TokenValidationResult validResult = TokenValidationResult.valid(username, expiration);
        UsernameNotFoundException userNotFoundException = new UsernameNotFoundException("User not found");
        
        when(tokenService.validateToken(token)).thenReturn(validResult);
        when(userDetailsService.loadUserByUsername(username)).thenThrow(userNotFoundException);
        
        OpaqueTokenAuthenticationToken authToken = new OpaqueTokenAuthenticationToken(token);
        
        // When & Then
        assertThatThrownBy(() -> provider.authenticate(authToken))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("User not found: " + username)
            .hasCause(userNotFoundException);
        
        verify(tokenService).validateToken(token);
        verify(userDetailsService).loadUserByUsername(username);
    }
    
    @Test
    void supports_WithOpaqueTokenAuthenticationToken_ShouldReturnTrue() {
        // When & Then
        assertThat(provider.supports(OpaqueTokenAuthenticationToken.class)).isTrue();
    }
    
    @Test
    void supports_WithOtherAuthenticationTypes_ShouldReturnFalse() {
        // When & Then
        assertThat(provider.supports(Authentication.class)).isFalse();
        assertThat(provider.supports(Object.class)).isFalse();
    }
    
    @Test
    void authenticate_WithUserHavingMultipleAuthorities_ShouldPreserveAllAuthorities() {
        // Given
        String token = "valid-token";
        String username = "admin";
        Instant expiration = Instant.now().plusSeconds(3600);
        
        TokenValidationResult validResult = TokenValidationResult.valid(username, expiration);
        UserDetails userDetails = User.builder()
            .username(username)
            .password("password")
            .authorities("ROLE_USER", "ROLE_ADMIN")
            .build();
        
        when(tokenService.validateToken(token)).thenReturn(validResult);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        
        OpaqueTokenAuthenticationToken authToken = new OpaqueTokenAuthenticationToken(token);
        
        // When
        Authentication result = provider.authenticate(authToken);
        
        // Then
        assertThat(result.getAuthorities()).hasSize(2);
        assertThat(result.getAuthorities().stream().map(Object::toString))
            .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}