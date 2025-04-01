package com.example.wallet.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Pobieramy token z nagłówka Authorization
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtTokenProvider.extractUsername(token);

            // Jeśli użytkownik nie jest jeszcze zalogowany, tworzymy autoryzację
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = jwtTokenProvider.getUserDetails(token);

                // Tworzymy obiekt autoryzacyjny na podstawie danych z tokena
                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Ustawiamy autoryzację w kontekście bezpieczeństwa Springa
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Przechodzimy do następnego filtra
        filterChain.doFilter(request, response);
    }
}
