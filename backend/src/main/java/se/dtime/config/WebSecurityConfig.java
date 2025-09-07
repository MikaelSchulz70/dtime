package se.dtime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import se.dtime.service.user.UserLoginService;
import se.dtime.service.user.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final UserLoginService userLoginService;
    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    
    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @Value("${security.enable-csrf:true}")
    private boolean csrfEnabled;

    @Value("${oauth.google.enabled:false}")
    private boolean googleOAuthEnabled;

    public WebSecurityConfig(UserLoginService userLoginService, 
                             CustomAuthenticationSuccessHandler successHandler,
                             CustomOAuth2UserService customOAuth2UserService) {
        this.userLoginService = userLoginService;
        this.successHandler = successHandler;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userLoginService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/perform_login", "/error", "/api/auth/google/status").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> request.getRequestURI().startsWith("/api/")
                        )
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/perform_login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .permitAll()
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                )
                .rememberMe(rememberMe -> rememberMe
                        .key("dtime-remember-me-key")
                        .tokenValiditySeconds(86400) // 24 hours
                );

        // Configure OAuth2 login if enabled and client registration is available
        if (googleOAuthEnabled && clientRegistrationRepository != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .loginPage("/login")
                    .successHandler(successHandler)
                    .failureHandler((request, response, exception) -> {
                        response.sendRedirect("/login?error=oauth");
                    })
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService)
                    )
                    .clientRegistrationRepository(clientRegistrationRepository)
            );
        }

        http.sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(-1) // Allow unlimited sessions for development
                        .maxSessionsPreventsLogin(false)
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(content -> {})
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                        )
                        .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                );

        if (csrfEnabled) {
            http.csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers("/logout")
            );
        } else {
            http.csrf(csrf -> csrf.disable());
        }

        return http.build();
    }
}