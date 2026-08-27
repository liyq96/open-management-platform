package com.openplatform.common.database.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import java.util.List;
import net.sf.jsqlparser.schema.Column;
import org.junit.jupiter.api.Test;

class PlatformTenantLineHandlerTest {

    private final PlatformTenantLineHandler handler = new PlatformTenantLineHandler();

    @Test
    void shouldUseExplicitTenantContextAndRestoreNestedScope() {
        try (TenantContextHolder.TenantScope ignored = TenantContextHolder.use(10L)) {
            assertEquals("10", handler.getTenantId().toString());
            try (TenantContextHolder.TenantScope nested = TenantContextHolder.use(20L)) {
                assertEquals("20", handler.getTenantId().toString());
            }
            assertEquals("10", handler.getTenantId().toString());
        }
        assertTrue(TenantContextHolder.currentTenantId().isEmpty());
    }

    @Test
    void shouldOnlyIgnoreTenantDirectoryTable() {
        assertTrue(handler.ignoreTable("tenant_info"));
        assertTrue(handler.ignoreTable("TENANT_INFO"));
        assertFalse(handler.ignoreTable("menu_info"));
        assertFalse(handler.ignoreTable("permission_info"));
        assertFalse(handler.ignoreTable("permission_group_info"));
        assertFalse(handler.ignoreTable("user_info"));
        assertFalse(handler.ignoreInsert(List.of(new Column("tenant_id")), "tenant_id"));
    }

    @Test
    void shouldRewriteBusinessSqlWithCurrentTenant() {
        TenantLineInnerInterceptor interceptor = new TenantLineInnerInterceptor(handler);
        try (TenantContextHolder.TenantScope ignored = TenantContextHolder.use(7L)) {
            String sql = interceptor.parserSingle(
                    "SELECT m.id FROM menu_info m JOIN permission_info p ON p.id=m.id "
                            + "JOIN tenant_info t ON t.id=m.tenant_id WHERE m.enabled=TRUE",
                    null).toLowerCase();

            assertTrue(sql.contains("m.tenant_id = 7"));
            assertTrue(sql.contains("p.tenant_id = 7"));
            assertFalse(sql.contains("t.tenant_id"));
        }
    }

    @Test
    void shouldRewriteInsertUpdateAndDeleteWithCurrentTenant() {
        TenantLineInnerInterceptor interceptor = new TenantLineInnerInterceptor(handler);
        try (TenantContextHolder.TenantScope ignored = TenantContextHolder.use(7L)) {
            String insertSql = interceptor.parserSingle(
                    "INSERT INTO role_menu_relation(id,role_id,menu_id) "
                            + "VALUES (1,2,3),(4,5,6)", null).toLowerCase();
            String updateSql = interceptor.parserSingle(
                    "UPDATE user_info SET auth_version=auth_version+1 WHERE id=8", null)
                    .toLowerCase();
            String deleteSql = interceptor.parserSingle(
                    "DELETE FROM user_role_relation WHERE user_id=8", null).toLowerCase();

            assertTrue(insertSql.contains("tenant_id"));
            assertEquals(2, insertSql.split("\\b7\\b", -1).length - 1);
            assertTrue(updateSql.contains("tenant_id = 7"));
            assertTrue(deleteSql.contains("tenant_id = 7"));
        }
    }
}
