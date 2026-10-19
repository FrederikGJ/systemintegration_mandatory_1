package dk.library.soap.messages.publishingcompany;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/** Output fra CreatePublishingCompany: det genererede id. */
@XmlRootElement(name = "CreatePublishingCompanyResponse", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreatePublishingCompanyResponse", namespace = Namespace.URI,
        propOrder = {"id"})
public class CreatePublishingCompanyResponse {

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private int id;

    public CreatePublishingCompanyResponse() { }

    public CreatePublishingCompanyResponse(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
}
