package dk.library.db.model;

/** Række i tauthor. surname kan være null. */
public record Author(int id, String name, String surname) {

    public Author withId(int newId) {
        return new Author(newId, name, surname);
    }
}
