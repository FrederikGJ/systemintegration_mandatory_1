package dk.library.grpc;

import dk.library.db.Database;
import dk.library.grpc.proto.Author;
import dk.library.grpc.proto.AuthorServiceGrpc;
import dk.library.grpc.proto.Book;
import dk.library.grpc.proto.BookServiceGrpc;
import dk.library.grpc.proto.CreateAuthorRequest;
import dk.library.grpc.proto.CreateBookRequest;
import dk.library.grpc.proto.DeleteBookRequest;
import dk.library.grpc.proto.GetAuthorRequest;
import dk.library.grpc.proto.GetBookRequest;
import dk.library.grpc.proto.ListBooksRequest;
import dk.library.grpc.proto.UpdateBookRequest;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrpcServerTest {
}
