package com.openplatform.system.menu.model.dto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
/** 菜单修改参数。 */
@Data public class MenuUpdateDTO {
    @NotNull @Positive private Long menuId;
    @Positive private Long parentId;
    @NotBlank @Size(max=25) private String menuName;
    private String routePath;
    private String componentCode;
    private String icon;
    @NotNull @Min(0) private Integer sortOrder;
    @NotNull private Boolean enabled;
}
