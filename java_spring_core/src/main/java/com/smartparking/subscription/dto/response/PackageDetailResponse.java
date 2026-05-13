package com.smartparking.subscription.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PackageDetailResponse {
    private Integer packageId;
    private Integer profileId;
    private String  profileName;
    private String packageCode;
    private String packageName;
    private String description;

    // Chứa danh sách các loại xe trong gói này
    private List<VehicleTypeDetail> vehicleTypes;
}

