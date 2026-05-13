package com.smartparking.payment.controller.admin;

import com.smartparking.operation.entity.DayType;
import com.smartparking.payment.entity.TariffRule;
import com.smartparking.payment.service.TariffRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/tariff-rules")
@RequiredArgsConstructor
public class TariffRuleAdminController {

    private final TariffRuleService tariffRuleService;

    @GetMapping
    public ResponseEntity<List<TariffRule>> list(
            @RequestParam(required = false) Integer vehicleTypeId,
            @RequestParam(required = false) DayType dayType,
            @RequestParam(required = false) Boolean isActive
    ) {
        return ResponseEntity.ok(tariffRuleService.list(vehicleTypeId, dayType, isActive));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TariffRule body) {
        try {
            TariffRule created = tariffRuleService.create(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody TariffRule body) {
        try {
            return tariffRuleService.update(id, body)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        boolean deleted = tariffRuleService.delete(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
