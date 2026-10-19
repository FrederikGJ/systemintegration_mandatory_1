package dk.library.graphql;
import dk.library.db.model.*;
import dk.library.graphql.Inputs.*;
import java.util.List;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
@Controller
public class LibraryController {
    private final LibraryService service;
    public LibraryController(LibraryService service) { this.service = service; }
    @QueryMapping public Book bookById(@Argument("id") String id) { return service.book(LibraryService.id(id)); }
    @QueryMapping public List<Author> authors() { return service.authors(); }
    @QueryMapping public List<PublishingCompany> publishers() { return service.publishers(); }
    @SchemaMapping(typeName="Book", field="author") public Author author(Book book) { return service.author(book.authorId()); }
    @SchemaMapping(typeName="Book", field="publisher") public PublishingCompany publisher(Book book) { return service.publisher(book.publishingCompanyId()); }
    @MutationMapping public Book createBook(@Argument("input") BookInput input) { return service.createBook(input); }
    @MutationMapping public Book updateBook(@Argument("id") String id, @Argument("input") BookInput input) { return service.updateBook(LibraryService.id(id), input); }
    @MutationMapping public boolean deleteBook(@Argument("id") String id) { return service.deleteBook(LibraryService.id(id)); }
    @MutationMapping public Author createAuthor(@Argument("input") AuthorInput input) { return service.createAuthor(input); }
    @MutationMapping public Author updateAuthor(@Argument("id") String id, @Argument("input") AuthorInput input) { return service.updateAuthor(LibraryService.id(id), input); }
    @MutationMapping public boolean deleteAuthor(@Argument("id") String id) { return service.deleteAuthor(LibraryService.id(id)); }
    @MutationMapping public PublishingCompany createPublisher(@Argument("input") PublisherInput input) { return service.createPublisher(input); }
    @MutationMapping public PublishingCompany updatePublisher(@Argument("id") String id, @Argument("input") PublisherInput input) { return service.updatePublisher(LibraryService.id(id), input); }
    @MutationMapping public boolean deletePublisher(@Argument("id") String id) { return service.deletePublisher(LibraryService.id(id)); }
}
