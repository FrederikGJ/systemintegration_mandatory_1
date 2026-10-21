package dk.library.rest.dto;

import dk.library.db.model.PublishingCompany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body til POST og PUT /api/publishers. Længden følger cName i tpublishingcompany. */
public record PublisherRequest(@NotBlank @Size(max = 40) String name) {

    public PublishingCompany toModel(int id) {
        return new PublishingCompany(id, name);
    }
}
