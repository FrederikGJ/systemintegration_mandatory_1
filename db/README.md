# db – fælles datalag

Java-bibliotek, som de fire API'er bruger til at læse og skrive i `library.db`. Ren JDBC, ingen Spring.

## Databasen

`library.db` er **ikke** i git. Hent den fra undervisningen og læg den her som `db/library.db`.

| Tabel                | Indhold  | Kolonner                                                          |
|----------------------|----------|-------------------------------------------------------------------|
| `tbook`              | Bøger    | nBookID, cTitle, nAuthorID, nPublishingYear, nPublishingCompanyID |
| `tauthor`            | Forfattere | nAuthorID, cName, cSurname                                      |
| `tpublishingcompany` | Forlag   | nPublishingCompanyID, cName                                       |

Skemaet rører vi ikke. Kolonnenavnene mappes til pæne Java-navne i `model/`.

## Opbygning

| Pakke / klasse            | Ansvar                                                                                   |
|---------------------------|------------------------------------------------------------------------------------------|
| `Database`                | Finder filen, åbner forbindelser (WAL + 5 s busy_timeout), fejler ved start hvis filen mangler |
| `DatabaseException`       | Unchecked indpakning af `SQLException`                                                   |
| `model/`                  | `Book`, `Author`, `PublishingCompany` som records                                        |
| `repository/`             | Én klasse pr. tabel med `findAll`, `findById`, `create`, `update`, `delete`              |

Én forbindelse åbnes pr. kald og lukkes igen. Det er billigt i SQLite og gør klasserne trådsikre.

## Brug fra et API

```xml
<dependency>
  <groupId>dk.library</groupId>
  <artifactId>db</artifactId>
</dependency>
```

```java
var db = new Database();              // finder filen via LIBRARY_DB eller ../db/library.db
var books = new BookRepository(db);
books.findById(1000);
```

| Miljøvariabel | Default            | Betydning                 |
|---------------|--------------------|---------------------------|
| `LIBRARY_DB`  | `../db/library.db` | Sti til SQLite-filen      |

## Test

```sh
./mvnw -pl db test
```

Testene kører mod en midlertidig kopi af `library.db` og springes over, hvis filen mangler.
