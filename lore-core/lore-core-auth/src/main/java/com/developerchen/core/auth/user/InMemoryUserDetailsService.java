package com.developerchen.core.auth.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版本的用户详情服务实现
 * 用于演示和测试目的
 * 
 * @author syc
 */
@Service
public class InMemoryUserDetailsService implements UserDetailsService {

    private final Map<String, UserDetails> users = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    public InMemoryUserDetailsService() {
        // 初始化一些测试用户
        createUser("admin", "password", "ROLE_ADMIN", "ROLE_USER");
        createUser("user", "password", "ROLE_USER");
        createUser("test", "test123", "ROLE_USER");
    }

    /**
     * 创建用户
     * 
     * @param username 用户名
     * @param password 密码
     * @param authorities 权限
     */
    private void createUser(String username, String password, String... authorities) {
        UserDetails user = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .authorities(authorities)
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(false)
            .build();
        
        users.put(username, user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = users.get(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return user;
    }

    /**
     * 添加用户（用于测试）
     * 
     * @param username 用户名
     * @param password 密码
     * @param authorities 权限
     */
    public void addUser(String username, String password, String... authorities) {
        createUser(username, password, authorities);
    }

    /**
     * 删除用户（用于测试）
     * 
     * @param username 用户名
     */
    public void deleteUser(String username) {
        users.remove(username);
    }

    /**
     * 检查用户是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
}