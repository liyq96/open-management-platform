package com.openplatform.system.audit.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.openplatform.system.audit.model.vo.LoginLogVO;
import com.openplatform.system.audit.model.vo.OperationLogVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** 审计日志数据访问。 */
@Mapper
public interface AuditLogMapper {

    @Insert("""
            INSERT INTO operation_log(
                id, user_id, module_name, operation_name,
                request_id, request_path, success, created_at
            ) VALUES (
                #{id}, #{userId}, #{moduleName},
                #{operationName}, #{requestId}, #{requestPath}, #{success}, CURRENT_TIMESTAMP
            )
            """)
    int insertOperation(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("moduleName") String moduleName,
            @Param("operationName") String operationName,
            @Param("requestId") String requestId,
            @Param("requestPath") String requestPath,
            @Param("success") boolean success);

    @Select("""
            <script>
            SELECT id AS log_id, username, login_ip, user_agent, success,
                   failure_reason, created_at
            FROM login_log
            WHERE 1=1
            <if test="username != null and username != ''">
                AND username ILIKE CONCAT('%', #{username}, '%')
            </if>
            <if test="success != null">AND success=#{success}</if>
            ORDER BY created_at DESC
            </script>
            """)
    IPage<LoginLogVO> selectLoginPage(
            Page<LoginLogVO> page,
            @Param("username") String username,
            @Param("success") Boolean success);

    @Select("""
            <script>
            SELECT id AS log_id, user_id, module_name, operation_name,
                   request_id, request_path, success, created_at
            FROM operation_log
            WHERE 1=1
            <if test="moduleName != null and moduleName != ''">AND module_name=#{moduleName}</if>
            <if test="success != null">AND success=#{success}</if>
            ORDER BY created_at DESC
            </script>
            """)
    IPage<OperationLogVO> selectOperationPage(
            Page<OperationLogVO> page,
            @Param("moduleName") String moduleName,
            @Param("success") Boolean success);
}
