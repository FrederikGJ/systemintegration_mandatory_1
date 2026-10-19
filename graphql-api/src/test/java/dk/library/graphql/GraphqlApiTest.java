package dk.library.graphql;

import com.fasterxml.jackson.databind.JsonNode;
import dk.library.db.Database;
import java.nio.file.*;
import java.util.Map;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GraphqlApiTest {
    static final Path db = copyDatabase();
    @Autowired TestRestTemplate http;
    @Autowired Database database;
    static Path copyDatabase() {
        try {
            Path temp = Files.createTempDirectory("graphql-test-").resolve("library.db");
            Files.copy(Path.of("../db/library.db"), temp);
            temp.toFile().deleteOnExit();
            return temp;
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r) {
        r.add("library.db.path", () -> db.toString());
    }
    JsonNode request(String query) {
        var response = http.postForEntity("/graphql", Map.of("query", query), JsonNode.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return response.getBody();
    }
    JsonNode ok(String query) {
        JsonNode result = request(query);
        assertThat(result.has("errors")).as(result.toString()).isFalse();
        return result.path("data");
    }
    void error(String query, String code) {
        JsonNode result = request(query);
        assertThat(result.path("errors").isArray()).isTrue();
        assertThat(result.at("/errors/0/extensions/code").asText()).isEqualTo(code);
    }
    @Test void readsSeedDataAndNestedRelations() {
        var data = ok("{ bookById(id:1000) { id title authorId publishingCompanyId author { id name surname } publisher { id name } } authors { id name surname } publishers { id name } }");
        assertThat(data.at("/bookById/id").asText()).isEqualTo("1000");
        assertThat(data.at("/bookById/author/id").asText()).isEqualTo(data.at("/bookById/authorId").asText());
        assertThat(data.at("/bookById/publisher/id").asText()).isEqualTo(data.at("/bookById/publishingCompanyId").asText());
        assertThat(data.path("authors").size()).isGreaterThan(0);
        assertThat(data.path("publishers").size()).isGreaterThan(0);
    }
    @Test void completeCrudAndConflictProtection() {
        String a = ok("mutation { createAuthor(input:{name:\"GraphQL\",surname:\"Test\"}) { id } }").at("/createAuthor/id").asText();
        String p = ok("mutation { createPublisher(input:{name:\"GraphQL Test\"}) { id } }").at("/createPublisher/id").asText();
        String b = ok("mutation { createBook(input:{title:\"Before\",authorId:"+a+",publishingCompanyId:"+p+",publishingYear:2020}) { id } }").at("/createBook/id").asText();
        error("mutation { deleteAuthor(id:"+a+") }", "CONFLICT");
        error("mutation { deletePublisher(id:"+p+") }", "CONFLICT");
        assertThat(ok("mutation { updateAuthor(id:"+a+",input:{name:\"After\",surname:\"Changed\"}) { name surname } }").at("/updateAuthor/surname").asText()).isEqualTo("Changed");
        assertThat(ok("mutation { updatePublisher(id:"+p+",input:{name:\"After\"}) { name } }").at("/updatePublisher/name").asText()).isEqualTo("After");
        var updated = ok("mutation { updateBook(id:"+b+",input:{title:\"After\",authorId:394,publishingCompanyId:74,publishingYear:2026}) { title authorId publishingCompanyId publishingYear } }").path("updateBook");
        assertThat(updated.path("title").asText()).isEqualTo("After");
        assertThat(updated.path("authorId").asInt()).isEqualTo(394);
        assertThat(updated.path("publishingCompanyId").asInt()).isEqualTo(74);
        assertThat(updated.path("publishingYear").asInt()).isEqualTo(2026);
        assertThat(ok("mutation { deleteBook(id:"+b+") }").path("deleteBook").asBoolean()).isTrue();
        error("{ bookById(id:"+b+") { id } }", "NOT_FOUND");
        ok("mutation { deleteAuthor(id:"+a+") deletePublisher(id:"+p+") }");
    }
    @Test void missingEntitiesHaveUsefulErrors() {
        error("{ bookById(id:2147483647) { id } }", "NOT_FOUND");
        for (String type : new String[]{"Book", "Author", "Publisher"})
            error("mutation { delete"+type+"(id:2147483647) }", "NOT_FOUND");
        error("mutation { updateBook(id:2147483647,input:{title:\"X\",authorId:394,publishingCompanyId:74,publishingYear:2026}) { id } }", "NOT_FOUND");
        error("mutation { updateAuthor(id:2147483647,input:{name:\"X\",surname:\"Y\"}) { id } }", "NOT_FOUND");
        error("mutation { updatePublisher(id:2147483647,input:{name:\"X\"}) { id } }", "NOT_FOUND");
    }
    @Test void validatesIdsAndText() {
        error("{ bookById(id:\"abc\") { id } }", "VALIDATION_ERROR");
        error("{ bookById(id:-1) { id } }", "VALIDATION_ERROR");
        error("{ bookById(id:\"999999999999\") { id } }", "VALIDATION_ERROR");
        error("mutation { createAuthor(input:{name:\"   \",surname:\"X\"}) { id } }", "VALIDATION_ERROR");
        error("mutation { createPublisher(input:{name:\""+"x".repeat(41)+"\"}) { id } }", "VALIDATION_ERROR");
        error("mutation { createBook(input:{title:\"\",authorId:394,publishingCompanyId:74,publishingYear:2026}) { id } }", "VALIDATION_ERROR");
    }
    @Test void validatesReferencesAndYear() {
        error("mutation { createBook(input:{title:\"X\",authorId:2147483647,publishingCompanyId:74,publishingYear:2026}) { id } }", "VALIDATION_ERROR");
        error("mutation { createBook(input:{title:\"X\",authorId:394,publishingCompanyId:2147483647,publishingYear:2026}) { id } }", "VALIDATION_ERROR");
        error("mutation { createBook(input:{title:\"X\",authorId:394,publishingCompanyId:74,publishingYear:10000}) { id } }", "VALIDATION_ERROR");
        error("mutation { updateBook(id:1000,input:{title:\"X\",authorId:2147483647,publishingCompanyId:74,publishingYear:2026}) { id } }", "VALIDATION_ERROR");
    }
    @Test void schemaRejectsPartialUpdatesAndUnknownFields() {
        assertThat(request("mutation { updateAuthor(id:1,input:{name:\"Only name\"}) { id } }").has("errors")).isTrue();
        assertThat(request("mutation { updateBook(id:1000,input:{title:\"Only title\"}) { id } }").has("errors")).isTrue();
        assertThat(request("{ bookById(id:1000) { unknownField } }").has("errors")).isTrue();
    }
    @Test void acceptsVariablesAndOperationName() {
        var response = http.postForEntity("/graphql", Map.of("query", "query Find($id: ID!) { bookById(id:$id) { id } }", "variables", Map.of("id", "1000"), "operationName", "Find"), JsonNode.class);
        assertThat(response.getBody().at("/data/bookById/id").asText()).isEqualTo("1000");
    }
    @Test void readsNullableLegacyValues() throws Exception {
        int id;
        try (var c = database.connect(); var st = c.createStatement()) {
            st.executeUpdate("INSERT INTO tbook(cTitle,nAuthorID,nPublishingCompanyID,nPublishingYear) VALUES('Nullable year',394,74,NULL)");
            try(var rs = st.executeQuery("SELECT last_insert_rowid()")) { rs.next(); id = rs.getInt(1); }
        }
        try { assertThat(ok("{ bookById(id:"+id+") { publishingYear } }").at("/bookById/publishingYear").isNull()).isTrue(); }
        finally { ok("mutation { deleteBook(id:"+id+") }"); }
    }
}
