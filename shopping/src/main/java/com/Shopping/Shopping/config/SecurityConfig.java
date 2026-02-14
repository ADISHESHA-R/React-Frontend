package com.Shopping.Shopping.config;

import com.Shopping.Shopping.service.UserDetailsServiceImpl;
import com.Shopping.Shopping.service.SellerDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    private final CustomLoginSuccessHandler successHandler;
    private final UserDetailsServiceImpl userDetailsService;
    private final SellerDetailsService sellerDetailsService;

    public SecurityConfig(CustomLoginSuccessHandler successHandler,
                          UserDetailsServiceImpl userDetailsService,
                          SellerDetailsService sellerDetailsService) {
        this.successHandler = successHandler;
        this.userDetailsService = userDetailsService;
        this.sellerDetailsService = sellerDetailsService;
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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

    /**
     * ✅ Seller Security Configuration
     */
    @Bean
    @Order(1)
    public SecurityFilterChain sellerFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/seller-login", "/seller-signup", "/seller-logout", "/seller-home", "/seller-dashboard", "/upload-product", "/seller-profile", "/seller/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/seller-login", "/seller-signup").permitAll()
                        .requestMatchers("/uploads/**", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/seller-home", "/seller-dashboard", "/upload-product", "/seller-profile", "/seller/**").hasRole("SELLER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/seller-login")
                        .loginProcessingUrl("/seller-login")
                        .successHandler(successHandler)
                        .failureUrl("/seller-login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/seller-logout")
                        .logoutSuccessUrl("/?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                )
                .authenticationProvider(sellerAuthProvider(passwordEncoder()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/h2-console/**", "/seller-signup")
                )
                .headers(headers -> headers.frameOptions().disable());

        return http.build();
    }

    /**
     * ✅ User + OAuth2 Security Configuration
     */
    @Bean
    @Order(2)
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/product/**", "/product-image/**", "/signup", "/login", "/seller-login", "/seller-signup", "/oauth2/**", "/h2-console/**", "/uploads/**", "/css/**", "/js/**", "/images/**", "/admin/**").permitAll()
                        .requestMatchers("/search").permitAll() // Allow search for everyone
                        .requestMatchers("/cart/add/**", "/cart/remove/**", "/cart/update/**", "/cart").permitAll() // Allow cart operations for everyone
                        .requestMatchers("/help-center", "/contact-us", "/privacy-policy", "/terms-of-service").permitAll() // Allow public access to info pages
                        .requestMatchers("/home", "/profile", "/buy-now/**", "/buy-now/address", "/payment-success", "/create-order").hasRole("USER")
                        .requestMatchers("/user/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                )
                .authenticationProvider(userAuthProvider(passwordEncoder()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/h2-console/**", "/seller-signup")
                )
                .headers(headers -> headers.frameOptions().disable());

        return http.build();
    }
}
