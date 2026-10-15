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
}
