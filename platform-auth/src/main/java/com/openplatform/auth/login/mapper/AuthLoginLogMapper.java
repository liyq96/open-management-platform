package com.openplatform.auth.login.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 登录日志写入。 */
@Mapper
public interface AuthLoginLogMapper {

    @Insert("""
            INSERT INTO login_log(
                id, user_id, username, login_ip,
                user_agent, success, failure_reason, created_at
            ) VALUES (
                #{id}, #{userId}, #{username},
                #{loginIp}, #{userAgent}, #{success}, #{failureReason}, CURRENT_TIMESTAMP
            )
            """)
    int insert(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("username") String username,
            @Param("loginIp") String loginIp,
            @Param("userAgent") String userAgent,
            @Param("success") boolean success,
            @Param("failureReason") String failureReason);
}
