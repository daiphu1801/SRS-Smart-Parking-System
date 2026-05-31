package com.smartparking.shared.logging;
import com.smartparking.shared.dto.CustomAccountPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component("auditorProvider")
public class AuditAwareImpl implements AuditorAware<Integer> { // Trả về Integer vì accountId của ông là Integer

    @Override
    public Optional<Integer> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        CustomAccountPrincipal principal = (CustomAccountPrincipal) auth.getPrincipal();

        return Optional.of(principal.getAccountId());
    }
}