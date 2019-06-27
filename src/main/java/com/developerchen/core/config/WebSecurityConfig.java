package com.developerchen.core.config;

import com.developerchen.core.constant.Const;
import com.developerchen.core.security.JwtAuthenticationFailureHandler;
import com.developerchen.core.security.JwtAuthenticationSuccessHandler;
import com.developerchen.core.security.JwtAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

/**
 * Spring Security 配置文件
 *
 * @author syc
 */
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsServiceImpl;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
            .csrf().disable()
            .exceptionHandling()
                .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/admin/index"),
                        new RequestHeaderRequestMatcher("X-Requested-With", ""))
                .defaultAuthenticationEntryPointFor(new Http403ForbiddenEntryPoint(),
                        new RequestHeaderRequestMatcher("X-Requested-With","XMLHttpRequest"))
                .and()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .addFilterAfter(new JwtAuthorizationFilter(userDetailsServiceImpl), UsernamePasswordAuthenticationFilter.class)
            .formLogin()
                .loginPage("/admin/login").permitAll()
                .loginProcessingUrl("/admin/login")
                .successHandler(new JwtAuthenticationSuccessHandler("/admin/index"))
                .failureHandler(new JwtAuthenticationFailureHandler())
                .and()
            .logout()
                .logoutUrl("/admin/logout").permitAll()
                .logoutSuccessUrl("/admin/login")
                .deleteCookies(Const.COOKIE_ACCESS_TOKEN)
                .and()
            .authorizeRequests()
                // actuator endpoint
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority(Const.ROLE_ADMIN)
                .antMatchers("/admin/**").hasAuthority(Const.ROLE_ADMIN)
                .antMatchers("/druid/**").hasAuthority(Const.ROLE_ADMIN)
                // 静态资源
                .antMatchers("/resources/**").permitAll()
                .anyRequest().permitAll()
                .and()
            .headers()
                .frameOptions()
                .sameOrigin()
                .and()
            .servletApi()
                .disable();
        // @formatter:on
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
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 权限认证完全基于role(SimpleGrantedAuthority), 将所有attributes视作role即可，不需要默认的 "ROLE_" 前缀来区分角色属性
     * 通过阅读源码分析出此设置方式，官方文档（当前5.0.4.RELEASE）没有提到
     * <p>
     * {@link org.springframework.security.core.authority.SimpleGrantedAuthority}
     * {@link org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer}
     *
     * @see <A HREF="https://docs.spring.io/spring-security/site/docs/current/reference/htmlsingle/#appendix-faq-role-prefix">What does "ROLE_" mean</A>
     */
    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }
}
