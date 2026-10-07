package dk.library.db.repository;

import dk.library.db.Database;
import dk.library.db.DatabaseException;
import dk.library.db.model.Author;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AuthorRepository {

    private final Database db;

    public AuthorRepository(Database db) {
        this.db = db;
    }

    public List<Author> findAll() {
        var sql = "SELECT nAuthorID, cName, cSurname FROM tauthor ORDER BY nAuthorID";
        try (var c = db.connect(); var st = c.createStatement(); var rs = st.executeQuery(sql)) {
            var authors = new ArrayList<Author>();
            while (rs.next()) {
                authors.add(map(rs));
            }
            return authors;
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente forfattere", e);
        }
    }

    public Optional<Author> findById(int id) {
        var sql = "SELECT nAuthorID, cName, cSurname FROM tauthor WHERE nAuthorID = ?";
        try (var c = db.connect(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente forfatter " + id, e);
        }
    }

    /** Indsætter forfatteren og returnerer den med det genererede id. */
    public Author create(Author author) {
        var sql = "INSERT INTO tauthor (cName, cSurname) VALUES (?, ?)";
        try (var c = db.connect(); var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, author.name());
            ps.setString(2, author.surname());
            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                keys.next();
                return author.withId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke oprette forfatter", e);
        }
    }

    /** @return true hvis en række blev opdateret */
    public boolean update(Author author) {
        var sql = "UPDATE tauthor SET cName = ?, cSurname = ? WHERE nAuthorID = ?";
        try (var c = db.connect(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, author.name());
            ps.setString(2, author.surname());
            ps.setInt(3, author.id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke opdatere forfatter " + author.id(), e);
        }
    }

    /** @return true hvis en række blev slettet */
    public boolean delete(int id) {
        try (var c = db.connect(); var ps = c.prepareStatement("DELETE FROM tauthor WHERE nAuthorID = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke slette forfatter " + id, e);
        }
    }

    private static Author map(ResultSet rs) throws SQLException {
        return new Author(rs.getInt("nAuthorID"), rs.getString("cName"), rs.getString("cSurname"));
    }
}
