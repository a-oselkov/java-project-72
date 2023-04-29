package hexlet.code;

import hexlet.code.controllers.RootController;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class App {
    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "8000");
        return Integer.valueOf(port);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static boolean isProduction() {
        return getMode().equals("production");
    }

    // Javalin поддерживает работу с шаблонизатором thymeleaf
    private static TemplateEngine getTemplateEngine() {
        // Создаём инстанс движка шаблонизатора
        TemplateEngine templateEngine = new TemplateEngine();
        // Добавляем к нему диалекты
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());
        // Настраиваем преобразователь шаблонов, так, чтобы обрабатывались
        // шаблоны в директории /templates/
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setCharacterEncoding("UTF-8");
        // Добавляем преобразователь шаблонов к движку шаблонизатора
        templateEngine.addTemplateResolver(templateResolver);

        return templateEngine;
    }

    // Метод добавляет маршруты в переданное приложение
    private static void addRoutes(Javalin app) {
        // Для GET-запроса на маршрут / будет выполняться
        // обработчик welcome в контроллере RootController
        app.get("/", RootController.welcome);

        // При помощи методов routes() и path() маршруты можно группировать

//        app.routes(() -> {
//            path("articles", () -> {
//                get(ArticleController.listArticles);
//                post(ArticleController.createArticle);
//
//                get("new", ArticleController.newArticle);
//
//                path("{id}", () -> {
//                    get(ArticleController.showArticle);
//                    path("edit", () -> {
//                        get(ArticleController.editArticle);
//                        post(ArticleController.updateArticle);
//                    });
//                    path("delete", () -> {
//                        get(ArticleController.deleteArticle);
//                        post(ArticleController.destroyArticle);
//                    });
//                });
//            });
//        });
    }

    public static Javalin getApp() {

        // Создаём приложение
        Javalin app = Javalin.create(config -> {
            // Включаем логгирование
            if (!isProduction()) {
                config.enableDevLogging();
            }
            config.enableWebjars();
            // Подключаем настроенный шаблонизатор к фреймворку

            JavalinThymeleaf.configure(getTemplateEngine());
        });

        // Добавляем маршруты в приложение
        addRoutes(app);

        // Обработчик before запускается перед каждым запросом
        // Устанавливаем атрибут ctx для запросов
        app.before(ctx -> ctx.attribute("ctx", ctx));

        return app;
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }
}
