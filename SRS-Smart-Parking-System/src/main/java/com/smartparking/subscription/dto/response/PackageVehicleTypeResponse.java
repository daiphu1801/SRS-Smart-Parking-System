package com.smartparking.subscription.dto.response;

public interface PackageVehicleTypeResponse {
    Integer getId();
    Integer getPackageId();
    Integer getVehicleTypeId();
    Integer getMaxQuantity();
    String getVehicleTypeCode(); // JOIN từ bảng VehicleType
    String getVehicleTypeName(); // JOIN từ bảng VehicleType
}