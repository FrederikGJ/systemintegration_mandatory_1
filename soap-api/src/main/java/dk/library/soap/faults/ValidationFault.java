package dk.library.soap.faults;

import dk.library.soap.Namespace;
import jakarta.xml.ws.WebFault;

/** ValidationFault: feltværdier mangler, er ugyldige eller henviser til noget, der ikke findes. */
@WebFault(name = "ValidationFault", targetNamespace = Namespace.URI)
public class ValidationFault extends Exception {

    private final FaultDetail faultInfo;

    public ValidationFault(String message) {
        this(message, new FaultDetail(message));
    }

    public ValidationFault(String message, FaultDetail faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public ValidationFault(String message, FaultDetail faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    public FaultDetail getFaultInfo() {
        return faultInfo;
    }
}
