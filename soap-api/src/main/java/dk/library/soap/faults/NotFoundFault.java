package dk.library.soap.faults;

import dk.library.soap.Namespace;
import jakarta.xml.ws.WebFault;

/**
 * NotFoundFault: det efterspurgte id findes ikke.
 * En checked exception med @WebFault bliver til en wsdl:fault på hver operation, hvis metode erklærer den med throws.
 * Konstruktørerne med FaultDetail og getFaultInfo er dem, JAX-WS kræver af en fault-exception.
 */
@WebFault(name = "NotFoundFault", targetNamespace = Namespace.URI)
public class NotFoundFault extends Exception {

    private final FaultDetail faultInfo;

    public NotFoundFault(String message) {
        this(message, new FaultDetail(message));
    }

    public NotFoundFault(String message, FaultDetail faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public NotFoundFault(String message, FaultDetail faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    public FaultDetail getFaultInfo() {
        return faultInfo;
    }
}
