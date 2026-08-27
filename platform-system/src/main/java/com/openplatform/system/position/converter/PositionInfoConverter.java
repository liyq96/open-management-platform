package com.openplatform.system.position.converter;
import com.openplatform.system.position.model.entity.PositionInfo;
import com.openplatform.system.position.model.vo.PositionInfoVO;
/** 岗位对象转换器。 */
public final class PositionInfoConverter { private PositionInfoConverter(){ } public static PositionInfoVO toVO(PositionInfo e){PositionInfoVO v=new PositionInfoVO();v.setPositionId(e.getId());v.setPositionCode(e.getPositionCode());v.setPositionName(e.getPositionName());v.setSortOrder(e.getSortOrder());v.setEnabled(e.getEnabled());v.setCreatedAt(e.getCreatedAt());return v;} }
