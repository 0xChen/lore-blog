package com.developerchen.core.config;

import com.developerchen.core.constant.Const;
import com.developerchen.core.security.JwtAuthenticationFailureHandler;
import com.developerchen.core.security.JwtAuthenticationSuccessHandler;
import com.developerchen.core.security.JwtAuthorizationFilter;
import com.developerchen.core.security.JwtLoginConfigurer;
import com.developerchen.core.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
 * Spring Security 配置文件
 *
 * @author syc
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private UserDetailsService userDetailsServiceImpl;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> {
            // do something
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling((exceptionHandling) ->
                        exceptionHandling.defaultAuthenticationEntryPointFor(
                                        new LoginUrlAuthenticationEntryPoint("/admin/index"),
                                        new RequestHeaderRequestMatcher("X-Requested-With", ""))
                                .defaultAuthenticationEntryPointFor(
                                        new Http403ForbiddenEntryPoint(),
                                        new RequestHeaderRequestMatcher("X-Requested-With", "XMLHttpRequest"))
                )
                .sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(new JwtAuthorizationFilter(userDetailsServiceImpl), UsernamePasswordAuthenticationFilter.class)
                .with(new JwtLoginConfigurer<>(), (loginConfigurer) -> {
                    loginConfigurer.loginPage("/admin/index").permitAll()
                            .loginProcessingUrl("/admin/login")
                            .successHandler(new JwtAuthenticationSuccessHandler("/admin/index"))
                            .failureHandler(new JwtAuthenticationFailureHandler());
                });

        http.logout(logout -> logout.logoutUrl("/admin/logout").permitAll()
                        .defaultLogoutSuccessHandlerFor(new HttpStatusReturningLogoutSuccessHandler(),
                                new AntPathRequestMatcher("/admin/logout", "POST"))
                        .logoutSuccessUrl("/admin/index")
                        .deleteCookies(Const.COOKIE_ACCESS_TOKEN)
                )
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        // actuator endpoint
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority(Const.ROLE_ADMIN)
                        .requestMatchers("/admin/**").hasAuthority(Const.ROLE_ADMIN)
                        .requestMatchers("/druid/**").hasAuthority(Const.ROLE_ADMIN)
                        // 静态资源
                        .requestMatchers(AppConfig.staticPathPattern).permitAll()
                        .anyRequest().permitAll()
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                );

        http.servletApi(AbstractHttpConfigurer::disable);

        return http.build();
    }

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

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsServiceImpl);
    }

    /**
     * 确保每次升级Spring Security框架后, 都能够使用Spring官方推荐的当前最为可靠的密码编码器,
     * 并且能够正确解码历史密码
     *
     * @see <A HREF="https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#pe-dpe">PasswordEncoder</A>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return SecurityUtils.USER_PASSWORD_ENCODER;
    }

    /**
     * 权限认证完全基于role(SimpleGrantedAuthority), 将所有attributes视作role即可，不需要默认的 "ROLE_" 前缀来区分角色属性
     * 通过阅读源码分析出此设置方式，官方文档（当前5.0.4.RELEASE）没有提到
     * <p>
     * {@link org.springframework.security.core.authority.SimpleGrantedAuthority}
     * {@link org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer}
     *
     * @see <A HREF="https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#appendix-faq-role-prefix">What does "ROLE_" mean</A>
     */
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }
}
