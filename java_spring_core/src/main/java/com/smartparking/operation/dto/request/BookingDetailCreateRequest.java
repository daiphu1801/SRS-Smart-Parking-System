package com.smartparking.operation.dto.request;

import com.smartparking.operation.entity.BookingStatus;
import com.smartparking.subscription.entity.VehicleType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDetailCreateRequest {
    @NotNull(message = "Lỗi: Vui lòng chọn Nhóm (Booking Id)!")
    private Integer bookingId;
    @NotNull(message = "Lỗi: Vui lòng chọn người sở hữu")
    private Integer customerId;
    @NotNull(message = "Lỗi: Vui lòng chọn gói")
    private Integer packagePriceId;
    private String packagePriceName;
    @NotNull(message = "Lỗi: Vui lòng nhập biển số xe")
    private String vehicleNo;
    @NotNull(message = "Lỗi: Vui lòng không để ngày bắt đầu trống")
    // đoạn này lưu ý startDate nên có thời gian  giờ-phút-giây là 00h00m00s
    private LocalDateTime startDate;
    // đoạn này lưu ý endDate nên có thời gian giờ-phút-giây :  23h59m59s
    private LocalDateTime endDate;
    private BookingStatus status;
    private LocalDateTime createdAt;
    private Integer vehicleTypeId;
}
