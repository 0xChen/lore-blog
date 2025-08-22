package com.developerchen.core.auth.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TokenAuthenticationConverter 单元测试
 * 
 * @author syc
 */
class TokenAuthenticationConverterTest {
    
    private TokenAuthenticationConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new TokenAuthenticationConverter();
    }
    
    @Test
    void convert_JwtTokenFromHeader_ReturnsBearerTokenAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        request.addHeader("Authorization", "Bearer " + jwtToken);
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(BearerTokenAuthenticationToken.class);
        assertThat(result.getCredentials()).isEqualTo(jwtToken);
    }
    
    @Test
    void convert_UuidTokenFromHeader_ReturnsOpaqueTokenAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String uuidToken = "550e8400-e29b-41d4-a716-446655440000";
        request.addHeader("Authorization", "Bearer " + uuidToken);
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(OpaqueTokenAuthenticationToken.class);
        OpaqueTokenAuthenticationToken opaqueToken = (OpaqueTokenAuthenticationToken) result;
        assertThat(opaqueToken.getToken()).isEqualTo(uuidToken);
        assertThat(opaqueToken.getCredentials()).isEqualTo(uuidToken);
    }
    
    @Test
    void convert_JwtTokenFromParameter_ReturnsBearerTokenAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        request.setParameter("access_token", jwtToken);
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(BearerTokenAuthenticationToken.class);
        assertThat(result.getCredentials()).isEqualTo(jwtToken);
    }
    
    @Test
    void convert_UuidTokenFromParameter_ReturnsOpaqueTokenAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String uuidToken = "550e8400-e29b-41d4-a716-446655440000";
        request.setParameter("access_token", uuidToken);
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(OpaqueTokenAuthenticationToken.class);
        OpaqueTokenAuthenticationToken opaqueToken = (OpaqueTokenAuthenticationToken) result;
        assertThat(opaqueToken.getToken()).isEqualTo(uuidToken);
    }
    
    @Test
    void convert_HeaderTakesPrecedenceOverParameter() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String headerToken = "header-token.payload.signature";
        String parameterToken = "parameter-token";
        request.addHeader("Authorization", "Bearer " + headerToken);
        request.setParameter("access_token", parameterToken);
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(BearerTokenAuthenticationToken.class);
        assertThat(result.getCredentials()).isEqualTo(headerToken);
    }
    
    @Test
    void convert_NoToken_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_EmptyAuthorizationHeader_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_NonBearerAuthorizationHeader_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_BearerWithoutToken_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer ");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_EmptyTokenParameter_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("access_token", "");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_WhitespaceOnlyToken_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer   ");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_SimpleStringToken_ReturnsOpaqueTokenAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String simpleToken = "simple-access-token";
        request.addHeader("Authorization", "Bearer " + simpleToken);
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(OpaqueTokenAuthenticationToken.class);
        OpaqueTokenAuthenticationToken opaqueToken = (OpaqueTokenAuthenticationToken) result;
        assertThat(opaqueToken.getToken()).isEqualTo(simpleToken);
    }
    
    @Test
    void convert_TokenWithTwoDotsButNotJwt_ReturnsBearerTokenAuthentication() {
        // Given - 虽然有两个点，但这是一个有效的 JWT 格式，应该被识别为 JWT
        MockHttpServletRequest request = new MockHttpServletRequest();
        String tokenWithDots = "part1.part2.part3";
        request.addHeader("Authorization", "Bearer " + tokenWithDots);
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(BearerTokenAuthenticationToken.class);
        assertThat(result.getCredentials()).isEqualTo(tokenWithDots);
    }
    
    @Test
    void convert_TokenWithOneDot_ReturnsOpaqueTokenAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        String tokenWithOneDot = "part1.part2";
        request.addHeader("Authorization", "Bearer " + tokenWithOneDot);
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(OpaqueTokenAuthenticationToken.class);
        OpaqueTokenAuthenticationToken opaqueToken = (OpaqueTokenAuthenticationToken) result;
        assertThat(opaqueToken.getToken()).isEqualTo(tokenWithOneDot);
    }
}