package dk.library.grpc.service;

import com.google.protobuf.Empty;
import dk.library.db.repository.BookRepository;
import dk.library.grpc.proto.Book;
import dk.library.grpc.proto.BookServiceGrpc;
import dk.library.grpc.proto.CreateBookRequest;
import dk.library.grpc.proto.DeleteBookRequest;
import dk.library.grpc.proto.GetBookRequest;
import dk.library.grpc.proto.ListBooksRequest;
import dk.library.grpc.proto.ListBooksResponse;
import dk.library.grpc.proto.UpdateBookRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

public final class BookServiceImpl extends BookServiceGrpc.BookServiceImplBase {

    private final BookRepository books;

    public BookServiceImpl(BookRepository books) {
        this.books = books;
    }

    @Override
    public void listBooks(ListBooksRequest request, StreamObserver<ListBooksResponse> responseObserver) {
        var found = request.hasAuthorId() ? books.findByAuthor(request.getAuthorId()) : books.findAll();
        var response = ListBooksResponse.newBuilder();
        found.forEach(book -> response.addBooks(ProtoMapper.toProto(book)));
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getBook(GetBookRequest request, StreamObserver<Book> responseObserver) {
        var book = books.findById(request.getId()).orElseThrow(() -> notFound(request.getId()));
        responseObserver.onNext(ProtoMapper.toProto(book));
        responseObserver.onCompleted();
    }

    @Override
    public void createBook(CreateBookRequest request, StreamObserver<Book> responseObserver) {
        validate(request.getBook());
        var created = books.create(ProtoMapper.toModel(request.getBook()));
        responseObserver.onNext(ProtoMapper.toProto(created));
        responseObserver.onCompleted();
    }

    @Override
    public void updateBook(UpdateBookRequest request, StreamObserver<Book> responseObserver) {
        validate(request.getBook());
        var book = ProtoMapper.toModel(request.getBook());
        if (!books.update(book)) {
            throw notFound(book.id());
        }
        responseObserver.onNext(ProtoMapper.toProto(book));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBook(DeleteBookRequest request, StreamObserver<Empty> responseObserver) {
        if (!books.delete(request.getId())) {
            throw notFound(request.getId());
        }
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private static void validate(Book book) {
        if (book.getTitle().isBlank()) {
            throw Status.INVALID_ARGUMENT.withDescription("title må ikke være tom").asRuntimeException();
        }
    }

    private static StatusRuntimeException notFound(int id) {
        return Status.NOT_FOUND.withDescription("Bog " + id + " findes ikke").asRuntimeException();
    }
}
