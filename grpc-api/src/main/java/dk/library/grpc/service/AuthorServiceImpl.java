package dk.library.grpc.service;

import com.google.protobuf.Empty;
import dk.library.db.repository.AuthorRepository;
import dk.library.grpc.proto.Author;
import dk.library.grpc.proto.AuthorServiceGrpc;
import dk.library.grpc.proto.CreateAuthorRequest;
import dk.library.grpc.proto.DeleteAuthorRequest;
import dk.library.grpc.proto.GetAuthorRequest;
import dk.library.grpc.proto.ListAuthorsRequest;
import dk.library.grpc.proto.ListAuthorsResponse;
import dk.library.grpc.proto.UpdateAuthorRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public final class AuthorServiceImpl extends AuthorServiceGrpc.AuthorServiceImplBase {

    private final AuthorRepository authors;

    public AuthorServiceImpl(AuthorRepository authors) {
        this.authors = authors;
    }

    @Override
    public void listAuthors(ListAuthorsRequest request, StreamObserver<ListAuthorsResponse> responseObserver) {
        var response = ListAuthorsResponse.newBuilder();
        authors.findAll().forEach(author -> response.addAuthors(ProtoMapper.toProto(author)));
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getAuthor(GetAuthorRequest request, StreamObserver<Author> responseObserver) {
        var author = authors.findById(request.getId()).orElseThrow(() -> notFound(request.getId()));
        responseObserver.onNext(ProtoMapper.toProto(author));
        responseObserver.onCompleted();
    }

    @Override
    public void createAuthor(CreateAuthorRequest request, StreamObserver<Author> responseObserver) {
        validate(request.getAuthor());
        var created = authors.create(ProtoMapper.toModel(request.getAuthor()));
        responseObserver.onNext(ProtoMapper.toProto(created));
        responseObserver.onCompleted();
    }

    @Override
    public void updateAuthor(UpdateAuthorRequest request, StreamObserver<Author> responseObserver) {
        validate(request.getAuthor());
        var author = ProtoMapper.toModel(request.getAuthor());
        if (!authors.update(author)) {
            throw notFound(author.id());
        }
        responseObserver.onNext(ProtoMapper.toProto(author));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAuthor(DeleteAuthorRequest request, StreamObserver<Empty> responseObserver) {
        if (!authors.delete(request.getId())) {
            throw notFound(request.getId());
        }
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private static void validate(Author author) {
        if (author.getName().isBlank()) {
            throw Status.INVALID_ARGUMENT.withDescription("name må ikke være tom").asRuntimeException();
        }
    }

    private static StatusRuntimeException notFound(int id) {
        return Status.NOT_FOUND.withDescription("Forfatter " + id + " findes ikke").asRuntimeException();
    }
}
