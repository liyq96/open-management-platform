package com.openplatform.system.permission.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openplatform.system.permission.model.entity.PermissionInfo;
import com.openplatform.system.permission.model.enums.PermissionType;
import com.openplatform.system.permission.model.vo.PermissionInfoVO;
import org.junit.jupiter.api.Test;

class PermissionInfoConverterTest {

    @Test
    void shouldConvertDatabasePermissionTypeToEnum() {
        PermissionInfo entity = new PermissionInfo();
        entity.setId(1L);
        entity.setGroupId(2L);
        entity.setPermissionCode("system:user:list");
        entity.setPermissionName("用户查询");
        entity.setPermissionType("API");
        entity.setEnabled(true);

        PermissionInfoVO vo = PermissionInfoConverter.toVO(entity);

        assertEquals(1L, vo.getPermissionId());
        assertEquals(2L, vo.getGroupId());
        assertEquals("system:user:list", vo.getPermissionCode());
        assertEquals(PermissionType.API, vo.getPermissionType());
    }
}
