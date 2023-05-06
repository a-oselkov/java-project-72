package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import kong.unirest.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Parser {
    public static UrlCheck parse(Url url, HttpResponse<String> response) {
//            Connection.Response response = Jsoup.connect(url.getName()).followRedirects(true).execute();
//            Document doc = Jsoup.connect(url.getName()).get();
//            int statusCode = response.statusCode();
        Document doc = Jsoup.parse(response.getBody());

        int statusCode = response.getStatus();
        String title = doc.title();
        String h1 = doc.selectFirst("h1") == null ? "" : doc.selectFirst("h1").text();
        String description = doc.selectFirst("meta[name=description]") == null
                ? "" : doc.selectFirst("meta[name=description]").attr("content");

        return new UrlCheck(url, statusCode, title, h1, description);
    }
}
