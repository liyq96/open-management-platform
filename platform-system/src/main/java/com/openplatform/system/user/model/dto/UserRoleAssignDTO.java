package com.openplatform.system.user.model.dto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
/** 用户角色分配参数。 */
@Data public class UserRoleAssignDTO { @NotNull @Positive private Long userId; @NotNull private Set<@Positive Long> roleIds=new LinkedHashSet<>(); }
