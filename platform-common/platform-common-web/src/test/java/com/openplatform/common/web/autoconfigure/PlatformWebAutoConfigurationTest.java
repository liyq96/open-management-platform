package com.openplatform.common.web.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

class PlatformWebAutoConfigurationTest {

    @Test
    void shouldSerializeLongAsStringForJavascriptClients() throws Exception {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new PlatformWebAutoConfiguration().platformLongIdJacksonModule())
                .build();

        assertEquals("{\"userId\":\"9223372036854775807\"}",
                mapper.writeValueAsString(new LongPayload(Long.MAX_VALUE)));
    }

    private static final class LongPayload {

        private final Long userId;

        private LongPayload(Long userId) {
            this.userId = userId;
        }

        public Long getUserId() {
            return userId;
        }
    }
}
