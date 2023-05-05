package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
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

import static org.assertj.core.api.Assertions.assertThat;


class AppTest {

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
            HttpResponse<String> response = Unirest.get(baseUrl).asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Анализатор страниц");
        }
    }

    @Nested
    class UrlControllerTest {
        @Test
        void showListUrl() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("https://example.com");
        }

        @Test
        void testCreateUrl() {
            String inputUrl = "https://www.example1.com";

            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asString();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(inputUrl);
            assertThat(body).contains("Страница успешно добавлена");

            Url url = new QUrl()
                    .name.equalTo(inputUrl)
                    .findOne();

            assertThat(url).isNotNull();
            assertThat(url.getName()).isEqualTo(inputUrl);
        }

        @Test
        void testIncorrectUrl() {
            String inputUrl = "example.com";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asString();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/")
                    .asString();
            String body = response.getBody();

            Url url = new QUrl()
                    .name.equalTo(inputUrl)
                    .findOne();

            assertThat(url).isNull();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("Некорректный URL");
        }

        @Test
        void testExistUrl() {
            String inputUrl = "https://example.com";

            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asString();

            assertThat(responsePost.getStatus()).isEqualTo(302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/")
                    .asString();
            String content = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(content).contains("Страница уже добавлена");
        }

        @Test
        void testShowUrl() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/1")
                    .asString();
            String content = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(content).contains("https://example.com");
        }

        @Test
        void testCheckUrl() throws IOException {
            String filePath = "src/test/resources/test.html";
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            String testHtml = Files.readString(path);

            MockWebServer mockServer = new MockWebServer();
            MockResponse mockedResponse = new MockResponse().setBody(testHtml);
            mockServer.enqueue(mockedResponse);
            mockServer.start();

            String inputUrl = mockServer.url("/").toString().replaceAll("/$", "");

            HttpResponse responseCreateUrl = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputUrl)
                    .asEmpty();
            assertThat(responseCreateUrl.getStatus()).isEqualTo(302);
            assertThat(responseCreateUrl.getHeaders().getFirst("Location")).isEqualTo("/urls");

            Url url = new QUrl()
                    .name.eq(inputUrl)
                    .findOne();

            HttpResponse<String> responseCheck = Unirest
                    .post(baseUrl + "/urls/" + url.getId() + "/checks")
                    .asString();
            assertThat(responseCheck.getStatus()).isEqualTo(302);
            assertThat(responseCheck.getHeaders().getFirst("Location")).isEqualTo("/urls/" + url.getId());


            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + url.getId())
                    .asString();
            String content = response.getBody();
            assertThat(content).contains("h1Test", "titleTest", "descriptionTest", "Страница успешно проверена");

            mockServer.shutdown();
        }
    }
}
