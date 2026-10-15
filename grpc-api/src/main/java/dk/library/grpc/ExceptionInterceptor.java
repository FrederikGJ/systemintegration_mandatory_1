package dk.library.grpc;

import dk.library.db.DatabaseException;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Oversætter exceptions fra services til statuskoder. Uden den sender grpc-java alt som UNKNOWN.
 * StatusRuntimeException beholder sin kode, DatabaseException bliver INTERNAL.
 */
public final class ExceptionInterceptor implements ServerInterceptor {

    private static final Logger LOG = Logger.getLogger(ExceptionInterceptor.class.getName());

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(next.startCall(call, headers)) {
            // Ved unære kald kører service-metoden, når klienten har sendt hele requesten (half-close)
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (RuntimeException e) {
                    call.close(toStatus(e), new Metadata());
                }
            }
        };
    }

    private static Status toStatus(RuntimeException e) {
        return switch (e) {
            case StatusRuntimeException sre -> sre.getStatus();
            case DatabaseException dbe -> {
                LOG.log(Level.SEVERE, "Databasefejl", dbe);
                yield Status.INTERNAL.withDescription(dbe.getMessage());
            }
            default -> {
                LOG.log(Level.SEVERE, "Uventet fejl", e);
                yield Status.INTERNAL.withDescription("Uventet fejl på serveren");
            }
        };
    }
}
