package com.smartparking.identity.dto.response;
import lombok.Builder;
import lombok.Data;
import java.util.List;

// 1. DTO cho ma trận Tạo Role
@Data
@Builder
public class SystemFunctionActionResponse {
    private List<FunctionDto> functions;
    private List<ActionDto> actions;

    @Data @Builder
    public static class FunctionDto {
        private Integer id;
        private String code;
        private String name; // VD: "Quản lý thanh toán"
    }

    @Data @Builder
    public static class ActionDto {
        private Integer id;
        private String code;
        private String name; // VD: "Tạo mới"
    }
}

