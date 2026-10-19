package dk.library.graphql;
import dk.library.db.model.*;
import dk.library.db.repository.*;
import dk.library.graphql.Inputs.*;
import java.util.List;
import org.springframework.stereotype.Service;

/** Validation belongs to this API; the shared repositories remain unchanged. */
@Service
public class LibraryService {
    private final BookRepository books;
    private final AuthorRepository authors;
    private final PublishingCompanyRepository publishers;
    public LibraryService(BookRepository books, AuthorRepository authors, PublishingCompanyRepository publishers) {
        this.books = books; this.authors = authors; this.publishers = publishers;
    }
    public Book book(int id) { return books.findById(id).orElseThrow(() -> missing("Book", id)); }
    public Author author(int id) { return authors.findById(id).orElseThrow(() -> missing("Author", id)); }
    public PublishingCompany publisher(int id) { return publishers.findById(id).orElseThrow(() -> missing("Publisher", id)); }
    public List<Author> authors() { return authors.findAll(); }
    public List<PublishingCompany> publishers() { return publishers.findAll(); }
    public Book createBook(BookInput input) { return books.create(validBook(0, input)); }
    public Book updateBook(int id, BookInput input) {
        book(id); var value = validBook(id, input);
        if (!books.update(value)) throw missing("Book", id);
        return value;
    }
    public boolean deleteBook(int id) {
        if (!books.delete(id)) throw missing("Book", id);
        return true;
    }
    public Author createAuthor(AuthorInput input) { return authors.create(validAuthor(0, input)); }
    public Author updateAuthor(int id, AuthorInput input) {
        author(id); var value = validAuthor(id, input);
        if (!authors.update(value)) throw missing("Author", id);
        return value;
    }
    public boolean deleteAuthor(int id) {
        author(id);
        if (!books.findByAuthor(id).isEmpty()) throw new ApiException("CONFLICT", "Author is referenced by books");
        if (!authors.delete(id)) throw missing("Author", id);
        return true;
    }
    public PublishingCompany createPublisher(PublisherInput input) {
        return publishers.create(new PublishingCompany(0, text("name", input.name(), 40)));
    }
    public PublishingCompany updatePublisher(int id, PublisherInput input) {
        publisher(id); var value = new PublishingCompany(id, text("name", input.name(), 40));
        if (!publishers.update(value)) throw missing("Publisher", id);
        return value;
    }
    public boolean deletePublisher(int id) {
        publisher(id);
        if (!books.findByPublishingCompany(id).isEmpty()) throw new ApiException("CONFLICT", "Publisher is referenced by books");
        if (!publishers.delete(id)) throw missing("Publisher", id);
        return true;
    }
    private Book validBook(int id, BookInput input) {
        var title = text("title", input.title(), 255);
        if (input.publishingYear() < 1 || input.publishingYear() > 9999)
            throw new ApiException("VALIDATION_ERROR", "publishingYear must be between 1 and 9999");
        if (authors.findById(input.authorId()).isEmpty())
            throw new ApiException("VALIDATION_ERROR", "authorId must reference an existing author");
        if (publishers.findById(input.publishingCompanyId()).isEmpty())
            throw new ApiException("VALIDATION_ERROR", "publishingCompanyId must reference an existing publisher");
        return new Book(id, title, input.authorId(), input.publishingYear(), input.publishingCompanyId());
    }
    private Author validAuthor(int id, AuthorInput input) {
        var name = text("name", input.name(), 40);
        // Empty surname is valid for single-name authors, as in the supplied database.
        if (input.surname() == null || input.surname().length() > 60)
            throw new ApiException("VALIDATION_ERROR", "surname is required and must be at most 60 characters");
        return new Author(id, name, input.surname().trim());
    }
    private static String text(String field, String value, int max) {
        if (value == null || value.isBlank() || value.length() > max)
            throw new ApiException("VALIDATION_ERROR", field + " must contain 1 to " + max + " characters");
        return value.trim();
    }
    private static ApiException missing(String type, int id) { return new ApiException("NOT_FOUND", type + " " + id + " does not exist"); }
    public static int id(String value) {
        try { int id = Integer.parseInt(value); if (id > 0) return id; }
        catch (NumberFormatException ignored) { }
        throw new ApiException("VALIDATION_ERROR", "id must be a positive 32-bit integer");
    }
}
