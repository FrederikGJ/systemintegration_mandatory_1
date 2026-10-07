package dk.library.db.repository;

import dk.library.db.Database;
import dk.library.db.DatabaseException;
import dk.library.db.model.Book;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BookRepository {

    private static final String COLUMNS = "nBookID, cTitle, nAuthorID, nPublishingYear, nPublishingCompanyID";

    private final Database db;

    public BookRepository(Database db) {
        this.db = db;
    }

    public List<Book> findAll() {
        var sql = "SELECT " + COLUMNS + " FROM tbook ORDER BY nBookID";
        try (var c = db.connect(); var st = c.createStatement(); var rs = st.executeQuery(sql)) {
            var books = new ArrayList<Book>();
            while (rs.next()) {
                books.add(map(rs));
            }
            return books;
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente bøger", e);
        }
    }

    public Optional<Book> findById(int id) {
        var sql = "SELECT " + COLUMNS + " FROM tbook WHERE nBookID = ?";
        try (var c = db.connect(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente bog " + id, e);
        }
    }

    public List<Book> findByAuthor(int authorId) {
        var sql = "SELECT " + COLUMNS + " FROM tbook WHERE nAuthorID = ? ORDER BY nBookID";
        try (var c = db.connect(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            try (var rs = ps.executeQuery()) {
                var books = new ArrayList<Book>();
                while (rs.next()) {
                    books.add(map(rs));
                }
                return books;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente bøger for forfatter " + authorId, e);
        }
    }

    /** Indsætter bogen og returnerer den med det genererede id. */
    public Book create(Book book) {
        var sql = "INSERT INTO tbook (cTitle, nAuthorID, nPublishingYear, nPublishingCompanyID) VALUES (?, ?, ?, ?)";
        try (var c = db.connect(); var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.title());
            ps.setInt(2, book.authorId());
            setYear(ps, 3, book.publishingYear());
            ps.setInt(4, book.publishingCompanyId());
            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                keys.next();
                return book.withId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke oprette bog", e);
        }
    }

    /** @return true hvis en række blev opdateret */
    public boolean update(Book book) {
        var sql = "UPDATE tbook SET cTitle = ?, nAuthorID = ?, nPublishingYear = ?, nPublishingCompanyID = ? WHERE nBookID = ?";
        try (var c = db.connect(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, book.title());
            ps.setInt(2, book.authorId());
            setYear(ps, 3, book.publishingYear());
            ps.setInt(4, book.publishingCompanyId());
            ps.setInt(5, book.id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke opdatere bog " + book.id(), e);
        }
    }

    /** @return true hvis en række blev slettet */
    public boolean delete(int id) {
        try (var c = db.connect(); var ps = c.prepareStatement("DELETE FROM tbook WHERE nBookID = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke slette bog " + id, e);
        }
    }

    private static Book map(ResultSet rs) throws SQLException {
        int year = rs.getInt("nPublishingYear");
        Integer publishingYear = rs.wasNull() ? null : year;
        return new Book(
                rs.getInt("nBookID"),
                rs.getString("cTitle"),
                rs.getInt("nAuthorID"),
                publishingYear,
                rs.getInt("nPublishingCompanyID"));
    }

    private static void setYear(java.sql.PreparedStatement ps, int index, Integer year) throws SQLException {
        if (year == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, year);
        }
    }
}
