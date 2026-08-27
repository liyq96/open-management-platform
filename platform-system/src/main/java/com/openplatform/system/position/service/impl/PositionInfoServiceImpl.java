package com.openplatform.system.position.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.core.model.PageResult;
import com.openplatform.common.security.support.SecurityContextUtils;
import com.openplatform.system.error.SystemErrorCode;
import com.openplatform.system.position.converter.PositionInfoConverter;
import com.openplatform.system.position.mapper.PositionInfoMapper;
import com.openplatform.system.position.mapper.PositionReferenceMapper;
import com.openplatform.system.position.model.dto.*;
import com.openplatform.system.position.model.entity.PositionInfo;
import com.openplatform.system.position.model.vo.PositionInfoVO;
import com.openplatform.system.position.service.PositionInfoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
/** 岗位管理服务实现。 */
@Service @RequiredArgsConstructor
public class PositionInfoServiceImpl implements PositionInfoService {
    private final PositionInfoMapper mapper; private final PositionReferenceMapper referenceMapper;
    @Override public PageResult<PositionInfoVO> page(PositionQueryDTO dto){var wrapper=new LambdaQueryWrapper<PositionInfo>().eq(dto.getEnabled()!=null,PositionInfo::getEnabled,dto.getEnabled()).and(StringUtils.hasText(dto.getKeyword()),q->q.like(PositionInfo::getPositionCode,dto.getKeyword()).or().like(PositionInfo::getPositionName,dto.getKeyword())).orderByAsc(PositionInfo::getSortOrder).orderByAsc(PositionInfo::getId);Page<PositionInfo> result=mapper.selectPage(new Page<>(dto.getPage(),dto.getPageSize()),wrapper);return PageResult.of(result.getRecords().stream().map(PositionInfoConverter::toVO).toList(),result.getCurrent(),result.getSize(),result.getTotal());}
    @Override public PositionInfoVO detail(Long id){return PositionInfoConverter.toVO(requirePosition(id));}
    @Override @Transactional(rollbackFor=Exception.class) public Long create(PositionCreateDTO dto){validateCode(dto.getPositionCode(),null);Long op=SecurityContextUtils.requireUserId();PositionInfo e=new PositionInfo();e.setPositionCode(dto.getPositionCode());e.setPositionName(dto.getPositionName());e.setSortOrder(dto.getSortOrder());e.setEnabled(dto.getEnabled());e.setCreatedBy(op);e.setUpdatedBy(op);e.setVersion(0);e.setDeleted(false);mapper.insert(e);return e.getId();}
    @Override @Transactional(rollbackFor=Exception.class) public void update(PositionUpdateDTO dto){PositionInfo e=requirePosition(dto.getPositionId());validateCode(dto.getPositionCode(),dto.getPositionId());e.setPositionCode(dto.getPositionCode());e.setPositionName(dto.getPositionName());e.setSortOrder(dto.getSortOrder());e.setEnabled(dto.getEnabled());e.setUpdatedBy(SecurityContextUtils.requireUserId());mapper.updateById(e);}
    @Override @Transactional(rollbackFor=Exception.class) public void delete(PositionDeleteDTO dto){List<Long> ids=dto.getPositionIds().stream().distinct().toList();if(mapper.selectCount(new LambdaQueryWrapper<PositionInfo>().in(PositionInfo::getId,ids))!=ids.size())throw new BusinessException(SystemErrorCode.POSITION_NOT_FOUND);for(Long id:ids)if(referenceMapper.countUsers(id)>0)throw new BusinessException(SystemErrorCode.POSITION_IN_USE);mapper.deleteByIds(ids);}
    private void validateCode(String code,Long excluded){if(mapper.selectCount(new LambdaQueryWrapper<PositionInfo>().eq(PositionInfo::getPositionCode,code).ne(excluded!=null,PositionInfo::getId,excluded))>0)throw new BusinessException(SystemErrorCode.POSITION_CODE_EXISTS);}
    private PositionInfo requirePosition(Long id){PositionInfo e=mapper.selectOne(new LambdaQueryWrapper<PositionInfo>().eq(PositionInfo::getId,id));if(e==null)throw new BusinessException(SystemErrorCode.POSITION_NOT_FOUND);return e;}
}
