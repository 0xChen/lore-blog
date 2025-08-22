package com.developerchen.core.auth.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * InMemoryUserDetailsService 测试
 * 
 * @author syc
 */
class InMemoryUserDetailsServiceTest {

    private InMemoryUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new InMemoryUserDetailsService();
    }

    @Test
    void shouldLoadExistingUser() {
        UserDetails user = userDetailsService.loadUserByUsername("admin");
        
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getAuthorities()).hasSize(2);
        assertThat(user.isEnabled()).isTrue();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void shouldLoadUserWithCorrectAuthorities() {
        UserDetails adminUser = userDetailsService.loadUserByUsername("admin");
        UserDetails regularUser = userDetailsService.loadUserByUsername("user");
        
        assertThat(adminUser.getAuthorities()).hasSize(2);
        assertThat(regularUser.getAuthorities()).hasSize(1);
        
        assertThat(adminUser.getAuthorities().toString()).contains("ROLE_ADMIN", "ROLE_USER");
        assertThat(regularUser.getAuthorities().toString()).contains("ROLE_USER");
    }

    @Test
    void shouldThrowExceptionForNonExistentUser() {
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nonexistent"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("用户不存在: nonexistent");
    }

    @Test
    void shouldAddNewUser() {
        userDetailsService.addUser("newuser", "newpassword", "ROLE_USER");
        
        assertThat(userDetailsService.userExists("newuser")).isTrue();
        
        UserDetails newUser = userDetailsService.loadUserByUsername("newuser");
        assertThat(newUser.getUsername()).isEqualTo("newuser");
        assertThat(newUser.getAuthorities()).hasSize(1);
    }

    @Test
    void shouldDeleteUser() {
        assertThat(userDetailsService.userExists("user")).isTrue();
        
        userDetailsService.deleteUser("user");
        
        assertThat(userDetailsService.userExists("user")).isFalse();
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("user"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldCheckUserExistence() {
        assertThat(userDetailsService.userExists("admin")).isTrue();
        assertThat(userDetailsService.userExists("user")).isTrue();
        assertThat(userDetailsService.userExists("test")).isTrue();
        assertThat(userDetailsService.userExists("nonexistent")).isFalse();
    }
}