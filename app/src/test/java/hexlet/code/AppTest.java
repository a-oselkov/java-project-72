package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static hexlet.code.Utils.Query.findUrlByName;
import static hexlet.code.Utils.Query.findUrlCheckByUrl;
import static hexlet.code.Utils.Response.getResponse;
import static hexlet.code.Utils.Response.postResponse;
import static org.assertj.core.api.Assertions.assertThat;

class AppTest {
    private static Javalin app;
    private static String baseUrl;
    private static Database database;
    private static final String TEST_FILE_PATH = "src/test/resources/test.html";
    private static final String EXIST_URL_NAME = "https://example.com";
    private static final String INCORRECT_URL_NAME = "example.com";
    private static final String NEW_URL_NAME = "https://example1.com";
    private static final String NOT_AVAILABLE_URL_NAME = "https://example";

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/clearTables.sql");
        database.script().run("/seed.sql");
    }

    @Nested
    class RootControllerTest {
        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Анализатор страниц");
        }
    }

    @Nested
    class UrlControllerTest {
        @Test
        void showListUrl() {
            HttpResponse<String> response = getResponse(baseUrl + "/urls");
            String content = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(content).contains(EXIST_URL_NAME);
        }

        @Test
        void testCreateUrl() {
            HttpResponse<String> responsePost = postResponse(baseUrl + "/urls",
                    "url", NEW_URL_NAME);
            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = getResponse(baseUrl + "/urls");
            String content = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(content).contains(NEW_URL_NAME);
            assertThat(content).contains("Страница успешно добавлена");
            assertThat(content).contains("Проверка еще не проводилась");

            Url url = findUrlByName(NEW_URL_NAME);
            assertThat(url).isNotNull();
            assertThat(url.getName()).isEqualTo(NEW_URL_NAME);
        }

        @Test
        void testIncorrectUrl() {
            HttpResponse<String> responsePost = postResponse(baseUrl + "/urls",
                    "url", INCORRECT_URL_NAME);
            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

            HttpResponse<String> response = getResponse(baseUrl + "/");
            String body = response.getBody();

            Url url = findUrlByName(INCORRECT_URL_NAME);
            assertThat(url).isNull();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("Некорректный URL");
        }

        @Test
        void testExistUrl() {
            HttpResponse<String> responsePost = postResponse(baseUrl + "/urls",
                    "url", EXIST_URL_NAME);
            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

            HttpResponse<String> response = getResponse(baseUrl + "/");
            String content = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(content).contains("Страница уже добавлена");
        }

        @Test
        void testShowUrl() {
            HttpResponse<String> response = getResponse(baseUrl + "/urls/1");
            String content = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(content).contains(EXIST_URL_NAME);
        }

        @Test
        void testCheckUrl() throws IOException {
            Path path = Paths.get(TEST_FILE_PATH).toAbsolutePath().normalize();
            String testHtml = Files.readString(path);

            MockWebServer mockServer = new MockWebServer();
            MockResponse mockedResponse = new MockResponse().setBody(testHtml);
            mockServer.enqueue(mockedResponse);
            mockServer.start();

            String urlName = mockServer.url("/").toString().replaceAll("/$", "");

            HttpResponse<String> responseCreateUrl = postResponse(baseUrl + "/urls",
                    "url", urlName);
            assertThat(responseCreateUrl.getStatus()).isEqualTo(302);
            assertThat(responseCreateUrl.getHeaders().getFirst("Location")).isEqualTo("/urls");

            Url url = findUrlByName(urlName);
            HttpResponse<String> responseCheck = postResponse(baseUrl + "/urls/" + url.getId() + "/checks");

            assertThat(responseCheck.getStatus()).isEqualTo(302);
            assertThat(responseCheck.getHeaders().getFirst("Location")).isEqualTo("/urls/" + url.getId());

            HttpResponse<String> response = getResponse(baseUrl + "/urls/" + url.getId());
            String content = response.getBody();
            assertThat(content).contains("h1Test", "titleTest", "descriptionTest", "Страница успешно проверена");

            UrlCheck urlCheck = findUrlCheckByUrl(url);
            assertThat(urlCheck).isNotNull();

            mockServer.shutdown();
        }

        @Test
        void testCheckUrlNotAvailable() {
            Url url = new Url(NOT_AVAILABLE_URL_NAME);
            url.save();

            HttpResponse<String> responseCheck = postResponse(baseUrl + "/urls/" + url.getId() + "/checks");

            assertThat(responseCheck.getHeaders().getFirst("Location")).isEqualTo("/urls/" + url.getId());

            HttpResponse<String> response = getResponse(baseUrl + "/urls/" + url.getId());
            String content = response.getBody();
            assertThat(responseCheck.getStatus()).isEqualTo(302);
            assertThat(content).contains("Страница недоступна");

            response = getResponse(baseUrl + "/urls");
            content = response.getBody();
            assertThat(content).contains("Страница недоступна/Некорректный адрес");
        }
    }
}
