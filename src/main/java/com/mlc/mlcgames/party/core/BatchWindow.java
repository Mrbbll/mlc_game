package com.mlc.mlcgames.party.core;

/** Prevents overflow when shutdown drains a partly processed arena journal. */
final class BatchWindow {
    static int end(int size, int cursor, int budget) {
        if (cursor < 0 || cursor > size || budget < 0) throw new IllegalArgumentException("非法批处理窗口");
        return (int) Math.min(size, (long) cursor + budget);
    }
}
