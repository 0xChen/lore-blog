package com.developerchen.core.system.security;

import com.developerchen.core.domain.entity.User;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.io.Serial;
import java.util.Collection;

/**
 * An {@link org.springframework.security.core.Authentication} implementation
 *
 * @author syc
 */
public class UserAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;


    private final Object principal;

    private Object credentials;

    @Getter
    private final User user;

    /**
     * This constructor can be safely used by any code that wishes to create a
     * <code>UserAuthenticationToken</code>, as the {@link #isAuthenticated()}
     * will return <code>false</code>.
     */
    public UserAuthenticationToken(Object principal, Object credentials) {
        super(null);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);

        if (principal instanceof ApiUser apiUser) {
            this.user = apiUser.toUser();
        } else if (principal instanceof User u) {
            this.user = u;
        } else {
            this.user = null;
        }
    }

    /**
     * This constructor should only be used by <code>AuthenticationManager</code> or
     * <code>AuthenticationProvider</code> implementations that are satisfied with
     * producing a trusted (i.e. {@link #isAuthenticated()} = <code>true</code>)
     * authentication token.
     *
     * @param principal the principal being authenticated
     * @param credentials the credentials that should be presented for authentication for
     * @param authorities the authorities that should be granted to the caller
     */
    public UserAuthenticationToken(Object principal, Object credentials,
                                   Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false);

        if (principal instanceof ApiUser apiUser) {
            this.user = apiUser.toUser();
        } else if (principal instanceof User u) {
            this.user = u;
        } else {
            this.user = null;
        }
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        super.setAuthenticated(isAuthenticated);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.credentials = null;
    }

}
