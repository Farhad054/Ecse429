package ecse429.parta;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class TodosCollectionTest extends BaseTest {

    @Test
    void get_all_todos_200() {
        given().when().get("/todos")
                .then().statusCode(200).body("$", notNullValue());
    }

    @Test
    void filter_doneStatus_and_title() {
        String id = createTodo("S2-C", false, "C_desc");
        try {
            given().when().get("/todos?doneStatus=false&title=S2-C")
                    .then().statusCode(200)
                    .body("todos.id", hasItem(id));
        } finally { deleteTodoIfExists(id); }
    }

    @Test
    void post_valid_creates_201_or_200() {
        String id = given().contentType(ContentType.JSON)
                .body("{\"title\":\"feed dog\",\"doneStatus\":false,\"description\":\"give him food\"}")
                .when().post("/todos")
                .then().statusCode(anyOf(is(200), is(201)))
                .extract().jsonPath().getString("id");
        deleteTodoIfExists(id);
    }

    @Test
    void post_missing_title_400() {
        given().contentType(ContentType.JSON)
                .body("{\"doneStatus\":false,\"description\":\"give him food\"}")
                .when().post("/todos")
                .then().statusCode(anyOf(is(400), is(422)))
                .body("errorMessages", hasItem(containsString("title")));
    }

    @Test
    void post_extra_field_400() {
        given().contentType(ContentType.JSON)
                .body("{\"title\":\"Feed my dog\",\"doneStatus\":false,\"description\":\"x\",\"monthCreated\":\"OCT\"}")
                .when().post("/todos")
                .then().statusCode(anyOf(is(400), is(422)))
                .body("errorMessages[0]", containsString("monthCreated"));
    }

    @Test
    void head_collection_200() {
        given().when().head("/todos").then().statusCode(200);
    }
}
