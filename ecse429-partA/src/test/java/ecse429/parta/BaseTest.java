package ecse429.parta;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.is;

/**
 * Shared test base for Part A.
 * - Sets RestAssured base URI from system property BASE_URL (defaults to http://localhost:4567)
 * - Provides helpers to create and clean up todos
 */
public abstract class BaseTest {

    // Allow override: mvn -q -DBASE_URL=http://localhost:4567 test
    protected static final String BASE = System.getProperty("BASE_URL", "http://localhost:4567");

    @BeforeAll
    static void setupRestAssured() {
        RestAssured.baseURI = BASE;
        RestAssured.useRelaxedHTTPSValidation();
    }

    /**
     * Create a todo and return its id as a String.
     */
    protected String createTodo(String title, boolean done, String description) {
        String json = String.format(
                "{\"title\":\"%s\",\"doneStatus\":%s,\"description\":\"%s\"}",
                title, done, description
        );

        return RestAssured
                .given()
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/todos")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract()
                .jsonPath().getString("id");
    }

    /**
     * Delete a todo by id if it exists; ignore if already gone.
     */
    protected void deleteTodoIfExists(String id) {
        if (id == null || id.isEmpty()) return;

        RestAssured
                .given()
                .when()
                .delete("/todos/{id}", id)
                .then()
                .statusCode(anyOf(is(200), is(204), is(404)));
    }
}
