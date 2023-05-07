package hexlet.code.Utils;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;

import java.util.List;

public final class Query {
    public static Url getUrlByName(String name) {
        Url url = new QUrl()
                .name.eq(name)
                .findOne();
        return url;
    }

    public static Url getUrlById(long id) {
        Url url = new QUrl()
                .id.eq(id)
                .findOne();
        return url;
    }

    public static UrlCheck getUrlCheckByUrl(Url url) {
        UrlCheck urlCheck = new QUrlCheck()
                .url.eq(url)
                .findOne();
        return urlCheck;
    }

    public static List<UrlCheck> getUrlChecks(Url url) {
        List<UrlCheck> urlChecks = new QUrlCheck()
                .url.equalTo(url)
                .orderBy().id.desc()
                .findList();
        return urlChecks;
    }
}
