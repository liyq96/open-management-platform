package com.openplatform.common.database.id;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * 平台统一雪花 ID 生成器。
 *
 * <p>与 MyBatis-Plus {@code IdType.ASSIGN_ID} 共用同一个 IdentifierGenerator，
 * 避免业务主表和自定义批量 INSERT 使用不同雪花序列。</p>
 */
@RequiredArgsConstructor
public class PlatformIdGenerator {

    private final IdentifierGenerator identifierGenerator;

    public Long nextId() {
        return identifierGenerator.nextId(null).longValue();
    }

    public List<RelationInsertItem> relationItems(Collection<Long> targetIds) {
        return targetIds.stream()
                .map(targetId -> new RelationInsertItem(nextId(), targetId))
                .toList();
    }
}
