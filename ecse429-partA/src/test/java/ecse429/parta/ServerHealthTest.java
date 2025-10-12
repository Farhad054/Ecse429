package ecse429.parta;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
class ServerHealthTest extends BaseTest {
    @Test void api_is_up() { given().when().get("/todos").then().statusCode(200); }
}
