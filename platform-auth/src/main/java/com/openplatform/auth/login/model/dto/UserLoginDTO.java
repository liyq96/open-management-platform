package com.openplatform.auth.login.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 用户登录参数。
 */
@Getter
@Setter
public class UserLoginDTO {

    @NotBlank
    @Size(max = 25)
    private String tenantCode;

    @NotBlank
    @Size(max = 25)
    private String username;

    @NotBlank
    @Size(max = 128)
    @ToString.Exclude
    private String password;

    @NotBlank
    @Size(max = 64)
    private String captchaId;

    @NotBlank
    @Size(min = 4, max = 8)
    private String captchaCode;
}
