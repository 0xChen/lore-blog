package com.developerchen.core.system.security;

import com.developerchen.core.common.constant.Const;
import com.developerchen.core.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 用于生成token
 *
 * @author syc
 */
public class ApiUser implements UserDetails {

    private final Long id;
    private final String nickname;
    private final String username;
    private final String password;
    private final String status;
    private final String email;
    private final LocalDateTime updateTime;
    private final Set<GrantedAuthority> authorities;

    public ApiUser(
            Long id,
            String nickname,
            String username,
            String password,
            String status,
            String email,
            LocalDateTime updateTime,
            Set<? extends GrantedAuthority> authorities
    ) {
        this.id = id;
        this.nickname = nickname;
        this.username = username;
        this.password = password;
        this.status = status;
        this.email = email;
        this.updateTime = updateTime;
        this.authorities = Collections.unmodifiableSet(authorities);
    }

    @JsonSerialize(using = ToStringSerializer.class)
    public Long getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return !Const.USER_STATUS_EXPIRED.equals(status);
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return !Const.USER_STATUS_LOCKED.equals(status);
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    public String getNickname() {
        return nickname;
    }

    public String getStatus() {
        return status;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isEnabled() {
        return Const.USER_STATUS_ENABLED.equals(status);
    }

    @Override
    public String toString() {
        return "ApiUser{" +
                "id=" + id +
                ", nickname='" + nickname + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", status='" + status + '\'' +
                ", email='" + email + '\'' +
                ", updateTime=" + updateTime +
                ", authorities=" + authorities +
                '}';
    }

    public User toUser() {
        User user = new User();
        user.setId(this.id);
        user.setNickname(this.nickname);
        user.setUsername(this.username);
        user.setPassword(this.password);
        user.setEmail(this.email);
        user.setStatus(this.status);
        user.setUpdateTime(this.updateTime);
        return user;
    }

    /**
     * Builds the JwtUser instance
     */
    public static class ApiUserBuilder {

        private ApiUserBuilder() {
        }

        public static ApiUser build(User user) {
            return new ApiUser(
                    user.getId(),
                    user.getNickname(),
                    user.getUsername(),
                    user.getPassword(),
                    user.getStatus(),
                    user.getEmail(),
                    user.getUpdateTime(),
                    grantedAuthoritiesBuilder(user.getRole())
            );
        }

        private static Set<? extends GrantedAuthority> grantedAuthoritiesBuilder(String role) {

            Set<SimpleGrantedAuthority> authorities = new HashSet<>(1);
            authorities.add(new SimpleGrantedAuthority(role));

            return authorities;
        }

    }
}
