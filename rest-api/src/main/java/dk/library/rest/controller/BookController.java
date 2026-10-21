package dk.library.rest.controller;

import dk.library.db.model.Book;
import dk.library.db.repository.AuthorRepository;
import dk.library.db.repository.BookRepository;
import dk.library.db.repository.PublishingCompanyRepository;
import dk.library.rest.dto.BookRequest;
import dk.library.rest.dto.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookRepository books;
    private final AuthorRepository authors;
    private final PublishingCompanyRepository publishers;

    public BookController(BookRepository books, AuthorRepository authors, PublishingCompanyRepository publishers) {
        this.books = books;
        this.authors = authors;
        this.publishers = publishers;
    }

    /** GET /api/books?limit=20&offset=40 giver bog nr. 41-60. limit er 1-100 (default 20), offset er mindst 0. */
    @GetMapping
    public PageResponse<Book> list(@RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
                                   @RequestParam(defaultValue = "0") @Min(0) int offset) {
        return new PageResponse<>(books.findPage(limit, offset), limit, offset, books.count());
    }

    @GetMapping("/{id}")
    public Book get(@PathVariable int id) {
        return books.findById(id).orElseThrow(() -> notFound(id));
    }

    /** 201 Created med den nye bog i body og dens URL i Location-headeren. */
    @PostMapping
    public ResponseEntity<Book> create(@Valid @RequestBody BookRequest request) {
        checkReferences(request);
        var book = books.create(request.toModel(0));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(book.id())
                .toUri();
        return ResponseEntity.created(location).body(book);
    }

    /** Overskriver hele bogen. */
    @PutMapping("/{id}")
    public Book update(@PathVariable int id, @Valid @RequestBody BookRequest request) {
        checkReferences(request);
        var book = request.toModel(id);
        if (!books.update(book)) {
            throw notFound(id);
        }
        return book;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        if (!books.delete(id)) {
            throw notFound(id);
        }
    }

    /** SQLite-skemaet har ingen fremmednøgler, så vi tjekker selv, at forfatter og forlag findes. */
    private void checkReferences(BookRequest request) {
        if (authors.findById(request.authorId()).isEmpty()) {
            throw badReference("Forfatter " + request.authorId() + " findes ikke");
        }
        if (publishers.findById(request.publishingCompanyId()).isEmpty()) {
            throw badReference("Forlag " + request.publishingCompanyId() + " findes ikke");
        }
    }

    private static ResponseStatusException badReference(String detail) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, detail);
    }

    private static ResponseStatusException notFound(int id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Bog " + id + " findes ikke");
    }
}
