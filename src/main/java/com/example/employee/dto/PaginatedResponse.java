package com.example.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic DTO for paginated responses to provide clear metadata to API clients.
 *
 * @param <T> The type of the items in the paginated list.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;

    /**
     * Factory method to create a PaginatedResponse from a Spring Data Page object.
     *
     * @param page The Spring Data Page object.
     * @param <T>  The item type.
     * @return A new PaginatedResponse.
     */
    public static <T> PaginatedResponse<T> from(Page<T> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
    }
}
