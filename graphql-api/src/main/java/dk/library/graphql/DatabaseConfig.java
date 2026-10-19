package dk.library.graphql;
import dk.library.db.Database;
import dk.library.db.repository.*;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
@Configuration
public class DatabaseConfig {
    @Bean Database database(@Value("${library.db.path}") String path) { return new Database(Path.of(path)); }
    @Bean BookRepository books(Database db) { return new BookRepository(db); }
    @Bean AuthorRepository authors(Database db) { return new AuthorRepository(db); }
    @Bean PublishingCompanyRepository publishers(Database db) { return new PublishingCompanyRepository(db); }
}
