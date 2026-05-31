package com.smartparking.shared.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ZoneTransitionEvent {
    private Integer deviceId;
    private Integer zoneFromId;
    private Integer zoneToId;
    private Long timestamp;
}