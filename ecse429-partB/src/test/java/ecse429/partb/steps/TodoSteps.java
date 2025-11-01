package ecse429.partb.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.*;
import static java.util.Collections.emptyList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class TodoSteps {

    private String savedId;
    private Response lastResponse;

    /* =========================
       Utilities
       ========================= */

    private static String todoJson(String title, String doneStatus, String description) {
        if (description == null) description = "";
        if (title == null) title = "";
        return String.format("{\"title\":\"%s\",\"doneStatus\":%s,\"description\":\"%s\"}",
                escape(title), doneStatus, escape(description));
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extractId(Response r) {
        if (r == null) return null;
        JsonPath jp = r.jsonPath();
        String id = jp.getString("id");
        if (id == null) id = jp.getString("todos[0].id");
        return id;
    }

    private static String getString(JsonPath jp, String... candidates) {
        for (String c : candidates) {
            try {
                String v = jp.getString(c);
                if (v != null) return v;
            } catch (Exception ignored) {}
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> extractTodos(JsonPath jp) {
        try {
            List<Map<String, Object>> list = jp.getList("todos");
            if (list != null) return list;
        } catch (Exception ignored) {}
        try {
            Map<String, Object> one = jp.getMap("$");
            if (one != null && one.containsKey("id")) {
                return Arrays.asList(one);
            }
        } catch (Exception ignored) {}
        return emptyList();
    }

    /* =========================
       Healthcheck & baseline
       ========================= */

    @Given("the API is running")
    public void the_api_is_running() {
        given().accept(ContentType.JSON)
                .when().get("/todos")
                .then().statusCode(anyOf(is(200), is(204)));
    }

    @Given("the todo server is running")
    public void the_server_is_running() {
        the_api_is_running();
    }

    @Given("a clean slate for this scenario")
    public void a_clean_slate_for_this_scenario() {
        Response r = given().accept(ContentType.JSON)
                .when().get("/todos")
                .then().statusCode(anyOf(is(200), is(204))).extract().response();

        JsonPath jp = r.jsonPath();
        for (Map<String, Object> t : extractTodos(jp)) {
            Object id = t.get("id");
            if (id != null) {
                try {
                    given().accept(ContentType.JSON)
                            .when().delete("/todos/{id}", id)
                            .then().statusCode(anyOf(is(200), is(204), is(404)));
                } catch (Exception ignored) {}
            }
        }
    }

    /* =========================
       Creation variants
       ========================= */

    @When("I create a todo titled {string} with doneStatus {string} and description {string}")
    public void createTodoFull(String title, String doneStatus, String description) {
        String body = todoJson(title, doneStatus, description);

        // If title is empty, the API should reject (400). Otherwise expect success (200/201).
        boolean expectReject = title == null || title.trim().isEmpty();

        if (expectReject) {
            lastResponse = given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post("/todos")
                    .then()
                    .statusCode(greaterThanOrEqualTo(400))
                    .extract().response();
        } else {
            lastResponse = given()
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post("/todos")
                    .then()
                    .statusCode(anyOf(is(200), is(201)))
                    .extract().response();
        }
    }

    @When("I create a todo titled {string} with doneStatus {string} and remember its id")
    public void createTodoAndRemember(String title, String doneStatus) {
        String body = todoJson(title, doneStatus, "");
        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/todos")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract().response();

        savedId = extractId(lastResponse);
        assertThat("POST did not return an id", savedId, not(emptyOrNullString()));
    }

    @When("I create a todo titled {string} with doneStatus {string}")
    public void createTodoNoRemember(String title, String doneStatus) {
        String body = todoJson(title, doneStatus, "");
        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/todos")
                .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract().response();
    }

    /* =========================
       Querying & assertions
       ========================= */

    @Then("GET that id returns title {string} and doneStatus {string}")
    public void getThatIdTitleDone(String expectedTitle, String expectedDone) {
        Response r = given()
                .accept(ContentType.JSON)
                .when()
                .get("/todos/{id}", savedId)
                .then()
                .statusCode(200)
                .extract().response();

        JsonPath jp = r.jsonPath();
        String actualTitle = getString(jp, "title", "todos[0].title");
        String actualDone  = getString(jp, "doneStatus", "todos[0].doneStatus");

        assertThat(actualTitle, equalTo(expectedTitle));
        assertThat(actualDone,  equalTo(expectedDone));
    }

    @Then("GET that id shows description {string}")
    public void get_that_id_shows_description(String expected) {
        Response r = given().accept(ContentType.JSON)
                .when().get("/todos/{id}", savedId)
                .then().statusCode(200).extract().response();

        JsonPath jp = r.jsonPath();
        String actual = getString(jp, "description", "todos[0].description");
        if (expected == null) expected = "";
        if (actual == null) actual = "";
        assertThat(actual, equalTo(expected));
    }

    @When("I GET path {string} with query {string}")
    public void i_get_path_with_query(String path, String query) {
        lastResponse = given()
                .accept(ContentType.JSON)
                .when()
                .get(path + "?" + query)
                .then()
                .statusCode(200)
                .extract().response();
    }

    @Then("every item in the response has doneStatus {string}")
    public void every_item_in_the_response_has_done_status(String expected) {
        JsonPath jp = lastResponse.jsonPath();
        for (Map<String, Object> t : extractTodos(jp)) {
            Object ds = t.get("doneStatus");
            assertThat(String.valueOf(ds), equalTo(expected));
        }
    }

    @Then("the response contains exactly {int} item titled {string}")
    public void the_response_contains_exactly_item_titled(Integer expectedCount, String expectedTitle) {
        JsonPath jp = lastResponse.jsonPath();
        List<Map<String, Object>> todos = extractTodos(jp);
        assertThat(todos.size(), equalTo(expectedCount));
        if (expectedCount > 0) {
            assertThat(String.valueOf(todos.get(0).get("title")), equalTo(expectedTitle));
        }
    }

    /* =========================
       Updating (POST/PUT to id)
       ========================= */

    @When("I POST to that id with JSON:")
    public void i_post_to_that_id_with_json(String docString) {
        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(docString)
                .when()
                .post("/todos/{id}", savedId)
                .then()
                .statusCode(anyOf(is(200), is(201), is(204)))
                .extract().response();
    }

    @When("I POST to id {string} with JSON:")
    public void i_post_to_id_with_json(String id, String docString) {
        // Scenario asserts the status later (e.g., 4xx). Do not assert here.
        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(docString)
                .when()
                .post("/todos/{id}", id)
                .then()
                .extract().response();
    }

    @When("I PUT to that id with JSON:")
    public void i_put_to_that_id_with_json(String docString) {
        boolean containsTitle = docString != null && docString.contains("\"title\"");
        if (containsTitle) {
            lastResponse = given()
                    .contentType(ContentType.JSON)
                    .body(docString)
                    .when()
                    .put("/todos/{id}", savedId)
                    .then()
                    .statusCode(anyOf(is(200), is(204)))
                    .extract().response();
        } else {
            // Scenario "Partial PUT without title is rejected" expects 4xx
            lastResponse = given()
                    .contentType(ContentType.JSON)
                    .body(docString)
                    .when()
                    .put("/todos/{id}", savedId)
                    .then()
                    .statusCode(greaterThanOrEqualTo(400))
                    .extract().response();
        }
    }

    @When("I PUT id {string} with JSON:")
    public void i_put_id_with_json(String id, String docString) {
        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(docString)
                .when()
                .put("/todos/{id}", id)
                .then()
                .statusCode(anyOf(is(200), is(204)))
                .extract().response();
    }

    /* =========================
       Delete / Toggle / Status checks
       ========================= */

    @When("I delete that todo")
    public void i_delete_that_todo() {
        lastResponse = given()
                .accept(ContentType.JSON)
                .when()
                .delete("/todos/{id}", savedId)
                .then()
                .statusCode(anyOf(is(200), is(204), is(404)))
                .extract().response();
    }

    @When("I DELETE that id")
    public void i_delete_that_id() {
        lastResponse = given()
                .accept(ContentType.JSON)
                .when()
                .delete("/todos/{id}", savedId)
                .then()
                .statusCode(anyOf(is(200), is(204), is(404)))
                .extract().response();
    }

    @When("I DELETE id {string}")
    public void i_delete_id(String id) {
        lastResponse = given()
                .accept(ContentType.JSON)
                .when()
                .delete("/todos/{id}", id)
                .then()
                .statusCode(anyOf(is(200), is(204), is(404)))
                .extract().response();
    }

    @Then("GET that id returns 404")
    public void get_that_id_returns_404() {
        given().accept(ContentType.JSON)
                .when().get("/todos/{id}", savedId)
                .then().statusCode(404);
    }

    @When("I toggle done on that todo to {string}")
    public void i_toggle_done_on_that_todo_to(String doneStatus) {
        String patch = String.format("{\"doneStatus\":%s}", doneStatus);
        lastResponse = given()
                .contentType(ContentType.JSON)
                .body(patch)
                .when()
                .post("/todos/{id}", savedId) // change to PUT if your API requires it
                .then()
                .statusCode(anyOf(is(200), is(204)))
                .extract().response();
    }

    @Then("GET that id shows doneStatus {string}")
    public void get_id_shows_doneStatus(String expectedDone) {
        Response r = given()
                .accept(ContentType.JSON)
                .when()
                .get("/todos/{id}", savedId)
                .then()
                .statusCode(200)
                .extract().response();

        JsonPath jp = r.jsonPath();
        String actualDone = getString(jp, "doneStatus", "todos[0].doneStatus");
        assertThat(actualDone, equalTo(expectedDone));
    }

    /* =========================
       Generic status helpers
       ========================= */

    @Then("the last response status is {int}")
    public void the_last_response_status_is(Integer expected) {
        assertThat(lastResponse.getStatusCode(), equalTo(expected));
    }

    @Then("the response status is {int}")
    public void the_response_status_is(Integer expected) {
        assertThat(lastResponse.getStatusCode(), equalTo(expected));
    }

    @Then("the response status is {int} or {int}")
    public void the_response_status_is_or(Integer a, Integer b) {
        int code = lastResponse.getStatusCode();
        assertThat(code, anyOf(equalTo(a), equalTo(b)));
    }

    @Then("the response status is 4xx")
    public void the_response_status_is_4xx() {
        int code = lastResponse.getStatusCode();
        assertThat(code / 100, equalTo(4));
    }

    @Then("the last response has a non-empty id")
    public void last_response_has_id() {
        String id = extractId(lastResponse);
        assertThat(id, not(isEmptyOrNullString()));
    }

    @Then("I remember the created id")
    public void i_remember_the_created_id() {
        savedId = extractId(lastResponse);
        assertThat("No id present in last response", savedId, not(emptyOrNullString()));
    }

    @Then("the todo titled {string} exists")
    public void the_todo_titled_exists(String title) {
        Response r = given().accept(ContentType.JSON)
                .when().get("/todos?title=" + title)
                .then().statusCode(200)
                .extract().response();

        JsonPath jp = r.jsonPath();
        String found = getString(jp, "todos[0].title");
        assertThat(found, equalTo(title));
    }
}
