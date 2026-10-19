package dk.library.soap.messages.publishingcompany;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/** Input til ListPublishingCompanies: en tom request. */
@XmlRootElement(name = "ListPublishingCompaniesRequest", namespace = Namespace.URI)
@XmlType(name = "ListPublishingCompaniesRequest", namespace = Namespace.URI)
public class ListPublishingCompaniesRequest {

    public ListPublishingCompaniesRequest() { }
}
