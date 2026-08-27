package com.openplatform.system.role.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.openplatform.system.role.model.entity.RoleInfo;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoleInfoConverterTest {
    @Test void shouldConvertRoleAndPermissions(){
        RoleInfo role=new RoleInfo(); role.setId(1L); role.setRoleCode("ADMIN"); role.setRoleName("管理员"); role.setEnabled(true);
        var vo=RoleInfoConverter.toVO(role,List.of(10L,20L));
        assertEquals(List.of(10L,20L),vo.getPermissionIds());
    }
}
