package dk.library.soap.messages.publishingcompany;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/** Input til UpdatePublishingCompany: id plus name. Alle felter er påkrævet. */
@XmlRootElement(name = "UpdatePublishingCompanyRequest", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdatePublishingCompanyRequest", namespace = Namespace.URI,
        propOrder = {"id", "name"})
public class UpdatePublishingCompanyRequest {

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private Integer id;

    @XmlElement(required = true)
    private String name;

    public UpdatePublishingCompanyRequest() { }

    public UpdatePublishingCompanyRequest(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
