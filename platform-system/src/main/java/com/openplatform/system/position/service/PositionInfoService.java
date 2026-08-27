package com.openplatform.system.position.service;
import com.openplatform.common.core.model.PageResult;
import com.openplatform.system.position.model.dto.*;
import com.openplatform.system.position.model.vo.PositionInfoVO;
/** 岗位管理服务。 */
public interface PositionInfoService {
    PageResult<PositionInfoVO> page(PositionQueryDTO dto);
    PositionInfoVO detail(Long positionId);
    Long create(PositionCreateDTO dto);
    void update(PositionUpdateDTO dto);
    void delete(PositionDeleteDTO dto);
}
