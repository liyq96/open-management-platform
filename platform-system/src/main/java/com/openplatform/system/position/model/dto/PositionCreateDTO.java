package com.openplatform.system.position.model.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
/** 岗位新增参数。 */
@Data
public class PositionCreateDTO {
    @NotBlank @Size(max = 25)
    private String positionCode;
    @NotBlank @Size(max = 25) private String positionName;
    @NotNull @Min(0) private Integer sortOrder = 0;
    @NotNull private Boolean enabled = true;
}
