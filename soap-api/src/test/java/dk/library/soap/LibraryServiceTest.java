package dk.library.soap;

import dk.library.db.Database;
import dk.library.soap.faults.ConflictFault;
import dk.library.soap.faults.NotFoundFault;
import dk.library.soap.faults.ValidationFault;
import dk.library.soap.messages.author.CreateAuthorRequest;
import dk.library.soap.messages.author.DeleteAuthorRequest;
import dk.library.soap.messages.author.GetAuthorByIdRequest;
import dk.library.soap.messages.author.ListAuthorsRequest;
import dk.library.soap.messages.author.UpdateAuthorRequest;
import dk.library.soap.messages.book.CreateBookRequest;
import dk.library.soap.messages.book.DeleteBookRequest;
import dk.library.soap.messages.book.GetBookByIdRequest;
import dk.library.soap.messages.book.UpdateBookRequest;
import dk.library.soap.messages.publishingcompany.CreatePublishingCompanyRequest;
import dk.library.soap.messages.publishingcompany.DeletePublishingCompanyRequest;
import dk.library.soap.messages.publishingcompany.GetPublishingCompanyByIdRequest;
import dk.library.soap.messages.publishingcompany.ListPublishingCompaniesRequest;
import dk.library.soap.messages.publishingcompany.UpdatePublishingCompanyRequest;
import dk.library.soap.service.LibraryService;
import dk.library.soap.service.LibraryServiceImpl;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.Service;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Publicerer endpointet på en ledig port mod en midlertidig kopi af library.db og kalder det som en rigtig klient:
 * en JAX-WS-proxy bygget ud fra WSDL'en, og rå SOAP-XML over HTTP til de tilfælde, proxyen ikke kan sende.
 */
class LibraryServiceTest {

    private static Endpoint endpoint;
    private static String address;
    private static LibraryService client;

    @BeforeAll
    static void publish(@TempDir Path tmp) throws IOException {
        var db = new Database(Files.copy(Path.of(Database.DEFAULT_PATH), tmp.resolve("library.db")));
        try (var socket = new ServerSocket(0)) {
            address = "http://localhost:" + socket.getLocalPort() + "/library";
        }
        endpoint = Endpoint.publish(address, new LibraryServiceImpl(db));

        var service = Service.create(URI.create(address + "?wsdl").toURL(),
                new QName(Namespace.URI, "LibraryService"));
        client = service.getPort(new QName(Namespace.URI, "LibraryPort"), LibraryService.class);
    }

    @AfterAll
    static void stop() {
        endpoint.stop();
    }

    // ---------- Book ----------

    @Test
    void getsSeedBook() throws Exception {
        var book = client.getBookById(new GetBookByIdRequest(1000)).getBook();
        assertEquals("Harry Potter and the Sorcerer's Stone (Book 1)", book.getTitle());
        assertEquals(394, book.getAuthorId());
        assertEquals(74, book.getPublishingCompanyId());
        assertEquals(1976, book.getPublishingYear());
    }

    @Test
    void bookCrudRoundTrip() throws Exception {
        int id = client.createBook(new CreateBookRequest("Testbog", 394, 74, 1900)).getId();
        assertTrue(id > 0);
        assertEquals(1900, client.getBookById(new GetBookByIdRequest(id)).getBook().getPublishingYear());

        var updated = client.updateBook(new UpdateBookRequest(id, "Rettet titel", 1, 158, 2024));
        assertTrue(updated.isSuccess());
        var book = client.getBookById(new GetBookByIdRequest(id)).getBook();
        assertEquals("Rettet titel", book.getTitle());
        assertEquals(158, book.getPublishingCompanyId());

        assertTrue(client.deleteBook(new DeleteBookRequest(id)).isSuccess());
        assertThrows(NotFoundFault.class, () -> client.getBookById(new GetBookByIdRequest(id)));
        assertThrows(NotFoundFault.class, () -> client.deleteBook(new DeleteBookRequest(id)));
    }

    @Test
    void unknownOrMissingBookIdGivesNotFoundFault() {
        var fault = assertThrows(NotFoundFault.class, () -> client.getBookById(new GetBookByIdRequest(-1)));
        assertEquals("Book -1 does not exist", fault.getFaultInfo().getMessage());
        assertThrows(NotFoundFault.class, () -> client.getBookById(new GetBookByIdRequest(null)));
    }

    @Test
    void invalidBookGivesValidationFault() {
        assertValidationFault(() -> client.createBook(new CreateBookRequest(null, 394, 74, 2000)), "title is missing");
        assertValidationFault(() -> client.createBook(new CreateBookRequest("  ", 394, 74, 2000)), "title must not be empty");
        assertValidationFault(() -> client.createBook(new CreateBookRequest("x".repeat(256), 394, 74, 2000)),
                "title must be at most 255 characters, was 256");
        assertValidationFault(() -> client.createBook(new CreateBookRequest("Bog", 394, 74, null)),
                "publishingYear is missing or not a valid integer");
        assertValidationFault(() -> client.createBook(new CreateBookRequest("Bog", 394, 74, 1899)),
                "publishingYear must be between 1900 and 9999, was 1899");
        assertValidationFault(() -> client.createBook(new CreateBookRequest("Bog", 999999, 74, 2000)),
                "authorId 999999 does not reference an existing author");
        assertValidationFault(() -> client.createBook(new CreateBookRequest("Bog", 394, 999999, 2000)),
                "publishingCompanyId 999999 does not reference an existing publishing company");
    }

    @Test
    void updateBookChecksFieldsThenIdThenReferences() {
        assertValidationFault(() -> client.updateBook(new UpdateBookRequest(null, "Bog", 394, 74, 2000)),
                "id is missing or not a valid integer");
        assertValidationFault(() -> client.updateBook(new UpdateBookRequest(999999, "Bog", 394, 74, 1800)),
                "publishingYear must be between 1900 and 9999, was 1800");
        assertThrows(NotFoundFault.class, () -> client.updateBook(new UpdateBookRequest(999999, "Bog", 999999, 74, 2000)));
        assertValidationFault(() -> client.updateBook(new UpdateBookRequest(1000, "Bog", 999999, 74, 2000)),
                "authorId 999999 does not reference an existing author");
    }

    // ---------- Author ----------

    @Test
    void authorCrudRoundTrip() throws Exception {
        int id = client.createAuthor(new CreateAuthorRequest("Test", "Forfatter")).getId();
        assertEquals("Forfatter", client.getAuthorById(new GetAuthorByIdRequest(id)).getAuthor().getSurname());

        assertTrue(client.updateAuthor(new UpdateAuthorRequest(id, "Rettet", "Navn")).isSuccess());
        assertEquals("Rettet", client.getAuthorById(new GetAuthorByIdRequest(id)).getAuthor().getName());

        assertTrue(client.deleteAuthor(new DeleteAuthorRequest(id)).isSuccess());
        assertThrows(NotFoundFault.class, () -> client.getAuthorById(new GetAuthorByIdRequest(id)));
    }

    @Test
    void listsAuthors() {
        var authors = client.listAuthors(new ListAuthorsRequest()).getAuthors();
        assertTrue(authors.size() >= 508);
        assertEquals("Aeschylus", authors.getFirst().getName());
    }

    @Test
    void surnameIsRequired() {
        assertValidationFault(() -> client.createAuthor(new CreateAuthorRequest("Homer", null)), "surname is missing");
        assertValidationFault(() -> client.createAuthor(new CreateAuthorRequest("Homer", "")), "surname must not be empty");
        assertThrows(NotFoundFault.class, () -> client.updateAuthor(new UpdateAuthorRequest(999999, "A", "B")));
    }

    @Test
    void deletingAuthorWithBooksGivesConflictFault() {
        var fault = assertThrows(ConflictFault.class, () -> client.deleteAuthor(new DeleteAuthorRequest(394)));
        assertTrue(fault.getFaultInfo().getMessage().startsWith("Author 394 cannot be deleted"));
        assertThrows(NotFoundFault.class, () -> client.deleteAuthor(new DeleteAuthorRequest(999999)));
    }

    // ---------- PublishingCompany ----------

    @Test
    void publishingCompanyCrudRoundTrip() throws Exception {
        int id = client.createPublishingCompany(new CreatePublishingCompanyRequest("Testforlag")).getId();
        assertEquals("Testforlag",
                client.getPublishingCompanyById(new GetPublishingCompanyByIdRequest(id)).getPublishingCompany().getName());

        assertTrue(client.updatePublishingCompany(new UpdatePublishingCompanyRequest(id, "Rettet forlag")).isSuccess());
        assertTrue(client.deletePublishingCompany(new DeletePublishingCompanyRequest(id)).isSuccess());
        assertThrows(NotFoundFault.class,
                () -> client.getPublishingCompanyById(new GetPublishingCompanyByIdRequest(id)));
    }

    @Test
    void listsPublishingCompanies() {
        assertTrue(client.listPublishingCompanies(new ListPublishingCompaniesRequest()).getPublishingCompanies().size() >= 158);
    }

    @Test
    void publishingCompanyRules() {
        assertValidationFault(() -> client.createPublishingCompany(new CreatePublishingCompanyRequest("x".repeat(41))),
                "name must be at most 40 characters, was 41");
        assertThrows(ConflictFault.class, () -> client.deletePublishingCompany(new DeletePublishingCompanyRequest(74)));
    }

    // ---------- Rå SOAP-XML ----------

    @Test
    void faultOnTheWire() throws Exception {
        var response = post("GetBookById", """
                <lib:GetBookByIdRequest><id>-1</id></lib:GetBookByIdRequest>""");
        assertEquals(500, response.statusCode());   // SOAP 1.1 svarer 500 ved alle faults
        assertTrue(response.body().contains("<faultstring>Book -1 does not exist</faultstring>"));
        assertTrue(response.body().contains("NotFoundFault xmlns:ns2=\"" + Namespace.URI + "\"><message>"));
    }

    @Test
    void textInNumberFieldGivesValidationFault() throws Exception {
        var response = post("CreateBook", """
                <lib:CreateBookRequest>
                  <title>Bog</title><authorId>394</authorId><publishingCompanyId>74</publishingCompanyId>
                  <publishingYear>abc</publishingYear>
                </lib:CreateBookRequest>""");
        assertEquals(500, response.statusCode());
        assertTrue(response.body().contains("ValidationFault xmlns:ns2=\"" + Namespace.URI + "\">"
                + "<message>publishingYear is missing or not a valid integer</message>"));
    }

    private static HttpResponse<String> post(String operation, String body) throws Exception {
        var envelope = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:lib="%s">
                  <soapenv:Body>%s</soapenv:Body>
                </soapenv:Envelope>""".formatted(Namespace.URI, body);
        var request = HttpRequest.newBuilder(URI.create(address))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"" + Namespace.URI + operation + "\"")
                .POST(HttpRequest.BodyPublishers.ofString(envelope))
                .build();
        try (var http = HttpClient.newHttpClient()) {
            return http.send(request, HttpResponse.BodyHandlers.ofString());
        }
    }

    private static void assertValidationFault(org.junit.jupiter.api.function.Executable call, String message) {
        var fault = assertThrows(ValidationFault.class, call);
        assertEquals(message, fault.getFaultInfo().getMessage());
    }
}
