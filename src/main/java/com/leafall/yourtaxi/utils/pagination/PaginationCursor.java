package com.leafall.yourtaxi.utils.pagination;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaginationCursor {
    @NotNull
    @Schema(description = "Лимит", example = "10")
    private Integer limit;
    @NotNull
    @Schema(description = "Текущая страница", example = "1")
    private Integer page;
    @NotNull
    @Schema(description = "Количество элементов", example = "100")
    private Long total;

    public PaginationCursor(PaginationParams params, Long total) {
        this.limit = params.limit();
        this.page = params.page();
        this.total = total;
    }
}
