package com.kitloop.config;

import com.kitloop.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(
                        userDetailsService
                );

        provider.setPasswordEncoder(
                passwordEncoder
        );

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider
    ) throws Exception {

        http
                .authenticationProvider(
                        authenticationProvider
                )

                .authorizeHttpRequests(authorize ->
                        authorize

                                .requestMatchers(
                                        "/",
                                        "/login",
                                        "/register",
                                        "/verify-otp",
                                        "/resend-otp",
                                        "/access-denied",
                                        "/error",
                                        "/css/**",
                                        "/js/**",
                                        "/favicon.ico"
                                )
                                .permitAll()

                                .requestMatchers(
                                        "/uploads/**"
                                )
                                .denyAll()

                                .requestMatchers(
                                        "/admin/**"
                                )
                                .hasRole("ADMIN")

                                .requestMatchers(
                                        "/user/**"
                                )
                                .hasRole("USER")

                                .requestMatchers(
                                        "/post-login"
                                )
                                .authenticated()

                                .anyRequest()
                                .authenticated()
                )

                .formLogin(form ->
                        form
                                .loginPage("/")
                                .loginProcessingUrl("/login")
                                .usernameParameter("email")
                                .passwordParameter("password")
                                .defaultSuccessUrl(
                                        "/post-login",
                                        true
                                )
                                .failureUrl("/?error")
                                .permitAll()
                )

                .logout(logout ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/?logout")
                                .invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .deleteCookies("JSESSIONID")
                                .permitAll()
                )

                .exceptionHandling(exception ->
                        exception
                                .accessDeniedPage(
                                        "/access-denied"
                                )
                );

        return http.build();
    }
}