package com.smartparking.operation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteDraftsRequest {

    @NotEmpty(message = "Danh sách xe cần xóa khỏi giỏ hàng không được để trống!")
    private List<Integer> draftIds;

}