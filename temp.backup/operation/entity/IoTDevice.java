package com.smartparking.operation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "iot_devices")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class IoTDevice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column(name = "zone_id_from") private Integer zoneIdFrom;
    @Column(name = "zone_id_to") private Integer zoneIdTo;
    @Column(name = "device_code", unique = true, length = 50) private String deviceCode;
    @Column(name = "device_name", length = 100) private String deviceName;
    @Column(name = "ip_address", length = 20) private String ipAddress;
    @Enumerated(EnumType.STRING) @Column(name = "device_type") private DeviceType deviceType;
    @Enumerated(EnumType.STRING) private Direction direction;
    @Enumerated(EnumType.STRING) @Builder.Default private DeviceStatus status = DeviceStatus.OFFLINE;
    @Column(name = "last_ping") private LocalDateTime lastPing;
}
