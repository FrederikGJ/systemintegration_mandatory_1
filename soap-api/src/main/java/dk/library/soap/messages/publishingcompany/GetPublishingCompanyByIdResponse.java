package dk.library.soap.messages.publishingcompany;

import dk.library.soap.Namespace;
import dk.library.soap.data.PublishingCompany;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/** Output fra GetPublishingCompanyById: forlaget som complex type PublishingCompany. */
@XmlRootElement(name = "GetPublishingCompanyByIdResponse", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetPublishingCompanyByIdResponse", namespace = Namespace.URI,
        propOrder = {"publishingCompany"})
public class GetPublishingCompanyByIdResponse {

    @XmlElement(required = true)
    private PublishingCompany publishingCompany;

    public GetPublishingCompanyByIdResponse() { }

    public GetPublishingCompanyByIdResponse(PublishingCompany publishingCompany) {
        this.publishingCompany = publishingCompany;
    }

    public PublishingCompany getPublishingCompany() { return publishingCompany; }
    public void setPublishingCompany(PublishingCompany publishingCompany) { this.publishingCompany = publishingCompany; }
}
