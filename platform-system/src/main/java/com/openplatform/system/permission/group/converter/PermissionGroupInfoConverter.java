package com.openplatform.system.permission.group.converter;

import com.openplatform.system.permission.group.model.entity.PermissionGroupInfo;
import com.openplatform.system.permission.group.model.vo.PermissionGroupInfoVO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限分组对象转换器。
 */
public final class PermissionGroupInfoConverter {

    private PermissionGroupInfoConverter() {
    }

    public static PermissionGroupInfoVO toVO(PermissionGroupInfo entity) {
        PermissionGroupInfoVO vo = new PermissionGroupInfoVO();
        vo.setGroupId(entity.getId());
        vo.setParentId(entity.getParentId());
        vo.setGroupCode(entity.getGroupCode());
        vo.setGroupName(entity.getGroupName());
        vo.setSortOrder(entity.getSortOrder());
        vo.setEnabled(entity.getEnabled());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    public static List<PermissionGroupInfoVO> toTree(List<PermissionGroupInfo> entities) {
        Map<Long, PermissionGroupInfoVO> nodeMap = new LinkedHashMap<>();
        for (PermissionGroupInfo entity : entities) {
            nodeMap.put(entity.getId(), toVO(entity));
        }
        List<PermissionGroupInfoVO> roots = new ArrayList<>();
        for (PermissionGroupInfoVO node : nodeMap.values()) {
            PermissionGroupInfoVO parent = node.getParentId() == null ? null : nodeMap.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }
}
