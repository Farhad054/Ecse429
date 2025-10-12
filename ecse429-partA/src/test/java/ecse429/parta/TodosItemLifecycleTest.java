package ecse429.parta;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class TodosItemLifecycleTest extends BaseTest {

    @Test
    void get_existing_id_200_and_404_for_missing() {
        String id = createTodo("scan paperwork", false, "");
        try {
            given().when().get("/todos/{id}", id).then().statusCode(200).body("todos[0].id", equalTo(id));
            given().when().get("/todos/999999").then().statusCode(anyOf(is(404), is(400)));
        } finally { deleteTodoIfExists(id); }
    }

    @Test
    void post_amend_existing_id_partial_ok() {
        String id = createTodo("scan paperwork", false, "");
        try {
            given().contentType(ContentType.JSON)
                    .body("{\"doneStatus\":true,\"description\":\"all paperwork scanned\"}")
                    .when().post("/todos/{id}", id)
                    .then().statusCode(anyOf(is(200), is(201)))
                    .body("doneStatus", equalTo("true"))
                    .body("description", equalTo("all paperwork scanned"));
        } finally { deleteTodoIfExists(id); }
    }

    @Test
    void post_amend_non_existing_404() {
        given().contentType(ContentType.JSON)
                .body("{\"doneStatus\":true}")
                .when().post("/todos/999999")
                .then().statusCode(404);
    }

    @Test
    void put_update_existing_requires_title_full_replace() {
        String id = createTodo("Wash Dog", false, "giving him a bath");
        try {
            given().contentType(ContentType.JSON)
                    .body("{\"title\":\"Wash Dog\",\"doneStatus\":false,\"description\":\"giving him a bath\"}")
                    .when().put("/todos/{id}", id)
                    .then().statusCode(200).body("title", equalTo("Wash Dog"));

            // your Session-2 finding: PUT with partial body should be 4xx
            given().contentType(ContentType.JSON)
                    .body("{\"doneStatus\":true}")
                    .when().put("/todos/{id}", id)
                    .then().statusCode(anyOf(is(400), is(422)));
        } finally { deleteTodoIfExists(id); }
    }

    @Test
    void put_non_existing_404() {
        given().contentType(ContentType.JSON)
                .body("{\"title\":\"Wash Dog\",\"doneStatus\":false,\"description\":\"giving him a bath\"}")
                .when().put("/todos/999999")
                .then().statusCode(404);
    }

    @Test
    void delete_once_200_204_then_404() {
        String id = createTodo("temp-delete", false, "");
        given().when().delete("/todos/{id}", id).then().statusCode(anyOf(is(200), is(204)));
        given().when().delete("/todos/{id}", id).then().statusCode(anyOf(is(404), is(400)));
    }

    @Test
    void head_item_200_and_404() {
        String id = createTodo("head-check", false, "");
        try {
            given().when().head("/todos/{id}", id).then().statusCode(200);
            given().when().head("/todos/999999").then().statusCode(anyOf(is(404), is(400)));
        } finally { deleteTodoIfExists(id); }
    }
}
