package com.openplatform.system.menu.model.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
/** 菜单删除参数。 */
@Data public class MenuDeleteDTO { @NotNull @Positive private Long menuId; }
