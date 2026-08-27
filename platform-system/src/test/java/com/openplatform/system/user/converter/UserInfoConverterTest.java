package com.openplatform.system.user.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.openplatform.system.user.model.entity.UserInfo;
import com.openplatform.system.user.model.vo.UserInfoVO;
import org.junit.jupiter.api.Test;

class UserInfoConverterTest {

    @Test
    void shouldNotExposePassword() {
        UserInfo entity = new UserInfo();
        entity.setId(1L);
        entity.setUsername("admin");
        entity.setPassword("secret-hash");

        UserInfoVO vo = UserInfoConverter.toVO(entity);

        assertEquals(1L, vo.getUserId());
        assertEquals("admin", vo.getUsername());
        assertNull(vo.getEmail());
    }
}
