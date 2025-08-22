package com.developerchen.core.system.config;

import com.developerchen.core.common.constant.Const;
import com.developerchen.core.system.security.*;
import com.developerchen.core.system.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Spring Security 配置类
 * 支持JWT和自定义令牌格式，以及表单和JSON登录方式
 *
 * @author syc
 */
@Configuration
@EnableWebSecurity
public class MultiFormatSecurityConfig {

    @Autowired
    private UserDetailsService userDetailsServiceImpl;

    /**
     * 配置不需要安全过滤的请求
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            // 可以在这里配置不需要安全过滤的请求
        };
    }

    /**
     * 配置安全过滤链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.securityContext(Customizer.withDefaults());
        // 配置CORS
        http.cors(withDefaults());
        
        // 禁用CSRF
        http.csrf(AbstractHttpConfigurer::disable);
        
        // 配置异常处理
        http.exceptionHandling((exceptionHandling) ->
                exceptionHandling.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/admin/index"),
                                new RequestHeaderRequestMatcher("X-Requested-With", ""))
                        .defaultAuthenticationEntryPointFor(
                                new Http403ForbiddenEntryPoint(),
                                new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest"))
        );
        
        // 配置会话管理，使用无状态会话
        http.sessionManagement((sessionManagement) -> 
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        // 添加多格式令牌过滤器
        MultiFormatTokenResolver tokenResolver = multiFormatTokenResolver();
        MultiFormatAuthenticationConverter authenticationConverter = new MultiFormatAuthenticationConverter(tokenResolver);
        MultiFormatAuthenticationFilter authenticationFilter = new MultiFormatAuthenticationFilter(authenticationConverter, tokenResolver);
        authenticationFilter.setUserDetailsService(userDetailsServiceImpl);
        http.addFilterAfter(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // 配置表单登录
        http.formLogin(form -> form
                .loginPage("/admin/index").permitAll()
                .permitAll());
        
        // 配置API登录（支持JSON格式）
        http.with(new ApiLoginConfigurer<>(), (loginConfigurer) -> {
            loginConfigurer.loginPage("/admin/index").permitAll()
                    .loginProcessingUrl("/admin/login")
                    .successHandler(new ApiAuthenticationSuccessHandler("/admin/index"))
                    .failureHandler(new ApiAuthenticationFailureHandler());
        });
        
        // 配置登出
        http.logout(logout -> logout.logoutUrl("/admin/logout").permitAll()
                .defaultLogoutSuccessHandlerFor(new HttpStatusReturningLogoutSuccessHandler(),
                        new AntPathRequestMatcher("/admin/logout", "POST"))
                .logoutSuccessUrl("/admin/index")
                .deleteCookies(Const.COOKIE_ACCESS_TOKEN)
        );
        
        // 配置请求授权
        http.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                // actuator endpoint
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority(Const.ROLE_ADMIN)
                .requestMatchers("/admin/**").hasAuthority(Const.ROLE_ADMIN)
                // 静态资源
                .requestMatchers(SystemConfig.staticPathPattern).permitAll()
                .anyRequest().permitAll()
        );
        
        // 配置头信息
        http.headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
        );
        
        // 禁用Servlet API集成
        http.servletApi(AbstractHttpConfigurer::disable);
        
        return http.build();
    }

    /**
     * 配置多格式令牌解析器
     */
    @Bean
    @Primary
    public MultiFormatTokenResolver multiFormatTokenResolver() {
        MultiFormatTokenResolver resolver = new MultiFormatTokenResolver();
        resolver.setAllowUriQueryParameter(true);
        return resolver;
    }

    /**
     * 配置CORS
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Collections.singletonList("*"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(120L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 配置密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return SecurityUtils.USER_PASSWORD_ENCODER;
    }

    /**
     * 配置权限前缀
     */
    @Bean
    static GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }

    /**
     * 配置认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
