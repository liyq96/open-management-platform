package com.openplatform.system.position.model.dto;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.Data;
/** 岗位批量删除参数。 */
@Data
public class PositionDeleteDTO {
    @NotEmpty private List<@Positive Long> positionIds;
}
