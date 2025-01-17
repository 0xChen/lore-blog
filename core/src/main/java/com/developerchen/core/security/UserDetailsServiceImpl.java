package com.developerchen.core.security;

import com.developerchen.core.domain.entity.User;
import com.developerchen.core.service.IUserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * <p>
 * for Spring Security
 * </p>
 *
 * @author syc
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final IUserService userService;

    public UserDetailsServiceImpl(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.getUserByUsername(username.toLowerCase());

        if (user == null) {
            throw new UsernameNotFoundException(" 用户名不存在. ");
        }
        return JwtUser.JwtUserBuilder.build(user);
    }
}
