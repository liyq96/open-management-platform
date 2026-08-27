package com.openplatform.system.department.model.vo;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 部门信息响应。
 */
@Data
public class DepartmentInfoVO {

    private Long departmentId;
    private Long parentId;
    private String departmentCode;
    private String departmentName;
    private Integer sortOrder;
    private Boolean enabled;
    private OffsetDateTime createdAt;
    private List<DepartmentInfoVO> children = new ArrayList<>();
}
