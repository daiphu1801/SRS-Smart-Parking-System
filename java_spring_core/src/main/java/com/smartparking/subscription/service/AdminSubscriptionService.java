package com.smartparking.subscription.service;

import com.smartparking.identity.repository.GroupsProfileRepository;
import com.smartparking.subscription.dto.response.PackageDetailResponse;
import com.smartparking.subscription.dto.response.PackageVehicleTypeResponse;
import com.smartparking.subscription.dto.response.PackageWithProfileInfo;
import com.smartparking.subscription.dto.response.VehicleTypeDetail;
import com.smartparking.subscription.entity.Package;
import com.smartparking.subscription.entity.PackagePrice;
import com.smartparking.subscription.entity.PackageVehicleType;
import com.smartparking.subscription.entity.VehicleType;
import com.smartparking.subscription.repository.PackagePriceRepository;
import com.smartparking.subscription.repository.PackageRepository;
import com.smartparking.subscription.repository.PackageVehicleTypeRepository;
import com.smartparking.subscription.repository.VehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminSubscriptionService {

    private final PackageRepository packageRepository;
    private final PackagePriceRepository packagePriceRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final PackageVehicleTypeRepository packageVehicleTypeRepository;
    private final GroupsProfileRepository groupsProfileRepository;


    public Page<VehicleType> getVehicleTypes(String search, Pageable pageable) {
        if (StringUtils.hasText(search)) {
            return vehicleTypeRepository.searchByCodeOrName(search.trim(), pageable);
        }
        // Nếu ô search bỏ trống -> Trả về tất cả (có phân trang)
        return vehicleTypeRepository.findAll(pageable);
    }

    public VehicleType getVehicleTypeById(Integer id) {
        return vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy loại phương tiện với ID: " + id));
    }

    @Transactional
    public VehicleType createVehicleType(VehicleType vehicleType) {
        // Bắt lỗi: Không cho phép tạo trùng type_code
        if (vehicleTypeRepository.existsByTypeCode(vehicleType.getTypeCode())) {
            throw new RuntimeException("Lỗi: Mã phương tiện '" + vehicleType.getTypeCode() + "' đã tồn tại!");
        }
        return vehicleTypeRepository.save(vehicleType);
    }

    @Transactional
    public VehicleType updateVehicleType(Integer id, VehicleType vehicleTypeUpdates) {
        VehicleType existing = getVehicleTypeById(id);

        if (!existing.getTypeCode().equalsIgnoreCase(vehicleTypeUpdates.getTypeCode()) &&
                vehicleTypeRepository.existsByTypeCode(vehicleTypeUpdates.getTypeCode())) {
            throw new RuntimeException("Lỗi: Mã phương tiện '" + vehicleTypeUpdates.getTypeCode() + "' đã được sử dụng!");
        }

        existing.setTypeCode(vehicleTypeUpdates.getTypeCode());
        existing.setTypeName(vehicleTypeUpdates.getTypeName());

        return vehicleTypeRepository.save(existing);
    }

    @Transactional
    public void deleteVehicleType(Integer id) {
        VehicleType existing = getVehicleTypeById(id);

        try {
            vehicleTypeRepository.delete(existing);
            vehicleTypeRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Lỗi: Không thể xóa loại xe này vì đang có Gói cước hoặc Bảng giá sử dụng nó!");
        }
    }

    public Page<Package> getPackages(String searchName, Boolean status, Pageable pageable) {
        String finalSearch = StringUtils.hasText(searchName) ?
                "%" + searchName.trim().toLowerCase() + "%" : "%";

        return packageRepository.filterDynamic(finalSearch, status, pageable);
    }

    public Package getPackageById(Integer id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy gói cước với ID: " + id));
    }

    @Transactional
    public Package createPackage(Package pkg) {
        if (packageRepository.existsByPackageCode(pkg.getPackageCode())) {
            throw new RuntimeException("Lỗi: Mã gói cước '" + pkg.getPackageCode() + "' đã tồn tại!");
        }
        return packageRepository.save(pkg);
    }

    @Transactional
    public Package updatePackage(Integer id, Package packageUpdates) {
        Package existing = getPackageById(id);

        if (!existing.getPackageCode().equalsIgnoreCase(packageUpdates.getPackageCode()) &&
                packageRepository.existsByPackageCode(packageUpdates.getPackageCode())) {
            throw new RuntimeException("Lỗi: Mã gói cước '" + packageUpdates.getPackageCode() + "' đã được sử dụng!");
        }

        existing.setPackageCode(packageUpdates.getPackageCode());
        existing.setPackageName(packageUpdates.getPackageName());
        existing.setDescription(packageUpdates.getDescription());
        existing.setIsAvailable(packageUpdates.getIsAvailable());
        existing.setProfileId(packageUpdates.getProfileId());

        return packageRepository.save(existing);
    }

    @Transactional
    public void deletePackage(Integer id) {
        Package existing = getPackageById(id);

        try {
            packageRepository.delete(existing);
            packageRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Lỗi: Không thể xóa Gói cước này vì đã có dữ liệu Bảng Giá hoặc Khách Hàng đang sử dụng!");
        }
    }

    public PackageVehicleType getPackageVehicleTypeById(Integer id) {
        return packageVehicleTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy gói cước với ID: " + id));
    }

    @Transactional
    public PackageVehicleType createPackageVehicleType(PackageVehicleType entity) {
        // Validate: Trong 1 Gói không được add trùng 1 Loại Xe 2 lần
        if (packageVehicleTypeRepository.existsByPackageIdAndVehicleTypeId(
                entity.getPackageId(), entity.getVehicleTypeId())) {
            throw new RuntimeException("Lỗi: Loại phương tiện này đã được cấu hình trong gói cước rồi!");
        }
        return packageVehicleTypeRepository.save(entity);
    }

    @Transactional
    public PackageVehicleType updatePackageVehicleType(Integer id, PackageVehicleType updates) {
        PackageVehicleType existing = getPackageVehicleTypeById(id);

        // Validate: Nếu Admin có đổi sang loại xe khác, check xem loại mới đó đã tồn tại trong gói chưa
        if (!existing.getVehicleTypeId().equals(updates.getVehicleTypeId()) &&
                packageVehicleTypeRepository.existsByPackageIdAndVehicleTypeId(updates.getPackageId(), updates.getVehicleTypeId())) {
            throw new RuntimeException("Lỗi: Loại phương tiện này đã tồn tại trong gói cước, không thể đổi sang!");
        }

        // Đè toàn bộ data mới lên data cũ
        existing.setPackageId(updates.getPackageId());
        existing.setVehicleTypeId(updates.getVehicleTypeId());
        existing.setMaxQuantity(updates.getMaxQuantity());

        return packageVehicleTypeRepository.save(existing);
    }

    public void deletePackageVehicleType(Integer id) {
        PackageVehicleType existing = getPackageVehicleTypeById(id);

        try {
            packageVehicleTypeRepository.delete(existing);
            packageVehicleTypeRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Lỗi: Không thể xóa Gói cước này vì đã có dữ liệu Bảng Giá hoặc Khách Hàng đang sử dụng!");
        }
    }


    public PackagePrice getPackagePriceById(Integer id) {
        return packagePriceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy gói cước với ID: " + id));
    }

    public PackagePrice createPackagePrice(PackagePrice entity) {
        // Chỗ này ông có thể thêm Validate:
        // VD: Không cho phép tạo 2 mức giá cho cùng 1 mốc thời gian (durationDays) trong cùng 1 cấu hình xe.
        // Tạm thời tôi để save bình thường, ông có thể bổ sung logic sau nếu cần.

        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }

        return packagePriceRepository.save(entity);
    }

    @Transactional
    public PackagePrice updatePackagePrice(Integer id, PackagePrice updates) {
        PackagePrice existing = getPackagePriceById(id);

        // Đè toàn bộ data mới lên data cũ
        existing.setPkgVehTypeId(updates.getPkgVehTypeId());
        existing.setDurationMonths(updates.getDurationMonths());
        existing.setPrice(updates.getPrice());
        existing.setIsActive(updates.getIsActive());

        return packagePriceRepository.save(existing);
    }

    public void deletePackagePrice(Integer id) {
        PackagePrice existing = getPackagePriceById(id);

        try {
            packagePriceRepository.delete(existing);
            packagePriceRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Lỗi: Không thể xóa Gói cước này vì đã có dữ liệu Bảng Giá hoặc Khách Hàng đang sử dụng!");
        }
    }


    public PackageDetailResponse getPackageDetails(Integer id) {

        // 1. Lấy thông tin Gói (Nếu ID sai nó sẽ tự văng lỗi ở hàm này luôn rồi)
        PackageWithProfileInfo pkgInfo = packageRepository.findPackageWithProfileById(id)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy gói cước với ID: " + id));


        // 2. Lấy danh sách Loại xe của gói này (Đã JOIN sẵn lấy tên xe)
        List<PackageVehicleTypeResponse> listVehicleTypes = packageVehicleTypeRepository.findByPackageIdWithVehicleInfo(id);

        // 3. Khởi tạo mảng chứa loại xe để trả về
        List<VehicleTypeDetail> vehicleTypeDetails = new ArrayList<>();

        // 4. Lặp qua từng loại xe, đi tìm xem nó có mấy cái Bảng Giá
        for (PackageVehicleTypeResponse vt : listVehicleTypes) {

            // Tìm các mức giá dựa trên pkgVehTypeId
            List<PackagePrice> prices = packagePriceRepository.findByPkgVehTypeId(vt.getId());

            // Lắp ráp vào DTO con
            VehicleTypeDetail detail = VehicleTypeDetail.builder()
                    .pkgVehTypeId(vt.getId())
                    .vehicleTypeId(vt.getVehicleTypeId())
                    .vehicleTypeCode(vt.getVehicleTypeCode())
                    .vehicleTypeName(vt.getVehicleTypeName())
                    .maxQuantity(vt.getMaxQuantity())
                    .prices(prices) // Nhét mảng giá vào đây
                    .build();

            vehicleTypeDetails.add(detail);
        }

        // 5. Đóng gói lại thành 1 cục bự ném cho Frontend
        return PackageDetailResponse.builder()
                .packageId(pkgInfo.getPackageId())
                .profileId(pkgInfo.getProfileId())
                // Xử lý chống Null nếu gói này không thuộc nhóm nào
                .profileName(pkgInfo.getProfileName() != null ? pkgInfo.getProfileName() : "Dùng chung")
                .packageCode(pkgInfo.getPackageCode())
                .packageName(pkgInfo.getPackageName())
                .description(pkgInfo.getDescription())
                .vehicleTypes(vehicleTypeDetails)
                .build();
    }
}
