package com.smartparking.identity.dto.request;

import com.smartparking.identity.entity.OtpType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendOtpRequest {

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(\\+84|0)[0-9]{8,10}$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    // Nếu ông muốn mở rộng cho luồng Đăng ký, Quên mật khẩu,... thì truyền Type lên,
    // Nếu không thì xóa biến này đi, ép cứng trong Service cũng được.
    private OtpType type;
}