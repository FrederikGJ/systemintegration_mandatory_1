package dk.library.rest.controller;

import dk.library.db.model.Author;
import dk.library.db.repository.AuthorRepository;
import dk.library.rest.dto.AuthorRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorRepository authors;

    public AuthorController(AuthorRepository authors) {
        this.authors = authors;
    }

    @GetMapping
    public List<Author> list() {
        return authors.findAll();
    }

    @GetMapping("/{id}")
    public Author get(@PathVariable int id) {
        return authors.findById(id).orElseThrow(() -> notFound(id));
    }

    /** 201 Created med den nye forfatter i body og dens URL i Location-headeren. */
    @PostMapping
    public ResponseEntity<Author> create(@Valid @RequestBody AuthorRequest request) {
        var author = authors.create(request.toModel(0));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(author.id())
                .toUri();
        return ResponseEntity.created(location).body(author);
    }

    /** Overskriver hele forfatteren. */
    @PutMapping("/{id}")
    public Author update(@PathVariable int id, @Valid @RequestBody AuthorRequest request) {
        var author = request.toModel(id);
        if (!authors.update(author)) {
            throw notFound(id);
        }
        return author;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        if (!authors.delete(id)) {
            throw notFound(id);
        }
    }

    private static ResponseStatusException notFound(int id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Forfatter " + id + " findes ikke");
    }
}
