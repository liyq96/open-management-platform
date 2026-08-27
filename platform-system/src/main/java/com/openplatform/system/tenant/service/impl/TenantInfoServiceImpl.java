package com.openplatform.system.tenant.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openplatform.common.core.exception.BusinessException;
import com.openplatform.common.core.model.PageResult;
import com.openplatform.common.database.tenant.TenantContextHolder;
import com.openplatform.common.security.support.SecurityContextUtils;
import com.openplatform.system.error.SystemErrorCode;
import com.openplatform.system.tenant.converter.TenantInfoConverter;
import com.openplatform.system.tenant.mapper.TenantInfoMapper;
import com.openplatform.system.tenant.model.dto.*;
import com.openplatform.system.tenant.model.entity.TenantInfo;
import com.openplatform.system.tenant.model.vo.TenantInfoVO;
import com.openplatform.system.tenant.service.TenantInfoService;
import com.openplatform.system.tenant.service.TenantProvisioningService;
import com.openplatform.system.security.service.UserAccessCacheInvalidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
/** 租户管理服务实现。 */
@Service @RequiredArgsConstructor
public class TenantInfoServiceImpl implements TenantInfoService {
    private final TenantInfoMapper mapper; private final TenantProvisioningService tenantProvisioningService;
    private final UserAccessCacheInvalidationService cacheInvalidationService;
    @Override public PageResult<TenantInfoVO> page(TenantQueryDTO dto){var wrapper=new LambdaQueryWrapper<TenantInfo>().eq(dto.getEnabled()!=null,TenantInfo::getEnabled,dto.getEnabled()).and(StringUtils.hasText(dto.getKeyword()),q->q.like(TenantInfo::getTenantCode,dto.getKeyword()).or().like(TenantInfo::getTenantName,dto.getKeyword())).orderByAsc(TenantInfo::getTenantCode);Page<TenantInfo> result=mapper.selectPage(new Page<>(dto.getPage(),dto.getPageSize()),wrapper);return PageResult.of(result.getRecords().stream().map(TenantInfoConverter::toVO).toList(),result.getCurrent(),result.getSize(),result.getTotal());}
    @Override public TenantInfoVO detail(Long id){return TenantInfoConverter.toVO(requireTenant(id));}
    @Override @Transactional(rollbackFor=Exception.class) public Long create(TenantCreateDTO dto){validateCode(dto.getTenantCode(),null);if(!dto.getAdminPassword().equals(dto.getAdminConfirmPassword()))throw new BusinessException(SystemErrorCode.PASSWORD_CONFIRM_MISMATCH);Long sourceTenantId=SecurityContextUtils.requireTenantId();Long op=SecurityContextUtils.requireUserId();TenantInfo e=new TenantInfo();e.setTenantCode(dto.getTenantCode());e.setTenantName(dto.getTenantName());e.setEnabled(dto.getEnabled());e.setCreatedBy(op);e.setUpdatedBy(op);e.setVersion(0);e.setDeleted(false);mapper.insert(e);tenantProvisioningService.initialize(sourceTenantId,e.getId(),dto,op);return e.getId();}
    @Override @Transactional(rollbackFor=Exception.class) public void update(TenantUpdateDTO dto){TenantInfo e=requireTenant(dto.getTenantId());validateCode(dto.getTenantCode(),dto.getTenantId());boolean stateChanged=!e.getEnabled().equals(dto.getEnabled());e.setTenantCode(dto.getTenantCode());e.setTenantName(dto.getTenantName());e.setEnabled(dto.getEnabled());e.setUpdatedBy(SecurityContextUtils.requireUserId());mapper.updateById(e);if(stateChanged){try(TenantContextHolder.TenantScope ignored=TenantContextHolder.use(dto.getTenantId())){mapper.incrementTenantUserAuthVersion();}cacheInvalidationService.invalidateTenantAfterCommit(dto.getTenantId());}}
    private void validateCode(String code,Long excluded){if(mapper.selectCount(new LambdaQueryWrapper<TenantInfo>().eq(TenantInfo::getTenantCode,code).ne(excluded!=null,TenantInfo::getId,excluded))>0)throw new BusinessException(SystemErrorCode.TENANT_CODE_EXISTS);}
    private TenantInfo requireTenant(Long id){TenantInfo e=mapper.selectById(id);if(e==null)throw new BusinessException(SystemErrorCode.TENANT_NOT_FOUND);return e;}
}
