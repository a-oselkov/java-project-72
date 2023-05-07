package hexlet.code.controllers;

import hexlet.code.Utils.Query;
import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static hexlet.code.Utils.Paging.getPageNumbers;
import static hexlet.code.Utils.Paging.getPagedUrls;
import static hexlet.code.Utils.Parser.parse;
import static hexlet.code.Utils.Query.getUrlChecks;

public final class UrlController {
    private static final int ROWS_PER_PAGE = 10;

    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        PagedList<Url> pagedUrls = getPagedUrls(page, ROWS_PER_PAGE);

        List<Url> urls = pagedUrls.getList();
        List<Integer> pages = getPageNumbers(pagedUrls);
        int currentPage = pagedUrls.getPageIndex() + 1;

        ctx.attribute("urls", urls);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/index.html");
    };

    public static Handler createUrl = ctx -> {
        try {
            URL urlNameFromForm = new URL(ctx.formParam("url"));
            String normalizedUrl = urlNameFromForm.getProtocol() + "://" + urlNameFromForm.getAuthority();

            Url existUrl = Query.getUrlByName(normalizedUrl);

            if (existUrl != null) {
                ctx.sessionAttribute("flash", "Страница уже добавлена");
                ctx.sessionAttribute("flash-type", "info");
                ctx.redirect("/");
                return;
            }
            Url url = new Url(normalizedUrl);
            url.save();
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
        Url url = Query.getUrlById(id);

        if (url == null) {
            throw new NotFoundResponse();
        }
        List<UrlCheck> urlChecks = getUrlChecks(url);
        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static Handler checkUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
        Url url = Query.getUrlById(id);
        if (url == null) {
            throw new NotFoundResponse();
        }
        try {
            HttpResponse<String> response = Unirest.get(url.getName()).asString();
            UrlCheck urlCheck = parse(url, response);
            urlCheck.save();
            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
        } catch (UnirestException e) {
            UrlCheck urlCheck = new UrlCheck(url, 0, "", "", "");
            urlCheck.save();
            ctx.sessionAttribute("flash", "Страница недоступна");
            ctx.sessionAttribute("flash-type", "danger");
        }
        ctx.redirect("/urls/" + id);
    };
}



