package dk.library.graphql;
public final class Inputs {
    private Inputs() {}
    public record BookInput(String title, int authorId, int publishingYear, int publishingCompanyId) {}
    public record AuthorInput(String name, String surname) {}
    public record PublisherInput(String name) {}
}
