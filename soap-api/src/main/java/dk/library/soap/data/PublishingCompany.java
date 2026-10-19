package dk.library.soap.data;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/** Complex type: xsd:complexType "PublishingCompany". */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PublishingCompany", namespace = Namespace.URI,
        propOrder = {"id", "name"})
public class PublishingCompany {

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private int id;                          // -> xs:integer

    @XmlElement(required = true)
    private String name;                     // -> xs:string

    public PublishingCompany() { }

    public PublishingCompany(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
