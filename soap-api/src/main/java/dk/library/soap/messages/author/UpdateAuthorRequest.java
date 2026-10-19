package dk.library.soap.messages.author;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/** Input til UpdateAuthor: id plus name og surname. Alle felter er påkrævet. */
@XmlRootElement(name = "UpdateAuthorRequest", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateAuthorRequest", namespace = Namespace.URI,
        propOrder = {"id", "name", "surname"})
public class UpdateAuthorRequest {

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private Integer id;

    @XmlElement(required = true)
    private String name;

    @XmlElement(required = true)
    private String surname;

    public UpdateAuthorRequest() { }

    public UpdateAuthorRequest(Integer id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
}
