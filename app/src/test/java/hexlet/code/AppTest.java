package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;

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

import static hexlet.code.Utils.Query.getUrlByName;
import static hexlet.code.Utils.Query.getUrlCheckByUrl;
import static hexlet.code.Utils.Response.getResponse;
import static hexlet.code.Utils.Response.postResponse;
import static hexlet.code.controllers.UrlController.ALREADY_ADDED_MSG;
import static hexlet.code.controllers.UrlController.INVALID_ADDRESS_MSG;
import static hexlet.code.controllers.UrlController.SUCCESSFULLY_ADDED_MSG;
import static hexlet.code.controllers.UrlController.SUCCESSFULLY_VERIFIED_MSG;
import static hexlet.code.controllers.UrlController.UNAVAILABLE_MSG;
import static org.assertj.core.api.Assertions.assertThat;

class AppTest {
    private static final String TEST_FILE_PATH = "src/test/resources/test.html";
    private static final String EXIST_URL_NAME = "https://example.com";
    private static final String INCORRECT_URL_NAME = "example.com";
    private static final String NEW_URL_NAME = "https://example1.com";
    private static final String NOT_AVAILABLE_URL_NAME = "https://example";
    private static Javalin app;
    private static String baseUrl;
    private static Database database;

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
            HttpResponse<String> response = getResponse(baseUrl);
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Анализатор страниц");
        }
    }

    @Nested
    class UrlControllerTest {
        @Test
        void showListUrl() {
            HttpResponse<String> response = getResponse(baseUrl + "/urls");
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains(EXIST_URL_NAME);
        }

        @Test
        void testCreateUrl() {
            HttpResponse<String> responsePost = postResponse(baseUrl + "/urls",
                    "url", NEW_URL_NAME);
            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> responseGet = getResponse(baseUrl + "/urls");
            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(responseGet.getBody()).contains(NEW_URL_NAME, SUCCESSFULLY_ADDED_MSG);

            Url url = getUrlByName(NEW_URL_NAME);
            assertThat(url).isNotNull();
            assertThat(url.getName()).isEqualTo(NEW_URL_NAME);
        }

        @Test
        void testIncorrectUrl() {
            HttpResponse<String> responsePost = postResponse(baseUrl + "/urls",
                    "url", INCORRECT_URL_NAME);
            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

            HttpResponse<String> responseGet = getResponse(baseUrl + "/");
            Url url = getUrlByName(INCORRECT_URL_NAME);
            assertThat(url).isNull();
            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(responseGet.getBody()).contains(INVALID_ADDRESS_MSG);
        }

        @Test
        void testExistUrl() {
            HttpResponse<String> responsePost = postResponse(baseUrl + "/urls",
                    "url", EXIST_URL_NAME);
            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

            HttpResponse<String> responseGet = getResponse(baseUrl + "/");
            assertThat(responseGet.getStatus()).isEqualTo(200);
            assertThat(responseGet.getBody()).contains(ALREADY_ADDED_MSG);
        }

        @Test
        void testShowUrl() {
            HttpResponse<String> response = getResponse(baseUrl + "/urls/1");
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains(EXIST_URL_NAME);
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

            HttpResponse<String> responsePost = postResponse(baseUrl + "/urls",
                    "url", urlName);
            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            Url url = getUrlByName(urlName);
            HttpResponse<String> respPost = postResponse(baseUrl + "/urls/" + url.getId() + "/checks");
            assertThat(respPost.getStatus()).isEqualTo(302);
            assertThat(respPost.getHeaders().getFirst("Location")).isEqualTo("/urls/" + url.getId());

            HttpResponse<String> responseGet = getResponse(baseUrl + "/urls/" + url.getId());
            assertThat(responseGet.getBody()).contains(SUCCESSFULLY_VERIFIED_MSG);
            assertThat(responseGet.getBody()).contains("h1Test", "titleTest", "descriptionTest");

            UrlCheck urlCheck = getUrlCheckByUrl(url);
            assertThat(urlCheck).isNotNull();

            mockServer.shutdown();
        }

        @Test
        void testCheckUrlNotAvailable() {
            Url url = new Url(NOT_AVAILABLE_URL_NAME);
            url.save();

            HttpResponse<String> responsePost = postResponse(baseUrl + "/urls/" + url.getId() + "/checks");
            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls/" + url.getId());

            HttpResponse<String> responseGet = getResponse(baseUrl + "/urls/" + url.getId());
            assertThat(responseGet.getBody()).contains(UNAVAILABLE_MSG);

            HttpResponse<String> respGet = getResponse(baseUrl + "/urls");
            assertThat(respGet.getBody()).contains("Страница недоступна/Некорректный адрес");
        }
    }
}
