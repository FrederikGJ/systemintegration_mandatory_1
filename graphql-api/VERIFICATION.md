# Verifikation — 19. oktober 2026

- Maven test: 8 integrationstests, 0 fejl, 0 skipped.
- Maven package: BUILD SUCCESS.
- Kørbar JAR startet; GraphiQL returnerede HTTP 200.
- Alle 12 requests fra den vedlagte Postman-collection kørt gennem HTTP mod en midlertidig databasekopi: bestået. Postmans UI er ikke anvendt.


Miljøet her har Java 17. Test og package blev derfor kørt med `-Djava.version=17`; den leverede pom.xml beholder Java 21 som projektet kræver. 
Java 21 og Windows-startscripts er ikke kørt i dette Linux-miljø. 
Hos os kører start.cmd build og tests med vores installerede JDK 21.

