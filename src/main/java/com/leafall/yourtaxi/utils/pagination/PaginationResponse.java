package com.leafall.yourtaxi.utils.pagination;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public record PaginationResponse<T>(@NotNull List<T> items, @NotNull PaginationCursor cursor) {
}

