package dk.library.soap.messages.publishingcompany;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/** Input til CreatePublishingCompany. Et manglende element bliver null, så servicen kan svare med ValidationFault. */
@XmlRootElement(name = "CreatePublishingCompanyRequest", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreatePublishingCompanyRequest", namespace = Namespace.URI,
        propOrder = {"name"})
public class CreatePublishingCompanyRequest {

    @XmlElement(required = true)
    private String name;

    public CreatePublishingCompanyRequest() { }

    public CreatePublishingCompanyRequest(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
