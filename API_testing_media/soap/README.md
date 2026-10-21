# SOAP – manuel test

`library-soap.postman_collection.json` er en Postman-collection med alle 14 operationer og de tre faults.

| Mappe               | Kald                                                                            |
|---------------------|---------------------------------------------------------------------------------|
| `WSDL og XSD`       | Henter kontrakten (`?wsdl` og `?xsd=1`)                                          |
| `Book`              | CreateBook, GetBookById, UpdateBook, GetBookById igen, DeleteBook                |
| `Author`            | CreateAuthor, GetAuthorById, ListAuthors, UpdateAuthor, DeleteAuthor             |
| `PublishingCompany` | CreatePublishingCompany, GetPublishingCompanyById, ListPublishingCompanies, UpdatePublishingCompany, DeletePublishingCompany |
| `Faults`            | Kald, der skal give NotFoundFault, ValidationFault eller ConflictFault           |

Create gemmer det nye id i en collection-variabel (`{{bookId}}`, `{{authorId}}`, `{{publishingCompanyId}}`), som Get, Update og Delete bruger bagefter. Hver mappe sletter det, den har oprettet. Hvert kald har tests, der tjekker HTTP-status og en del af svaret.

## Start serveren

Create, Update og Delete skriver i databasen, så kør mod en kopi. Fra roden:

```sh
./mvnw -pl soap-api -am install -DskipTests
cd soap-api
cp ../db/library.db target/test.db
LIBRARY_DB=target/test.db ../mvnw exec:exec
```

I PowerShell:

```powershell
.\mvnw.cmd -pl soap-api -am install -DskipTests
cd soap-api
Copy-Item ..\db\library.db target\test.db
$env:LIBRARY_DB = "target/test.db"; ..\mvnw.cmd exec:exec
```

## Kør collectionen

1. Postman: *Import* og vælg `library-soap.postman_collection.json`.
2. Højreklik på collectionen og vælg *Run collection*. Kør mapperne i rækkefølge, så id'erne er sat, når de skal bruges.

Serveren kører på `http://localhost:8080/library`. Ret variablen `baseUrl` på collectionen, hvis den kører et andet sted.

## Sådan ser et kald ud

Hvert kald er en `POST` til endpointet med to headers og en SOAP-envelope som body:

| Header         | Værdi                                       |
|----------------|---------------------------------------------|
| `Content-Type` | `text/xml; charset=utf-8`                   |
| `SOAPAction`   | `"http://soap.library.dk/<Operation>"`      |

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:lib="http://soap.library.dk/">
  <soapenv:Body>
    <lib:GetBookByIdRequest>
      <id>1000</id>
    </lib:GetBookByIdRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

Kun rod-elementet har `lib:`-præfikset. Felterne indeni er uden namespace, sådan som XSD'en beskriver dem.

En fault svarer HTTP 500 (SOAP 1.1). Hvilken fault det er, står i `<detail>`.
