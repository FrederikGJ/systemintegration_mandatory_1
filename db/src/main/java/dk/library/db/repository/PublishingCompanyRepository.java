package dk.library.db.repository;

import dk.library.db.Database;
import dk.library.db.DatabaseException;
import dk.library.db.model.PublishingCompany;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PublishingCompanyRepository {

    private final Database db;

    public PublishingCompanyRepository(Database db) {
        this.db = db;
    }

    public List<PublishingCompany> findAll() {
        var sql = "SELECT nPublishingCompanyID, cName FROM tpublishingcompany ORDER BY nPublishingCompanyID";
        try (var c = db.connect(); var st = c.createStatement(); var rs = st.executeQuery(sql)) {
            var companies = new ArrayList<PublishingCompany>();
            while (rs.next()) {
                companies.add(map(rs));
            }
            return companies;
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente forlag", e);
        }
    }

    public Optional<PublishingCompany> findById(int id) {
        var sql = "SELECT nPublishingCompanyID, cName FROM tpublishingcompany WHERE nPublishingCompanyID = ?";
        try (var c = db.connect(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke hente forlag " + id, e);
        }
    }

    /** Indsætter forlaget og returnerer det med det genererede id. */
    public PublishingCompany create(PublishingCompany company) {
        var sql = "INSERT INTO tpublishingcompany (cName) VALUES (?)";
        try (var c = db.connect(); var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, company.name());
            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                keys.next();
                return company.withId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke oprette forlag", e);
        }
    }

    /** @return true hvis en række blev opdateret */
    public boolean update(PublishingCompany company) {
        var sql = "UPDATE tpublishingcompany SET cName = ? WHERE nPublishingCompanyID = ?";
        try (var c = db.connect(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, company.name());
            ps.setInt(2, company.id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke opdatere forlag " + company.id(), e);
        }
    }

    /** @return true hvis en række blev slettet */
    public boolean delete(int id) {
        var sql = "DELETE FROM tpublishingcompany WHERE nPublishingCompanyID = ?";
        try (var c = db.connect(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke slette forlag " + id, e);
        }
    }

    private static PublishingCompany map(ResultSet rs) throws SQLException {
        return new PublishingCompany(rs.getInt("nPublishingCompanyID"), rs.getString("cName"));
    }
}
