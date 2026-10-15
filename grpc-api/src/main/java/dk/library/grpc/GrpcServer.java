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

/** Starter gRPC-serveren. Port fra GRPC_PORT (default 9090), database fra LIBRARY_DB. */
public final class GrpcServer {

    public static final String PORT_ENV_VAR = "GRPC_PORT";
    public static final int DEFAULT_PORT = 9090;

    private static final Logger LOG = Logger.getLogger(GrpcServer.class.getName());

    private GrpcServer() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(System.getenv().getOrDefault(PORT_ENV_VAR, String.valueOf(DEFAULT_PORT)));
        var db = new Database();

        var server = addServices(ServerBuilder.forPort(port), db)
                .addService(ProtoReflectionServiceV1.newInstance())
                .build()
                .start();
        LOG.info("gRPC-server lytter på port " + port + " med database " + db.file().toAbsolutePath().normalize());

        // Ctrl+C: lad igangværende kald blive færdige i op til 5 s, før serveren lukkes hårdt
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (!server.shutdown().awaitTermination(5, TimeUnit.SECONDS)) {
                    server.shutdownNow();
                }
            } catch (InterruptedException e) {
                server.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));
        server.awaitTermination();
    }

    /** Registrerer de tre services bag ExceptionInterceptor. Bruges også af testen med en in-process builder. */
    static ServerBuilder<?> addServices(ServerBuilder<?> builder, Database db) {
        var interceptor = new ExceptionInterceptor();
        builder.addService(ServerInterceptors.intercept(
                new BookServiceImpl(new BookRepository(db)), interceptor));
        builder.addService(ServerInterceptors.intercept(
                new AuthorServiceImpl(new AuthorRepository(db)), interceptor));
        builder.addService(ServerInterceptors.intercept(
                new PublishingCompanyServiceImpl(new PublishingCompanyRepository(db)), interceptor));
        return builder;
    }
}
