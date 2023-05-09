package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;

@Entity
public final class Url extends Model {
    @Id
    private long id;

    private final String name;

    @WhenCreated
    private  Instant createdAt;

    @OneToMany
    private List<UrlCheck> urlChecks;

    public Url(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<UrlCheck> getUrlChecks() {
        return urlChecks;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public UrlCheck getLastCheck() {
        if (!urlChecks.isEmpty()) {
            int lastCheckIndex = urlChecks.size() - 1;
            return urlChecks.get(lastCheckIndex);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }
}
