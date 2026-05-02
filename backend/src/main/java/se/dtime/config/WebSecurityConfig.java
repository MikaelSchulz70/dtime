package se.dtime.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import se.dtime.service.user.CustomOAuth2UserService;
import se.dtime.service.user.CustomOidcUserService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    @Value("${security.enable-csrf:true}")
    private boolean csrfEnabled;

    @Value("${oauth.authentik.enabled:false}")
    private boolean authentikOAuthEnabled;

    public WebSecurityConfig(CustomAuthenticationSuccessHandler successHandler,
                             CustomOAuth2UserService customOAuth2UserService,
                             CustomOidcUserService customOidcUserService) {
        this.successHandler = successHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenBasedRememberMeServices rememberMeServices(UserDetailsService userDetailsService) {
        TokenBasedRememberMeServices rememberMeServices =
                new TokenBasedRememberMeServices("dtime-remember-me-key", userDetailsService);
        rememberMeServices.setTokenValiditySeconds(86400);
        return rememberMeServices;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenBasedRememberMeServices rememberMeServices) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/error", "/api/auth/oidc/status", "/api/auth/oidc/failure", "/api/auth/oidc/switch-user", "/actuator/health", "/oauth2/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                request -> request.getRequestURI().startsWith("/api/")
                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .permitAll()
                        .deleteCookies("JSESSIONID", "remember-me")
                        .invalidateHttpSession(true)
                )
                .rememberMe(rememberMe -> rememberMe.rememberMeServices(rememberMeServices));

        if (authentikOAuthEnabled && clientRegistrationRepository != null) {
            OAuth2AuthorizationRequestResolver authorizationRequestResolver =
                    new SwitchUserOAuth2AuthorizationRequestResolver(clientRegistrationRepository);
            http.oauth2Login(oauth2 -> oauth2
                    .loginPage("/oauth2/authorization/authentik")
                    .successHandler(successHandler)
                    .failureHandler((request, response, exception) -> {
                        String errorCode = resolveErrorCode(exception);
                        response.sendRedirect("/api/auth/oidc/failure?reason=" + errorCode);
                    })
                    .authorizationEndpoint(authorization -> authorization
                            .authorizationRequestResolver(authorizationRequestResolver)
                    )
                    .tokenEndpoint(token -> token
                            .accessTokenResponseClient(authorizationCodeTokenResponseClient())
                    )
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService)
                            .oidcUserService(customOidcUserService)
                    )
                    .clientRegistrationRepository(clientRegistrationRepository)
            );
        } else {
            http.formLogin(form -> form.disable());
        }

        http.sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(-1) // Allow unlimited sessions for development
                        .maxSessionsPreventsLogin(false)
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(content -> {
                        })
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

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> authorizationCodeTokenResponseClient() {
        RestClientAuthorizationCodeTokenResponseClient accessTokenResponseClient =
                new RestClientAuthorizationCodeTokenResponseClient();
        RestClient restClient = RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .messageConverters(converters -> {
                    converters.clear();
                    converters.add(new FormHttpMessageConverter());
                    converters.add(new OAuth2AccessTokenResponseHttpMessageConverter());
                    converters.add(new StringHttpMessageConverter());
                })
                .build();
        accessTokenResponseClient.setRestClient(restClient);
        return accessTokenResponseClient;
    }

    private String resolveErrorCode(AuthenticationException exception) {
        if (exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauthEx) {
            String code = oauthEx.getError() != null ? oauthEx.getError().getErrorCode() : null;
            if (code != null && !code.isBlank()) {
                return code;
            }
        }
        return "oauth_failure";
    }
}