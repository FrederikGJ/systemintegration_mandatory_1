package dk.library.soap.faults;

import dk.library.soap.Namespace;
import jakarta.xml.ws.WebFault;

/** ConflictFault: forsøg på at slette noget, som en anden række henviser til (fx en forfatter med bøger). */
@WebFault(name = "ConflictFault", targetNamespace = Namespace.URI)
public class ConflictFault extends Exception {

    private final FaultDetail faultInfo;

    public ConflictFault(String message) {
        this(message, new FaultDetail(message));
    }

    public ConflictFault(String message, FaultDetail faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    public ConflictFault(String message, FaultDetail faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    public FaultDetail getFaultInfo() {
        return faultInfo;
    }
}
