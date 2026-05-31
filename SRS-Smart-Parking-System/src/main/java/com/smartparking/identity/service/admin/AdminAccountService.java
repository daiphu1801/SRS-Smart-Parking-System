package com.smartparking.identity.service.admin;

import com.smartparking.identity.dto.request.AccountUpdateRequest;
import com.smartparking.identity.dto.response.AccountResponse;
import com.smartparking.identity.entity.Account;
import com.smartparking.identity.entity.AccountType;
import com.smartparking.identity.entity.Role;
import com.smartparking.identity.repository.AccountRepository;
import com.smartparking.identity.repository.RoleRepository;
import com.smartparking.identity.specification.AccountSpecs;
import com.smartparking.identity.entity.GeneralStatus;
import com.smartparking.shared.dto.PageResponse;
import com.smartparking.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final AccountRepository accountRepo;
    private final RoleRepository roleRepo;

    public PageResponse<AccountResponse> getAccounts(
            Pageable pageable,
            Account filter) {

        Specification<Account> spec = Specification
                .where(AccountSpecs.hasUsername(filter.getUsername()))
                .and(AccountSpecs.hasRole(filter.getRoleId()))
                .and(AccountSpecs.hasAccountType(filter.getAccountType().toString()))
                .and(AccountSpecs.hasStatus(filter.getStatus().toString()));

        Page<Account> accountPage = accountRepo.findAll(spec, pageable);

        List<AccountResponse> content = accountPage.getContent().stream().map(acc -> {
            String roleName = roleRepo.findById(acc.getRoleId())
                    .map(Role::getRoleName)
                    .orElse("Unknown");

            return AccountResponse.builder()
                    .id(acc.getId())
                    .username(acc.getUsername())
                    .roleId(acc.getRoleId())
                    .roleName(roleName)
                    .accountType(acc.getAccountType() != null ? acc.getAccountType().name() : null)
                    .status(acc.getStatus() != null ? acc.getStatus().name() : "N/A")
                    .lastLogin(acc.getLastLogin())
                    .createdAt(acc.getCreatedAt())
                    .updatedAt(acc.getUpdatedAt())
                    .build();
        }).collect(Collectors.toList());

        return new PageResponse<>(content, accountPage.getTotalElements(), accountPage.getTotalPages());
    }

    @Transactional
    public AccountResponse updateAccount(Integer id, AccountUpdateRequest request) {
        Account account = accountRepo.findById(id).orElseThrow(() -> new BusinessException("Account not found"));
        account.setRoleId(request.getRoleId());
        account.setStatus(GeneralStatus.fromString(request.getStatus()));
        if (request.getAccountType() != null) {
            account.setAccountType(AccountType.valueOf(request.getAccountType()));
        }
        account = accountRepo.save(account);
        String roleName = roleRepo.findById(account.getRoleId()).map(Role::getRoleName).orElse(null);
        
        return AccountResponse.builder()
                .id(account.getId())
                .username(account.getUsername())
                .roleId(account.getRoleId())
                .roleName(roleName)
                .accountType(account.getAccountType() != null ? account.getAccountType().name() : null)
                .status(account.getStatus().toString())
                .build();
    }
}
