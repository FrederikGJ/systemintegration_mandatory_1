package dk.library.rest.dto;

import dk.library.db.model.Author;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body til POST og PUT /api/authors. Længderne følger kolonnerne i tauthor. surname må udelades. */
public record AuthorRequest(
        @NotBlank @Size(max = 40) String name,
        @Size(max = 60) String surname) {

    public Author toModel(int id) {
        return new Author(id, name, surname);
    }
}
