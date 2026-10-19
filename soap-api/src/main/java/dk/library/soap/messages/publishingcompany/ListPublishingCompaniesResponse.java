package dk.library.soap.messages.publishingcompany;

import dk.library.soap.Namespace;
import dk.library.soap.data.PublishingCompany;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/** Output fra ListPublishingCompanies: ét publishingCompany-element pr. forlag (maxOccurs="unbounded"). */
@XmlRootElement(name = "ListPublishingCompaniesResponse", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListPublishingCompaniesResponse", namespace = Namespace.URI,
        propOrder = {"publishingCompanies"})
public class ListPublishingCompaniesResponse {

    @XmlElement(name = "publishingCompany")
    private List<PublishingCompany> publishingCompanies = new ArrayList<>();

    public ListPublishingCompaniesResponse() { }

    public ListPublishingCompaniesResponse(List<PublishingCompany> publishingCompanies) {
        this.publishingCompanies = publishingCompanies;
    }

    public List<PublishingCompany> getPublishingCompanies() { return publishingCompanies; }
    public void setPublishingCompanies(List<PublishingCompany> publishingCompanies) { this.publishingCompanies = publishingCompanies; }
}
