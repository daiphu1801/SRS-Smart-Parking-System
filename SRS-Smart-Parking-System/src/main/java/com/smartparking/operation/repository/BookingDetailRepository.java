package com.smartparking.operation.repository;

import com.smartparking.operation.dto.BookingDetailDto;
import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.subscription.entity.PackageVehicleType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Integer>, JpaSpecificationExecutor<BookingDetail> {
        @Query("SELECT new com.smartparking.operation.dto.BookingDetailDto(" +
                "bd.id, " +
                "bd.bookingId, " +
                "c.id, c.phone, c.fullName, " +
                "p.id, p.price, p.packagePriceName, p.durationMonths, " +
                "bd.vehicleNo, bd.startDate, bd.endDate, bd.status, bd.createdAt, " +
                "vt.id, vt.typeName) " +
                "FROM BookingDetail bd " +
                "LEFT JOIN Customer c ON bd.customerId = c.id " +
                "LEFT JOIN PackagePrice p ON bd.packagePriceId = p.id " +
                "LEFT JOIN PackageVehicleType pvt ON p.pkgVehTypeId = pvt.id " +
                "LEFT JOIN VehicleType vt ON pvt.vehicleTypeId = vt.id " +
                "WHERE bd.bookingId = :bookingId " +
                "AND bd.status IN :statuses") // Thêm đúng 1 chốt chặn này
        List<BookingDetailDto> findBookingDetailsWithJoinByBookingIdAndStatusIn(
                @Param("bookingId") Integer bookingId,
                @Param("statuses") List<BookingStatus> statuses);

        @Query("SELECT new com.smartparking.operation.dto.BookingDetailDto(" +
                "bd.id, " +
                "bd.bookingId, " +
                "c.id, c.phone, c.fullName, " +
                "p.id, p.price, p.packagePriceName, p.durationMonths, " +
                "bd.vehicleNo, bd.startDate, bd.endDate, bd.status, bd.createdAt, " +
                "vt.id, vt.typeName) " + // Thêm 2 tham số này vào cuối
                "FROM BookingDetail bd " +
                "LEFT JOIN Customer c ON bd.customerId = c.id " +
                "LEFT JOIN PackagePrice p ON bd.packagePriceId = p.id " +
                "LEFT JOIN PackageVehicleType pvt ON p.pkgVehTypeId = pvt.id " +
                "LEFT JOIN VehicleType vt ON pvt.vehicleTypeId = vt.id " +// Thêm dòng JOIN này
                "WHERE bd.id = :id") // Lưu ý điều kiện của hàm này là bd.id nhé
        Optional<BookingDetailDto> findDtoById(@Param("id") Integer id);

        @Query("SELECT new com.smartparking.operation.dto.BookingDetailDto(" +
                "bd.id, " +
                "bd.bookingId, " +
                "c.id, c.phone, c.fullName, " +
                "p.id, p.price, p.packagePriceName, p.durationMonths, " +
                "bd.vehicleNo, bd.startDate, bd.endDate, bd.status, bd.createdAt, " +
                "vt.id, vt.typeName) " + // Thêm 2 tham số này vào cuối
                "FROM BookingDetail bd " +
                "LEFT JOIN Customer c ON bd.customerId = c.id " +
                "LEFT JOIN PackagePrice p ON bd.packagePriceId = p.id " +
                "LEFT JOIN PackageVehicleType pvt ON p.pkgVehTypeId = pvt.id " +
                "LEFT JOIN VehicleType vt ON pvt.vehicleTypeId = vt.id " )
        Page<BookingDetailDto> findListDto(Pageable pageable);


        @Modifying
        @Query("UPDATE BookingDetail b SET b.status = 'ACTIVE' WHERE b.id IN :ids")
        void updateStatusToActive(@Param("ids") List<Integer> ids);

        @Modifying
        @Query("UPDATE BookingDetail b SET b.status = 'CANCELED' WHERE b.id IN :ids")
        int updateStatusToCanceled(@Param("ids") List<Integer> ids);

        @Query("SELECT b.vehicleNo, MAX(b.endDate) FROM BookingDetail b " +
                "WHERE b.vehicleNo IN :vehicleNos AND b.status = :status " +
                "GROUP BY b.vehicleNo")
        List<Object[]> findMaxEndDatesByVehicleNos(
                @Param("vehicleNos") List<String> vehicleNos,
                @Param("status") BookingStatus status);

        @Query("SELECT MAX(b.endDate) FROM BookingDetail b " +
                "WHERE b.vehicleNo = :vehicleNo AND b.status = :status")
        Optional<LocalDateTime> findMaxEndDateByVehicleNo(
                @Param("vehicleNo") String vehicleNo,
                @Param("status") BookingStatus status);

        Optional<BookingDetail> findFirstByVehicleNoAndStatus(String vehicleNo, BookingStatus status);

        boolean existsByVehicleNoAndStatusNotIn(String vehicleNo, List<BookingStatus> statuses);

        // 1. Dùng để lấy DTO trả về cho API xem giỏ hàng (Kế thừa cấu trúc SELECT cũ của ông)
        @Query("SELECT new com.smartparking.operation.dto.BookingDetailDto(" +
                "bd.id, " +
                "bd.bookingId, " +
                "c.id, c.phone, c.fullName, " +
                "p.id, p.price, p.packagePriceName, p.durationMonths, " +
                "bd.vehicleNo, bd.startDate, bd.endDate, bd.status, bd.createdAt, " +
                "vt.id, vt.typeName) " +
                "FROM BookingDetail bd " +
                "LEFT JOIN Customer c ON bd.customerId = c.id " +
                "LEFT JOIN PackagePrice p ON bd.packagePriceId = p.id " +
                "LEFT JOIN PackageVehicleType pvt ON p.pkgVehTypeId = pvt.id " +
                "LEFT JOIN VehicleType vt ON pvt.vehicleTypeId = vt.id " +
                "WHERE bd.bookingId = :bookingId AND bd.status = :status")
        List<BookingDetailDto> findDtoByBookingIdAndStatus(
                @Param("bookingId") Integer bookingId,
                @Param("status") BookingStatus status);

        // 2. Dùng để lấy List Entity để thực hiện việc update trạng thái (xóa mềm)
        List<BookingDetail> findByBookingIdAndStatus(Integer bookingId, BookingStatus status);

        @Query("SELECT COUNT(DISTINCT bd.vehicleNo) FROM BookingDetail bd " +
                "JOIN PackagePrice pp ON bd.packagePriceId = pp.id " +
                "WHERE pp.pkgVehTypeId = :pkgVehTypeId " +
                "AND bd.booking.groupId = :groupId " +
                "AND bd.status NOT IN :excludedStatuses")
        long countDistinctVehiclesInUse(
                @Param("pkgVehTypeId") Integer pkgVehTypeId,
                @Param("groupId") Integer groupId,
                @Param("excludedStatuses") List<BookingStatus> excludedStatuses);

        @Query("SELECT COUNT(DISTINCT bd.vehicleNo) FROM BookingDetail bd " +
                "JOIN PackagePrice pp ON bd.packagePriceId = pp.id " +
                "WHERE pp.pkgVehTypeId = :pkgVehTypeId " +
                "AND bd.booking.groupId = :groupId " +
                "AND bd.status NOT IN :excludedStatuses " +
                "AND bd.vehicleNo != :currentVehicleNo")
        long countOtherDistinctVehiclesInUse(
                @Param("pkgVehTypeId") Integer pkgVehTypeId,
                @Param("groupId") Integer groupId,
                @Param("excludedStatuses") List<BookingStatus> excludedStatuses,
                @Param("currentVehicleNo") String currentVehicleNo);

        @Modifying
        @Query("UPDATE BookingDetail b SET b.status = :newStatus WHERE b.status = :oldStatus AND b.endDate < :time")
        int updateExpiredBookings(@Param("oldStatus") BookingStatus oldStatus,
                                  @Param("newStatus") BookingStatus newStatus,
                                  @Param("time") LocalDateTime time);

        // 2. KÍCH HOẠT GÓI CƯỚC: Nếu đang PENDING_ACTIVATION và startDate <= mốc thời gian
        @Modifying
        @Query("UPDATE BookingDetail b SET b.status = :newStatus WHERE b.status = :oldStatus AND b.startDate <= :time")
        int updateActiveBookings(@Param("oldStatus") BookingStatus oldStatus,
                                 @Param("newStatus") BookingStatus newStatus,
                                 @Param("time") LocalDateTime time);

        @Query("SELECT new com.smartparking.operation.dto.BookingDetailDto(" +
                "bd.id, " +
                "bd.bookingId, " +
                "c.id, c.phone, c.fullName, " +
                "p.id, p.price, p.packagePriceName, p.durationMonths, " +
                "bd.vehicleNo, bd.startDate, bd.endDate, bd.status, bd.createdAt, " +
                "vt.id, vt.typeName) " +
                "FROM BookingDetail bd " +
                "LEFT JOIN Customer c ON bd.customerId = c.id " +
                "LEFT JOIN PackagePrice p ON bd.packagePriceId = p.id " +
                "LEFT JOIN PackageVehicleType pvt ON p.pkgVehTypeId = pvt.id " +
                "LEFT JOIN VehicleType vt ON pvt.vehicleTypeId = vt.id " +
                "WHERE bd.customerId = :customerId AND bd.status = :status " +
                "ORDER BY bd.endDate ASC")
        List<BookingDetailDto> findDtoByCustomerIdAndStatus(
                @Param("customerId") Integer customerId,
                @Param("status") BookingStatus status);

        @Query("SELECT COUNT(bd) FROM BookingDetail bd " +
                "WHERE bd.customerId = :customerId AND bd.status IN :statuses")
        long countByCustomerIdAndStatusIn(
                @Param("customerId") Integer customerId,
                @Param("statuses") List<BookingStatus> statuses);


        @Query("SELECT new com.smartparking.operation.dto.BookingDetailDto(" +
                "bd.id, " +
                "bd.bookingId, " +
                "c.id, c.phone, c.fullName, " +
                "p.id, p.price, p.packagePriceName, p.durationMonths, " +
                "bd.vehicleNo, bd.startDate, bd.endDate, bd.status, bd.createdAt, " +
                "vt.id, vt.typeName) " +
                "FROM BookingDetail bd " +
                "LEFT JOIN Customer c ON bd.customerId = c.id " +
                "LEFT JOIN PackagePrice p ON bd.packagePriceId = p.id " +
                "LEFT JOIN PackageVehicleType pvt ON p.pkgVehTypeId = pvt.id " +
                "LEFT JOIN VehicleType vt ON pvt.vehicleTypeId = vt.id " +
                "WHERE bd.bookingId = :bookingId")
        List<BookingDetailDto> findBookingDetailsWithJoinByBookingId(@Param("bookingId") Integer bookingId);

        @Query("SELECT new com.smartparking.operation.dto.BookingDetailDto(" +
                "bd.id, " +
                "bd.bookingId, " +
                "c.id, c.phone, c.fullName, " +
                "p.id, p.price, p.packagePriceName, p.durationMonths, " +
                "bd.vehicleNo, bd.startDate, bd.endDate, bd.status, bd.createdAt, " +
                "vt.id, vt.typeName) " +
                "FROM BookingDetail bd " +
                "LEFT JOIN Customer c ON bd.customerId = c.id " +
                "LEFT JOIN PackagePrice p ON bd.packagePriceId = p.id " +
                "LEFT JOIN PackageVehicleType pvt ON p.pkgVehTypeId = pvt.id " +
                "LEFT JOIN VehicleType vt ON pvt.vehicleTypeId = vt.id " +
                "WHERE bd.id = :detailId " +
                "AND bd.booking.groupId = :groupId")
        Optional<BookingDetailDto> findDtoByIdAndGroupId(
                @Param("detailId") Integer detailId,
                @Param("groupId") Integer groupId);

        @Query("SELECT bd FROM BookingDetail bd " +
                "JOIN FETCH bd.customer " + // SỬA: Thêm dòng này để ép nó lấy luôn thông tin Customer
                "WHERE bd.status = :status " +
                "AND bd.endDate >= :startDate " +
                "AND bd.endDate <= :endDate")
        List<BookingDetail> findExpiringBookings(
                @Param("status") BookingStatus status,
                @Param("startDate") LocalDateTime startDate,
                @Param("endDate") LocalDateTime endDate);
}
