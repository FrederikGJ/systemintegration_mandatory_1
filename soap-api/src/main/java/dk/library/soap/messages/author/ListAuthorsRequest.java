package dk.library.soap.messages.author;

import dk.library.soap.Namespace;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

/** Input til ListAuthors: en tom request. */
@XmlRootElement(name = "ListAuthorsRequest", namespace = Namespace.URI)
@XmlType(name = "ListAuthorsRequest", namespace = Namespace.URI)
public class ListAuthorsRequest {

    public ListAuthorsRequest() { }
}
