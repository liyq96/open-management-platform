package com.openplatform.system.department.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openplatform.system.department.model.entity.DepartmentInfo;
import com.openplatform.system.department.model.vo.DepartmentInfoVO;
import java.util.List;
import org.junit.jupiter.api.Test;

class DepartmentInfoConverterTest {

    @Test
    void shouldBuildDepartmentTreeWhenChildrenAppearBeforeParent() {
        DepartmentInfo child = department(2L, 1L, "DEVELOPMENT", "研发部");
        DepartmentInfo root = department(1L, null, "HEADQUARTERS", "总部");
        DepartmentInfo grandchild = department(3L, 2L, "BACKEND", "后端组");

        List<DepartmentInfoVO> tree = DepartmentInfoConverter.toTree(List.of(child, root, grandchild));

        assertEquals(1, tree.size());
        assertEquals(1L, tree.getFirst().getDepartmentId());
        assertEquals(2L, tree.getFirst().getChildren().getFirst().getDepartmentId());
        assertEquals(3L, tree.getFirst().getChildren().getFirst().getChildren().getFirst().getDepartmentId());
    }

    @Test
    void shouldTreatMissingParentAsRoot() {
        DepartmentInfo orphan = department(2L, 99L, "ORPHAN", "独立部门");

        List<DepartmentInfoVO> tree = DepartmentInfoConverter.toTree(List.of(orphan));

        assertEquals(1, tree.size());
        assertEquals(2L, tree.getFirst().getDepartmentId());
    }

    private DepartmentInfo department(Long id, Long parentId, String code, String name) {
        DepartmentInfo entity = new DepartmentInfo();
        entity.setId(id);
        entity.setParentId(parentId);
        entity.setDepartmentCode(code);
        entity.setDepartmentName(name);
        entity.setSortOrder(0);
        entity.setEnabled(true);
        return entity;
    }
}
