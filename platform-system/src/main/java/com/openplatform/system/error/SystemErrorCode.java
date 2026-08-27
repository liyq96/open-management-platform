package com.openplatform.system.error;

import com.openplatform.common.core.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 系统管理模块错误码。
 */
@Getter
@RequiredArgsConstructor
public enum SystemErrorCode implements ErrorCode {

    USER_NOT_FOUND("SYSTEM_001", "用户不存在", 404),
    USERNAME_EXISTS("SYSTEM_002", "用户名已存在", 409),
    DEPARTMENT_NOT_FOUND("SYSTEM_003", "部门不存在", 404),
    CANNOT_DELETE_CURRENT_USER("SYSTEM_004", "不能删除当前登录用户", 409),
    DEPARTMENT_CODE_EXISTS("SYSTEM_005", "部门编码已存在", 409),
    DEPARTMENT_PARENT_INVALID("SYSTEM_006", "上级部门不能是当前部门或其下级部门", 409),
    DEPARTMENT_HAS_CHILDREN("SYSTEM_007", "部门存在下级部门，不能删除", 409),
    DEPARTMENT_HAS_USERS("SYSTEM_008", "部门存在用户，不能删除", 409),
    PERMISSION_NOT_FOUND("SYSTEM_009", "权限标识不存在", 404),
    PERMISSION_CODE_EXISTS("SYSTEM_010", "权限编码已存在", 409),
    PERMISSION_IN_USE("SYSTEM_011", "权限已被角色使用，不能删除", 409),
    ROLE_NOT_FOUND("SYSTEM_012", "角色不存在", 404),
    ROLE_CODE_EXISTS("SYSTEM_013", "角色编码已存在", 409),
    ROLE_IN_USE("SYSTEM_014", "角色已分配给用户，不能删除", 409),
    MENU_NOT_FOUND("SYSTEM_015", "菜单不存在", 404),
    MENU_PARENT_INVALID("SYSTEM_016", "上级菜单不能是当前菜单或其下级菜单", 409),
    MENU_HAS_CHILDREN("SYSTEM_017", "菜单存在下级菜单，不能删除", 409),
    MENU_IN_USE("SYSTEM_018", "菜单已被角色使用，不能删除", 409),
    MENU_ROUTE_EXISTS("SYSTEM_019", "菜单路由已存在", 409),
    PLATFORM_MENU_NOT_ASSIGNABLE("SYSTEM_020", "平台级菜单不能分配给普通租户", 409),
    POSITION_NOT_FOUND("SYSTEM_021", "岗位不存在", 404),
    POSITION_CODE_EXISTS("SYSTEM_022", "岗位编码已存在", 409),
    POSITION_IN_USE("SYSTEM_023", "岗位已分配给用户，不能删除", 409),
    TENANT_NOT_FOUND("SYSTEM_024", "租户不存在", 404),
    TENANT_CODE_EXISTS("SYSTEM_025", "租户编码已存在", 409),
    OLD_PASSWORD_INCORRECT("SYSTEM_027", "原密码错误", 400),
    PASSWORD_CONFIRM_MISMATCH("SYSTEM_028", "两次输入的新密码不一致", 400),
    NEW_PASSWORD_SAME_AS_OLD("SYSTEM_029", "新密码不能与原密码相同", 400),
    PERMISSION_GROUP_NOT_FOUND("SYSTEM_030", "权限分组不存在", 404),
    PERMISSION_GROUP_CODE_EXISTS("SYSTEM_031", "权限分组编码已存在", 409),
    PERMISSION_GROUP_PARENT_INVALID("SYSTEM_032", "上级权限分组不能是当前分组或其下级分组", 409),
    PERMISSION_GROUP_HAS_CHILDREN("SYSTEM_033", "权限分组存在下级分组，不能删除", 409),
    PERMISSION_GROUP_IN_USE("SYSTEM_034", "权限分组下存在权限，不能删除", 409);

    private final String code;
    private final String message;
    private final int httpStatus;
}
