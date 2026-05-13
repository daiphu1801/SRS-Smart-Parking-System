package com.smartparking.subscription.service;

import com.smartparking.identity.entity.GroupsCustomer;
import com.smartparking.identity.repository.GroupsCustomersRepository;
import com.smartparking.subscription.dto.BookingMetadataDto.AllowedVehicleTypeResponse;
import com.smartparking.subscription.dto.BookingMetadataDto.AvailablePackagePriceResponse;
import com.smartparking.subscription.entity.Package;
import com.smartparking.operation.repository.*;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.subscription.entity.PackagePrice;
import com.smartparking.subscription.entity.PackageVehicleType;
import com.smartparking.subscription.entity.VehicleType;
import com.smartparking.subscription.repository.PackagePriceRepository;
import com.smartparking.subscription.repository.PackageRepository;
import com.smartparking.subscription.repository.PackageVehicleTypeRepository;
import com.smartparking.subscription.repository.VehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerMetadataService {

    private final GroupsCustomersRepository groupRepo;
    private final PackageRepository packageRepo;
    private final PackageVehicleTypeRepository pvtRepo;
    private final VehicleTypeRepository vehicleTypeRepo; // Bảng danh mục loại xe (Ô tô, Xe máy...)
    private final PackagePriceRepository packagePriceRepo;
    private final BookingDetailRepository bookingDetailRepo;

    // Danh sách trạng thái KHÔNG ĐƯỢC tính vào hạn ngạch (Giống hệt hàm Validation cũ)
    private final List<BookingStatus> EXCLUDED_STATUSES = Arrays.asList(BookingStatus.CANCELED, BookingStatus.EXPIRED,BookingStatus.COMPLETE);

    /**
     * API 1: LẤY DANH SÁCH LOẠI XE KÈM HẠN NGẠCH (QUOTA)
     */
    @Transactional(readOnly = true)
    public List<AllowedVehicleTypeResponse> getAllowedVehicleTypes(Integer groupId) {

        // 1. Lấy thông tin Group và Package
        GroupsCustomer group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bãi đỗ (Group)!"));

        Package parkingPackage = packageRepo.findByProfileId(group.getProfileId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói cước cấu hình cho bãi đỗ này!"));

        // 2. Lấy tất cả các loại xe được phép trong gói này
        List<PackageVehicleType> pvtList = pvtRepo.findByPackageId(parkingPackage.getId());

        List<AllowedVehicleTypeResponse> responses = new ArrayList<>();

        // 3. Vòng lặp đếm Quota cho từng loại xe
        // Lưu ý: Thường 1 bãi chỉ có 2-3 loại xe (Ô tô, Xe máy, Xe đạp) nên vòng lặp này cực nhẹ, không lo N+1
        for (PackageVehicleType pvt : pvtList) {

            VehicleType vt = vehicleTypeRepo.findById(pvt.getVehicleTypeId())
                    .orElseThrow(() -> new RuntimeException("Dữ liệu loại xe bị lỗi!"));

            // Dùng lại đúng hàm đếm Tối ưu đã viết ở các bước trước
            long currentCount = bookingDetailRepo.countDistinctVehiclesInUse(
                    pvt.getId(),
                    groupId,
                    EXCLUDED_STATUSES
            );

            responses.add(AllowedVehicleTypeResponse.builder()
                    .vehicleTypeId(vt.getId())
                    .vehicleTypeName(vt.getTypeName())
                    .currentQuantity((int) currentCount)
                    .maxQuantity(pvt.getMaxQuantity())
                    .build());
        }

        return responses;
    }

    /**
     * API 2: LẤY DANH SÁCH MỨC GIÁ SAU KHI CHỌN LOẠI XE
     */
    @Transactional(readOnly = true)
    public List<AvailablePackagePriceResponse> getAvailablePackages(Integer groupId, Integer vehicleTypeId) {

        // 1. Kiểm tra tuyến đầu: Lấy Group và Package
        GroupsCustomer group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin bãi đỗ!"));

        Package parkingPackage = packageRepo.findByProfileId(group.getProfileId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gói cước cấu hình cho bãi đỗ!"));

        // 2. Tìm đúng cấu hình của loại xe khách vừa chọn
        PackageVehicleType pvt = pvtRepo.findByPackageIdAndVehicleTypeId(parkingPackage.getId(), vehicleTypeId)
                .orElseThrow(() -> new RuntimeException("Loại phương tiện này không được hỗ trợ tại bãi đỗ!"));

        // ==========================================
        // 3. CHỐT CHẶN BẢO MẬT GIAO DIỆN (Tránh khách dùng Postman gọi thẳng API qua mặt Frontend)
        // ==========================================
        long currentCount = bookingDetailRepo.countDistinctVehiclesInUse(
                pvt.getId(),
                groupId,
                EXCLUDED_STATUSES
        );

        if (currentCount >= pvt.getMaxQuantity()) {
            throw new RuntimeException("Bạn đã đạt giới hạn số lượng đăng ký cho loại phương tiện này!");
        }

        // 4. Nếu còn Slot, móc danh sách giá (chỉ lấy những giá đang Active)
        List<PackagePrice> activePrices = packagePriceRepo.findByPkgVehTypeIdAndIsActiveTrue(pvt.getId());

        if (activePrices.isEmpty()) {
            throw new RuntimeException("Loại phương tiện này hiện đang tạm ngừng cung cấp gói cước mới!");
        }

        // 5. Map sang DTO trả về cho Frontend
        return activePrices.stream()
                .map(price -> AvailablePackagePriceResponse.builder()
                        .packagePriceId(price.getId())
                        .packagePriceName(price.getPackagePriceName())
                        .price(price.getPrice())
                        .durationMonths(price.getDurationMonths())
                        .build())
                .collect(Collectors.toList());
    }
}