package com.openplatform.system.role.model.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
/** 角色菜单分配参数。 */
@Data public class RoleMenuAssignDTO { @NotNull @Positive private Long roleId; @NotNull private Set<@Positive Long> menuIds=new LinkedHashSet<>(); }
