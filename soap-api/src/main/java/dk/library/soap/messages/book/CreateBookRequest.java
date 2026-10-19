package dk.library.soap.messages.book;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/** Input til CreateBook. Et manglende element bliver null, så servicen kan svare med ValidationFault. */
@XmlRootElement(name = "CreateBookRequest", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CreateBookRequest", namespace = Namespace.URI,
        propOrder = {"title", "authorId", "publishingCompanyId", "publishingYear"})
public class CreateBookRequest {

    @XmlElement(required = true)
    private String title;

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private Integer authorId;

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private Integer publishingCompanyId;

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private Integer publishingYear;

    public CreateBookRequest() { }

    public CreateBookRequest(String title, Integer authorId, Integer publishingCompanyId, Integer publishingYear) {
        this.title = title;
        this.authorId = authorId;
        this.publishingCompanyId = publishingCompanyId;
        this.publishingYear = publishingYear;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getAuthorId() { return authorId; }
    public void setAuthorId(Integer authorId) { this.authorId = authorId; }

    public Integer getPublishingCompanyId() { return publishingCompanyId; }
    public void setPublishingCompanyId(Integer publishingCompanyId) { this.publishingCompanyId = publishingCompanyId; }

    public Integer getPublishingYear() { return publishingYear; }
    public void setPublishingYear(Integer publishingYear) { this.publishingYear = publishingYear; }
}
