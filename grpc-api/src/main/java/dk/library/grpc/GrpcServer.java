package dk.library.grpc;

import dk.library.db.Database;
import dk.library.db.repository.AuthorRepository;
import dk.library.db.repository.BookRepository;
import dk.library.db.repository.PublishingCompanyRepository;
import dk.library.grpc.service.AuthorServiceImpl;
import dk.library.grpc.service.BookServiceImpl;
import dk.library.grpc.service.PublishingCompanyServiceImpl;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class GrpcServer {
}
