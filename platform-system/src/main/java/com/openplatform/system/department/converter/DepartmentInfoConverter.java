package com.openplatform.system.department.converter;

import com.openplatform.system.department.model.entity.DepartmentInfo;
import com.openplatform.system.department.model.vo.DepartmentInfoVO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门对象转换器。
 */
public final class DepartmentInfoConverter {

    private DepartmentInfoConverter() {
    }

    public static DepartmentInfoVO toVO(DepartmentInfo entity) {
        DepartmentInfoVO vo = new DepartmentInfoVO();
        vo.setDepartmentId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setDepartmentCode(entity.getDepartmentCode());
        vo.setDepartmentName(entity.getDepartmentName());
        vo.setSortOrder(entity.getSortOrder());
        vo.setEnabled(entity.getEnabled());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static List<DepartmentInfoVO> toTree(List<DepartmentInfo> entities) {
        Map<Long, DepartmentInfoVO> nodeMap = new LinkedHashMap<>();
        for (DepartmentInfo entity : entities) {
            nodeMap.put(entity.getId(), toVO(entity));
        }
        List<DepartmentInfoVO> roots = new ArrayList<>();
        for (DepartmentInfoVO node : nodeMap.values()) {
            DepartmentInfoVO parent = node.getParentId() == null ? null : nodeMap.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }
}
