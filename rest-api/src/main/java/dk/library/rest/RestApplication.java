package dk.library.rest;

import dk.library.db.Database;
import dk.library.db.repository.AuthorRepository;
import dk.library.db.repository.BookRepository;
import dk.library.db.repository.PublishingCompanyRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Starter REST-serveren. Port fra SERVER_PORT (Spring Boots egen, default 8080), database fra LIBRARY_DB.
 * db-modulet kender ikke Spring, så dets klasser registreres her som beans, som controllerne kan få injiceret.
 */
@SpringBootApplication
public class RestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestApplication.class, args);
    }

    @Bean
    Database database() {
        return new Database();
    }

    @Bean
    BookRepository bookRepository(Database db) {
        return new BookRepository(db);
    }

    @Bean
    AuthorRepository authorRepository(Database db) {
        return new AuthorRepository(db);
    }

    @Bean
    PublishingCompanyRepository publishingCompanyRepository(Database db) {
        return new PublishingCompanyRepository(db);
    }
}
