package com.smartparking.shared.config;

import com.smartparking.identity.dto.CustomAccountPrincipal;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
                List<Integer> masterGroupIds = new ArrayList<>();
                List<Integer> memberGroupIds = new ArrayList<>();

                // Lấy app_metadata từ token payload
                Map<String, Object> appMetadata = jwtAuth.getToken().getClaim("app_metadata");

                if (appMetadata != null) {
                    if (appMetadata.get("master_group_ids") instanceof List) {
                        masterGroupIds = (List<Integer>) appMetadata.get("master_group_ids");
                    }
                    if (appMetadata.get("member_group_ids") instanceof List) {
                        memberGroupIds = (List<Integer>) appMetadata.get("member_group_ids");
                    }
                }

                CustomAccountPrincipal principal = new CustomAccountPrincipal(
                        account.getId(),
                        account.getRoleName(),
                        masterGroupIds,
                        memberGroupIds
                );

                var customAuth = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + account.getRoleName()))
                );
                SecurityContextHolder.getContext().setAuthentication(customAuth);
            });
        }

        filterChain.doFilter(request, response);
    }

}
