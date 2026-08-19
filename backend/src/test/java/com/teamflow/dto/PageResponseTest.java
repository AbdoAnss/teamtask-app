package com.teamflow.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    void fromShouldCopyPaginationFields() {
        var page = new PageImpl<>(List.of("a", "b"), PageRequest.of(1, 2), 7);

        PageResponse<String> response = PageResponse.from(page);

        assertEquals(List.of("a", "b"), response.getContent());
        assertEquals(1, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(7, response.getTotalElements());
        assertEquals(4, response.getTotalPages());
        assertFalse(response.isLast());
    }

    @Test
    void fromShouldMarkSinglePageAsLast() {
        var page = new PageImpl<>(List.of("a"), PageRequest.of(0, 10), 1);

        assertTrue(PageResponse.from(page).isLast());
    }
}
