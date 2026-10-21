package dk.library.rest.controller;

import dk.library.db.model.PublishingCompany;
import dk.library.db.repository.PublishingCompanyRepository;
import dk.library.rest.dto.PublisherRequest;
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

/** Forlag. Opgaven kalder dem publishers, databasen kalder dem publishing companies. */
@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    private final PublishingCompanyRepository publishers;

    public PublisherController(PublishingCompanyRepository publishers) {
        this.publishers = publishers;
    }

    @GetMapping
    public List<PublishingCompany> list() {
        return publishers.findAll();
    }

    @GetMapping("/{id}")
    public PublishingCompany get(@PathVariable int id) {
        return publishers.findById(id).orElseThrow(() -> notFound(id));
    }

    /** 201 Created med det nye forlag i body og dets URL i Location-headeren. */
    @PostMapping
    public ResponseEntity<PublishingCompany> create(@Valid @RequestBody PublisherRequest request) {
        var publisher = publishers.create(request.toModel(0));
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(publisher.id())
                .toUri();
        return ResponseEntity.created(location).body(publisher);
    }

    /** Overskriver hele forlaget. */
    @PutMapping("/{id}")
    public PublishingCompany update(@PathVariable int id, @Valid @RequestBody PublisherRequest request) {
        var publisher = request.toModel(id);
        if (!publishers.update(publisher)) {
            throw notFound(id);
        }
        return publisher;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        if (!publishers.delete(id)) {
            throw notFound(id);
        }
    }

    private static ResponseStatusException notFound(int id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Forlag " + id + " findes ikke");
    }
}
