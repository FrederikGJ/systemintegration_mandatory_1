# Library APIs – Systemintegration, Mandatory Assignment 1

Fire API'er til et bibliotek, bygget oven på én fælles SQLite-database.

**Gruppe:** Lukas, Mahdi og Frederik

## Tech stack

| Lag       | Valg                                                        |
|-----------|-------------------------------------------------------------|
| Sprog     | **Java 21**                                                 |
| Build     | Maven, multi-module. `./mvnw` henter selv Maven, så kun Java 21 skal være installeret |
| Database  | SQLite via `sqlite-jdbc`. Ren JDBC i `db/`, ingen ORM       |
| REST      | Spring Boot (Spring Web)                                    |
| SOAP      | Jakarta XML Web Services (JAX-WS) via Metro, uden Spring    |
| GraphQL   | Spring Boot (Spring for GraphQL)                            |
| gRPC      | grpc-java + protobuf                                        |

## Struktur

| Mappe                | Indhold                                                   |
|----------------------|-----------------------------------------------------------|
| `db/`                | SQLite-databasen og det fælles datalag, alle API'er bruger. Se [db/README.md](db/README.md) |
| `rest-api/`          | REST API                                                  |
| `soap-api/`          | SOAP API. Se [soap-api/README.md](soap-api/README.md)     |
| `graphql-api/`       | GraphQL API                                               |
| `grpc-api/`          | gRPC API. Se [grpc-api/README.md](grpc-api/README.md)     |
| `API_testing_media/` | Manuel test af API'erne, én mappe pr. API. Se [API_testing_media/grpc/README.md](API_testing_media/grpc/README.md) og [API_testing_media/soap/README.md](API_testing_media/soap/README.md) |

## Kom i gang

Databasen ligger i git, så der er intet at hente først. Byg og test alt:

```sh
./mvnw test
```

## Licens

GNU General Public License v3.0, se [LICENSE](LICENSE).
