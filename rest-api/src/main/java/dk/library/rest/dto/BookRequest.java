package dk.library.rest.dto;

import dk.library.db.model.Book;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body til POST og PUT /api/books. publishingYear må udelades (NULL i databasen).
 * Integer i stedet for int, så et manglende felt bliver null og fanges af @NotNull i stedet for at blive 0.
 */
public record BookRequest(
        @NotBlank @Size(max = 255) String title,
        @NotNull Integer authorId,
        @Min(0) @Max(9999) Integer publishingYear,
        @NotNull Integer publishingCompanyId) {

    public Book toModel(int id) {
        return new Book(id, title, authorId, publishingYear, publishingCompanyId);
    }
}
