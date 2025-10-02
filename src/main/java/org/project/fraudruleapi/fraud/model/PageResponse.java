package org.project.fraudruleapi.fraud.model;

import lombok.Builder;

import java.util.List;

@Builder
public record PageResponse<T>(
        List<T> content,
        long page,
        long size,
        long totalElements,
        long totalPages
) {
}