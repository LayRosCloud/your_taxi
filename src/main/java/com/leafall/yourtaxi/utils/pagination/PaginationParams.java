package com.leafall.yourtaxi.utils.pagination;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PaginationParams(@Parameter(description = "Лимит", example = "0") Integer limit,
                               @Parameter(description = "Номер страницы", example = "0") Integer page) {

    public PaginationParams {
        if (limit == null) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100;
        } else if (limit <= 0) {
            limit = 1;
        }
        if (page == null || page <= 0) {
            page = 0;
        }
    }

    public Pageable getPageable(Boolean isAscending, String... args) {
        Sort sort = null;
        if (isAscending) {
            sort = Sort.by(args).ascending();
        } else {
            sort = Sort.by(args).descending();
        }
        return PageRequest.of(page, limit, sort);
    }
}
