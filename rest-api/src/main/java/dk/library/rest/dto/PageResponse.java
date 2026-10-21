package dk.library.rest.dto;

import java.util.List;

/** Én side af en liste. total er antallet i alt, så klienten ved, hvornår der ikke er flere sider. */
public record PageResponse<T>(List<T> items, int limit, int offset, int total) {
}
