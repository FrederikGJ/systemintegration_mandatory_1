package dk.library.soap.messages.book;

import dk.library.soap.Namespace;
import dk.library.soap.data.Book;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/** Output fra GetBookById: bogen som complex type Book. */
@XmlRootElement(name = "GetBookByIdResponse", namespace = Namespace.URI)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetBookByIdResponse", namespace = Namespace.URI,
        propOrder = {"book"})
public class GetBookByIdResponse {

    @XmlElement(required = true)
    private Book book;

    public GetBookByIdResponse() { }

    public GetBookByIdResponse(Book book) {
        this.book = book;
    }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
}
