package com.developerchen.core.system.config;

import com.developerchen.core.common.constant.Const;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.springframework.security.config.Customizer.withDefaults;


//@Configuration
//@EnableWebSecurity
public class OAuth2SecurityConfig {


    @Bean
    public SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
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
                .formLogin(withDefaults())
                .sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
//                .with(new JwtLoginConfigurer<>(), (loginConfigurer) -> {
//                    loginConfigurer.loginPage("/admin/index").permitAll()
//                            .loginProcessingUrl("/admin/login")
//                            .successHandler(new JwtAuthenticationSuccessHandler("/admin/index"))
//                            .failureHandler(new JwtAuthenticationFailureHandler());
//                });

        http.logout(logout -> logout.logoutUrl("/admin/logout").permitAll()
                        .defaultLogoutSuccessHandlerFor(new HttpStatusReturningLogoutSuccessHandler(),
                                new AntPathRequestMatcher("/admin/logout", "POST"))
                        .logoutSuccessUrl("/admin/index")
                        .deleteCookies(Const.COOKIE_ACCESS_TOKEN)
                )
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                                // actuator endpoint
                                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasAuthority(Const.ROLE_ADMIN)
//                        .requestMatchers("/admin/**").hasAuthority(Const.ROLE_ADMIN)
                                // 静态资源
                                .requestMatchers(SystemConfig.staticPathPattern).permitAll()
                                .anyRequest().permitAll()
                )
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                );

        http.servletApi(AbstractHttpConfigurer::disable);
        return http.build();
    }


    @Bean
    public JwtDecoder jwtDecoder() {
        // 使用 HMAC 密钥
        SecretKey secretKey = new SecretKeySpec("WoQu-@Nian~Mai$Le%#GeDa^Jin#Biao"
                .getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }

}
