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
}
