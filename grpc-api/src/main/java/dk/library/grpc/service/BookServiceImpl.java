package dk.library.grpc.service;

import com.google.protobuf.Empty;
import dk.library.db.repository.BookRepository;
import dk.library.grpc.proto.Book;
import dk.library.grpc.proto.BookServiceGrpc;
import dk.library.grpc.proto.CreateBookRequest;
import dk.library.grpc.proto.CreateBookResponse;
import dk.library.grpc.proto.DeleteBookRequest;
import dk.library.grpc.proto.GetBookByIdRequest;
import dk.library.grpc.proto.ListBooksRequest;
import dk.library.grpc.proto.ListBooksResponse;
import dk.library.grpc.proto.UpdateBookRequest;
import dk.library.grpc.proto.WatchBooksRequest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class BookServiceImpl extends BookServiceGrpc.BookServiceImplBase {

    private final BookRepository books;

    // Åbne WatchBooks-streams. CreateBook og WatchBooks kører på hver sin tråd, derfor et trådsikkert Set
    private final Set<ServerCallStreamObserver<Book>> watchers = ConcurrentHashMap.newKeySet();

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
    public void getBookById(GetBookByIdRequest request, StreamObserver<Book> responseObserver) {
        var book = books.findById(request.getId()).orElseThrow(() -> notFound(request.getId()));
        responseObserver.onNext(ProtoMapper.toProto(book));
        responseObserver.onCompleted();
    }

    @Override
    public void createBook(CreateBookRequest request, StreamObserver<CreateBookResponse> responseObserver) {
        validateTitle(request.getTitle());
        var created = ProtoMapper.toProto(books.create(ProtoMapper.toModel(request)));
        responseObserver.onNext(CreateBookResponse.newBuilder().setBook(created).build());
        responseObserver.onCompleted();
        notifyWatchers(created);
    }

    @Override
    public void updateBook(UpdateBookRequest request, StreamObserver<Book> responseObserver) {
        validateTitle(request.getBook().getTitle());
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

    /** Registrerer klienten og returnerer med det samme. Streamen forbliver åben, så der kaldes aldrig onCompleted. */
    @Override
    public void watchBooks(WatchBooksRequest request, StreamObserver<Book> responseObserver) {
        var watcher = (ServerCallStreamObserver<Book>) responseObserver;
        // Klienten afbryder (Ctrl+C, deadline, lukket kanal): fjern den, så vi ikke sender til en død stream
        watcher.setOnCancelHandler(() -> watchers.remove(watcher));
        watchers.add(watcher);
    }

    private void notifyWatchers(Book book) {
        for (var watcher : watchers) {
            // En StreamObserver må ikke kaldes fra to tråde på én gang, og to CreateBook kan køre samtidig
            synchronized (watcher) {
                watcher.onNext(book);
            }
        }
    }

    private static void validateTitle(String title) {
        if (title.isBlank()) {
            throw Status.INVALID_ARGUMENT.withDescription("title må ikke være tom").asRuntimeException();
        }
    }

    private static StatusRuntimeException notFound(int id) {
        return Status.NOT_FOUND.withDescription("Bog " + id + " findes ikke").asRuntimeException();
    }
}
