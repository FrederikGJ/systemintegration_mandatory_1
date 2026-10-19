package dk.library.soap.messages.author;

import dk.library.soap.Namespace;
import dk.library.soap.data.Author;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/** Output fra ListAuthors: ét author-element pr. forfatter (maxOccurs="unbounded"). */
@XmlRootElement(name = "ListAuthorsResponse", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ListAuthorsResponse", namespace = Namespace.URI,
        propOrder = {"authors"})
public class ListAuthorsResponse {

    @XmlElement(name = "author")
    private List<Author> authors = new ArrayList<>();

    public ListAuthorsResponse() { }

    public ListAuthorsResponse(List<Author> authors) {
        this.authors = authors;
    }

    public List<Author> getAuthors() { return authors; }
    public void setAuthors(List<Author> authors) { this.authors = authors; }
}
