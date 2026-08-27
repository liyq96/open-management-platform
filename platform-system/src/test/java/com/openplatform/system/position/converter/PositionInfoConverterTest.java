package com.openplatform.system.position.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openplatform.system.position.model.entity.PositionInfo;
import org.junit.jupiter.api.Test;

class PositionInfoConverterTest {

    @Test
    void shouldConvertPosition() {
        PositionInfo entity = new PositionInfo();
        entity.setId(1L);
        entity.setPositionCode("SUPER_ADMIN");
        entity.setPositionName("管理员");

        var vo = PositionInfoConverter.toVO(entity);

        assertEquals(1L, vo.getPositionId());
        assertEquals("SUPER_ADMIN", vo.getPositionCode());
    }
}
