package dk.library.db.model;

/** Række i tpublishingcompany. */
public record PublishingCompany(int id, String name) {

    public PublishingCompany withId(int newId) {
        return new PublishingCompany(newId, name);
    }
}
