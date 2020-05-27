package com.developerchen.core.security;

import com.developerchen.core.domain.entity.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.util.Collection;

/**
 * An {@link org.springframework.security.core.Authentication} implementation
 *
 * @author syc
 */
public class JwtAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    private User user;

    /**
     * This constructor can be safely used by any code that wishes to create a
     * <code>JwtAuthenticationToken</code>, as the {@link #isAuthenticated()}
     * will return <code>false</code>.
     */
    public JwtAuthenticationToken(Object principal, Object credentials) {
        super(principal, credentials);
        user = ((JwtUser) principal).toUser();
    }

    /**
     * This constructor should only be used by <code>AuthenticationManager</code> or
     * <code>AuthenticationProvider</code> implementations that are satisfied with
     * producing a trusted (i.e. {@link #isAuthenticated()} = <code>true</code>)
     * authentication token.
     *
     * @param principal
     * @param credentials
     * @param authorities
     */
    public JwtAuthenticationToken(Object principal, Object credentials,
                                  Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        user = ((JwtUser) principal).toUser();
    }

    public User getUser() {
        return user;
    }
}
