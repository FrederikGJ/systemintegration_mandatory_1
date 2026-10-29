# REST – manuel test

`library-rest.postman_collection.json` er en Postman-collection (v2.1) med alle 15 endpoints og fejltilfældene, 26 requests i alt. Hver request har tests (`pm.test`), så en kørsel af hele collectionen viser med det samme, om API'et svarer rigtigt.

## Start serveren

POST, PUT og DELETE skriver i databasen. Kør derfor mod en kopi, så `db/library.db` i git ikke ændres. Fra roden:

```sh
./mvnw -pl rest-api -am package -DskipTests
cp db/library.db rest-api/target/test.db
LIBRARY_DB=rest-api/target/test.db java -jar rest-api/target/rest-api.jar
```

I PowerShell:

```powershell
.\mvnw.cmd -pl rest-api -am package -DskipTests
Copy-Item db\library.db rest-api\target\test.db
$env:LIBRARY_DB = "rest-api/target/test.db"; java -jar rest-api\target\rest-api.jar
```

`target/` er gitignoret og slettes ved `./mvnw clean`.

Collectionen sletter godt nok det, den opretter, men SQLite's `AUTOINCREMENT` tæller stadig op, så id'erne ikke genbruges. Kørt mod `db/library.db` ville filen altså ændre sig i git.

## Kør collectionen

1. Postman: *Import* og vælg `library-rest.postman_collection.json`.
2. Højreklik på *Library REST API* → *Run collection* → *Run*.

Requests skal køre i rækkefølge, for de bruger hinandens id'er:

| Mappe                          | Requests                                                                  |
|--------------------------------|---------------------------------------------------------------------------|
| `1 Forfattere`                 | Opret, hent, list alle, opdater. Fejl: findes ikke (404), tomt navn (400) |
| `2 Forlag`                     | Samme som forfattere                                                      |
| `3 Bøger`                      | Opret, hent, list (default, side 3 med 5 pr. side, sidste side), opdater. Fejl: ukendt forfatter, ugyldig body, `limit=500` (400). Slet, og hent den slettede (404) |
| `4 Slet forfatter og forlag`   | Slet begge (204), slet forfatteren igen (404)                             |

Sletning af forfatter og forlag ligger for sig til sidst, fordi bogen i mappe 3 bruger dem.

Enkelte requests kan også sendes alene, når collection-variablerne er sat, fx efter én kørsel.

## Variabler

Collection-variabler (fanen *Variables* på collectionen):

| Variabel      | Værdi                   | Sat af                                    |
|---------------|-------------------------|-------------------------------------------|
| `baseUrl`     | `http://localhost:8080` | Dig. Ret den, hvis serveren kører med en anden `SERVER_PORT` |
| `unknownId`   | `999999`                | Fast id, der ikke findes                  |
| `authorId`    | (tom)                   | Test-scriptet på *Opret forfatter*        |
| `publisherId` | (tom)                   | Test-scriptet på *Opret forlag*           |
| `bookId`      | (tom)                   | Test-scriptet på *Opret bog*              |
| `lastOffset`  | (tom)                   | Pre-request-scriptet på *List bøger (sidste side)*: `total - 1` |

## Hvad testene tjekker

- **Status** på hver request: `200`, `201`, `204`, `400` eller `404`.
- **Opret:** `Location`-headeren peger på den nye række, og body har et nyt id og felterne fra requesten. Id'et gemmes i en variabel.
- **Hent og opdater:** felterne i svaret. Opdater tjekker også, at et udeladt felt (`surname`, `publishingYear`) bliver `null`.
- **List bøger:** `limit` og `offset` i svaret, antallet af bøger på siden, sortering efter id, og at den nye bog står på sidste side.
- **Fejl:** `Content-Type: application/problem+json`, teksten i `detail` og felterne i `errors`.
- **Slet:** `204` uden body, og at rækken derefter giver `404`.
