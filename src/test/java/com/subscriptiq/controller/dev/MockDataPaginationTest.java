package com.subscriptiq.controller.dev;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MockDataPaginationTest {
    @Test
    void pageHelperSlicesFilteredResultsAndHandlesOutOfRangePages() {
        List<Map<String, Object>> records = IntStream.range(0, 15)
                .mapToObj(index -> Map.<String, Object>of("name", "Item " + index))
                .toList();

        var firstPage = MockData.page(records, false, 0);
        var secondPage = MockData.page(records, false, 1);
        var outOfRangePage = MockData.page(records, false, 2);

        assertEquals(10, firstPage.getNumberOfElements());
        assertEquals(2, firstPage.getTotalPages());
        assertEquals("Item 10", secondPage.getContent().getFirst().get("name"));
        assertEquals(5, secondPage.getNumberOfElements());
        assertTrue(outOfRangePage.isEmpty());
    }

    @Test
    void searchMatchingIsTrimmedPartialAndCaseInsensitive() {
        assertTrue(MockSearch.matches("  NORTH  ", "Northstar Labs"));
        assertTrue(MockSearch.matches(null, "Any record"));
        assertFalse(MockSearch.matches("missing", "Northstar Labs"));
    }
}
