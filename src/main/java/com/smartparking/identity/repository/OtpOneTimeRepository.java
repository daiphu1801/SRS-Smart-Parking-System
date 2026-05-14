package com.smartparking.identity.repository;

import com.smartparking.identity.entity.OtpOneTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpOneTimeRepository extends JpaRepository<OtpOneTime, Long> {

    // Lấy mã OTP mới nhất, chưa sử dụng theo Số điện thoại
    Optional<OtpOneTime> findTopByPhoneAndIsUsedFalseOrderByIdDesc(String phone);

    // Hủy toàn bộ mã cũ chưa dùng của một SĐT
    @Modifying
    @Query("UPDATE OtpOneTime o SET o.isUsed = true WHERE o.phone = :phone AND o.isUsed = false")
    void invalidateOldOtps(@Param("phone") String phone);
}