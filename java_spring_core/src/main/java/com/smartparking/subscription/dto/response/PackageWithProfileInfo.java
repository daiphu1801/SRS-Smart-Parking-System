package com.smartparking.subscription.dto.response;

public interface PackageWithProfileInfo {
    Integer getPackageId();
    Integer getProfileId();
    String getProfileName(); // Cột ăn ké từ bảng GroupProfile
    String getPackageCode();
    String getPackageName();
    String getDescription();
}