package com.openplatform.system.tenant.security;

import com.openplatform.common.security.support.SecurityContextUtils;
import org.springframework.stereotype.Component;

/** 平台级资源边界，普通租户即使残留同名权限也不能获得平台能力。 */
@Component("platformResourcePolicy")
public class PlatformResourcePolicy {

    public static final String TENANT_PERMISSION_PREFIX = "system:tenant:";
    public static final String TENANT_MENU_COMPONENT = "TenantPage";
    public static final String TENANT_MENU_ROUTE = "/system/tenant";

    public boolean isPlatformAdmin() {
        return SecurityContextUtils.isPlatformAdmin();
    }

    public static boolean isPlatformPermission(String permissionCode) {
        return permissionCode != null && permissionCode.startsWith(TENANT_PERMISSION_PREFIX);
    }

    public static boolean isPlatformMenu(String componentCode, String routePath) {
        return TENANT_MENU_COMPONENT.equals(componentCode) || TENANT_MENU_ROUTE.equals(routePath);
    }
}
