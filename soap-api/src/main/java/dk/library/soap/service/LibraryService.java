package dk.library.soap.service;

import dk.library.soap.Namespace;
import dk.library.soap.faults.ConflictFault;
import dk.library.soap.faults.NotFoundFault;
import dk.library.soap.faults.ValidationFault;
import dk.library.soap.messages.author.CreateAuthorRequest;
import dk.library.soap.messages.author.CreateAuthorResponse;
import dk.library.soap.messages.author.DeleteAuthorRequest;
import dk.library.soap.messages.author.DeleteAuthorResponse;
import dk.library.soap.messages.author.GetAuthorByIdRequest;
import dk.library.soap.messages.author.GetAuthorByIdResponse;
import dk.library.soap.messages.author.ListAuthorsRequest;
import dk.library.soap.messages.author.ListAuthorsResponse;
import dk.library.soap.messages.author.UpdateAuthorRequest;
import dk.library.soap.messages.author.UpdateAuthorResponse;
import dk.library.soap.messages.book.CreateBookRequest;
import dk.library.soap.messages.book.CreateBookResponse;
import dk.library.soap.messages.book.DeleteBookRequest;
import dk.library.soap.messages.book.DeleteBookResponse;
import dk.library.soap.messages.book.GetBookByIdRequest;
import dk.library.soap.messages.book.GetBookByIdResponse;
import dk.library.soap.messages.book.UpdateBookRequest;
import dk.library.soap.messages.book.UpdateBookResponse;
import dk.library.soap.messages.publishingcompany.CreatePublishingCompanyRequest;
import dk.library.soap.messages.publishingcompany.CreatePublishingCompanyResponse;
import dk.library.soap.messages.publishingcompany.DeletePublishingCompanyRequest;
import dk.library.soap.messages.publishingcompany.DeletePublishingCompanyResponse;
import dk.library.soap.messages.publishingcompany.GetPublishingCompanyByIdRequest;
import dk.library.soap.messages.publishingcompany.GetPublishingCompanyByIdResponse;
import dk.library.soap.messages.publishingcompany.ListPublishingCompaniesRequest;
import dk.library.soap.messages.publishingcompany.ListPublishingCompaniesResponse;
import dk.library.soap.messages.publishingcompany.UpdatePublishingCompanyRequest;
import dk.library.soap.messages.publishingcompany.UpdatePublishingCompanyResponse;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 * Service Endpoint Interface (kontrakten): alle 14 operationer fra opgaven bag ét endpoint.
 *
 * @WebService               -> wsdl:portType "LibraryPortType"
 * @WebMethod                -> wsdl:operation med navnet fra opgaven
 * throws ...Fault           -> wsdl:fault på operationen. Hver metode erklærer præcis de faults, opgaven nævner
 * DOCUMENT / LITERAL / BARE -> hver besked har én part: request- eller response-elementet fra messages/
 */
@WebService(name = "LibraryPortType", targetNamespace = Namespace.URI)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT,
        use = SOAPBinding.Use.LITERAL,
        parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface LibraryService {

    // ---------- Book ----------

    @WebMethod(operationName = "CreateBook", action = Namespace.URI + "CreateBook")
    @WebResult(name = "CreateBookResponse", targetNamespace = Namespace.URI)
    CreateBookResponse createBook(
            @WebParam(name = "CreateBookRequest", targetNamespace = Namespace.URI)
            CreateBookRequest request) throws ValidationFault;

    @WebMethod(operationName = "GetBookById", action = Namespace.URI + "GetBookById")
    @WebResult(name = "GetBookByIdResponse", targetNamespace = Namespace.URI)
    GetBookByIdResponse getBookById(
            @WebParam(name = "GetBookByIdRequest", targetNamespace = Namespace.URI)
            GetBookByIdRequest request) throws NotFoundFault;

    @WebMethod(operationName = "UpdateBook", action = Namespace.URI + "UpdateBook")
    @WebResult(name = "UpdateBookResponse", targetNamespace = Namespace.URI)
    UpdateBookResponse updateBook(
            @WebParam(name = "UpdateBookRequest", targetNamespace = Namespace.URI)
            UpdateBookRequest request) throws ValidationFault, NotFoundFault;

    @WebMethod(operationName = "DeleteBook", action = Namespace.URI + "DeleteBook")
    @WebResult(name = "DeleteBookResponse", targetNamespace = Namespace.URI)
    DeleteBookResponse deleteBook(
            @WebParam(name = "DeleteBookRequest", targetNamespace = Namespace.URI)
            DeleteBookRequest request) throws NotFoundFault;

    // ---------- Author ----------

    @WebMethod(operationName = "CreateAuthor", action = Namespace.URI + "CreateAuthor")
    @WebResult(name = "CreateAuthorResponse", targetNamespace = Namespace.URI)
    CreateAuthorResponse createAuthor(
            @WebParam(name = "CreateAuthorRequest", targetNamespace = Namespace.URI)
            CreateAuthorRequest request) throws ValidationFault;

    @WebMethod(operationName = "GetAuthorById", action = Namespace.URI + "GetAuthorById")
    @WebResult(name = "GetAuthorByIdResponse", targetNamespace = Namespace.URI)
    GetAuthorByIdResponse getAuthorById(
            @WebParam(name = "GetAuthorByIdRequest", targetNamespace = Namespace.URI)
            GetAuthorByIdRequest request) throws NotFoundFault;

    @WebMethod(operationName = "ListAuthors", action = Namespace.URI + "ListAuthors")
    @WebResult(name = "ListAuthorsResponse", targetNamespace = Namespace.URI)
    ListAuthorsResponse listAuthors(
            @WebParam(name = "ListAuthorsRequest", targetNamespace = Namespace.URI)
            ListAuthorsRequest request);

    @WebMethod(operationName = "UpdateAuthor", action = Namespace.URI + "UpdateAuthor")
    @WebResult(name = "UpdateAuthorResponse", targetNamespace = Namespace.URI)
    UpdateAuthorResponse updateAuthor(
            @WebParam(name = "UpdateAuthorRequest", targetNamespace = Namespace.URI)
            UpdateAuthorRequest request) throws ValidationFault, NotFoundFault;

    @WebMethod(operationName = "DeleteAuthor", action = Namespace.URI + "DeleteAuthor")
    @WebResult(name = "DeleteAuthorResponse", targetNamespace = Namespace.URI)
    DeleteAuthorResponse deleteAuthor(
            @WebParam(name = "DeleteAuthorRequest", targetNamespace = Namespace.URI)
            DeleteAuthorRequest request) throws NotFoundFault, ConflictFault;

    // ---------- PublishingCompany ----------

    @WebMethod(operationName = "CreatePublishingCompany", action = Namespace.URI + "CreatePublishingCompany")
    @WebResult(name = "CreatePublishingCompanyResponse", targetNamespace = Namespace.URI)
    CreatePublishingCompanyResponse createPublishingCompany(
            @WebParam(name = "CreatePublishingCompanyRequest", targetNamespace = Namespace.URI)
            CreatePublishingCompanyRequest request) throws ValidationFault;

    @WebMethod(operationName = "GetPublishingCompanyById", action = Namespace.URI + "GetPublishingCompanyById")
    @WebResult(name = "GetPublishingCompanyByIdResponse", targetNamespace = Namespace.URI)
    GetPublishingCompanyByIdResponse getPublishingCompanyById(
            @WebParam(name = "GetPublishingCompanyByIdRequest", targetNamespace = Namespace.URI)
            GetPublishingCompanyByIdRequest request) throws NotFoundFault;

    @WebMethod(operationName = "ListPublishingCompanies", action = Namespace.URI + "ListPublishingCompanies")
    @WebResult(name = "ListPublishingCompaniesResponse", targetNamespace = Namespace.URI)
    ListPublishingCompaniesResponse listPublishingCompanies(
            @WebParam(name = "ListPublishingCompaniesRequest", targetNamespace = Namespace.URI)
            ListPublishingCompaniesRequest request);

    @WebMethod(operationName = "UpdatePublishingCompany", action = Namespace.URI + "UpdatePublishingCompany")
    @WebResult(name = "UpdatePublishingCompanyResponse", targetNamespace = Namespace.URI)
    UpdatePublishingCompanyResponse updatePublishingCompany(
            @WebParam(name = "UpdatePublishingCompanyRequest", targetNamespace = Namespace.URI)
            UpdatePublishingCompanyRequest request) throws ValidationFault, NotFoundFault;

    @WebMethod(operationName = "DeletePublishingCompany", action = Namespace.URI + "DeletePublishingCompany")
    @WebResult(name = "DeletePublishingCompanyResponse", targetNamespace = Namespace.URI)
    DeletePublishingCompanyResponse deletePublishingCompany(
            @WebParam(name = "DeletePublishingCompanyRequest", targetNamespace = Namespace.URI)
            DeletePublishingCompanyRequest request) throws NotFoundFault, ConflictFault;
}
