package hexlet.code.Utils;

import hexlet.code.domain.Url;
import hexlet.code.domain.UrlCheck;
import hexlet.code.domain.query.QUrl;
import hexlet.code.domain.query.QUrlCheck;

import java.util.List;

public final class Query {
    public static Url findUrlByName(String name) {
        return new QUrl()
                .name.eq(name)
                .findOne();
    }

    public static Url findUrlById(long id) {
        return new QUrl()
                .id.eq(id)
                .findOne();
    }

    public static UrlCheck findUrlCheckByUrl(Url url) {
        return new QUrlCheck()
                .url.eq(url)
                .findOne();
    }

    public static List<UrlCheck> findUrlChecks(Url url) {
        return new QUrlCheck()
                .url.equalTo(url)
                .orderBy().id.desc()
                .findList();
    }
}
