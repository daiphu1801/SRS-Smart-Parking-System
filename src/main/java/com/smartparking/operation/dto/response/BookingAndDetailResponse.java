package com.smartparking.operation.dto.response;

import com.smartparking.operation.dto.BookingDetailDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class BookingAndDetailResponse {
    private BookingResponse bookingInfo;
    private List<BookingDetailDto> details;
}