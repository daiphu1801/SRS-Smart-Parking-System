package com.smartparking.operation.repository;

import com.smartparking.operation.dto.BookingDetailDto;
import com.smartparking.operation.entity.BookingDetail;
import com.smartparking.operation.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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
                "c.id, c.phone, c.fullName, " + // 3, 4, 5: Customer info
                "p.id, p.price, p.packagePriceName, p.durationMonths, " + // 6, 7, 8, 9: Package info (Đã thêm p.price vào đây)
                "bd.vehicleNo, bd.startDate, bd.endDate, bd.status, bd.createdAt) " + // 10, 11, 12, 13, 14: Booking detail info
                "FROM BookingDetail bd " +
                "LEFT JOIN Customer c ON bd.customerId = c.id " +
                "LEFT JOIN PackagePrice p ON bd.packagePriceId = p.id " +
                "WHERE bd.bookingId = :bookingId")
        List<BookingDetailDto> findBookingDetailsWithJoinByBookingId(@Param("bookingId") Integer bookingId);

        @Query("SELECT new com.smartparking.operation.dto.BookingDetailDto(" +
                "bd.id, " +
                "bd.bookingId, " +
                "c.id, c.phone, c.fullName, " +
                "p.id, p.price, p.packagePriceName, p.durationMonths, " + // Thêm p.price
                "bd.vehicleNo, bd.startDate, bd.endDate, bd.status, bd.createdAt) " +
                "FROM BookingDetail bd " +
                "LEFT JOIN Customer c ON bd.customerId = c.id " +
                "LEFT JOIN PackagePrice p ON bd.packagePriceId = p.id " +
                "WHERE bd.id = :id")
        Optional<BookingDetailDto> findDtoById(@Param("id") Integer id);

        @Query("SELECT new com.smartparking.operation.dto.BookingDetailDto(" +
                "bd.id, " +
                "bd.bookingId, " +
                "c.id, c.phone, c.fullName, " +
                "p.id, p.price, p.packagePriceName, p.durationMonths, " + // Thêm p.price
                "bd.vehicleNo, bd.startDate, bd.endDate, bd.status, bd.createdAt) " +
                "FROM BookingDetail bd " +
                "LEFT JOIN Customer c ON bd.customerId = c.id " +
                "LEFT JOIN PackagePrice p ON bd.packagePriceId = p.id ")
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
                "bd.id, bd.bookingId, c.id, c.phone, c.fullName, " +
                "p.id, p.price, p.packagePriceName, p.durationMonths, " +
                "bd.vehicleNo, bd.startDate, bd.endDate, bd.status, bd.createdAt) " +
                "FROM BookingDetail bd " +
                "LEFT JOIN Customer c ON bd.customerId = c.id " +
                "LEFT JOIN PackagePrice p ON bd.packagePriceId = p.id " +
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
}
