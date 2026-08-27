package com.openplatform.common.database.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.junit.jupiter.api.Test;

class PlatformMetaObjectHandlerTest {

    private static final Instant NOW = Instant.parse("2026-08-13T12:00:00Z");

    private final PlatformMetaObjectHandler handler =
            new PlatformMetaObjectHandler(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void shouldFillCreateAndUpdateTimeOnInsert() {
        AuditData data = new AuditData();
        MetaObject metaObject = SystemMetaObject.forObject(data);

        handler.insertFill(metaObject);

        OffsetDateTime expected = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);
        assertEquals(expected, data.getCreatedAt());
        assertEquals(expected, data.getUpdatedAt());
    }

    public static class AuditData {

        private OffsetDateTime createdAt;

        private OffsetDateTime updatedAt;

        public OffsetDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public OffsetDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }
    }
}
