package hexlet.code.utils;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.PagedList;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Paging {
    public static PagedList<Url> getPagedUrls(int page, int rowsPerPage) {
        return new QUrl()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();
    }
    public static List<Integer> getPageNumbers(PagedList<Url> pagedUrls) {
        int lastPage = pagedUrls.getTotalPageCount() + 1;
        return IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());
    }
}
