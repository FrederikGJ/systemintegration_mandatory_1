package dk.library.grpc.service;

import dk.library.grpc.proto.Author;
import dk.library.grpc.proto.Book;
import dk.library.grpc.proto.PublishingCompany;

/**
 * db-records ↔ protobuf-beskeder. Klasserne hedder det samme i begge pakker,
 * så db-modellerne skrives fuldt ud (dk.library.db.model.Book).
 * NULL i db = feltet er ikke sat i beskeden (has...() er false).
 */
final class ProtoMapper {

    private ProtoMapper() {
    }

    static Book toProto(dk.library.db.model.Book book) {
        var builder = Book.newBuilder()
                .setId(book.id())
                .setTitle(book.title())
                .setAuthorId(book.authorId())
                .setPublishingCompanyId(book.publishingCompanyId());
        if (book.publishingYear() != null) {
            builder.setPublishingYear(book.publishingYear());
        }
        return builder.build();
    }

    static dk.library.db.model.Book toModel(Book book) {
        return new dk.library.db.model.Book(
                book.getId(),
                book.getTitle(),
                book.getAuthorId(),
                book.hasPublishingYear() ? book.getPublishingYear() : null,
                book.getPublishingCompanyId());
    }

    static Author toProto(dk.library.db.model.Author author) {
        var builder = Author.newBuilder()
                .setId(author.id())
                .setName(author.name());
        if (author.surname() != null) {
            builder.setSurname(author.surname());
        }
        return builder.build();
    }

    static dk.library.db.model.Author toModel(Author author) {
        return new dk.library.db.model.Author(
                author.getId(),
                author.getName(),
                author.hasSurname() ? author.getSurname() : null);
    }

    static PublishingCompany toProto(dk.library.db.model.PublishingCompany company) {
        return PublishingCompany.newBuilder()
                .setId(company.id())
                .setName(company.name())
                .build();
    }

    static dk.library.db.model.PublishingCompany toModel(PublishingCompany company) {
        return new dk.library.db.model.PublishingCompany(company.getId(), company.getName());
    }
}
