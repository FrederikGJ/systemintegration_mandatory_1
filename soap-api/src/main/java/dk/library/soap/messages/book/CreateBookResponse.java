package dk.library.soap.messages.book;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/** Output fra CreateBook: det genererede id. */
@XmlRootElement(name = "CreateBookResponse", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateBookResponse", namespace = Namespace.URI,
        propOrder = {"id"})
public class CreateBookResponse {

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private int id;

    public CreateBookResponse() { }

    public CreateBookResponse(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}
