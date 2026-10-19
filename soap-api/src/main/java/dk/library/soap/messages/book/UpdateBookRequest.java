package dk.library.soap.messages.book;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/** Input til UpdateBook: id plus alle bogens felter. Rækken overskrives helt, så alle felter er påkrævet. */
@XmlRootElement(name = "UpdateBookRequest", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UpdateBookRequest", namespace = Namespace.URI,
        propOrder = {"id", "title", "authorId", "publishingCompanyId", "publishingYear"})
public class UpdateBookRequest {

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private Integer id;

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

    public UpdateBookRequest() { }

    public UpdateBookRequest(Integer id, String title, Integer authorId, Integer publishingCompanyId, Integer publishingYear) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.publishingCompanyId = publishingCompanyId;
        this.publishingYear = publishingYear;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getAuthorId() { return authorId; }
    public void setAuthorId(Integer authorId) { this.authorId = authorId; }

    public Integer getPublishingCompanyId() { return publishingCompanyId; }
    public void setPublishingCompanyId(Integer publishingCompanyId) { this.publishingCompanyId = publishingCompanyId; }

    public Integer getPublishingYear() { return publishingYear; }
    public void setPublishingYear(Integer publishingYear) { this.publishingYear = publishingYear; }
}
