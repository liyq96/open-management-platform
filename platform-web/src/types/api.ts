export type EntityId = string | number;

export interface ApiResponse<T> {
  code: string;
  message: string;
  data: T;
  requestId?: string;
}

export interface PageResult<T> {
  records: T[];
  page: number;
  pageSize: number;
  total: number;
}

export interface PageQuery {
  page: number;
  pageSize: number;
}

export interface CaptchaVO {
  captchaId: string;
  imageBase64: string;
  expiresIn: number;
}

export interface UserLoginDTO {
  tenantCode: string;
  username: string;
  password: string;
  captchaId: string;
  captchaCode: string;
}

export interface UserLoginVO {
  token: string;
  tokenType: string;
  expiresIn: number;
}

export interface JwtClaims {
  user_id: EntityId;
  username: string;
  tenant_id: EntityId;
  department_id?: EntityId;
  auth_version?: number;
  platform_admin?: boolean;
  iat?: number;
  exp?: number;
}

export interface UserInfoVO {
  userId: EntityId;
  departmentId?: EntityId;
  username: string;
  displayName: string;
  email?: string;
  phone?: string;
  enabled: boolean;
  createdAt: string;
  roleIds: EntityId[];
  positionIds: EntityId[];
}

export interface DepartmentInfoVO {
  departmentId: EntityId;
  parentId?: EntityId;
  departmentCode: string;
  departmentName: string;
  sortOrder: number;
  enabled: boolean;
  createdAt: string;
  children?: DepartmentInfoVO[];
}

export interface PositionInfoVO {
  positionId: EntityId;
  positionCode: string;
  positionName: string;
  sortOrder: number;
  enabled: boolean;
  createdAt: string;
}

export type PermissionType = 'API' | 'BUTTON';

export interface RoleInfoVO {
  roleId: EntityId;
  roleCode: string;
  roleName: string;
  enabled: boolean;
  permissionIds: EntityId[];
  menuIds: EntityId[];
  createdAt: string;
}

export interface PermissionInfoVO {
  permissionId: EntityId;
  groupId: EntityId;
  permissionCode: string;
  permissionName: string;
  permissionType: PermissionType;
  enabled: boolean;
  createdAt: string;
}

export interface PermissionGroupInfoVO {
  groupId: EntityId;
  parentId?: EntityId;
  groupCode: string;
  groupName: string;
  sortOrder: number;
  enabled: boolean;
  createdAt: string;
  children?: PermissionGroupInfoVO[];
}

export interface MenuInfoVO {
  menuId: EntityId;
  parentId?: EntityId;
  menuName: string;
  routePath?: string;
  componentCode?: string;
  icon?: string;
  sortOrder: number;
  enabled: boolean;
  createdAt: string;
  children?: MenuInfoVO[];
}

export interface TenantInfoVO {
  tenantId: EntityId;
  tenantCode: string;
  tenantName: string;
  enabled: boolean;
  createdAt: string;
}

export interface LoginLogVO {
  logId: EntityId;
  username: string;
  loginIp?: string;
  userAgent?: string;
  success: boolean;
  failureReason?: string;
  createdAt: string;
}

export interface OperationLogVO {
  logId: EntityId;
  userId?: EntityId;
  moduleName: string;
  operationName: string;
  requestId?: string;
  requestPath?: string;
  success: boolean;
  createdAt: string;
}
