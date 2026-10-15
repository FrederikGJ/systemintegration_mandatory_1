# grpc-api – gRPC API

gRPC-server (grpc-java) oven på `db/`. Kontrakten er `src/main/proto/library.proto`; Maven genererer Java-klasserne ud fra den ved build.

## Services

| Service (`library.`)       | RPC'er                                                                                  |
|----------------------------|-----------------------------------------------------------------------------------------|
| `BookService`              | `ListBooks` (valgfrit filter `author_id`), `GetBookById`, `CreateBook`, `UpdateBook`, `DeleteBook`, `WatchBooks` (stream) |
| `AuthorService`            | `ListAuthors`, `GetAuthor`, `CreateAuthor`, `UpdateAuthor`, `DeleteAuthor`               |
| `PublishingCompanyService` | `ListPublishingCompanies`, `GetPublishingCompany`, `CreatePublishingCompany`, `UpdatePublishingCompany`, `DeletePublishingCompany` |

`Update...` overskriver hele rækken. `Delete...` svarer med `google.protobuf.Empty`.

### Opgavens krav

| Krav                                                         | Hvor                                                                 |
|--------------------------------------------------------------|----------------------------------------------------------------------|
| Unary `GetBookById(GetBookByIdRequest) returns (Book)`        | `BookService/GetBookById`, request `{ id }`                          |
| Unary `CreateBook(CreateBookRequest) returns (CreateBookResponse)` | `BookService/CreateBook`, request `{ title, author_id, publishing_company_id, publishing_year }`, svar `{ book }` med det nye id |
| Streaming `WatchBooks(WatchBooksRequest) returns (stream Book)` | `BookService/WatchBooks`: streamen er åben, indtil klienten afbryder, og sender hver bog, der oprettes med `CreateBook` |

Resten (List, Update, Delete og services til forfattere og forlag) er ekstra.

**WatchBooks ser kun bøger fra gRPC.** De fire API'er er hver sin proces og deler kun SQLite-filen. En bog oprettet via REST, SOAP eller GraphQL kommer derfor ikke ud på streamen; kun bøger fra denne servers `CreateBook` gør.

| Fejl                 | Statuskode         |
|----------------------|--------------------|
| id findes ikke       | `NOT_FOUND`        |
| tom titel eller navn | `INVALID_ARGUMENT` |
| databasefejl         | `INTERNAL`         |

## Opbygning

| Fil / klasse                   | Ansvar                                            |
|--------------------------------|---------------------------------------------------|
| `src/main/proto/library.proto` | Kontrakten: beskeder og services                  |
| `GrpcServer`                   | `main`: starter serveren og registrerer services  |
| `ExceptionInterceptor`         | Oversætter exceptions til statuskoder             |
| `service/*ServiceImpl`         | Én pr. service, kalder repository'et i `db/`      |
| `service/ProtoMapper`          | db-records ↔ protobuf-beskeder                    |

De genererede klasser (`Book`, `BookServiceGrpc`, ...) ligger i `target/generated-sources/protobuf`, pakke `dk.library.grpc.proto`. Dem skriver vi aldrig selv.

## Kør

```sh
./mvnw -pl grpc-api -am package -DskipTests
cd grpc-api && java -jar target/grpc-api.jar
```

| Miljøvariabel | Default            | Betydning                                            |
|---------------|--------------------|------------------------------------------------------|
| `GRPC_PORT`   | `9090`             | Port                                                 |
| `LIBRARY_DB`  | `../db/library.db` | Sti til databasen, se [db/README.md](../db/README.md) |

## Prøv det

Med [grpcurl](https://github.com/fullstorydev/grpcurl). Serveren har reflection, så klienten behøver ikke `.proto`-filen:

```sh
grpcurl -plaintext localhost:9090 list
grpcurl -plaintext -d '{"id": 1000}' localhost:9090 library.BookService/GetBookById
grpcurl -plaintext -d '{"author_id": 394}' localhost:9090 library.BookService/ListBooks
grpcurl -plaintext -d '{"title": "Ny bog", "author_id": 394, "publishing_company_id": 74, "publishing_year": 2026}' \
  localhost:9090 library.BookService/CreateBook
```

WatchBooks i to terminaler. Den første bliver ved med at køre og skriver hver ny bog, indtil du trykker Ctrl+C:

```sh
grpcurl -plaintext -d '{}' localhost:9090 library.BookService/WatchBooks   # terminal 1
grpcurl -plaintext -d '{"title": "Ny bog", "author_id": 394, "publishing_company_id": 74}' \
  localhost:9090 library.BookService/CreateBook                            # terminal 2
```

I Postman: *New → gRPC*, skriv `localhost:9090` og brug server reflection (eller importér `library.proto`).

Create, Update og Delete skriver i `db/library.db`, som ligger i git.

Alle 16 RPC'er på én gang, kørsel mod en kopi af databasen og web-UI'et grpcui: se [API_testing_media/grpc/](../API_testing_media/grpc/README.md).

## Test

```sh
./mvnw -pl grpc-api -am test
```

Testen starter serveren in-process (samme JVM, intet netværk) mod en midlertidig kopi af `library.db`. WatchBooks testes med to abonnenter, hvor den ene afbryder undervejs.

## Tekniske valg, kort forklaret

- **Kontrakt først.** `.proto`-filen er API'et. protoc genererer beskedklasser og en basisklasse pr. service; vi skriver kun implementeringen (`extends BookServiceImplBase`). En klient i et andet sprog genererer sin egen kode fra samme fil.
- **Feltnumre.** Protobuf sender felterne binært med deres nummer (`= 1`, `= 2`), ikke navnet. Derfor må et nummer aldrig skifte betydning, når API'et er i brug.
- **`optional` = NULL.** Protobuf kender ikke null; et felt, der ikke er sat, læses som `0` eller `""`. `optional` giver en `has...()`-metode, så `publishing_year` og `surname` kan skelne NULL fra 0 og tom tekst.
- **Én request-besked pr. RPC.** `GetBookByIdRequest { id }` i stedet for et rent tal. Så kan felter tilføjes senere uden at bryde klienter, sådan som `ListBooksRequest` har fået filteret `author_id`. Derfor findes `WatchBooksRequest {}`, selvom den er tom.
- **Server-streaming.** `watchBooks` gemmer klientens `StreamObserver` i et trådsikkert `Set` og returnerer med det samme uden `onCompleted`, så streamen forbliver åben. `createBook` sender den nye bog med `onNext` til alle i sættet. Når klienten afbryder, fjerner en `setOnCancelHandler` den igen.
- **`synchronized` om `onNext`.** En `StreamObserver` må ikke kaldes fra to tråde på én gang, og to `CreateBook` kan køre samtidig på hver sin tråd. Derfor låses der pr. abonnent, mens bogen sendes.
- **ExceptionInterceptor.** grpc-java sender enhver exception til klienten som `UNKNOWN`. Vores services kaster `Status.NOT_FOUND...asRuntimeException()`, og interceptoren sender den rigtige kode, samme idé som `@ControllerAdvice` i Spring.
- **Netty kun ved kørsel.** Koden kender kun `io.grpc`-API'et. HTTP/2-serveren (`grpc-netty-shaded`) findes via `ServiceLoader`, når programmet starter. *Shaded* betyder, at Netty er pakket om, så det ikke kolliderer med andre Netty-versioner.
- **Kørbar jar.** `maven-shade-plugin` samler alle afhængigheder i `target/grpc-api.jar`. `ServicesResourceTransformer` fletter filerne i `META-INF/services`, ellers finder gRPC ikke Netty.
- **protoc via Maven.** `os-maven-plugin` finder styresystem og CPU, så Maven selv henter den rigtige `protoc`. Der skal ikke installeres noget.
