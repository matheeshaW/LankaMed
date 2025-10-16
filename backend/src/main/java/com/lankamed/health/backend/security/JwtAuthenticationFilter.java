package com.lankamed.health.backend.security;

import com.lankamed.health.backend.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String jwt;
        final String username;

        System.out.println("JwtAuthenticationFilter: Processing request " + request.getMethod() + " " + request.getRequestURI());
        System.out.println("JwtAuthenticationFilter: Authorization header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("JwtAuthenticationFilter: No Authorization header or not a Bearer token");
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        System.out.println("JwtAuthenticationFilter: JWT token: " + jwt);
        
        try {
            username = jwtUtil.extractUsername(jwt);
            System.out.println("JwtAuthenticationFilter: Extracted username: " + username);
        } catch (Exception e) {
            System.out.println("JwtAuthenticationFilter: Error extracting username: " + e.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                System.out.println("JwtAuthenticationFilter: Loading user details for: " + username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                System.out.println("JwtAuthenticationFilter: User details loaded: " + userDetails.getUsername());
                
                if (jwtUtil.isTokenValid(jwt, userDetails.getUsername())) {
                    System.out.println("JwtAuthenticationFilter: Token is valid, setting authentication");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("JwtAuthenticationFilter: Authentication set successfully");
                } else {
                    System.out.println("JwtAuthenticationFilter: Token is invalid for user: " + username);
                }
            } catch (Exception ex) {
                System.out.println("JwtAuthenticationFilter: Failed to authenticate JWT for user " + username + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        filterChain.doFilter(request, response);
    }
}
