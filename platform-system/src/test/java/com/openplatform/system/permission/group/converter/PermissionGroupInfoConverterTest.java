package com.openplatform.system.permission.group.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.openplatform.system.permission.group.model.entity.PermissionGroupInfo;
import com.openplatform.system.permission.group.model.vo.PermissionGroupInfoVO;
import java.util.List;
import org.junit.jupiter.api.Test;

class PermissionGroupInfoConverterTest {

    @Test
    void shouldBuildPermissionGroupTreeWhenChildrenAppearBeforeParent() {
        PermissionGroupInfo child = group(2L, 1L, "system:user", "用户管理");
        PermissionGroupInfo root = group(1L, null, "system", "系统管理");

        List<PermissionGroupInfoVO> tree = PermissionGroupInfoConverter.toTree(List.of(child, root));

        assertEquals(1, tree.size());
        assertEquals(1L, tree.getFirst().getGroupId());
        assertEquals(2L, tree.getFirst().getChildren().getFirst().getGroupId());
    }

    private PermissionGroupInfo group(Long id, Long parentId, String code, String name) {
        PermissionGroupInfo entity = new PermissionGroupInfo();
        entity.setId(id);
        entity.setParentId(parentId);
        entity.setGroupCode(code);
        entity.setGroupName(name);
        entity.setSortOrder(0);
        entity.setEnabled(true);
        return entity;
    }
}
