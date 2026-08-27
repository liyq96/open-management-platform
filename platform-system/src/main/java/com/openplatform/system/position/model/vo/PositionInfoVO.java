package com.openplatform.system.position.model.vo;
import java.time.OffsetDateTime;
import lombok.Data;
/** 岗位信息响应。 */
@Data
public class PositionInfoVO {
    private Long positionId;
    private String positionCode;
    private String positionName;
    private Integer sortOrder;
    private Boolean enabled;
    private OffsetDateTime createdAt;
}
