package hexlet.code.Utils;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Paging {
    public static PagedList<Url> getPagedUrls(int page, int rowsPerPage) {
        return new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();
    }

    public static List<Integer> getPagesAmount(int lastPage) {
        return IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());
    }
}
