package com.openplatform.system.menu.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openplatform.common.database.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 菜单信息实体。 */
@Getter @Setter @TableName("menu_info")
public class MenuInfo extends BaseEntity {
    private Long parentId;
    private String menuName;
    private String routePath;
    private String componentCode;
    private String icon;
    private Integer sortOrder;
    private Boolean enabled;
}
