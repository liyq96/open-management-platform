package com.openplatform.common.database.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import java.time.Clock;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;

/**
 * 自动填充实体创建和更新时间。
 */
@RequiredArgsConstructor
public class PlatformMetaObjectHandler implements MetaObjectHandler {

    private final Clock clock;

    @Override
    public void insertFill(MetaObject metaObject) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        fillWhenEmpty(metaObject, "createdAt", now);
        fillWhenEmpty(metaObject, "updatedAt", now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        setFieldValByName("updatedAt", OffsetDateTime.now(clock), metaObject);
    }

    private void fillWhenEmpty(MetaObject metaObject, String fieldName, OffsetDateTime value) {
        if (metaObject.hasSetter(fieldName) && getFieldValByName(fieldName, metaObject) == null) {
            setFieldValByName(fieldName, value, metaObject);
        }
    }
}
