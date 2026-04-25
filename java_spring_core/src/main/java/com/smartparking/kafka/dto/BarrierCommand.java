package com.smartparking.kafka.dto;

import lombok.Builder;
import lombok.Data;

/** Lệnh mở/đóng barie gửi về Python Edge */
@Data
@Builder
public class BarrierCommand {
    private String deviceId;
    private String command;     // "OPEN" | "DENY"
    private Long sessionId;
    private String reason;

    public static BarrierCommand open(String deviceId, Long sessionId, String reason) {
        return BarrierCommand.builder()
            .deviceId(deviceId).command("OPEN")
            .sessionId(sessionId).reason(reason).build();
    }

    public static BarrierCommand deny(String deviceId, Long sessionId, String reason) {
        return BarrierCommand.builder()
            .deviceId(deviceId).command("DENY")
            .sessionId(sessionId).reason(reason).build();
    }
}
