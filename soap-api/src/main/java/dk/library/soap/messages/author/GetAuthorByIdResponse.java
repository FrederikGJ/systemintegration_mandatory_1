package dk.library.soap.messages.author;

import dk.library.soap.Namespace;
import dk.library.soap.data.Author;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/** Output fra GetAuthorById: forfatteren som complex type Author. */
@XmlRootElement(name = "GetAuthorByIdResponse", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetAuthorByIdResponse", namespace = Namespace.URI,
        propOrder = {"author"})
public class GetAuthorByIdResponse {

    @XmlElement(required = true)
    private Author author;

    public GetAuthorByIdResponse() { }

    public GetAuthorByIdResponse(Author author) {
        this.author = author;
    }

    public Author getAuthor() { return author; }
    public void setAuthor(Author author) { this.author = author; }
}
