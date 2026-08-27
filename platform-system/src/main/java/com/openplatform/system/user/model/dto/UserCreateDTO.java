package com.openplatform.system.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.ToString;

/** 用户创建参数。 */
@Data
public class UserCreateDTO {
    @NotBlank @Size(max = 25) private String username;
    @NotBlank @Size(min = 8, max = 128) @ToString.Exclude private String password;
    @NotBlank @Size(max = 25) private String displayName;
    @NotNull private Long departmentId;
    @Email(message = "邮箱格式不正确") private String email;
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确") private String phone;
    private Boolean enabled = true;
}
