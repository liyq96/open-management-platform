package com.openplatform.system.tenant.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openplatform.system.tenant.model.entity.TenantInfo;
import org.junit.jupiter.api.Test;

class TenantInfoConverterTest {

    @Test
    void shouldConvertTenant() {
        TenantInfo entity = new TenantInfo();
        entity.setId(1L);
        entity.setTenantCode("platform");
        entity.setTenantName("平台租户");

        var vo = TenantInfoConverter.toVO(entity);

        assertEquals(1L, vo.getTenantId());
        assertEquals("platform", vo.getTenantCode());
    }
}
