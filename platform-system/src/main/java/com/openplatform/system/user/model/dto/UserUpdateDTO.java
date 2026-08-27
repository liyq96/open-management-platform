package com.openplatform.system.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 用户修改参数。 */
@Data
public class UserUpdateDTO {
    @NotNull private Long userId;
    @NotBlank @Size(max = 25) private String displayName;
    @NotNull private Long departmentId;
    @Email(message = "邮箱格式不正确") private String email;
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确") private String phone;
    @NotNull private Boolean enabled;
}
