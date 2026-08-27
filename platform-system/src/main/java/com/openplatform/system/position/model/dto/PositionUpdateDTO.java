package com.openplatform.system.position.model.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
/** 岗位修改参数。 */
@Data
public class PositionUpdateDTO {
    @NotNull @Positive private Long positionId;
    @NotBlank @Size(max = 25)
    private String positionCode;
    @NotBlank @Size(max = 25) private String positionName;
    @NotNull @Min(0) private Integer sortOrder;
    @NotNull private Boolean enabled;
}
