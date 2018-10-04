package es.bvalero.replacer.persistence;

import org.hibernate.annotations.Immutable;
import org.jetbrains.annotations.NonNls;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * An article in the database containing potential errors.
 */
@Entity
@Immutable
@Table(name = "article")
public class Article implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "dtadd", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime additionDate;

    @Column(name = "dtreview", columnDefinition = "TIMESTAMP")
    private LocalDateTime reviewDate;

    // Removed relationship to improve performance on bulk indexing
    // @OneToMany(mappedBy = "article", cascade = {CascadeType.ALL}, orphanRemoval = true)
    // private List<Replacement> replacements = new ArrayList<>();

    public Article() {
        // Needed by JPA
    }

    private Article(int id, String title, LocalDateTime additionDate, LocalDateTime reviewDate) {
        this.id = id;
        this.title = title;
        this.additionDate = additionDate;
        this.reviewDate = reviewDate;
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

    public LocalDateTime getAdditionDate() {
        return additionDate;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
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
                ", additionDate=" + additionDate +
                ", reviewDate=" + reviewDate +
                '}';
    }

    public Article withTitle(String title) {
        if (this.title.equals(title)) return this;
        return new Article(id, title, additionDate, reviewDate);
    }

    public Article withAdditionDate(LocalDateTime additionDate) {
        if (this.additionDate.equals(additionDate)) return this;
        return new Article(id, title, additionDate, reviewDate);
    }

    public Article withReviewDate(LocalDateTime reviewDate) {
        if (this.reviewDate == null ? reviewDate == null : this.reviewDate.equals(reviewDate)) return this;
        return new Article(id, title, additionDate, reviewDate);
    }

    public static class ArticleBuilder {
        private int id;
        private String title;
        private LocalDateTime additionDate;
        private LocalDateTime reviewDate;

        ArticleBuilder() {
            additionDate = LocalDateTime.now();
        }

        public Article.ArticleBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public Article.ArticleBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Article.ArticleBuilder setAdditionDate(LocalDateTime additionDate) {
            this.additionDate = additionDate;
            return this;
        }

        public Article.ArticleBuilder setReviewDate(LocalDateTime reviewDate) {
            this.reviewDate = reviewDate;
            return this;
        }

        public Article build() {
            return new Article(id, title, additionDate, reviewDate);
        }

    }

}
