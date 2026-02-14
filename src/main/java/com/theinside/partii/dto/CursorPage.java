package com.theinside.partii.dto;

import java.util.List;

/**
 * Generic cursor-based pagination response.
 * Provides O(1) performance for pagination regardless of page depth.
 *
 * @param <T> the type of content in the page
 */
public record CursorPage<T>(
    List<T> content,
    String nextCursor,
    boolean hasNext,
    int size
) {
    public static <T> CursorPage<T> of(List<T> content, String nextCursor, int requestedSize) {
        boolean hasNext = content.size() > requestedSize;
        List<T> actualContent = hasNext ? content.subList(0, requestedSize) : content;
        return new CursorPage<>(
            actualContent,
            hasNext ? nextCursor : null,
            hasNext,
            actualContent.size()
        );
    }

    public static <T> CursorPage<T> empty() {
        return new CursorPage<>(List.of(), null, false, 0);
    }
}
