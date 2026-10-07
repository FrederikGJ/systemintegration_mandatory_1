# db – fælles datalag

Java-bibliotek, som de fire API'er bruger til at læse og skrive i `library.db`. Ren JDBC, ingen Spring.

## Databasen

`library.db` ligger i git og indeholder seed-data fra undervisningen. Filen ændrer sig, når API'erne skriver i den, så tjek `git status` før du committer, hvis du ikke vil have testdata med. SQLites side-filer (`-wal`, `-shm`, `-journal`) er gitignoret.

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

## Tekniske valg, kort forklaret

- **WAL (Write-Ahead Logging).** SQLites standard er en rollback-journal, hvor én skriver spærrer for alle læsere. I WAL-tilstand skrives ændringer først til `library.db-wal` og flyttes senere ind i `library.db` (et *checkpoint*). Læsere læser videre imens, så vores fire API-processer kan dele én fil uden at vente på hinanden. `-shm` er et lille index til WAL-filen. Kopier derfor ikke `library.db` alene, mens et API kører, for de nyeste skrivninger kan stadig ligge i `-wal`.
- **busy_timeout 5 s.** SQLite tillader kun én skriver ad gangen. Uden timeout ville skriver nr. to straks fejle med *database is locked*. Med 5 s venter den og prøver igen, før den giver op.
- **Én forbindelse pr. kald.** SQLite har ingen server; en forbindelse er bare en åben fil, så det er billigt at åbne. Hver repository-metode åbner sin egen i `try-with-resources` og lukker den igen. Ingen pool og ingen delt tilstand, og dermed trådsikkert, når Spring håndterer flere requests samtidig.
- **PreparedStatement med `?`.** Værdier sendes adskilt fra SQL'en, så de aldrig kan blive til SQL (SQL injection). Kun `findAll` bruger et almindeligt `Statement`, fordi den ingen parametre har.
- **`DatabaseException`.** `SQLException` er en checked exception, som ellers skulle erklæres hele vejen op gennem API-lagene. Vi pakker den i en unchecked exception og beholder originalen som `cause`.
- **Records og `Optional`.** Modellerne er records: uforanderlige og med `equals`, `hashCode` og `toString` gratis. `withId` findes, fordi et record-felt ikke kan ændres. `findById` returnerer `Optional` i stedet for `null`, så kalderen tvinges til at tage stilling til "ikke fundet" (fx 404).
- **Genererede id'er og NULL.** `create` beder JDBC om det id, SQLite tildeler (`RETURN_GENERATED_KEYS`), og returnerer objektet med det. `nPublishingYear` kan være NULL, og `rs.getInt` giver 0 for NULL, så vi tjekker `rs.wasNull()` og bruger `Integer` i modellen.

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

Stien er relativ til arbejdsmappen. Maven kører hvert modul fra dets egen mappe, derfor `../db/`. Starter du et API fra roden, så sæt `LIBRARY_DB=db/library.db`.

## Test

```sh
./mvnw -pl db test
```

Testene kører mod en midlertidig kopi af `library.db`, så filen i git ikke ændres.
