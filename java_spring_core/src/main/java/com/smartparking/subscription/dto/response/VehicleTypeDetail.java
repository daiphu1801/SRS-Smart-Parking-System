package com.smartparking.subscription.dto.response;

import com.smartparking.subscription.entity.PackagePrice;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VehicleTypeDetail {
    private Integer pkgVehTypeId; // Là cái ID của bảng PackageVehicleType
    private Integer vehicleTypeId;
    private String vehicleTypeCode;
    private String vehicleTypeName;
    private Integer maxQuantity;

    // Chứa danh sách các mức giá của xe này
    private List<PackagePrice> prices;
}
