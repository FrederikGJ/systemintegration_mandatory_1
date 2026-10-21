package dk.library.soap.faults;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Indholdet af soap:Fault/detail for alle tre faults. Elementets navn (NotFoundFault, ValidationFault,
 * ConflictFault) kommer fra @WebFault på exception-klassen; typen er den samme.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FaultDetail", namespace = Namespace.URI, propOrder = {"message"})
public class FaultDetail {

    @XmlElement(required = true)
    private String message;

    public FaultDetail() { }

    public FaultDetail(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
