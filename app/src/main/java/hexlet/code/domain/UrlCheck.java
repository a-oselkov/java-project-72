package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Entity
public final class UrlCheck extends Model {
    @Id
    private long id;

    @WhenCreated
    private Instant createdAt;

    private int statusCode;

    private String title;

    private String h1;

    @Lob
    private String description;

    @ManyToOne
    private final Url url;

    public UrlCheck(Url url, int statusCode, String title, String h1, String description) {
        this.title = title;
        this.h1 = h1;
        this.description = description;
        this.statusCode = statusCode;
        this.url = url;
    }

    public UrlCheck(Url url) {
        this(url, 0, "", "", "");
    }

    public long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getTitle() {
        return title;
    }

    public String getH1() {
        return h1;
    }

    public String getDescription() {
        return description;
    }

    public Url getUrl() {
        return url;
    }
}

