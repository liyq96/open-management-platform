package com.openplatform.common.database.tenant;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * 显式租户上下文，供登录、租户初始化等尚未建立 JWT 上下文的流程使用。
 */
public final class TenantContextHolder {

    private static final ThreadLocal<Deque<Long>> TENANT_STACK = ThreadLocal.withInitial(ArrayDeque::new);

    private TenantContextHolder() {
    }

    public static Optional<Long> currentTenantId() {
        return Optional.ofNullable(TENANT_STACK.get().peek());
    }

    public static TenantScope use(Long tenantId) {
        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException("tenantId must be positive");
        }
        TENANT_STACK.get().push(tenantId);
        return new TenantScope();
    }

    public static final class TenantScope implements AutoCloseable {

        private boolean closed;

        private TenantScope() {
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            Deque<Long> stack = TENANT_STACK.get();
            stack.pop();
            if (stack.isEmpty()) {
                TENANT_STACK.remove();
            }
            closed = true;
        }
    }
}
