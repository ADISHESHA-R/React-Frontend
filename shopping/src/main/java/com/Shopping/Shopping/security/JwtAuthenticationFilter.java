package com.Shopping.Shopping.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final com.Shopping.Shopping.service.SellerDetailsService sellerDetailsService;
    private final com.Shopping.Shopping.service.AdminDetailsService adminDetailsService;

    public JwtAuthenticationFilter(
            JwtTokenProvider tokenProvider,
            @Qualifier("userDetailsServiceImpl") UserDetailsService userDetailsService,
            com.Shopping.Shopping.service.SellerDetailsService sellerDetailsService,
            com.Shopping.Shopping.service.AdminDetailsService adminDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.sellerDetailsService = sellerDetailsService;
        this.adminDetailsService = adminDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        // Skip JWT validation for public endpoints
        if (requestPath.startsWith("/api/v1/products") ||
            requestPath.startsWith("/api/v1/auth/signup") ||
            requestPath.startsWith("/api/v1/auth/login") ||
            requestPath.startsWith("/api/v1/auth/verify-email") ||
            requestPath.startsWith("/api/v1/auth/resend-otp") ||
            requestPath.startsWith("/api/v1/seller/signup") ||
            requestPath.startsWith("/api/v1/seller/login") ||
            requestPath.startsWith("/api/v1/seller/verify-email") ||
            requestPath.startsWith("/api/v1/seller/resend-otp") ||
            requestPath.startsWith("/api/v1/admin/login") ||
            requestPath.startsWith("/product-image/") ||
            requestPath.startsWith("/h2-console/") ||
            requestPath.startsWith("/uploads/") ||
            requestPath.startsWith("/css/") ||
            requestPath.startsWith("/js/") ||
            requestPath.startsWith("/images/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            // If no token provided, handle based on request type
            if (jwt == null || jwt.trim().isEmpty()) {
                // For API endpoints, return JSON error
                if (requestPath.startsWith("/api/")) {
                    sendUnauthorizedResponse(response, "JWT token is missing");
                } else {
                    // For web pages, redirect to appropriate login page
                    if (requestPath.startsWith("/admin")) {
                        sendUnauthorizedRedirect(response, "/admin-login");
                    } else if (requestPath.startsWith("/seller")) {
                        sendUnauthorizedRedirect(response, "/seller-login");
                    } else {
                        sendUnauthorizedRedirect(response, "/login");
                    }
                }
                return;
            }

            // Validate token structure first (signature and expiration)
            if (!tokenProvider.validateTokenStructure(jwt)) {
                if (requestPath.startsWith("/api/")) {
                    sendUnauthorizedResponse(response, "Invalid or expired JWT token");
                } else {
                    if (requestPath.startsWith("/admin")) {
                        sendUnauthorizedRedirect(response, "/admin-login");
                    } else if (requestPath.startsWith("/seller")) {
                        sendUnauthorizedRedirect(response, "/seller-login");
                    } else {
                        sendUnauthorizedRedirect(response, "/login");
                    }
                }
                return;
            }

            String username = tokenProvider.getUsernameFromToken(jwt);
            @SuppressWarnings("unchecked")
            List<String> authorities = tokenProvider.getClaimFromToken(jwt, claims -> 
                (List<String>) claims.get("authorities"));

            // Determine which UserDetailsService to use based on authorities
            UserDetails userDetails = null;
            try {
                if (authorities != null) {
                    if (authorities.contains("ROLE_ADMIN")) {
                        userDetails = adminDetailsService.loadUserByUsername(username);
                    } else if (authorities.contains("ROLE_SELLER")) {
                        userDetails = sellerDetailsService.loadUserByUsername(username);
                    } else {
                        userDetails = userDetailsService.loadUserByUsername(username);
                    }
                } else {
                    // Fallback: try all services
                    try {
                        userDetails = adminDetailsService.loadUserByUsername(username);
                    } catch (Exception e) {
                        try {
                            userDetails = sellerDetailsService.loadUserByUsername(username);
                        } catch (Exception e2) {
                            userDetails = userDetailsService.loadUserByUsername(username);
                        }
                    }
                }

                if (userDetails != null && tokenProvider.validateToken(jwt, userDetails)) {
                    List<SimpleGrantedAuthority> grantedAuthorities = authorities != null ?
                        authorities.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList()) :
                        userDetails.getAuthorities().stream()
                            .map(auth -> new SimpleGrantedAuthority(auth.getAuthority()))
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    grantedAuthorities);

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    // Continue with the filter chain
                    filterChain.doFilter(request, response);
                } else {
                    if (requestPath.startsWith("/api/")) {
                        sendUnauthorizedResponse(response, "Invalid token or user not found");
                    } else {
                        if (requestPath.startsWith("/admin")) {
                            sendUnauthorizedRedirect(response, "/admin-login");
                        } else if (requestPath.startsWith("/seller")) {
                            sendUnauthorizedRedirect(response, "/seller-login");
                        } else {
                            sendUnauthorizedRedirect(response, "/login");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Error loading user details", e);
                if (requestPath.startsWith("/api/")) {
                    sendUnauthorizedResponse(response, "Authentication failed: " + e.getMessage());
                } else {
                    if (requestPath.startsWith("/admin")) {
                        sendUnauthorizedRedirect(response, "/admin-login");
                    } else if (requestPath.startsWith("/seller")) {
                        sendUnauthorizedRedirect(response, "/seller-login");
                    } else {
                        sendUnauthorizedRedirect(response, "/login");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("JWT authentication error", e);
            if (requestPath.startsWith("/api/")) {
                sendUnauthorizedResponse(response, "JWT token validation failed");
            } else {
                if (requestPath.startsWith("/admin")) {
                    sendUnauthorizedRedirect(response, "/admin-login");
                } else if (requestPath.startsWith("/seller")) {
                    sendUnauthorizedRedirect(response, "/seller-login");
                } else {
                    sendUnauthorizedRedirect(response, "/login");
                }
            }
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }


    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        errorResponse.put("data", null);

        ObjectMapper mapper = new ObjectMapper();
        String jsonResponse = mapper.writeValueAsString(errorResponse);

        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse);
        writer.flush();
    }

    private void sendUnauthorizedRedirect(HttpServletResponse response, String loginPage) throws IOException {
        // For web pages, redirect to login page
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.sendRedirect(loginPage + "?error=Please login to access this page");
    }
}
