package com.Shopping.Shopping.config;

import com.Shopping.Shopping.security.JwtAuthenticationFilter;
import com.Shopping.Shopping.service.UserDetailsServiceImpl;
import com.Shopping.Shopping.service.SellerDetailsService;
import com.Shopping.Shopping.service.AdminDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final SellerDetailsService sellerDetailsService;
    private final AdminDetailsService adminDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          SellerDetailsService sellerDetailsService,
                          AdminDetailsService adminDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.sellerDetailsService = sellerDetailsService;
        this.adminDetailsService = adminDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider userAuthProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider sellerAuthProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(sellerDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider adminAuthProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    /**
     * ✅ Admin Security Configuration (JWT-Only, API-Only)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/admin/**")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/admin/login").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized: Admin access required\",\"data\":null}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"Forbidden: Admin access required\",\"data\":null}");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(adminAuthProvider(passwordEncoder()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/admin/**"));

        return http.build();
    }

    /**
     * ✅ API Security Configuration (HIGHEST Priority - handles /api/** routes FIRST)
     * Uses JWT authentication instead of session-based
     * Must be checked BEFORE other filter chains to prevent HTML redirects
     */
    @Bean
    @Order(0) // Changed to 0 to ensure it's checked FIRST, before admin chain
    public SecurityFilterChain apiFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
            .securityMatcher("/api/**")
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Disable ALL authentication methods except JWT
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .logout(logout -> logout.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/products/**", 
                                 "/api/v1/auth/signup", 
                                 "/api/v1/auth/login",
                                 "/api/v1/seller/signup", 
                                 "/api/v1/seller/login",
                                 "/api/v1/admin/login").permitAll()
                .requestMatchers("/api/v1/user/**", "/api/v1/cart/**", "/api/v1/payment/**").hasRole("USER")
                .requestMatchers("/api/v1/seller/**").hasRole("SELLER")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized: JWT token is required\",\"data\":null}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Forbidden: Insufficient permissions\",\"data\":null}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authenticationProvider(userAuthProvider(passwordEncoder()))
            .authenticationProvider(sellerAuthProvider(passwordEncoder()))
            .authenticationProvider(adminAuthProvider(passwordEncoder()))
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }

    /**
     * ✅ Seller Security Configuration (JWT-Only, API-Only)
     */
    @Bean
    @Order(2)
    public SecurityFilterChain sellerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/seller/**")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/seller/signup", 
                                        "/api/v1/seller/login").permitAll()
                        .requestMatchers("/api/v1/seller/**").hasRole("SELLER")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized: Seller access required\",\"data\":null}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write("{\"success\":false,\"message\":\"Forbidden: Seller access required\",\"data\":null}");
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(sellerAuthProvider(passwordEncoder()))
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/v1/seller/**"));

        return http.build();
    }

    /**
     * ✅ User Security Configuration (JWT-Only, API-Only)
     * This chain is kept for any non-API paths but should not match anything in API-only deployment
     */
    @Bean
    @Order(3)
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(request -> {
                    String path = request.getRequestURI();
                    // Only match static resources, not API paths
                    return !path.startsWith("/api/") && 
                           !path.startsWith("/admin") && 
                           !path.startsWith("/seller");
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/uploads/**", "/product-image/**").permitAll()
                        .anyRequest().denyAll() // Deny all non-API requests in API-only mode
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authenticationProvider(userAuthProvider(passwordEncoder()))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

}
