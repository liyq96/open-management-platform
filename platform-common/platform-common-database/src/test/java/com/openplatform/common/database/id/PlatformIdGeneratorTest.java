package com.openplatform.common.database.id;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class PlatformIdGeneratorTest {

    @Test
    void shouldUseSharedIdentifierGeneratorForSingleAndRelationIds() {
        AtomicLong sequence = new AtomicLong(2_055_000_000_000_000_000L);
        IdentifierGenerator delegate = entity -> sequence.incrementAndGet();
        PlatformIdGenerator generator = new PlatformIdGenerator(delegate);

        assertEquals(2_055_000_000_000_000_001L, generator.nextId());
        List<RelationInsertItem> items = generator.relationItems(List.of(10L, 20L));

        assertEquals(2_055_000_000_000_000_002L, items.get(0).getId());
        assertEquals(10L, items.get(0).getTargetId());
        assertEquals(2_055_000_000_000_000_003L, items.get(1).getId());
        assertEquals(20L, items.get(1).getTargetId());
    }
}
