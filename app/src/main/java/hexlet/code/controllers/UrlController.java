package hexlet.code.controllers;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.utils.Paging;
import hexlet.code.utils.Parser;
import hexlet.code.utils.Querys;
import hexlet.code.utils.Responses;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.UnirestException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class UrlController {
    public static final String ALREADY_ADDED_MSG = "Страница уже добавлена";
    public static final String INVALID_ADDRESS_MSG = "Некорректный адрес";
    public static final String SUCCESSFULLY_ADDED_MSG = "Страница успешно добавлена";
    public static final String SUCCESSFULLY_VERIFIED_MSG = "Страница успешно проверена";
    public static final String UNAVAILABLE_MSG = "Страница недоступна";
    private static final int ROWS_PER_PAGE = 10;

    public static Handler listUrls = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        PagedList<Url> pagedUrls = Paging.getPagedUrls(page, ROWS_PER_PAGE);

        List<Url> urls = pagedUrls.getList();
        List<Integer> pages = Paging.getPageNumbers(pagedUrls);
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

            Url existUrl = Querys.getUrlByName(normalizedUrl);

            if (existUrl != null) {
                ctx.sessionAttribute("flash", ALREADY_ADDED_MSG);
                ctx.sessionAttribute("flash-type", "info");
                ctx.redirect("/");
                return;
            }
            Url url = new Url(normalizedUrl);
            url.save();
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", INVALID_ADDRESS_MSG);
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }
        ctx.sessionAttribute("flash", SUCCESSFULLY_ADDED_MSG);
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static Handler showUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
        Url url = Querys.getUrlById(id);
        if (url == null) {
            throw new NotFoundResponse();
        }
        List<UrlCheck> urlChecks = Querys.getUrlChecks(url);

        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static Handler checkUrl = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);
        Url url = Querys.getUrlById(id);
        if (url == null) {
            throw new NotFoundResponse();
        }
        UrlCheck urlCheck;
        try {
            HttpResponse<String> response = Responses.responseToGet(url.getName());
            urlCheck = Parser.parse(url, response);
            ctx.sessionAttribute("flash", SUCCESSFULLY_VERIFIED_MSG);
            ctx.sessionAttribute("flash-type", "success");
        } catch (UnirestException e) {
            urlCheck = new UrlCheck(url);
            ctx.sessionAttribute("flash", UNAVAILABLE_MSG);
            ctx.sessionAttribute("flash-type", "danger");
        }
        urlCheck.save();
        ctx.redirect("/urls/" + id);
    };
}



