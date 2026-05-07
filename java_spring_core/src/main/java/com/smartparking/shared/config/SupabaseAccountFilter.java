package com.smartparking.shared.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.smartparking.identity.repository.AccountRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SupabaseAccountFilter extends OncePerRequestFilter {


    private final AccountRepository accountRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Nếu token hợp lệ và là JWT token
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {

            String supabaseId = jwtAuth.getToken().getSubject();

            accountRepository.findBySupabaseId(UUID.fromString(supabaseId)).ifPresent(account -> {

                request.setAttribute("accountId", account.getId());

                var customAuth = new UsernamePasswordAuthenticationToken(
                        account.getId(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + account.getRoleName()))
                );
                SecurityContextHolder.getContext().setAuthentication(customAuth);
            });
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer "))
            return header.substring(7);
        return null;
    }
}
