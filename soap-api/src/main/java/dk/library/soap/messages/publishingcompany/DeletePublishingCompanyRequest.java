package dk.library.soap.messages.publishingcompany;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/** Input til DeletePublishingCompany. */
@XmlRootElement(name = "DeletePublishingCompanyRequest", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeletePublishingCompanyRequest", namespace = Namespace.URI,
        propOrder = {"id"})
public class DeletePublishingCompanyRequest {

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private Integer id;

    public DeletePublishingCompanyRequest() { }

    public DeletePublishingCompanyRequest(Integer id) {
        this.id = id;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
}
