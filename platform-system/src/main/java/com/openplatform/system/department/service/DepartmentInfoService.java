package com.openplatform.system.department.service;

import com.openplatform.system.department.model.dto.DepartmentCreateDTO;
import com.openplatform.system.department.model.dto.DepartmentDeleteDTO;
import com.openplatform.system.department.model.dto.DepartmentTreeQueryDTO;
import com.openplatform.system.department.model.dto.DepartmentUpdateDTO;
import com.openplatform.system.department.model.vo.DepartmentInfoVO;
import java.util.List;

/**
 * 部门管理服务。
 */
public interface DepartmentInfoService {

    List<DepartmentInfoVO> tree(DepartmentTreeQueryDTO dto);

    DepartmentInfoVO detail(Long departmentId);

    Long create(DepartmentCreateDTO dto);

    void update(DepartmentUpdateDTO dto);

    void delete(DepartmentDeleteDTO dto);
}
