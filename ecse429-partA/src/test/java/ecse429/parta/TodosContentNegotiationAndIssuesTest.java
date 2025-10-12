package ecse429.parta;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * Content negotiation and spec-vs-actual issues for /todos.
 */
class TodosContentNegotiationAndIssuesTest extends BaseTest {

    @Test
    void get_todos_accept_xml_returns_xml() {
        given()
                .header("Accept", "application/xml")
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .contentType(containsString("application/xml"))
                .body(containsString("<todos>"));
    }

    @Disabled("Documented expectation: JSON body + Content-Type: application/xml should be 415; server currently accepts it.")
    @Test
    void documented_expectation_mismatched_content_type_rejected() {
        String jsonBody = "{\"title\":\"S2-mismatch\",\"doneStatus\":false}";
        given()
                .header("Content-Type", "application/xml")
                .body(jsonBody) // intentionally JSON with XML Content-Type
                .when()
                .post("/todos")
                .then()
                .statusCode(415);
    }

    @Test
    void actual_behavior_mismatched_content_type_is_accepted() {
        String jsonBody = "{\"title\":\"S2-mismatch\",\"doneStatus\":false}";
        String id =
                given()
                        .header("Content-Type", "application/xml")
                        .body(jsonBody) // intentionally JSON with XML Content-Type
                        .when()
                        .post("/todos")
                        .then()
                        .statusCode(anyOf(is(200), is(201)))
                        .contentType(ContentType.JSON)
                        .extract().jsonPath().getString("id");
        deleteTodoIfExists(id);
    }

    @Test
    void malformed_json_returns_4xx() {
        // deliberately malformed: boolean token truncated and missing closing brace
        String badJson = "{\"title\":\"bad\",\"doneStatus\": tru";
        given()
                .contentType(ContentType.JSON)
                .body(badJson)
                .when()
                .post("/todos")
                .then()
                .statusCode(anyOf(is(400), is(415), is(422)));
    }

    @Test
    void malformed_xml_returns_4xx() {
        // deliberately malformed XML (unclosed tags)
        String xml = "<todo><title>bad</title><doneStatus>true";
        given()
                .header("Content-Type", "application/xml")
                .body(xml)
                .when()
                .post("/todos")
                .then()
                .statusCode(anyOf(is(400), is(415), is(422)));
    }

    @Test
    void get_todo_by_id_accept_xml_returns_xml() {
        String id = createTodo("xml-one", false, "");
        try {
            given()
                    .header("Accept", "application/xml")
                    .when()
                    .get("/todos/{id}", id)
                    .then()
                    .statusCode(200)
                    .contentType(containsString("application/xml"))
                    .body(containsString("<todo>"))
                    .body(containsString("<id>" + id + "</id>"));
        } finally {
            deleteTodoIfExists(id);
        }
    }
}
