# gRPC – manuel test

gRPC har ikke et collection-format, som Postman kan eksportere og importere. Her bruger vi i stedet:

| Værktøj   | Hvad                                    | Svarer til            |
|-----------|-----------------------------------------|-----------------------|
| `grpc.sh` | Script med alle 16 RPC'er og fejltjek   | Postman-collection    |
| `grpcurl` | Kommandolinje-klient, som scriptet bruger | `curl`              |
| `grpcui`  | Web-UI, der bygges ud fra serveren      | Postman / Swagger UI  |

Begge værktøjer bruger serverens reflection, så de behøver ikke `library.proto`.

## Installér (én gang)

Kræver Go:

```sh
go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest
go install github.com/fullstorydev/grpcui/cmd/grpcui@latest
```

De lægges i `~/go/bin`. Sæt den på `PATH` i `~/.zshrc` eller `~/.bashrc`:

```sh
export PATH="$PATH:$HOME/go/bin"
```

På Windows sætter Go's installer normalt selv `%USERPROFILE%\go\bin` på `PATH`. Åbn en ny terminal efter installationen. Mangler den, så tilføj den i PowerShell for det aktuelle vindue:

```powershell
$env:PATH += ";$env:USERPROFILE\go\bin"
```

## Start serveren

Create, Update og Delete skriver i databasen. Kør derfor mod en kopi, så `db/library.db` i git ikke ændres. Fra roden:

```sh
./mvnw -pl grpc-api -am package -DskipTests
cp db/library.db grpc-api/target/test.db
LIBRARY_DB=grpc-api/target/test.db java -jar grpc-api/target/grpc-api.jar
```

I PowerShell:

```powershell
.\mvnw.cmd -pl grpc-api -am package -DskipTests
Copy-Item db\library.db grpc-api\target\test.db
$env:LIBRARY_DB = "grpc-api/target/test.db"; java -jar grpc-api\target\grpc-api.jar
```

`target/` er gitignoret og slettes ved `./mvnw clean`.

## Kør scriptet

I en ny terminal, fra roden:

```sh
API_testing_media/grpc/grpc.sh              # det hele
API_testing_media/grpc/grpc.sh books        # kun én del
GRPC_HOST=localhost:9091 API_testing_media/grpc/grpc.sh
```

| Del         | Kald                                                                 |
|-------------|----------------------------------------------------------------------|
| `books`     | ListBooks (alle og forfatter 394), GetBookById, CreateBook, UpdateBook, DeleteBook |
| `watch`     | WatchBooks i baggrunden, CreateBook, tjek at bogen kom frem på streamen, DeleteBook |
| `authors`   | ListAuthors, GetAuthor, CreateAuthor, UpdateAuthor, DeleteAuthor     |
| `companies` | ListPublishingCompanies, GetPublishingCompany, Create, Update, Delete |
| `errors`    | Kald, der skal fejle med `NotFound` eller `InvalidArgument`          |

Hvert kald vises som `### Service/Metode`, request og svar. Lange lister forkortes til 20 linjer. Create, Update og Delete bruger det id, Create returnerer, så de rydder op efter sig selv. Scriptet stopper ved første uventede fejl.

## grpcui – web-UI

```sh
grpcui -plaintext localhost:9090
```

Åbner browseren. Vælg service og metode, udfyld felterne og tryk *Invoke*. God til at demonstrere API'et.

## Enkelte kald med grpcurl

```sh
grpcurl -plaintext localhost:9090 list                          # services
grpcurl -plaintext localhost:9090 describe library.BookService  # metoder
grpcurl -plaintext localhost:9090 describe library.Book         # felter
grpcurl -plaintext -d '{"id": 1000}' localhost:9090 library.BookService/GetBookById
```

- `-plaintext`: uden TLS, som vores server kører.
- `-d`: requesten som JSON med feltnavnene fra `library.proto`. grpcurl laver den om til binær protobuf.

## WatchBooks – streaming

`WatchBooks` svarer ikke én gang, men holder forbindelsen åben og sender hver bog, der oprettes med `CreateBook`. Prøv det i to terminaler:

```sh
# terminal 1: abonnér og vent (Ctrl+C stopper)
grpcurl -plaintext -d '{}' localhost:9090 library.BookService/WatchBooks

# terminal 2: opret en bog, som dukker op i terminal 1
grpcurl -plaintext -d '{"title": "Ny bog", "author_id": 394, "publishing_company_id": 74, "publishing_year": 2026}' \
  localhost:9090 library.BookService/CreateBook
```

Start gerne flere abonnenter; alle får bogen.

I grpcui virker WatchBooks også, men grpcui viser først beskederne, når kaldet slutter. Sæt derfor *Timeout* i formularen (fx 30 sekunder), opret bøger imens, og læs dem bagefter. Fanen viser så `DeadlineExceeded`, hvilket bare betyder, at tiden løb ud.

## Statuskoder

grpcurl skriver koderne med Go-navne:

| Java (`Status.Code`) | grpcurl           |
|----------------------|-------------------|
| `NOT_FOUND`          | `NotFound`        |
| `INVALID_ARGUMENT`   | `InvalidArgument` |
| `INTERNAL`           | `Internal`        |

## Hvorfor ikke Postman?

Postman kan godt kalde gRPC interaktivt (*New → gRPC*, `localhost:9090`, server reflection). Men gRPC-requests kommer ikke med, når en collection eksporteres som JSON. Det eneste filformat med gRPC er Postman v12's Native Git (YAML), og det er ikke dokumenteret. Derfor er `grpc.sh` vores collection: ren tekst, kan ligge i git og kan køres af alle.
