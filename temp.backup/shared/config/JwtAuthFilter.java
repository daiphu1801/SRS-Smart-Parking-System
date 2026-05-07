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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.smartparking.identity.service.JwtService;
import com.smartparking.identity.entity.Account;
import com.smartparking.identity.repository.AccountRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AccountRepository accountRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtService.isTokenValid(token)) {
            String supabaseId = jwtService.extractSupabaseId(token);
            
            // Look up the local account using the Supabase UUID
            accountRepository.findBySupabaseId(UUID.fromString(supabaseId)).ifPresent(account -> {
                // We add the Account ID to the request so controllers can use @RequestAttribute("accountId")
                request.setAttribute("accountId", account.getId());
                
                var auth = new UsernamePasswordAuthenticationToken(
                        account.getId(), null, List.of(new SimpleGrantedAuthority("ROLE_" + account.getRoleName())));
                SecurityContextHolder.getContext().setAuthentication(auth);
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

