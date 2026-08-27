package com.openplatform.system.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/** 当前用户修改密码参数。 */
@Getter
@Setter
public class UserPasswordChangeDTO {

    @NotBlank
    @Size(max = 128)
    @ToString.Exclude
    private String oldPassword;

    @NotBlank
    @Size(min = 8, max = 128)
    @ToString.Exclude
    private String newPassword;

    @NotBlank
    @Size(min = 8, max = 128)
    @ToString.Exclude
    private String confirmPassword;
}
