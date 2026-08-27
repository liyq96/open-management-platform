import { get, post } from '@/lib/http';
import type {
  CaptchaVO, DepartmentInfoVO, EntityId, LoginLogVO, MenuInfoVO,
  OperationLogVO, PageResult, PermissionGroupInfoVO, PermissionInfoVO, PermissionType, PositionInfoVO,
  RoleInfoVO, TenantInfoVO, UserInfoVO, UserLoginDTO, UserLoginVO,
} from '@/types/api';

export const authApi = {
  captcha: () => get<CaptchaVO>('/v1/auth/captcha'),
  login: (data: UserLoginDTO) => post<UserLoginVO>('/v1/auth/login', data),
  logout: () => post<void>('/v1/auth/logout'),
};

export const userApi = {
  page: (data: { keyword?: string; departmentId?: EntityId; enabled?: boolean; page: number; pageSize: number }) => post<PageResult<UserInfoVO>>('/v1/system/user/page', data),
  detail: (userId: EntityId) => get<UserInfoVO>('/v1/system/user/detail', { userId }),
  create: (data: object) => post<EntityId>('/v1/system/user/create', data),
  update: (data: object) => post<void>('/v1/system/user/update', data),
  delete: (userIds: EntityId[]) => post<void>('/v1/system/user/delete', { userIds }),
  assignRoles: (userId: EntityId, roleIds: EntityId[]) => post<void>('/v1/system/user/assign-roles', { userId, roleIds }),
  assignPositions: (userId: EntityId, positionIds: EntityId[]) => post<void>('/v1/system/user/assign-positions', { userId, positionIds }),
  changePassword: (data: { oldPassword: string; newPassword: string; confirmPassword: string }) => post<void>('/v1/system/user/change-password', data),
  resetPassword: (data: { userId: EntityId; newPassword: string; confirmPassword: string }) => post<void>('/v1/system/user/reset-password', data),
};

export const departmentApi = {
  tree: (enabled?: boolean) => post<DepartmentInfoVO[]>('/v1/system/department/tree', { enabled }),
  detail: (departmentId: EntityId) => get<DepartmentInfoVO>('/v1/system/department/detail', { departmentId }),
  create: (data: object) => post<EntityId>('/v1/system/department/create', data),
  update: (data: object) => post<void>('/v1/system/department/update', data),
  delete: (departmentId: EntityId) => post<void>('/v1/system/department/delete', { departmentId }),
};

export const positionApi = {
  page: (data: { keyword?: string; enabled?: boolean; page: number; pageSize: number }) => post<PageResult<PositionInfoVO>>('/v1/system/position/page', data),
  detail: (positionId: EntityId) => get<PositionInfoVO>('/v1/system/position/detail', { positionId }),
  create: (data: object) => post<EntityId>('/v1/system/position/create', data),
  update: (data: object) => post<void>('/v1/system/position/update', data),
  delete: (positionIds: EntityId[]) => post<void>('/v1/system/position/delete', { positionIds }),
};

export const roleApi = {
  page: (data: { keyword?: string; enabled?: boolean; page: number; pageSize: number }) => post<PageResult<RoleInfoVO>>('/v1/system/role/page', data),
  detail: (roleId: EntityId) => get<RoleInfoVO>('/v1/system/role/detail', { roleId }),
  create: (data: object) => post<EntityId>('/v1/system/role/create', data),
  update: (data: object) => post<void>('/v1/system/role/update', data),
  delete: (roleIds: EntityId[]) => post<void>('/v1/system/role/delete', { roleIds }),
  assignPermissions: (roleId: EntityId, permissionIds: EntityId[]) => post<void>('/v1/system/role/assign-permissions', { roleId, permissionIds }),
  assignMenus: (roleId: EntityId, menuIds: EntityId[]) => post<void>('/v1/system/role/assign-menus', { roleId, menuIds }),
};

export const permissionApi = {
  current: () => get<string[]>('/v1/system/permission/current'),
  page: (data: { keyword?: string; groupId?: EntityId; permissionType?: PermissionType; enabled?: boolean; page: number; pageSize: number }) => post<PageResult<PermissionInfoVO>>('/v1/system/permission/page', data),
  detail: (permissionId: EntityId) => get<PermissionInfoVO>('/v1/system/permission/detail', { permissionId }),
  create: (data: object) => post<EntityId>('/v1/system/permission/create', data),
  update: (data: object) => post<void>('/v1/system/permission/update', data),
  delete: (permissionIds: EntityId[]) => post<void>('/v1/system/permission/delete', { permissionIds }),
};

export const permissionGroupApi = {
  tree: (enabled?: boolean) => post<PermissionGroupInfoVO[]>('/v1/system/permission-group/tree', { enabled }),
  detail: (groupId: EntityId) => get<PermissionGroupInfoVO>('/v1/system/permission-group/detail', { groupId }),
  create: (data: object) => post<EntityId>('/v1/system/permission-group/create', data),
  update: (data: object) => post<void>('/v1/system/permission-group/update', data),
  delete: (groupId: EntityId) => post<void>('/v1/system/permission-group/delete', { groupId }),
};

export const menuApi = {
  current: () => get<MenuInfoVO[]>('/v1/system/menu/current'),
  tree: (enabled?: boolean) => post<MenuInfoVO[]>('/v1/system/menu/tree', { enabled }),
  detail: (menuId: EntityId) => get<MenuInfoVO>('/v1/system/menu/detail', { menuId }),
  create: (data: object) => post<EntityId>('/v1/system/menu/create', data),
  update: (data: object) => post<void>('/v1/system/menu/update', data),
  delete: (menuId: EntityId) => post<void>('/v1/system/menu/delete', { menuId }),
};

export const tenantApi = {
  page: (data: { keyword?: string; enabled?: boolean; page: number; pageSize: number }) => post<PageResult<TenantInfoVO>>('/v1/system/tenant/page', data),
  detail: (tenantId: EntityId) => get<TenantInfoVO>('/v1/system/tenant/detail', { tenantId }),
  menuOptions: () => get<MenuInfoVO[]>('/v1/system/tenant/menu-options'),
  create: (data: object) => post<EntityId>('/v1/system/tenant/create', data),
  update: (data: object) => post<void>('/v1/system/tenant/update', data),
};

export const auditApi = {
  loginPage: (data: { username?: string; success?: boolean; page: number; pageSize: number }) => post<PageResult<LoginLogVO>>('/v1/system/audit/login/page', data),
  operationPage: (data: { moduleName?: string; success?: boolean; page: number; pageSize: number }) => post<PageResult<OperationLogVO>>('/v1/system/audit/operation/page', data),
};
