package com.openplatform.system.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 管理员重置用户密码参数。 */
@Getter
@Setter
public class UserPasswordResetDTO {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(min = 8, max = 128)
    @ToString.Exclude
    private String newPassword;

    @NotBlank
    @Size(min = 8, max = 128)
    @ToString.Exclude
    private String confirmPassword;
}
