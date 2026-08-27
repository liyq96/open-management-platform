package com.openplatform.system.user.model.dto;
import jakarta.validation.constraints.*;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;
/** 用户岗位分配参数。 */
@Data public class UserPositionAssignDTO { @NotNull @Positive private Long userId; @NotNull private Set<@Positive Long> positionIds=new LinkedHashSet<>(); }
