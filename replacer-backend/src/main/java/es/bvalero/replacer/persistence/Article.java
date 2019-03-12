package es.bvalero.replacer.persistence;

import org.jetbrains.annotations.NonNls;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * An article in the database containing replacements.
 */
@Entity
@Table(name = "article")
public class Article implements Serializable {

    private static final long serialVersionUID = 2886241217232701474L;

    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @Column(name = "lastupdate", nullable = false)
    private LocalDate lastUpdate;

    public Article() {
        // Needed by JPA
    }

    private Article(int id, String title, LocalDate lastUpdate) {
        this.id = id;
        this.title = title;
        this.lastUpdate = lastUpdate;
    }

    public static Article.ArticleBuilder builder() {
        return new Article.ArticleBuilder();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Article article = (Article) obj;
        return id == article.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @NonNls
    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", lastUpdate=" + lastUpdate +
                '}';
    }

    public Article withTitle(String title) {
        if (this.title.equals(title)) return this;
        return new Article(id, title, lastUpdate);
    }

    public Article withLastUpdate(LocalDate lastUpdate) {
        if (this.lastUpdate.equals(lastUpdate)) return this;
        return new Article(id, title, lastUpdate);
    }

    public static class ArticleBuilder {
        private int id;
        private String title;
        private LocalDate lastUpdate;

        ArticleBuilder() {
            // Default value especially for tests
            lastUpdate = LocalDate.now();
        }

        public Article.ArticleBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public Article.ArticleBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Article.ArticleBuilder setLastUpdate(LocalDate lastUpdate) {
            this.lastUpdate = lastUpdate;
            return this;
        }

        public Article build() {
            return new Article(id, title, lastUpdate);
        }

    }

}
