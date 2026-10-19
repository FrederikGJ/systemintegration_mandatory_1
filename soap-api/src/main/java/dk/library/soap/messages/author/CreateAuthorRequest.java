package dk.library.soap.messages.author;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/** Input til CreateAuthor. Et manglende element bliver null, så servicen kan svare med ValidationFault. */
@XmlRootElement(name = "CreateAuthorRequest", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateAuthorRequest", namespace = Namespace.URI,
        propOrder = {"name", "surname"})
public class CreateAuthorRequest {

    @XmlElement(required = true)
    private String name;

    @XmlElement(required = true)
    private String surname;

    public CreateAuthorRequest() { }

    public CreateAuthorRequest(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
}
