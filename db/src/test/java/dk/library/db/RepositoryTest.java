package dk.library.db;

import dk.library.db.model.Author;
import dk.library.db.model.Book;
import dk.library.db.repository.AuthorRepository;
import dk.library.db.repository.BookRepository;
import dk.library.db.repository.PublishingCompanyRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Kører mod en midlertidig kopi af library.db, så den rigtige fil ikke ændres. Springes over, hvis filen mangler. */
class RepositoryTest {

    private static Database db;

    @BeforeAll
    static void copyDatabase(@TempDir Path tmp) throws IOException {
        var source = Path.of("library.db");
        assumeTrue(Files.isRegularFile(source), "db/library.db mangler, testen springes over");
        db = new Database(Files.copy(source, tmp.resolve("library.db")));
    }

    @Test
    void missingFileFailsFast(@TempDir Path tmp) {
        assertThrows(IllegalStateException.class, () -> new Database(tmp.resolve("findes-ikke.db")));
    }

    @Test
    void readsSeedData() {
        assertFalse(new BookRepository(db).findAll().isEmpty());
        assertFalse(new AuthorRepository(db).findAll().isEmpty());
        assertFalse(new PublishingCompanyRepository(db).findAll().isEmpty());
        assertTrue(new BookRepository(db).findById(1000).isPresent());
        assertTrue(new BookRepository(db).findById(-1).isEmpty());
    }

    @Test
    void bookCrudRoundTrip() {
        var books = new BookRepository(db);
        var author = new AuthorRepository(db).create(new Author(0, "Test", "Forfatter"));

        var created = books.create(new Book(0, "Testbog", author.id(), null, 1));
        assertTrue(created.id() > 0);
        assertNull(books.findById(created.id()).orElseThrow().publishingYear());
        assertEquals(1, books.findByAuthor(author.id()).size());

        assertTrue(books.update(new Book(created.id(), "Rettet titel", author.id(), 2024, 1)));
        var updated = books.findById(created.id()).orElseThrow();
        assertEquals("Rettet titel", updated.title());
        assertEquals(2024, updated.publishingYear());

        assertTrue(books.delete(created.id()));
        assertFalse(books.delete(created.id()));
        assertTrue(books.findById(created.id()).isEmpty());
    }
}
