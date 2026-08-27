package com.openplatform.system.position.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.openplatform.common.database.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/** 岗位信息实体。 */
@Getter
@Setter
@TableName("position_info")
public class PositionInfo extends BaseEntity {
    private String positionCode;
    private String positionName;
    private Integer sortOrder;
    private Boolean enabled;
}
