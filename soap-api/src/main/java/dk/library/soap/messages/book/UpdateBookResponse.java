package dk.library.soap.messages.book;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/** Kvittering fra UpdateBook. Fejl kommer som faults, så success er altid true. */
@XmlRootElement(name = "UpdateBookResponse", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateBookResponse", namespace = Namespace.URI,
        propOrder = {"success", "message"})
public class UpdateBookResponse {

    @XmlElement(required = true)
    private boolean success;

    @XmlElement(required = true)
    private String message;

    public UpdateBookResponse() { }

    public UpdateBookResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
