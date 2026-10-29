# GraphQL API — Mahdis del



## Start i Windows / IntelliJ

Åbn en PowerShell-terminal i projektroden:

```powershell
cd graphql-api
.\start.cmd
```

Scriptet bygger, kører tests og starter serveren. Stop med Ctrl+C.
GraphiQL: http://localhost:8082/graphiql
HTTP endpoint: POST http://localhost:8082/graphql

Test uden at starte serveren:

```powershell
cd graphql-api
.\test.cmd
```

Hvis du allerede står i graphql-api, spring `cd graphql-api` over.
I IntelliJ: højreklik på `graphql-api/pom.xml` → Add as Maven Project.
Hvis du kører GraphqlApplication direkte, sæt working directory til `graphql-api`.

Med en installeret Maven, fra graphql-api (også Linux/macOS):

```sh
mvn clean package
java -jar target/graphql-api.jar
```

## Database

Default er `../db/library.db`, relativt til arbejdsmappen. `LIBRARY_DB` kan være en absolut sti. Tests bruger automatisk en midlertidig kopi og ændrer ikke fællesdatabasen.
Den normale server skriver til fællesdatabasen. Lav en demokopi, mens alle API'er er stoppet, hvis du vil holde seed-data intakte:

```powershell
# Kør fra graphql-api, før start.cmd
Copy-Item ..\db\library.db .\demo.db
$env:LIBRARY_DB = (Resolve-Path .\demo.db).Path
.\start.cmd
```

`GRAPHQL_PORT` kan ændre porten; default er 8082.

## Første query

Indsæt i GraphiQL og tryk Play:

```graphql
query {
  bookById(id: 1000) {
    id
    title
    publishingYear
    author { name surname }
    publisher { name }
  }
}
```

## Krav og kontrakt

Schemaet er `src/main/resources/graphql/schema.graphqls`.

| Opgavens krav | GraphQL-felt |
|---|---|
| Get book by ID | bookById(id) |
| List all authors | authors |
| List all publishers | publishers |
| Create/update/delete book | createBook, updateBook, deleteBook |
| Create/update/delete author | createAuthor, updateAuthor, deleteAuthor |
| Create/update/delete publisher | createPublisher, updatePublisher, deletePublisher |

Create/update returnerer objektet med ID; delete returnerer true. Updates kræver alle inputfelter, også surname og publishingYear. Et tomt surname er tilladt for forfattere med ét navn. Returfelterne surname og publishingYear er nullable for at understøtte de eksisterende data. GraphQL-typen Publisher bruger den eksisterende Java-record PublishingCompany.

Inputvalidering: positive ID'er, ikke-blank titel/navn, kolonnelængder og eksisterende forfatter/forlag. Årstal 1–9999 er et lokalt designvalg; SOAP-kravet om minimum 1900 er ikke indført som et GraphQL-krav.

Fejl kommer i GraphQL-responsens `errors` med `extensions.code`:
- NOT_FOUND: objektet findes ikke.
- VALIDATION_ERROR: ugyldigt input eller reference.
- CONFLICT: forfatter/forlag bruges af bøger og kan ikke slettes.
Schemafejl (fx manglende påkrævet input) håndteres af GraphQL selv. Uventede serverfejl maskeres og logges af Spring.

## Postman og aflevering

Importér `API_testing_media/library-graphql.postman_collection.json` fra denne mappe.
Start serveren og kør collectionen i rækkefølge 01–12, gerne med Collection Runner. Den gemmer selv ID'er, demonstrerer alle 12 operationer og sletter sine demoposter til sidst. Hvis kørslen afbrydes, kan demoposter blive tilbage; brug demokopien ovenfor.
Testmaterialet ligger bevidst i din egen mappe for at respektere afgrænsningen. Det skal med i afleverings-ZIP'en sammen med resten af projektet.

## Koden og build-valget

- GraphqlApplication: starter Spring Boot.
- DatabaseConfig: opretter Database og de tre eksisterende repositories.
- LibraryController: mapper GraphQL-operationer og nested bogrelationer.
- LibraryService: inputkontrol og repository-kald.
- GraphqlExceptionHandler: oversætter forventede fejl.
- Inputs: de tre input-records.
- GraphqlApiTest: HTTP-integrationstests med rigtig SQLite-kopi.

Dette er et selvstændigt Maven-projekt med Spring Boot-parent. Build-helper læser Java-kilder fra `../db/src/main/java` og kompilerer dem ind i GraphQL-JAR'en; kilderne kopieres eller ændres ikke. Dermed kræves hverken ændring af rodens pom.xml eller installation af gruppens db-artifact. Kør build via denne mappes pom.xml/scripts. Rodens almindelige Maven-build inkluderer ikke GraphQL, da dens modul-liste er urørt.

## Kendt afgrænsning

Databasen har ikke fremmednøgle-constraints. Referencer og sletning kontrolleres i dette API, men kontrol og skrivning er separate repository-kald. Samtidige ændringer fra de andre API-processer kan derfor give et race; fuld atomisk integritet kræver en fælles ændring i datalaget, som ligger uden for den tilladte mappe. Dette API ændrer heller ikke gRPC-serverens WatchBooks; dens eksisterende stream udsender kun gRPC-oprettelser.


Spring-reference: https://docs.spring.io/spring-graphql/reference/controllers.html
