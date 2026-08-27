package com.openplatform.system.user.model.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class UserInfoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldAcceptValidPhoneAndEmail() {
        UserCreateDTO dto = validDto();
        dto.setPhone("13800138000");
        dto.setEmail("admin@example.com");

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void shouldRejectInvalidPhone() {
        UserCreateDTO dto = validDto();
        dto.setPhone("12345");

        assertFalse(validator.validate(dto).isEmpty());
    }

    @Test
    void shouldRejectInvalidEmail() {
        UserCreateDTO dto = validDto();
        dto.setEmail("invalid-email");

        assertFalse(validator.validate(dto).isEmpty());
    }

    private UserCreateDTO validDto() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("admin");
        dto.setPassword("12345678");
        dto.setDisplayName("管理员");
        dto.setDepartmentId(1L);
        dto.setEnabled(true);
        return dto;
    }
}
