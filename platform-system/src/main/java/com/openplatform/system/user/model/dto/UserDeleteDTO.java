package com.openplatform.system.user.model.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

/** 用户删除参数。 */
@Data
public class UserDeleteDTO {
    @NotEmpty private List<Long> userIds;
}
