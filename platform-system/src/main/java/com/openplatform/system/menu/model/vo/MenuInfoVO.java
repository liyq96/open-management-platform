package com.openplatform.system.menu.model.vo;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
/** 菜单信息响应。 */
@Data public class MenuInfoVO {
    private Long menuId; private Long parentId; private String menuName; private String routePath;
    private String componentCode; private String icon; private Integer sortOrder; private Boolean enabled;
    private OffsetDateTime createdAt; private List<MenuInfoVO> children=new ArrayList<>();
}
