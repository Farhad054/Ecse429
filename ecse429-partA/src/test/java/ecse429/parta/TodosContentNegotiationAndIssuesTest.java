package ecse429.parta;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class TodosContentNegotiationAndIssuesTest extends BaseTest {

    @Test
    void get_todos_accept_xml_returns_xml() {
        given().header("Accept", "application/xml")
                .when().get("/todos")
                .then().statusCode(200)
                .contentType(containsString("application/xml"))
                .body(containsString("<todos>"));
    }

    @Disabled("Spec expectation: JSON body + Content-Type: application/xml should be 415; server currently accepts it.")
    @Test
    void documented_expectation_mismatched_content_type_rejected() {
        given().header("Content-Type", "application/xml")
                .body("{\"title\":\"S2-mismatch\",\"doneStatus\":false}")
                .when().post("/todos")
                .then().statusCode(415);
    }

    @Test
    void actual_behavior_mismatched_content_type_is_accepted() {
        String id = given().header("Content-Type", "application/xml")
                .body("{\"title\":\"S2-mismatch\",\"doneStatus\":false}")
                .when().post("/todos")
                .then().statusCode(anyOf(is(200), is(201)))
                .contentType(ContentType.JSON)
                .extract().jsonPath().getString("id");
        deleteTodoIfExists(id);
    }
}
