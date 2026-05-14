package com.smartparking.operation.dto.response;

import com.smartparking.operation.entity.ZoneType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class ZoneTreeResponse {
    private Integer id;
    private Integer parentZoneId;
    private String zoneName;
    private ZoneType zoneType;
    private Integer capacity;
    private Integer currentOccupancy;

    // Mấu chốt của sơ đồ cây nằm ở đây
    @Builder.Default
    private List<ZoneTreeResponse> children = new ArrayList<>();
}
