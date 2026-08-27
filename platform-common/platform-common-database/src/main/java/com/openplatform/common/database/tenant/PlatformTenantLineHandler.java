package com.openplatform.common.database.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.openplatform.common.security.support.SecurityContextUtils;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;

/**
 * 平台强制租户处理器。租户目录表是唯一不追加 tenant_id 的业务表。
 */
public class PlatformTenantLineHandler implements TenantLineHandler {

    private static final String TENANT_TABLE = "tenant_info";

    @Override
    public Expression getTenantId() {
        Long tenantId = TenantContextHolder.currentTenantId()
                .orElseGet(SecurityContextUtils::requireTenantId);
        return new LongValue(tenantId);
    }

    @Override
    public boolean ignoreTable(String tableName) {
        return TENANT_TABLE.equalsIgnoreCase(tableName);
    }

    /**
     * 业务 INSERT 即使显式提供 tenant_id 也不能绕过拦截器。
     * 拦截器继续追加可信租户列，使违规 SQL 以重复列失败，而不是接受调用方租户值。
     */
    @Override
    public boolean ignoreInsert(List<Column> columns, String tenantIdColumn) {
        return false;
    }
}
