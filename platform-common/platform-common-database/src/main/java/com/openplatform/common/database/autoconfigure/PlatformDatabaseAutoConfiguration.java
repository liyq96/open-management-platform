package com.openplatform.common.database.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.openplatform.common.database.handler.PlatformMetaObjectHandler;
import com.openplatform.common.database.id.PlatformIdGenerator;
import com.openplatform.common.database.tenant.PlatformTenantLineHandler;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * PostgreSQL 和 MyBatis-Plus 公共配置。
 */
@AutoConfiguration
public class PlatformDatabaseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Clock platformDatabaseClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    public PlatformMetaObjectHandler platformMetaObjectHandler(Clock platformDatabaseClock) {
        return new PlatformMetaObjectHandler(platformDatabaseClock);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdentifierGenerator platformIdentifierGenerator() {
        return DefaultIdentifierGenerator.getInstance();
    }

    @Bean
    public PlatformIdGenerator platformIdGenerator(IdentifierGenerator identifierGenerator) {
        return new PlatformIdGenerator(identifierGenerator);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new PlatformTenantLineHandler()));
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
        return interceptor;
    }
}
