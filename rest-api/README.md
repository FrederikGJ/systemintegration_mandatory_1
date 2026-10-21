# rest-api – REST API

REST API (Spring Boot, Spring Web) oven på `db/`. JSON ind og ud, fejl som Problem Details (RFC 9457).

## Endpoints

| Ressource  | Metode   | URL                       | Svar                                     |
|------------|----------|---------------------------|------------------------------------------|
| Bøger      | `GET`    | `/api/books?limit=&offset=` | `200` + side: `{ items, limit, offset, total }` |
|            | `GET`    | `/api/books/{id}`         | `200` eller `404`                        |
|            | `POST`   | `/api/books`              | `201` + `Location`-header + ny bog       |
|            | `PUT`    | `/api/books/{id}`         | `200` + opdateret bog, eller `404`       |
|            | `DELETE` | `/api/books/{id}`         | `204` uden body, eller `404`             |
| Forfattere | `GET`    | `/api/authors`            | `200` + liste med alle                   |
|            | `GET`/`POST`/`PUT`/`DELETE` | `/api/authors[/{id}]` | Som bøger                    |
| Forlag     | `GET`    | `/api/publishers`         | `200` + liste med alle                   |
|            | `GET`/`POST`/`PUT`/`DELETE` | `/api/publishers[/{id}]` | Som bøger                 |

Opgaven kalder forlag *publishers*, databasen kalder dem *publishing companies*. URL'en følger opgaven, svarets felter følger `db/`.

### Bodies

```json
POST /api/books
{ "title": "Babettes gæstebud", "authorId": 511, "publishingYear": 1958, "publishingCompanyId": 159 }

POST /api/authors
{ "name": "Karen", "surname": "Blixen" }

POST /api/publishers
{ "name": "Gyldendal" }
```

`publishingYear` og `surname` må udelades og bliver så `null`. `id` står aldrig i en body; det kommer fra URL'en. `PUT` tager samme body som `POST` og overskriver hele rækken.

### Pagination

`GET /api/books?limit=5&offset=10` giver bog nr. 11–15, sorteret efter id. `limit` er 1–100 (default 20), `offset` er mindst 0 (default 0). `total` er antallet af bøger i alt, så klienten ved, hvornår der ikke er flere sider. Et `offset` efter sidste bog giver en tom liste, ikke en fejl.

### Fejl

| Fejl                                                       | Status |
|------------------------------------------------------------|--------|
| id findes ikke                                             | `404`  |
| ugyldig body (tom titel, for langt navn, manglende felt)   | `400` med `errors` |
| `limit`/`offset` uden for grænserne                        | `400` med `errors` |
| bog med `authorId` eller `publishingCompanyId`, der ikke findes | `400` |
| ødelagt JSON eller tekst, hvor der skal stå et tal          | `400`  |
| forkert `Content-Type`                                     | `415`  |
| databasefejl og andre uventede fejl                        | `500`  |

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Ugyldig request-body",
  "instance": "/api/books",
  "errors": ["authorId: must not be null", "title: must not be blank"]
}
```

## Opbygning

| Fil / klasse                    | Ansvar                                                         |
|---------------------------------|----------------------------------------------------------------|
| `RestApplication`               | `main` og beans for `Database` og de tre repositories          |
| `controller/*Controller`        | Én pr. ressource, kalder repository'et i `db/`                 |
| `dto/*Request`                  | Request-bodies uden `id`, med validering. `toModel(id)` laver db-recorden |
| `dto/PageResponse`              | Én side af en liste                                            |
| `ApiExceptionHandler`           | Oversætter exceptions til Problem Details                      |

Svarene er db-recordsene (`Book`, `Author`, `PublishingCompany`) direkte; Jackson laver dem om til JSON.

## Kør

```sh
./mvnw -pl rest-api -am package -DskipTests
cd rest-api && java -jar target/rest-api.jar
```

| Miljøvariabel | Default            | Betydning                                            |
|---------------|--------------------|------------------------------------------------------|
| `SERVER_PORT` | `8080`             | Port (Spring Boots egen variabel)                    |
| `LIBRARY_DB`  | `../db/library.db` | Sti til databasen, se [db/README.md](../db/README.md) |

## Prøv det

```sh
curl localhost:8080/api/books/1000
curl 'localhost:8080/api/books?limit=5&offset=10'
curl -i -H 'Content-Type: application/json' \
  -d '{"title": "Ny bog", "authorId": 394, "publishingCompanyId": 74, "publishingYear": 2026}' \
  localhost:8080/api/books
```

Alle endpoints med tests som Postman-collection: se [API_testing_media/rest/](../API_testing_media/rest/README.md).

POST, PUT og DELETE skriver i `db/library.db`, som ligger i git.

## Test

Modulet har ingen JUnit-tests. Testen er Postman-collectionen, som kører alle 15 endpoints og fejltilfældene mod en kørende server. Pagination i SQL testes i `db/` (`RepositoryTest.booksArePaged`).

## Tekniske valg, kort forklaret

- **Spring Boot uden dens forælder-pom.** Projektet har sin egen forælder (`library-parent`). Spring Boots versioner hentes derfor ved at importere BOM'en `spring-boot-dependencies`, og kun i dette modul, så den ikke ændrer versioner i `db` og `grpc-api`. Faste versioner fra forælderen (fx sqlite-jdbc 3.53) går forud for en importeret BOM.
- **To ting, forælder-pom'en ellers gør.** `spring-boot-maven-plugin` skal have målet `repackage` nævnt, før den laver den kørbare `target/rest-api.jar`. Og compileren skal have `<parameters>true</parameters>`, så parameternavne gemmes i class-filerne; ellers kan Spring ikke koble `@PathVariable int id` til `{id}`.
- **db-klasserne som `@Bean`.** `db/` er ren Java uden Spring, fordi gRPC også bruger det. Derfor laves `Database` og repositories i `RestApplication` og injiceres i controllernes constructors. Der findes én af hver (singleton).
- **Request-records uden `id`.** Klienten kan ikke sende et `id`, der modsiger URL'en. `Integer` i stedet for `int`, så et manglende felt bliver `null` og fanges af `@NotNull` i stedet for stille at blive `0`. Længdegrænserne (`@Size`) følger kolonnerne i skemaet, fordi SQLite selv ignorerer `varchar(40)`.
- **Problem Details (RFC 9457).** Standardformatet for fejl i HTTP-API'er; Spring har det indbygget som `ProblemDetail`. `ApiExceptionHandler` arver fra `ResponseEntityExceptionHandler`, som allerede håndterer Springs egne fejl (ugyldig JSON, forkert type, `415` osv.), og tilføjer `errors` med feltfejl, `500` ved `DatabaseException` og en generisk `500` for alt andet, så interne detaljer ikke lækker.
- **`ResponseStatusException` til 404.** Controllerne kaster `new ResponseStatusException(NOT_FOUND, "Bog 5 findes ikke")`; ingen egne exception-klasser.
- **`201` med `Location`.** REST-konventionen for "oprettet". URL'en bygges ud fra den aktuelle request, så host og port altid passer.
- **`PUT` overskriver hele rækken**, som `Update` i gRPC. Udeladte felter bliver `null`. Ingen `PATCH`.
- **Pagination i SQL.** `LIMIT ? OFFSET ?` henter kun den ene side fra SQLite. `ORDER BY nBookID` er nødvendig, ellers garanterer SQL ikke, at side 2 fortsætter, hvor side 1 slap. Max 100 pr. side, så én request ikke kan bede om hele tabellen.
- **Fremmednøgler tjekkes i controlleren.** Skemaet har ingen FK'er, så uden tjekket kunne en bog pege på en forfatter, der ikke findes. Det giver `400`, fordi fejlen ligger i bodyen, ikke i URL'en. Sletning af en forfatter eller et forlag med bøger er tilladt, som i gRPC.
