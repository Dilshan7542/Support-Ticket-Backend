package lk.di47.ticket.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PaginationUtil {
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;

    private PaginationUtil() {
    }

    public static Pageable toPageable(Integer page, Integer size) {
        int resolvedPage = page == null || page < 0 ? DEFAULT_PAGE : page;
        int resolvedSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return PageRequest.of(resolvedPage, resolvedSize);
    }

    public static Pageable toPageable(Integer page, Integer size, Sort sort) {
        int resolvedPage = page == null || page < 0 ? DEFAULT_PAGE : page;
        int resolvedSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return PageRequest.of(resolvedPage, resolvedSize, sort);
    }
}
