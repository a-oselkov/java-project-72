package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UrlController {
    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;

        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/index.html");
    };

    public static Handler createUrl = ctx -> {

        try {
            URL urlFromForm = new URL(ctx.formParam("url"));
            String normalizedUrl = urlFromForm.getProtocol() + "://" + urlFromForm.getAuthority();

            boolean urlExist = new QUrl()
                    .name.eq(normalizedUrl)
                    .exists();

            if (urlExist) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flash-type", "danger");
                ctx.render("index.html");
                return;
            }

            Url url = new Url(normalizedUrl);
            url.save();

        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.render("index.html");
            return;
        }

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
                .id.eq(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }
        List<UrlCheck> urlChecks = new QUrlCheck()
                .url.equalTo(url)
                .orderBy().id.desc()
                .findList();

        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static Handler checkUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
                .id.eq(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse();
        }

        String checkUrl = url.getName();
        try {
            HttpResponse<String> response = Unirest
                    .get(checkUrl)
                    .asString();

            String html = response.getBody();

            //Document doc = Jsoup.connect(checkUrl).get();

            Document doc = Jsoup.parse(html, "UTF-8");

            String title = doc.title() == null ? "" : doc.title();
            String h1 = doc.selectFirst("h1") == null ? "" : doc.selectFirst("h1").text();
            String description = doc.selectFirst("meta[name=description]") == null
                    ? "" : doc.selectFirst("meta[name=description]").attr("content");
            int statusCode = response.getStatus();

            UrlCheck urlCheck = new UrlCheck(title, h1, description, statusCode, url);
            urlCheck.save();

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
            ctx.redirect("/urls/" + id);
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Страница недоступна");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/urls/" + id);
        }
    };
}
