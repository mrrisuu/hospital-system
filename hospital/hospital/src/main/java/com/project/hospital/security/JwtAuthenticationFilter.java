package com.project.hospital.security;

import com.project.hospital.user.entity.User;
import com.project.hospital.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtService.parseClaims(token);
            String email = claims.getSubject();

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                userRepository.findByEmail(email)
                        .filter(user -> isTokenStillValid(user, claims))
                        .ifPresent(user -> {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            email,
                                            null,
                                            List.of(new SimpleGrantedAuthority(
                                                    "ROLE_" + user.getRole().name()))
                                    );

                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            }
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isTokenStillValid(User user, Claims claims) {
        if (!user.isActive() || claims.getIssuedAt() == null) {
            return false;
        }

        LocalDateTime changedAt = user.getPasswordChangedAt();
        if (changedAt == null) {
            return true;
        }

        // iat di JWT hanya presisi detik, jadi waktu ganti password dipotong ke detik
        LocalDateTime issuedAt = LocalDateTime.ofInstant(
                claims.getIssuedAt().toInstant(), ZoneId.systemDefault());

        return !issuedAt.isBefore(changedAt.truncatedTo(ChronoUnit.SECONDS));
    }
}