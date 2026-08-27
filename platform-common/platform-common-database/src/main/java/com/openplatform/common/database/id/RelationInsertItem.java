package com.openplatform.common.database.id;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 自定义关系表批量写入项。 */
@Getter
@RequiredArgsConstructor
public class RelationInsertItem {

    private final Long id;

    private final Long targetId;
}
