-- Open Management Platform initial database script.
-- PostgreSQL 16+.
-- Default super administrator: admin / Admin@123456
-- Change the default password immediately after the first login.

BEGIN;

-- ============================================================
-- 1. Schema
-- ============================================================

-- Open Management Platform PostgreSQL 16 schema.

CREATE TABLE tenant_info (
    id              bigint PRIMARY KEY,
    tenant_code     varchar(25) NOT NULL,
    tenant_name     varchar(25) NOT NULL,
    enabled         boolean NOT NULL DEFAULT true,
    created_by      bigint,
    created_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      bigint,
    updated_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         integer NOT NULL DEFAULT 0,
    deleted         boolean NOT NULL DEFAULT false
);

COMMENT ON TABLE tenant_info IS '租户信息表';

CREATE TABLE department_info (
    id              bigint PRIMARY KEY,
    tenant_id       bigint NOT NULL,
    parent_id       bigint,
    department_code varchar(25) NOT NULL,
    department_name varchar(25) NOT NULL,
    sort_order      integer NOT NULL DEFAULT 0,
    enabled         boolean NOT NULL DEFAULT true,
    created_by      bigint,
    created_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      bigint,
    updated_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         integer NOT NULL DEFAULT 0,
    deleted         boolean NOT NULL DEFAULT false
);

COMMENT ON TABLE department_info IS '部门信息表';

CREATE TABLE position_info (
    id              bigint PRIMARY KEY,
    tenant_id       bigint NOT NULL,
    position_code   varchar(25) NOT NULL,
    position_name   varchar(25) NOT NULL,
    sort_order      integer NOT NULL DEFAULT 0,
    enabled         boolean NOT NULL DEFAULT true,
    created_by      bigint,
    created_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      bigint,
    updated_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         integer NOT NULL DEFAULT 0,
    deleted         boolean NOT NULL DEFAULT false
);

COMMENT ON TABLE position_info IS '岗位信息表';

CREATE TABLE user_info (
    id              bigint PRIMARY KEY,
    tenant_id       bigint NOT NULL,
    department_id   bigint,
    username        varchar(25) NOT NULL,
    password        varchar(100) NOT NULL,
    display_name    varchar(25) NOT NULL,
    email           varchar(128),
    phone           varchar(11),
    enabled         boolean NOT NULL DEFAULT true,
    auth_version    integer NOT NULL DEFAULT 1,
    platform_admin  boolean NOT NULL DEFAULT false,
    created_by      bigint,
    created_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      bigint,
    updated_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         integer NOT NULL DEFAULT 0,
    deleted         boolean NOT NULL DEFAULT false
);

COMMENT ON TABLE user_info IS '用户信息表';
COMMENT ON COLUMN user_info.password IS 'BCrypt 密码摘要';
COMMENT ON COLUMN user_info.auth_version IS '认证版本，递增后旧 Token 失效';

CREATE TABLE role_info (
    id              bigint PRIMARY KEY,
    tenant_id       bigint NOT NULL,
    role_code       varchar(25) NOT NULL,
    role_name       varchar(25) NOT NULL,
    enabled         boolean NOT NULL DEFAULT true,
    created_by      bigint,
    created_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      bigint,
    updated_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         integer NOT NULL DEFAULT 0,
    deleted         boolean NOT NULL DEFAULT false
);

COMMENT ON TABLE role_info IS '角色信息表';

CREATE TABLE permission_group_info (
    id            bigint PRIMARY KEY,
    tenant_id     bigint NOT NULL,
    parent_id     bigint,
    group_code    varchar(25) NOT NULL,
    group_name    varchar(25) NOT NULL,
    sort_order    integer NOT NULL DEFAULT 0,
    enabled       boolean NOT NULL DEFAULT true,
    created_by    bigint,
    created_at    timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by    bigint,
    updated_at    timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version       integer NOT NULL DEFAULT 0,
    deleted       boolean NOT NULL DEFAULT false
);

COMMENT ON TABLE permission_group_info IS '权限分组信息表，与菜单结构相互独立';

CREATE TABLE permission_info (
    id               bigint PRIMARY KEY,
    tenant_id        bigint NOT NULL,
    group_id         bigint NOT NULL,
    permission_code  varchar(25) NOT NULL,
    permission_name  varchar(25) NOT NULL,
    permission_type  varchar(32) NOT NULL,
    enabled          boolean NOT NULL DEFAULT true,
    created_by       bigint,
    created_at       timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by       bigint,
    updated_at       timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version          integer NOT NULL DEFAULT 0,
    deleted          boolean NOT NULL DEFAULT false,
    CONSTRAINT ck_permission_info_type CHECK (permission_type IN ('API', 'BUTTON'))
);

COMMENT ON TABLE permission_info IS '权限信息表';

CREATE TABLE menu_info (
    id              bigint PRIMARY KEY,
    tenant_id       bigint NOT NULL,
    parent_id       bigint,
    menu_name       varchar(25) NOT NULL,
    route_path      varchar(256),
    component_code  varchar(128),
    icon            varchar(128),
    sort_order      integer NOT NULL DEFAULT 0,
    enabled         boolean NOT NULL DEFAULT true,
    created_by      bigint,
    created_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      bigint,
    updated_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         integer NOT NULL DEFAULT 0,
    deleted         boolean NOT NULL DEFAULT false
);

COMMENT ON TABLE menu_info IS '菜单信息表';
COMMENT ON COLUMN menu_info.component_code IS '前端安全组件注册编码，不是可执行文件路径';

CREATE TABLE user_role_relation (
    id          bigint PRIMARY KEY,
    tenant_id   bigint NOT NULL,
    user_id     bigint NOT NULL,
    role_id     bigint NOT NULL,
    created_by  bigint,
    created_at  timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_role_relation UNIQUE (tenant_id, user_id, role_id)
);

COMMENT ON TABLE user_role_relation IS '用户角色关系表';

CREATE TABLE user_position_relation (
    id           bigint PRIMARY KEY,
    tenant_id    bigint NOT NULL,
    user_id      bigint NOT NULL,
    position_id  bigint NOT NULL,
    created_by   bigint,
    created_at   timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_position_relation UNIQUE (tenant_id, user_id, position_id)
);

COMMENT ON TABLE user_position_relation IS '用户岗位关系表';

CREATE TABLE role_permission_relation (
    id             bigint PRIMARY KEY,
    tenant_id      bigint NOT NULL,
    role_id        bigint NOT NULL,
    permission_id  bigint NOT NULL,
    created_by     bigint,
    created_at     timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_role_permission_relation UNIQUE (tenant_id, role_id, permission_id)
);

COMMENT ON TABLE role_permission_relation IS '角色权限关系表';

CREATE TABLE role_menu_relation (
    id          bigint PRIMARY KEY,
    tenant_id   bigint NOT NULL,
    role_id     bigint NOT NULL,
    menu_id     bigint NOT NULL,
    created_by  bigint,
    created_at  timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_role_menu_relation UNIQUE (tenant_id, role_id, menu_id)
);

COMMENT ON TABLE role_menu_relation IS '角色菜单关系表';

CREATE TABLE login_log (
    id              bigint PRIMARY KEY,
    tenant_id       bigint NOT NULL,
    user_id         bigint,
    username        varchar(25),
    login_ip        varchar(64),
    user_agent      varchar(512),
    success         boolean NOT NULL,
    failure_reason  varchar(256),
    created_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE login_log IS '登录日志表';

CREATE TABLE operation_log (
    id              bigint PRIMARY KEY,
    tenant_id       bigint NOT NULL,
    user_id         bigint,
    module_name     varchar(25),
    operation_name  varchar(25),
    request_id      varchar(128),
    request_path    varchar(512),
    success         boolean NOT NULL,
    created_at      timestamptz NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE operation_log IS '操作日志表';

-- ============================================================
-- 2. Indexes
-- ============================================================

CREATE UNIQUE INDEX uk_tenant_info_code
    ON tenant_info (tenant_code)
    WHERE deleted = false;

CREATE UNIQUE INDEX uk_user_info_username
    ON user_info (tenant_id, username)
    WHERE deleted = false;

CREATE UNIQUE INDEX uk_department_info_tenant_code
    ON department_info (tenant_id, department_code)
    WHERE deleted = false;

CREATE UNIQUE INDEX uk_position_info_tenant_code
    ON position_info (tenant_id, position_code)
    WHERE deleted = false;

CREATE UNIQUE INDEX uk_permission_info_code
    ON permission_info (tenant_id, permission_code)
    WHERE deleted = false;

CREATE UNIQUE INDEX uk_permission_group_info_code
    ON permission_group_info (tenant_id, group_code)
    WHERE deleted = false;

CREATE INDEX idx_permission_group_info_parent
    ON permission_group_info (tenant_id, parent_id, sort_order)
    WHERE deleted = false;

CREATE INDEX idx_permission_info_group
    ON permission_info (tenant_id, group_id)
    WHERE deleted = false;

CREATE UNIQUE INDEX uk_role_info_tenant_code
    ON role_info (tenant_id, role_code)
    WHERE deleted = false;

CREATE UNIQUE INDEX uk_menu_info_tenant_route
    ON menu_info (tenant_id, route_path)
    WHERE deleted = false AND route_path IS NOT NULL;

CREATE INDEX idx_menu_info_tenant_parent
    ON menu_info (tenant_id, parent_id, sort_order)
    WHERE deleted = false;

CREATE INDEX idx_department_info_tenant_parent
    ON department_info (tenant_id, parent_id)
    WHERE deleted = false;

CREATE INDEX idx_user_info_tenant_department
    ON user_info (tenant_id, department_id)
    WHERE deleted = false;

CREATE INDEX idx_user_info_tenant_enabled
    ON user_info (tenant_id, enabled)
    WHERE deleted = false;

CREATE INDEX idx_role_info_tenant_enabled
    ON role_info (tenant_id, enabled)
    WHERE deleted = false;

CREATE INDEX idx_user_role_relation_user
    ON user_role_relation (tenant_id, user_id);

CREATE INDEX idx_user_position_relation_user
    ON user_position_relation (tenant_id, user_id);

CREATE INDEX idx_role_permission_relation_role
    ON role_permission_relation (tenant_id, role_id);

CREATE INDEX idx_role_menu_relation_role
    ON role_menu_relation (tenant_id, role_id);

CREATE INDEX idx_login_log_user_created
    ON login_log (tenant_id, user_id, created_at DESC);

CREATE INDEX idx_operation_log_user_created
    ON operation_log (tenant_id, user_id, created_at DESC);

-- ============================================================
-- 3. Base data
-- ============================================================

-- 以下 ID 是预生成并固定保留的雪花 ID；运行时由统一 IdentifierGenerator 生成。

INSERT INTO tenant_info (
    id, tenant_code, tenant_name, enabled, created_at, updated_at, version, deleted
) VALUES (
    2055000000000000000, 'platform', '平台默认租户', true,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false
);

INSERT INTO department_info (
    id, tenant_id, parent_id, department_code, department_name,
    sort_order, enabled, created_at, updated_at, version, deleted
) VALUES (
    2055000000000000001, 2055000000000000000, NULL, 'HEADQUARTERS', '总部', 0, true,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false
);

INSERT INTO permission_group_info (
    id, tenant_id, parent_id, group_code, group_name, sort_order,
    enabled, created_at, updated_at, version, deleted
) VALUES
    (2055000000000000010, 2055000000000000000, NULL, 'system', '系统管理', 0, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000011, 2055000000000000000, 2055000000000000010, 'system:user', '用户管理', 10, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000012, 2055000000000000000, 2055000000000000010, 'system:department', '部门管理', 20, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000013, 2055000000000000000, 2055000000000000010, 'system:role', '角色管理', 30, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000014, 2055000000000000000, 2055000000000000010, 'system:permission', '权限管理', 40, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000015, 2055000000000000000, 2055000000000000010, 'system:menu', '菜单管理', 50, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000016, 2055000000000000000, 2055000000000000010, 'system:position', '岗位管理', 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000017, 2055000000000000000, 2055000000000000010, 'system:tenant', '租户管理', 70, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000018, 2055000000000000000, 2055000000000000010, 'system:audit', '审计管理', 80, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false);

INSERT INTO permission_info (
    id, tenant_id, group_id, permission_code, permission_name, permission_type,
    enabled, created_at, updated_at, version, deleted
) VALUES
    (2055000000000000101, 2055000000000000000, 2055000000000000011, 'system:user:list', '用户查询', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000102, 2055000000000000000, 2055000000000000011, 'system:user:create', '用户新增', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000103, 2055000000000000000, 2055000000000000011, 'system:user:update', '用户修改', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000104, 2055000000000000000, 2055000000000000011, 'system:user:delete', '用户删除', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000105, 2055000000000000000, 2055000000000000012, 'system:department:list', '部门查询', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000106, 2055000000000000000, 2055000000000000012, 'system:department:create', '部门新增', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000107, 2055000000000000000, 2055000000000000012, 'system:department:update', '部门修改', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000108, 2055000000000000000, 2055000000000000012, 'system:department:delete', '部门删除', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000109, 2055000000000000000, 2055000000000000014, 'system:permission:list', '权限查询', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000110, 2055000000000000000, 2055000000000000014, 'system:permission:create', '权限新增', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000111, 2055000000000000000, 2055000000000000014, 'system:permission:update', '权限修改', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000112, 2055000000000000000, 2055000000000000014, 'system:permission:delete', '权限删除', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000113, 2055000000000000000, 2055000000000000013, 'system:role:list', '角色查询', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000114, 2055000000000000000, 2055000000000000013, 'system:role:create', '角色新增', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000115, 2055000000000000000, 2055000000000000013, 'system:role:update', '角色修改', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000116, 2055000000000000000, 2055000000000000013, 'system:role:delete', '角色删除', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000117, 2055000000000000000, 2055000000000000013, 'system:role:permission', '角色分配权限', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000118, 2055000000000000000, 2055000000000000013, 'system:role:assign-menu', '角色分配菜单', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000119, 2055000000000000000, 2055000000000000015, 'system:menu:list', '菜单查询', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000120, 2055000000000000000, 2055000000000000015, 'system:menu:create', '菜单新增', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000121, 2055000000000000000, 2055000000000000015, 'system:menu:update', '菜单修改', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000122, 2055000000000000000, 2055000000000000015, 'system:menu:delete', '菜单删除', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000123, 2055000000000000000, 2055000000000000011, 'system:user:assign-role', '用户分配角色', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000125, 2055000000000000000, 2055000000000000016, 'system:position:list', '岗位查询', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000126, 2055000000000000000, 2055000000000000016, 'system:position:create', '岗位新增', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000127, 2055000000000000000, 2055000000000000016, 'system:position:update', '岗位修改', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000128, 2055000000000000000, 2055000000000000016, 'system:position:delete', '岗位删除', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000129, 2055000000000000000, 2055000000000000011, 'system:user:position', '用户分配岗位', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000130, 2055000000000000000, 2055000000000000017, 'system:tenant:list', '租户查询', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000131, 2055000000000000000, 2055000000000000017, 'system:tenant:create', '租户新增', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000132, 2055000000000000000, 2055000000000000017, 'system:tenant:update', '租户修改', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000133, 2055000000000000000, 2055000000000000018, 'system:audit:login-list', '登录日志查询', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000134, 2055000000000000000, 2055000000000000018, 'system:audit:operation', '操作日志查询', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000135, 2055000000000000000, 2055000000000000011, 'system:user:reset-pwd', '用户密码重置', 'API', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false);

INSERT INTO role_info (
    id, tenant_id, role_code, role_name,
    enabled, created_at, updated_at, version, deleted
) VALUES (
    2055000000000000200, 2055000000000000000, 'SUPER_ADMIN', '超级管理员', true,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false
);

INSERT INTO role_permission_relation (id, tenant_id, role_id, permission_id, created_at)
VALUES
    (2055000000000000301, 2055000000000000000, 2055000000000000200, 2055000000000000101, CURRENT_TIMESTAMP),
    (2055000000000000302, 2055000000000000000, 2055000000000000200, 2055000000000000102, CURRENT_TIMESTAMP),
    (2055000000000000303, 2055000000000000000, 2055000000000000200, 2055000000000000103, CURRENT_TIMESTAMP),
    (2055000000000000304, 2055000000000000000, 2055000000000000200, 2055000000000000104, CURRENT_TIMESTAMP),
    (2055000000000000305, 2055000000000000000, 2055000000000000200, 2055000000000000105, CURRENT_TIMESTAMP),
    (2055000000000000306, 2055000000000000000, 2055000000000000200, 2055000000000000106, CURRENT_TIMESTAMP),
    (2055000000000000307, 2055000000000000000, 2055000000000000200, 2055000000000000107, CURRENT_TIMESTAMP),
    (2055000000000000308, 2055000000000000000, 2055000000000000200, 2055000000000000108, CURRENT_TIMESTAMP),
    (2055000000000000309, 2055000000000000000, 2055000000000000200, 2055000000000000109, CURRENT_TIMESTAMP),
    (2055000000000000310, 2055000000000000000, 2055000000000000200, 2055000000000000110, CURRENT_TIMESTAMP),
    (2055000000000000311, 2055000000000000000, 2055000000000000200, 2055000000000000111, CURRENT_TIMESTAMP),
    (2055000000000000312, 2055000000000000000, 2055000000000000200, 2055000000000000112, CURRENT_TIMESTAMP),
    (2055000000000000313, 2055000000000000000, 2055000000000000200, 2055000000000000113, CURRENT_TIMESTAMP),
    (2055000000000000314, 2055000000000000000, 2055000000000000200, 2055000000000000114, CURRENT_TIMESTAMP),
    (2055000000000000315, 2055000000000000000, 2055000000000000200, 2055000000000000115, CURRENT_TIMESTAMP),
    (2055000000000000316, 2055000000000000000, 2055000000000000200, 2055000000000000116, CURRENT_TIMESTAMP),
    (2055000000000000317, 2055000000000000000, 2055000000000000200, 2055000000000000117, CURRENT_TIMESTAMP),
    (2055000000000000318, 2055000000000000000, 2055000000000000200, 2055000000000000118, CURRENT_TIMESTAMP),
    (2055000000000000319, 2055000000000000000, 2055000000000000200, 2055000000000000119, CURRENT_TIMESTAMP),
    (2055000000000000320, 2055000000000000000, 2055000000000000200, 2055000000000000120, CURRENT_TIMESTAMP),
    (2055000000000000321, 2055000000000000000, 2055000000000000200, 2055000000000000121, CURRENT_TIMESTAMP),
    (2055000000000000322, 2055000000000000000, 2055000000000000200, 2055000000000000122, CURRENT_TIMESTAMP),
    (2055000000000000323, 2055000000000000000, 2055000000000000200, 2055000000000000123, CURRENT_TIMESTAMP),
    (2055000000000000325, 2055000000000000000, 2055000000000000200, 2055000000000000125, CURRENT_TIMESTAMP),
    (2055000000000000326, 2055000000000000000, 2055000000000000200, 2055000000000000126, CURRENT_TIMESTAMP),
    (2055000000000000327, 2055000000000000000, 2055000000000000200, 2055000000000000127, CURRENT_TIMESTAMP),
    (2055000000000000328, 2055000000000000000, 2055000000000000200, 2055000000000000128, CURRENT_TIMESTAMP),
    (2055000000000000329, 2055000000000000000, 2055000000000000200, 2055000000000000129, CURRENT_TIMESTAMP),
    (2055000000000000330, 2055000000000000000, 2055000000000000200, 2055000000000000130, CURRENT_TIMESTAMP),
    (2055000000000000331, 2055000000000000000, 2055000000000000200, 2055000000000000131, CURRENT_TIMESTAMP),
    (2055000000000000332, 2055000000000000000, 2055000000000000200, 2055000000000000132, CURRENT_TIMESTAMP),
    (2055000000000000333, 2055000000000000000, 2055000000000000200, 2055000000000000133, CURRENT_TIMESTAMP),
    (2055000000000000334, 2055000000000000000, 2055000000000000200, 2055000000000000134, CURRENT_TIMESTAMP),
    (2055000000000000335, 2055000000000000000, 2055000000000000200, 2055000000000000135, CURRENT_TIMESTAMP);

INSERT INTO menu_info (
    id, tenant_id, parent_id, menu_name, route_path, component_code, icon,
    sort_order, enabled, created_at, updated_at, version, deleted
) VALUES
    (2055000000000000401, 2055000000000000000, NULL, '系统管理', '/system', NULL, 'setting', 10, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000402, 2055000000000000000, 2055000000000000401, '用户管理', '/system/user', 'UserPage', 'user', 10, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000403, 2055000000000000000, 2055000000000000401, '部门管理', '/system/department', 'DepartmentPage', 'department', 20, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000404, 2055000000000000000, 2055000000000000401, '角色管理', '/system/role', 'RolePage', 'role', 30, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000405, 2055000000000000000, 2055000000000000401, '权限管理', '/system/permission', 'PermissionPage', 'permission', 40, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000406, 2055000000000000000, 2055000000000000401, '菜单管理', '/system/menu', 'MenuPage', 'menu', 50, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000407, 2055000000000000000, 2055000000000000401, '岗位管理', '/system/position', 'PositionPage', 'position', 60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000408, 2055000000000000000, 2055000000000000401, '租户管理', '/system/tenant', 'TenantPage', 'tenant', 70, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false),
    (2055000000000000409, 2055000000000000000, 2055000000000000401, '审计日志', '/system/audit', 'AuditPage', 'audit', 80, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false);

INSERT INTO role_menu_relation (id, tenant_id, role_id, menu_id, created_at)
VALUES
    (2055000000000000501, 2055000000000000000, 2055000000000000200, 2055000000000000401, CURRENT_TIMESTAMP),
    (2055000000000000502, 2055000000000000000, 2055000000000000200, 2055000000000000402, CURRENT_TIMESTAMP),
    (2055000000000000503, 2055000000000000000, 2055000000000000200, 2055000000000000403, CURRENT_TIMESTAMP),
    (2055000000000000504, 2055000000000000000, 2055000000000000200, 2055000000000000404, CURRENT_TIMESTAMP),
    (2055000000000000505, 2055000000000000000, 2055000000000000200, 2055000000000000405, CURRENT_TIMESTAMP),
    (2055000000000000506, 2055000000000000000, 2055000000000000200, 2055000000000000406, CURRENT_TIMESTAMP),
    (2055000000000000507, 2055000000000000000, 2055000000000000200, 2055000000000000407, CURRENT_TIMESTAMP),
    (2055000000000000508, 2055000000000000000, 2055000000000000200, 2055000000000000408, CURRENT_TIMESTAMP),
    (2055000000000000509, 2055000000000000000, 2055000000000000200, 2055000000000000409, CURRENT_TIMESTAMP);

INSERT INTO position_info (
    id, tenant_id, position_code, position_name, sort_order,
    enabled, created_at, updated_at, version, deleted
) VALUES (
    2055000000000000600, 2055000000000000000, 'SUPER_ADMIN', '超级管理员', 0, true,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, false
);

-- ============================================================
-- 4. Bootstrap super administrator
-- ============================================================

-- 创建首次登录超级管理员。
-- 默认账号：admin
-- 默认密码：Admin@123456
-- 首次登录后必须立即修改默认密码。


DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM tenant_info
         WHERE id = 2055000000000000000
           AND enabled = TRUE
           AND deleted = FALSE
    ) THEN
        RAISE EXCEPTION 'Default tenant initialization failed earlier in 01_init.sql.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM department_info
         WHERE id = 2055000000000000001
           AND tenant_id = 2055000000000000000
           AND enabled = TRUE
           AND deleted = FALSE
    ) THEN
        RAISE EXCEPTION 'Default department initialization failed earlier in 01_init.sql.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM role_info
         WHERE id = 2055000000000000200
           AND tenant_id = 2055000000000000000
           AND role_code = 'SUPER_ADMIN'
           AND enabled = TRUE
           AND deleted = FALSE
    ) THEN
        RAISE EXCEPTION 'SUPER_ADMIN role initialization failed earlier in 01_init.sql.';
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM position_info
         WHERE id = 2055000000000000600
           AND tenant_id = 2055000000000000000
           AND position_code = 'SUPER_ADMIN'
           AND enabled = TRUE
           AND deleted = FALSE
    ) THEN
        RAISE EXCEPTION 'SUPER_ADMIN position initialization failed earlier in 01_init.sql.';
    END IF;
END
$$;

INSERT INTO user_info (
    id, tenant_id, department_id, username, password, display_name,
    enabled, auth_version, platform_admin, created_at, updated_at, version, deleted
)
SELECT
    2055000000000000700, 2055000000000000000, 2055000000000000001, 'admin',
    '$2a$12$6V3EWlhIeAOauXIPeuIGceZcnSaBDrpG86Tpt3SvRB3kWQRWGm7p6',
    '系统超级管理员', TRUE, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, FALSE
WHERE NOT EXISTS (
    SELECT 1
      FROM user_info
     WHERE username = 'admin'
       AND tenant_id = 2055000000000000000
       AND deleted = FALSE
);

INSERT INTO user_role_relation (
    id, tenant_id, user_id, role_id, created_by, created_at
)
SELECT
    2055000000000000800, u.tenant_id, u.id, r.id, u.id, CURRENT_TIMESTAMP
  FROM user_info u
  JOIN role_info r
    ON r.tenant_id = u.tenant_id
   AND r.role_code = 'SUPER_ADMIN'
   AND r.enabled = TRUE
   AND r.deleted = FALSE
 WHERE u.username = 'admin'
   AND u.tenant_id = 2055000000000000000
   AND u.enabled = TRUE
   AND u.deleted = FALSE
ON CONFLICT (tenant_id, user_id, role_id) DO NOTHING;

INSERT INTO user_position_relation (
    id, tenant_id, user_id, position_id, created_by, created_at
)
SELECT
    2055000000000000801, u.tenant_id, u.id, p.id, u.id, CURRENT_TIMESTAMP
  FROM user_info u
  JOIN position_info p
    ON p.tenant_id = u.tenant_id
   AND p.position_code = 'SUPER_ADMIN'
   AND p.enabled = TRUE
   AND p.deleted = FALSE
 WHERE u.username = 'admin'
   AND u.tenant_id = 2055000000000000000
   AND u.enabled = TRUE
   AND u.deleted = FALSE
ON CONFLICT (tenant_id, user_id, position_id) DO NOTHING;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM user_info u
          JOIN user_role_relation ur
            ON ur.tenant_id = u.tenant_id
           AND ur.user_id = u.id
          JOIN role_info r
            ON r.tenant_id = ur.tenant_id
           AND r.id = ur.role_id
         WHERE u.username = 'admin'
           AND u.tenant_id = 2055000000000000000
           AND u.enabled = TRUE
           AND u.deleted = FALSE
           AND r.role_code = 'SUPER_ADMIN'
           AND r.enabled = TRUE
           AND r.deleted = FALSE
    ) THEN
        RAISE EXCEPTION 'Super administrator initialization verification failed.';
    END IF;
END
$$;


COMMIT;
