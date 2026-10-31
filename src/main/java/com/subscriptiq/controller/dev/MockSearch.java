package com.subscriptiq.controller.dev;

import java.util.Locale;

final class MockSearch {
    private MockSearch() {
    }

    static boolean matches(String query, Object... values) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        for (Object value : values) {
            if (value != null && value.toString().toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                return true;
            }
        }
        return false;
    }

    static int pageNumber(String page) {
        if (page == null || page.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(page));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
