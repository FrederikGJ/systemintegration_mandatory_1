# soap-api – SOAP API

SOAP-service oven på `db/`, bygget med Jakarta XML Web Services (JAX-WS) via Metro. Ingen Spring. Kontrakten skrives som annoterede Java-klasser, og WSDL og XSD genereres ud fra dem (*contract-last*).

Ét HTTP-endpoint med alle operationer:

| Hvad | Adresse                                |
|------|----------------------------------------|
| Endpoint | `http://localhost:8080/library`    |
| WSDL | `http://localhost:8080/library?wsdl`   |
| XSD  | `http://localhost:8080/library?xsd=1`  |

## Operationer

Namespace: `http://soap.library.dk/`. Hver operation har et `<Navn>Request`- og et `<Navn>Response`-element.

| Operation                  | Input                                                          | Output                    | Faults                        |
|----------------------------|----------------------------------------------------------------|---------------------------|-------------------------------|
| `CreateBook`               | title, authorId, publishingCompanyId, publishingYear           | `id`                      | ValidationFault               |
| `GetBookById`              | id                                                             | `book` (Book)             | NotFoundFault                 |
| `UpdateBook`               | id, title, authorId, publishingCompanyId, publishingYear       | `success`, `message`      | ValidationFault, NotFoundFault |
| `DeleteBook`               | id                                                             | `success`, `message`      | NotFoundFault                 |
| `CreateAuthor`             | name, surname                                                  | `id`                      | ValidationFault               |
| `GetAuthorById`            | id                                                             | `author` (Author)         | NotFoundFault                 |
| `ListAuthors`              | tom request                                                    | `author` × n              | ingen                         |
| `UpdateAuthor`             | id, name, surname                                              | `success`, `message`      | ValidationFault, NotFoundFault |
| `DeleteAuthor`             | id                                                             | `success`, `message`      | NotFoundFault, ConflictFault  |
| `CreatePublishingCompany`  | name                                                           | `id`                      | ValidationFault               |
| `GetPublishingCompanyById` | id                                                             | `publishingCompany`       | NotFoundFault                 |
| `ListPublishingCompanies`  | tom request                                                    | `publishingCompany` × n   | ingen                         |
| `UpdatePublishingCompany`  | id, name                                                       | `success`, `message`      | ValidationFault, NotFoundFault |
| `DeletePublishingCompany`  | id                                                             | `success`, `message`      | NotFoundFault, ConflictFault  |

Complex types: `Book` (id, title, authorId, publishingCompanyId, publishingYear), `Author` (id, name, surname) og `PublishingCompany` (id, name). Alle tal er `xs:integer`.

Kvitteringen (`success` og `message`, fx `Book 1000 updated`) er vores eget valg. Fejl kommer altid som faults, så `success` er altid `true`.

## Fejl

| Fault             | Hvornår                                                                                 |
|-------------------|-----------------------------------------------------------------------------------------|
| `NotFoundFault`   | Id'et, operationen handler om, findes ikke                                               |
| `ValidationFault` | Et felt mangler, er ugyldigt eller henviser til en forfatter eller et forlag, der ikke findes |
| `ConflictFault`   | En forfatter eller et forlag slettes, mens bøger stadig henviser til det                  |

Hver fault er et SOAP 1.1-fault med HTTP 500. Navnet står i `<detail>`, og beskeden både i `<faultstring>` og `<detail>`:

```xml
<S:Fault>
  <faultcode>S:Server</faultcode>
  <faultstring>Book -1 does not exist</faultstring>
  <detail>
    <ns2:NotFoundFault xmlns:ns2="http://soap.library.dk/"><message>Book -1 does not exist</message></ns2:NotFoundFault>
  </detail>
</S:Fault>
```

### Validering

| Felt                  | Regel                                                    |
|-----------------------|----------------------------------------------------------|
| alle felter           | Påkrævet. Tekst må ikke være tom, tal skal være heltal   |
| `title`               | Højst 255 tegn                                           |
| `name` (forfatter)    | Højst 40 tegn                                            |
| `surname`             | Højst 60 tegn                                            |
| `name` (forlag)       | Højst 40 tegn                                            |
| `publishingYear`      | Mellem 1900 (opgaven) og 9999 (kolonnen er `decimal(4,0)`) |
| `authorId`            | Forfatteren skal findes                                  |
| `publishingCompanyId` | Forlaget skal findes                                     |

Længderne kommer fra kolonnerne i databasen. SQLite håndhæver dem ikke selv.

I `Update...` tjekkes først requestens egne felter (ValidationFault), så om id'et findes (NotFoundFault) og til sidst om forfatter og forlag findes (ValidationFault). `Get...` og `Delete...` kan kun give NotFoundFault, så et manglende id svarer også med den.

## Opbygning

| Pakke (`dk.library.soap`) | Indhold                                                                     |
|---------------------------|-----------------------------------------------------------------------------|
| `LibraryPublisher`        | `main`: publicerer endpointet med `Endpoint.publish`                        |
| `service/LibraryService`  | Kontrakten: én metode pr. operation, `throws` bestemmer faults              |
| `service/LibraryServiceImpl` | Validering og kald til repositories i `db/`                              |
| `data/`                   | De tre complex types: `Book`, `Author`, `PublishingCompany`                 |
| `messages/`               | Request- og response-klasser, én mappe pr. entitet (28 i alt)               |
| `faults/`                 | `NotFoundFault`, `ValidationFault`, `ConflictFault` og deres fælles `FaultDetail` |
| `logging/LoggingHandler`  | Skriver hver SOAP-besked ud i konsollen                                     |

## Kør

```sh
./mvnw -pl soap-api -am install -DskipTests   # bygger db og soap-api og lægger db i ~/.m2
cd soap-api
../mvnw exec:exec
```

På Windows i PowerShell: `..\mvnw.cmd exec:exec`. I IntelliJ kan du også bare køre `LibraryPublisher.main`. En anden adresse kan gives som første argument, fx `http://localhost:8081/library`.

Create, Update og Delete skriver i `db/library.db`, som ligger i git. Kør derfor gerne mod en kopi:

```sh
cp ../db/library.db target/test.db
LIBRARY_DB=target/test.db ../mvnw exec:exec
```

| Miljøvariabel | Default            | Betydning                                             |
|---------------|--------------------|-------------------------------------------------------|
| `LIBRARY_DB`  | `../db/library.db` | Sti til databasen, se [db/README.md](../db/README.md) |

Under build skriver `wsgen` også kontrakten ud som filer: `target/generated-sources/wsdl/LibraryService.wsdl` og `LibraryService_schema1.xsd`.

## Prøv det

Postman-collection med alle operationer og faults: se [API_testing_media/soap/](../API_testing_media/soap/README.md).

Med curl:

```sh
curl -X POST http://localhost:8080/library \
  -H 'Content-Type: text/xml; charset=utf-8' \
  -H 'SOAPAction: "http://soap.library.dk/GetBookById"' \
  -d '<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:lib="http://soap.library.dk/">
        <soapenv:Body><lib:GetBookByIdRequest><id>1000</id></lib:GetBookByIdRequest></soapenv:Body>
      </soapenv:Envelope>'
```

## Test

```sh
./mvnw -pl soap-api -am test
```

Testen publicerer endpointet på en ledig port mod en midlertidig kopi af `library.db`. Den kalder det med en JAX-WS-klient bygget ud fra WSDL'en og med rå SOAP-XML over HTTP.

## Tekniske valg, kort forklaret

- **Contract-last.** Vi skriver Java-klasserne, og Metro genererer WSDL og XSD ud fra annoteringerne: `@WebService` bliver til portType og service, `@XmlType` til `xs:complexType`, og `List<Author>` til et element med `maxOccurs="unbounded"`.
- **DOCUMENT / LITERAL / BARE.** Hver besked har præcis én part, nemlig request- eller response-elementet. Derfor har hver operation sin egen request-klasse: Metro finder operationen ud fra request-elementets navn.
- **`xs:integer`.** En Java-`int` giver `xs:int`. `@XmlSchemaType(name = "integer")` får XSD'en til at bruge opgavens type.
- **Faults som checked exceptions.** `@WebFault` plus `getFaultInfo()` gør en exception til en `wsdl:fault`. Når en metode i `LibraryService` erklærer den med `throws`, kommer den med på netop den operation i WSDL'en. JAX-WS sætter `faultcode` til `S:Server` for den slags faults, og SOAP 1.1 svarer altid HTTP 500.
- **Validering i Java.** En genereret XSD kan ikke udtrykke "mindst 1900" eller "forfatteren skal findes", og Metro validerer ikke requests mod XSD'en. Derfor bruger request-klasserne `Integer` og `String`: et manglende element bliver `null` i stedet for `0`, så servicen kan svare med ValidationFault. Tekst i et talfelt (fx `abc`) bliver også `null`.
- **Ingen fremmednøgler.** `library.db` har ingen foreign keys, så det er servicen, der tjekker, at en bog peger på en forfatter og et forlag, der findes. Den tjekker også, at ingen bøger peger på en forfatter eller et forlag, før det slettes.
- **No-arg-konstruktør.** JAX-WS kræver en offentlig konstruktør uden parametre på implementeringen, ellers afviser `wsgen` den. Den finder databasen via `LIBRARY_DB`.
- **`exec:exec`, ikke `exec:java`.** `exec:java` kører programmet inde i Mavens egen JVM, hvor Metro ikke kan finde sin SAAJ-implementering, når `LoggingHandler` læser beskeden. `exec:exec` starter en almindelig `java`-proces.
- **Indbygget HTTP-server.** `Endpoint.publish` bruger JDK'ens `com.sun.net.httpserver`. Der er ingen servlet-container og ingen Tomcat.
