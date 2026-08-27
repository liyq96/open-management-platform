package com.openplatform.common.core.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultTest {

    @Test
    void shouldCreateDefensiveCopyOfRecords() {
        List<String> source = new ArrayList<>();
        source.add("admin");

        PageResult<String> result = PageResult.of(source, 1, 20, 1);
        source.add("operator");

        assertEquals(List.of("admin"), result.getRecords());
        assertThrows(UnsupportedOperationException.class, () -> result.getRecords().add("auditor"));
    }
}
