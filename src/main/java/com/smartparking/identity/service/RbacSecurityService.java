package com.smartparking.identity.service;

import com.smartparking.identity.entity.Account;
import com.smartparking.identity.repository.AccountRepository;
import com.smartparking.identity.repository.RoleFunctionActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service("rbac") // Đặt tên Bean ngắn gọn để gọi trong annotation
@RequiredArgsConstructor
public class RbacSecurityService {
    private final AccountRepository accountRepository;
    private final RoleFunctionActionRepository rfaRepository;

    @Transactional(readOnly = true)
    public boolean hasAccess(Integer accountId, String funcName, String actionCode) {
        if (accountId == null) return false;

        // Chỉ cần tìm Account nội bộ để lấy Role ID
        Account account = accountRepository.findById(accountId).orElse(null);

        if (account == null || account.getRoleId() == null) {
            return false;
        }

        return rfaRepository.hasPermission(account.getRoleId(), funcName, actionCode);
    }
}