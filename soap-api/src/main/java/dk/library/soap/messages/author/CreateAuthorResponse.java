package dk.library.soap.messages.author;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/** Output fra CreateAuthor: det genererede id. */
@XmlRootElement(name = "CreateAuthorResponse", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateAuthorResponse", namespace = Namespace.URI,
        propOrder = {"id"})
public class CreateAuthorResponse {

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private int id;

    public CreateAuthorResponse() { }

    public CreateAuthorResponse(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}
