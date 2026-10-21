package dk.library.soap;

import dk.library.db.Database;
import dk.library.soap.logging.LoggingHandler;
import dk.library.soap.service.LibraryServiceImpl;
import jakarta.xml.ws.Endpoint;
import jakarta.xml.ws.handler.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * Starter SOAP-endpointet (ét HTTP-endpoint med alle operationer) på JDK'ens indbyggede HTTP-server.
 * Adressen kan gives som første argument. Databasen findes via LIBRARY_DB, se db/README.md.
 */
public final class LibraryPublisher {

    public static final String DEFAULT_ADDRESS = "http://localhost:8080/library";

    private LibraryPublisher() {
    }

    public static void main(String[] args) {
        String address = args.length > 0 ? args[0] : DEFAULT_ADDRESS;
        var db = new Database();

        Endpoint endpoint = Endpoint.create(new LibraryServiceImpl(db));

        List<Handler> chain = new ArrayList<>();
        chain.add(new LoggingHandler());   // skriver hver request og response ud i konsollen
        endpoint.getBinding().setHandlerChain(chain);

        endpoint.publish(address);
        System.out.println("SOAP-service kører med database " + db.file().toAbsolutePath().normalize());
        System.out.println("WSDL: " + address + "?wsdl");
        System.out.println("Stop med Ctrl+C.");
    }
}
