package com.openplatform.system.department.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openplatform.common.database.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门信息实体。
 */
@Getter
@Setter
@TableName("department_info")
public class DepartmentInfo extends BaseEntity {

    private Long parentId;
    private String departmentCode;
    private String departmentName;
    private Integer sortOrder;
    private Boolean enabled;
}
