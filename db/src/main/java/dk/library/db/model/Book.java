package dk.library.db.model;

/** Række i tbook. publishingYear kan være null. */
public record Book(int id, String title, int authorId, Integer publishingYear, int publishingCompanyId) {

    public Book withId(int newId) {
        return new Book(newId, title, authorId, publishingYear, publishingCompanyId);
    }
}
