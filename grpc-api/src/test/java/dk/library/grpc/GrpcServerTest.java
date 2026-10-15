package dk.library.grpc;

import dk.library.db.Database;
import dk.library.grpc.proto.Author;
import dk.library.grpc.proto.AuthorServiceGrpc;
import dk.library.grpc.proto.Book;
import dk.library.grpc.proto.BookServiceGrpc;
import dk.library.grpc.proto.CreateAuthorRequest;
import dk.library.grpc.proto.CreateBookRequest;
import dk.library.grpc.proto.DeleteBookRequest;
import dk.library.grpc.proto.GetAuthorRequest;
import dk.library.grpc.proto.GetBookRequest;
import dk.library.grpc.proto.ListBooksRequest;
import dk.library.grpc.proto.UpdateBookRequest;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Server og klient i samme JVM (in-process) mod en midlertidig kopi af library.db. */
class GrpcServerTest {

    private static Server server;
    private static ManagedChannel channel;
    private static BookServiceGrpc.BookServiceBlockingStub books;
    private static AuthorServiceGrpc.AuthorServiceBlockingStub authors;

    @BeforeAll
    static void startServer(@TempDir Path tmp) throws IOException {
        var db = new Database(Files.copy(Path.of(Database.DEFAULT_PATH), tmp.resolve("library.db")));
        var name = InProcessServerBuilder.generateName();
        server = GrpcServer.addServices(InProcessServerBuilder.forName(name).directExecutor(), db).build().start();
        channel = InProcessChannelBuilder.forName(name).directExecutor().build();
        books = BookServiceGrpc.newBlockingStub(channel);
        authors = AuthorServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    static void stopServer() {
        channel.shutdownNow();
        server.shutdownNow();
    }

    @Test
    void getsSeedBook() {
        var book = books.getBook(GetBookRequest.newBuilder().setId(1000).build());
        assertEquals("Harry Potter and the Sorcerer's Stone (Book 1)", book.getTitle());
        assertEquals(394, book.getAuthorId());
        assertEquals(1976, book.getPublishingYear());
    }

    @Test
    void filtersBooksByAuthor() {
        var all = books.listBooks(ListBooksRequest.getDefaultInstance()).getBooksList();
        var byAuthor = books.listBooks(ListBooksRequest.newBuilder().setAuthorId(394).build()).getBooksList();
        assertFalse(byAuthor.isEmpty());
        assertTrue(byAuthor.size() < all.size());
        assertTrue(byAuthor.stream().allMatch(book -> book.getAuthorId() == 394));
    }

    @Test
    void bookCrudRoundTrip() {
        var created = books.createBook(CreateBookRequest.newBuilder()
                .setBook(Book.newBuilder().setTitle("Testbog").setAuthorId(394).setPublishingCompanyId(74))
                .build());
        assertTrue(created.getId() > 0);
        assertFalse(created.hasPublishingYear()); // ikke sat = NULL i db

        var updated = books.updateBook(UpdateBookRequest.newBuilder()
                .setBook(created.toBuilder().setTitle("Rettet titel").setPublishingYear(2024))
                .build());
        var id = GetBookRequest.newBuilder().setId(created.getId()).build();
        assertEquals(updated, books.getBook(id));

        books.deleteBook(DeleteBookRequest.newBuilder().setId(created.getId()).build());
        assertStatus(Status.Code.NOT_FOUND, () -> books.getBook(id));
    }

    @Test
    void authorWithoutSurnameKeepsNull() {
        var created = authors.createAuthor(CreateAuthorRequest.newBuilder()
                .setAuthor(Author.newBuilder().setName("Platon"))
                .build());
        var fetched = authors.getAuthor(GetAuthorRequest.newBuilder().setId(created.getId()).build());
        assertEquals("Platon", fetched.getName());
        assertFalse(fetched.hasSurname());
    }

    @Test
    void mapsErrorsToStatusCodes() {
        assertStatus(Status.Code.NOT_FOUND, () -> books.getBook(GetBookRequest.newBuilder().setId(-1).build()));
        assertStatus(Status.Code.NOT_FOUND, () -> books.deleteBook(DeleteBookRequest.newBuilder().setId(-1).build()));
        assertStatus(Status.Code.NOT_FOUND, () -> books.updateBook(UpdateBookRequest.newBuilder()
                .setBook(Book.newBuilder().setId(-1).setTitle("Findes ikke"))
                .build()));
        assertStatus(Status.Code.INVALID_ARGUMENT, () -> books.createBook(CreateBookRequest.newBuilder()
                .setBook(Book.newBuilder().setTitle("  "))
                .build()));
    }

    private static void assertStatus(Status.Code expected, Executable call) {
        var e = assertThrows(StatusRuntimeException.class, call);
        assertEquals(expected, e.getStatus().getCode());
    }
}
