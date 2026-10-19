package dk.library.soap.data;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Complex type: xsd:complexType "Book". Felter og rækkefølge som i opgaven.
 * Reglerne (forfatter og forlag skal findes, publishingYear mindst 1900) tjekkes i LibraryServiceImpl,
 * fordi en genereret XSD ikke kan udtrykke dem.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Book", namespace = Namespace.URI,
        propOrder = {"id", "title", "authorId", "publishingCompanyId", "publishingYear"})
public class Book {

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private int id;                          // -> xs:integer

    @XmlElement(required = true)
    private String title;                    // -> xs:string

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private int authorId;

    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private int publishingCompanyId;

    // Integer, fordi kolonnen tillader NULL. Seed-data har dog ingen
    @XmlElement(required = true)
    @XmlSchemaType(name = "integer")
    private Integer publishingYear;

    public Book() { }

    public Book(int id, String title, int authorId, int publishingCompanyId, Integer publishingYear) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.publishingCompanyId = publishingCompanyId;
        this.publishingYear = publishingYear;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public int getPublishingCompanyId() { return publishingCompanyId; }
    public void setPublishingCompanyId(int publishingCompanyId) { this.publishingCompanyId = publishingCompanyId; }

    public Integer getPublishingYear() { return publishingYear; }
    public void setPublishingYear(Integer publishingYear) { this.publishingYear = publishingYear; }
}
