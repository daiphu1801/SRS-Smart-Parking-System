package com.smartparking.operation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RenewalBookingRequest {

    @NotEmpty(message = "Danh sách xe cần gia hạn không được để trống!")
    @Valid // Cái này để nó check validate cho từng thằng con bên trong list
    private List<RenewItemRequest> items;

}