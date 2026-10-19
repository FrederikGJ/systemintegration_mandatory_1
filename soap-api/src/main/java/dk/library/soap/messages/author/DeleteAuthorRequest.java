package dk.library.soap.messages.author;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/** Input til DeleteAuthor. */
@XmlRootElement(name = "DeleteAuthorRequest", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeleteAuthorRequest", namespace = Namespace.URI,
        propOrder = {"id"})
public class DeleteAuthorRequest {

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private Integer id;

    public DeleteAuthorRequest() { }

    public DeleteAuthorRequest(Integer id) {
        this.id = id;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
}
