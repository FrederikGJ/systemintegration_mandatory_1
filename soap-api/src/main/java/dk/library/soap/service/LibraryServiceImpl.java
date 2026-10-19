package dk.library.soap.service;

import dk.library.db.Database;
import dk.library.db.repository.AuthorRepository;
import dk.library.db.repository.BookRepository;
import dk.library.db.repository.PublishingCompanyRepository;
import dk.library.soap.Namespace;
import dk.library.soap.data.Author;
import dk.library.soap.data.Book;
import dk.library.soap.data.PublishingCompany;
import dk.library.soap.faults.ConflictFault;
import dk.library.soap.faults.NotFoundFault;
import dk.library.soap.faults.ValidationFault;
import dk.library.soap.messages.author.CreateAuthorRequest;
import dk.library.soap.messages.author.CreateAuthorResponse;
import dk.library.soap.messages.author.DeleteAuthorRequest;
import dk.library.soap.messages.author.DeleteAuthorResponse;
import dk.library.soap.messages.author.GetAuthorByIdRequest;
import dk.library.soap.messages.author.GetAuthorByIdResponse;
import dk.library.soap.messages.author.ListAuthorsRequest;
import dk.library.soap.messages.author.ListAuthorsResponse;
import dk.library.soap.messages.author.UpdateAuthorRequest;
import dk.library.soap.messages.author.UpdateAuthorResponse;
import dk.library.soap.messages.book.CreateBookRequest;
import dk.library.soap.messages.book.CreateBookResponse;
import dk.library.soap.messages.book.DeleteBookRequest;
import dk.library.soap.messages.book.DeleteBookResponse;
import dk.library.soap.messages.book.GetBookByIdRequest;
import dk.library.soap.messages.book.GetBookByIdResponse;
import dk.library.soap.messages.book.UpdateBookRequest;
import dk.library.soap.messages.book.UpdateBookResponse;
import dk.library.soap.messages.publishingcompany.CreatePublishingCompanyRequest;
import dk.library.soap.messages.publishingcompany.CreatePublishingCompanyResponse;
import dk.library.soap.messages.publishingcompany.DeletePublishingCompanyRequest;
import dk.library.soap.messages.publishingcompany.DeletePublishingCompanyResponse;
import dk.library.soap.messages.publishingcompany.GetPublishingCompanyByIdRequest;
import dk.library.soap.messages.publishingcompany.GetPublishingCompanyByIdResponse;
import dk.library.soap.messages.publishingcompany.ListPublishingCompaniesRequest;
import dk.library.soap.messages.publishingcompany.ListPublishingCompaniesResponse;
import dk.library.soap.messages.publishingcompany.UpdatePublishingCompanyRequest;
import dk.library.soap.messages.publishingcompany.UpdatePublishingCompanyResponse;
import jakarta.jws.WebService;

/**
 * Implementeringen. @WebService her binder klassen til kontrakten og giver wsdl:service "LibraryService"
 * og wsdl:port "LibraryPort". Data hentes og gemmes via repositories i db-modulet.
 *
 * Rækkefølgen af tjek i Update: først requestens egne felter (ValidationFault), så om id'et findes
 * (NotFoundFault), til sidst om forfatter og forlag findes (ValidationFault).
 */
@WebService(endpointInterface = "dk.library.soap.service.LibraryService",
        serviceName = "LibraryService",
        portName = "LibraryPort",
        targetNamespace = Namespace.URI)
public class LibraryServiceImpl implements LibraryService {

    // Kolonnestørrelser fra databasediagrammet. SQLite håndhæver dem ikke selv
    private static final int MAX_TITLE = 255;
    private static final int MAX_AUTHOR_NAME = 40;
    private static final int MAX_SURNAME = 60;
    private static final int MAX_COMPANY_NAME = 40;
    private static final int MIN_YEAR = 1900;   // fra opgaven
    private static final int MAX_YEAR = 9999;   // nPublishingYear er decimal(4,0)

    private final BookRepository books;
    private final AuthorRepository authors;
    private final PublishingCompanyRepository companies;

    /** JAX-WS kræver en offentlig no-arg-konstruktør (wsgen afviser klassen uden). Finder databasen via LIBRARY_DB. */
    public LibraryServiceImpl() {
        this(new Database());
    }

    public LibraryServiceImpl(Database db) {
        this.books = new BookRepository(db);
        this.authors = new AuthorRepository(db);
        this.companies = new PublishingCompanyRepository(db);
    }

    // ---------- Book ----------

    @Override
    public CreateBookResponse createBook(CreateBookRequest request) throws ValidationFault {
        var book = validBook(0, request.getTitle(), request.getAuthorId(),
                request.getPublishingCompanyId(), request.getPublishingYear());
        requireReferences(book);
        return new CreateBookResponse(books.create(book).id());
    }

    @Override
    public GetBookByIdResponse getBookById(GetBookByIdRequest request) throws NotFoundFault {
        return new GetBookByIdResponse(toSoap(existingBook(request.getId())));
    }

    @Override
    public UpdateBookResponse updateBook(UpdateBookRequest request) throws ValidationFault, NotFoundFault {
        int id = require("id", request.getId());
        var book = validBook(id, request.getTitle(), request.getAuthorId(),
                request.getPublishingCompanyId(), request.getPublishingYear());
        existingBook(id);
        requireReferences(book);
        if (!books.update(book)) {
            throw bookNotFound(id);
        }
        return new UpdateBookResponse(true, "Book " + id + " updated");
    }

    @Override
    public DeleteBookResponse deleteBook(DeleteBookRequest request) throws NotFoundFault {
        int id = existingBook(request.getId()).id();
        if (!books.delete(id)) {
            throw bookNotFound(id);
        }
        return new DeleteBookResponse(true, "Book " + id + " deleted");
    }

    // ---------- Author ----------

    @Override
    public CreateAuthorResponse createAuthor(CreateAuthorRequest request) throws ValidationFault {
        var author = validAuthor(0, request.getName(), request.getSurname());
        return new CreateAuthorResponse(authors.create(author).id());
    }

    @Override
    public GetAuthorByIdResponse getAuthorById(GetAuthorByIdRequest request) throws NotFoundFault {
        return new GetAuthorByIdResponse(toSoap(existingAuthor(request.getId())));
    }

    @Override
    public ListAuthorsResponse listAuthors(ListAuthorsRequest request) {
        return new ListAuthorsResponse(authors.findAll().stream().map(LibraryServiceImpl::toSoap).toList());
    }

    @Override
    public UpdateAuthorResponse updateAuthor(UpdateAuthorRequest request) throws ValidationFault, NotFoundFault {
        int id = require("id", request.getId());
        var author = validAuthor(id, request.getName(), request.getSurname());
        existingAuthor(id);
        if (!authors.update(author)) {
            throw authorNotFound(id);
        }
        return new UpdateAuthorResponse(true, "Author " + id + " updated");
    }

    @Override
    public DeleteAuthorResponse deleteAuthor(DeleteAuthorRequest request) throws NotFoundFault, ConflictFault {
        int id = existingAuthor(request.getId()).id();
        int bookCount = books.findByAuthor(id).size();
        if (bookCount > 0) {
            throw new ConflictFault("Author " + id + " cannot be deleted: " + bookCount + " book(s) reference it");
        }
        if (!authors.delete(id)) {
            throw authorNotFound(id);
        }
        return new DeleteAuthorResponse(true, "Author " + id + " deleted");
    }

    // ---------- PublishingCompany ----------

    @Override
    public CreatePublishingCompanyResponse createPublishingCompany(CreatePublishingCompanyRequest request)
            throws ValidationFault {
        var company = validCompany(0, request.getName());
        return new CreatePublishingCompanyResponse(companies.create(company).id());
    }

    @Override
    public GetPublishingCompanyByIdResponse getPublishingCompanyById(GetPublishingCompanyByIdRequest request)
            throws NotFoundFault {
        return new GetPublishingCompanyByIdResponse(toSoap(existingCompany(request.getId())));
    }

    @Override
    public ListPublishingCompaniesResponse listPublishingCompanies(ListPublishingCompaniesRequest request) {
        return new ListPublishingCompaniesResponse(
                companies.findAll().stream().map(LibraryServiceImpl::toSoap).toList());
    }

    @Override
    public UpdatePublishingCompanyResponse updatePublishingCompany(UpdatePublishingCompanyRequest request)
            throws ValidationFault, NotFoundFault {
        int id = require("id", request.getId());
        var company = validCompany(id, request.getName());
        existingCompany(id);
        if (!companies.update(company)) {
            throw companyNotFound(id);
        }
        return new UpdatePublishingCompanyResponse(true, "Publishing company " + id + " updated");
    }

    @Override
    public DeletePublishingCompanyResponse deletePublishingCompany(DeletePublishingCompanyRequest request)
            throws NotFoundFault, ConflictFault {
        int id = existingCompany(request.getId()).id();
        int bookCount = books.findByPublishingCompany(id).size();
        if (bookCount > 0) {
            throw new ConflictFault(
                    "Publishing company " + id + " cannot be deleted: " + bookCount + " book(s) reference it");
        }
        if (!companies.delete(id)) {
            throw companyNotFound(id);
        }
        return new DeletePublishingCompanyResponse(true, "Publishing company " + id + " deleted");
    }

    // ---------- Validering (ValidationFault) ----------

    /** Felterne fra CreateBook og UpdateBook. Bygger db-modellen, hvis de er i orden. */
    private static dk.library.db.model.Book validBook(int id, String title, Integer authorId,
            Integer publishingCompanyId, Integer publishingYear) throws ValidationFault {
        requireText("title", title, MAX_TITLE);
        int author = require("authorId", authorId);
        int company = require("publishingCompanyId", publishingCompanyId);
        int year = require("publishingYear", publishingYear);
        if (year < MIN_YEAR || year > MAX_YEAR) {
            throw new ValidationFault("publishingYear must be between " + MIN_YEAR + " and " + MAX_YEAR
                    + ", was " + year);
        }
        return new dk.library.db.model.Book(id, title, author, year, company);
    }

    private static dk.library.db.model.Author validAuthor(int id, String name, String surname)
            throws ValidationFault {
        requireText("name", name, MAX_AUTHOR_NAME);
        requireText("surname", surname, MAX_SURNAME);
        return new dk.library.db.model.Author(id, name, surname);
    }

    private static dk.library.db.model.PublishingCompany validCompany(int id, String name) throws ValidationFault {
        requireText("name", name, MAX_COMPANY_NAME);
        return new dk.library.db.model.PublishingCompany(id, name);
    }

    /** Databasen har ingen fremmednøgler, så det er her, en bog tjekkes for at pege på noget, der findes. */
    private void requireReferences(dk.library.db.model.Book book) throws ValidationFault {
        if (authors.findById(book.authorId()).isEmpty()) {
            throw new ValidationFault("authorId " + book.authorId() + " does not reference an existing author");
        }
        if (companies.findById(book.publishingCompanyId()).isEmpty()) {
            throw new ValidationFault("publishingCompanyId " + book.publishingCompanyId()
                    + " does not reference an existing publishing company");
        }
    }

    /** Integer-felter er null, når elementet mangler, og også når det indeholder tekst som "abc" (JAXB opgiver værdien). */
    private static int require(String field, Integer value) throws ValidationFault {
        if (value == null) {
            throw new ValidationFault(field + " is missing or not a valid integer");
        }
        return value;
    }

    private static void requireText(String field, String value, int maxLength) throws ValidationFault {
        if (value == null) {
            throw new ValidationFault(field + " is missing");
        }
        if (value.isBlank()) {
            throw new ValidationFault(field + " must not be empty");
        }
        if (value.length() > maxLength) {
            throw new ValidationFault(field + " must be at most " + maxLength + " characters, was " + value.length());
        }
    }

    // ---------- Opslag (NotFoundFault) ----------

    // Get og Delete kan kun kaste NotFoundFault, så et manglende id behandles som "findes ikke"
    private dk.library.db.model.Book existingBook(Integer id) throws NotFoundFault {
        if (id == null) {
            throw new NotFoundFault("Book id is missing");
        }
        return books.findById(id).orElseThrow(() -> bookNotFound(id));
    }

    private dk.library.db.model.Author existingAuthor(Integer id) throws NotFoundFault {
        if (id == null) {
            throw new NotFoundFault("Author id is missing");
        }
        return authors.findById(id).orElseThrow(() -> authorNotFound(id));
    }

    private dk.library.db.model.PublishingCompany existingCompany(Integer id) throws NotFoundFault {
        if (id == null) {
            throw new NotFoundFault("Publishing company id is missing");
        }
        return companies.findById(id).orElseThrow(() -> companyNotFound(id));
    }

    private static NotFoundFault bookNotFound(int id) {
        return new NotFoundFault("Book " + id + " does not exist");
    }

    private static NotFoundFault authorNotFound(int id) {
        return new NotFoundFault("Author " + id + " does not exist");
    }

    private static NotFoundFault companyNotFound(int id) {
        return new NotFoundFault("Publishing company " + id + " does not exist");
    }

    // ---------- db-model -> SOAP-type ----------

    private static Book toSoap(dk.library.db.model.Book b) {
        return new Book(b.id(), b.title(), b.authorId(), b.publishingCompanyId(), b.publishingYear());
    }

    private static Author toSoap(dk.library.db.model.Author a) {
        return new Author(a.id(), a.name(), a.surname());
    }

    private static PublishingCompany toSoap(dk.library.db.model.PublishingCompany p) {
        return new PublishingCompany(p.id(), p.name());
    }
}
