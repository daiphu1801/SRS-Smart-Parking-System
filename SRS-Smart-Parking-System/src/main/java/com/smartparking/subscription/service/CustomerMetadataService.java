package com.smartparking.subscription.service;

import com.smartparking.identity.entity.GroupsCustomer;
import com.smartparking.identity.repository.GroupsCustomersRepository;
import com.smartparking.shared.exception.BusinessException;
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
import java.util.Map;
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

    private final List<BookingStatus> EXCLUDED_STATUSES = Arrays.asList(BookingStatus.CANCELED, BookingStatus.EXPIRED,BookingStatus.COMPLETE);


    @Transactional(readOnly = true)
    public List<AllowedVehicleTypeResponse> getAllowedVehicleTypes(Integer groupId) {

        GroupsCustomer group = groupRepo.findById(groupId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin bãi đỗ (Group)!"));

        Package parkingPackage = packageRepo.findByProfileId(group.getProfileId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy gói cước cấu hình cho bãi đỗ này!"));

        List<PackageVehicleType> pvtList = pvtRepo.findByPackageId(parkingPackage.getId());

        List<AllowedVehicleTypeResponse> responses = new ArrayList<>();


        List<Integer> vehicleTypeIds = pvtList.stream()
                .map(PackageVehicleType::getVehicleTypeId)
                .toList();

        Map<Integer, VehicleType> vtMap = vehicleTypeRepo.findAllById(vehicleTypeIds).stream()
                .collect(Collectors.toMap(VehicleType::getId, vt -> vt));

        for (PackageVehicleType pvt : pvtList) {

            VehicleType vt = vtMap.get(pvt.getVehicleTypeId());
            if (vt == null) throw new RuntimeException("Dữ liệu loại xe bị lỗi!");;

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


    @Transactional(readOnly = true)
    public List<AvailablePackagePriceResponse> getAvailablePackages(Integer groupId, Integer vehicleTypeId) {

        GroupsCustomer group = groupRepo.findById(groupId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy thông tin bãi đỗ!"));

        Package parkingPackage = packageRepo.findByProfileId(group.getProfileId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy gói cước cấu hình cho bãi đỗ!"));

        PackageVehicleType pvt = pvtRepo.findByPackageIdAndVehicleTypeId(parkingPackage.getId(), vehicleTypeId)
                .orElseThrow(() -> new BusinessException("Loại phương tiện này không được hỗ trợ tại bãi đỗ!"));

        long currentCount = bookingDetailRepo.countDistinctVehiclesInUse(
                pvt.getId(),
                groupId,
                EXCLUDED_STATUSES
        );

        if (currentCount >= pvt.getMaxQuantity()) {
            throw new BusinessException("Bạn đã đạt giới hạn số lượng đăng ký cho loại phương tiện này!");
        }

        List<PackagePrice> activePrices = packagePriceRepo.findByPkgVehTypeIdAndIsActiveTrue(pvt.getId());

        if (activePrices.isEmpty()) {
            throw new BusinessException("Loại phương tiện này hiện đang tạm ngừng cung cấp gói cước mới!");
        }

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