package dk.library.soap.data;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/** Complex type: xsd:complexType "Author". */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Author", namespace = Namespace.URI,
        propOrder = {"id", "name", "surname"})
public class Author {

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private int id;                          // -> xs:integer

    @XmlElement(required = true)
    private String name;                     // -> xs:string

    @XmlElement(required = true)
    private String surname;                  // -> xs:string

    public Author() { }

    public Author(int id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
}
