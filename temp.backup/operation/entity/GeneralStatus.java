package com.smartparking.operation.entity;

public enum GeneralStatus {
    ACTIVE, LOCKED, PENDING;
    public static GeneralStatus fromString(String status) {
        for (GeneralStatus s : GeneralStatus.values()) {
            if (s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        // Trả về mặc định hoặc báo lỗi tùy bạn
        return ACTIVE;
    }
}
