package com.openplatform.system.permission.model.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openplatform.system.permission.model.enums.PermissionType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PermissionCreateDTOTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldAcceptCommonPermissionCode() {
        PermissionCreateDTO dto = validDto("system:user:list");

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void shouldAcceptPermissionCodeWithoutFormatRestriction() {
        PermissionCreateDTO dto = validDto("user_list");

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void shouldAcceptUppercasePermissionCode() {
        PermissionCreateDTO dto = validDto("SYSTEM:USER:LIST");

        assertTrue(validator.validate(dto).isEmpty());
    }

    @Test
    void shouldRejectPermissionCodeLongerThanTwentyFiveCharacters() {
        PermissionCreateDTO dto = validDto("12345678901234567890123456");

        assertFalse(validator.validate(dto).isEmpty());
    }

    private PermissionCreateDTO validDto(String permissionCode) {
        PermissionCreateDTO dto = new PermissionCreateDTO();
        dto.setGroupId(1L);
        dto.setPermissionCode(permissionCode);
        dto.setPermissionName("用户查询");
        dto.setPermissionType(PermissionType.API);
        dto.setEnabled(true);
        return dto;
    }
}
