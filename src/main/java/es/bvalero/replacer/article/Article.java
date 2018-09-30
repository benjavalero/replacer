package es.bvalero.replacer.article;

import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
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
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "dtadd", nullable = false, columnDefinition = "TIMESTAMP")
    private Timestamp additionDate;

    @Column(name = "dtreview", columnDefinition = "TIMESTAMP")
    private Timestamp reviewDate;

    // Removed relationship to improve performance on bulk indexing
    // @OneToMany(mappedBy = "article", cascade = {CascadeType.ALL}, orphanRemoval = true)
    // private List<PotentialError> potentialErrors = new ArrayList<>();

    public Article() {
        // Needed by JPA
    }

    private Article(Integer id, String title, Timestamp additionDate, Timestamp reviewDate) {
        this.id = id;
        this.title = title;
        this.additionDate = additionDate;
        this.reviewDate = reviewDate;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Timestamp getAdditionDate() {
        return additionDate;
    }

    public Timestamp getReviewDate() {
        return reviewDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Article article = (Article) o;
        return Objects.equals(id, article.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", additionDate=" + additionDate +
                ", reviewDate=" + reviewDate +
                '}';
    }

    public static class ArticleBuilder {
        private Integer id;
        private String title;
        private Timestamp additionDate;
        private Timestamp reviewDate;

        public ArticleBuilder() {
            this.additionDate = new Timestamp(System.currentTimeMillis());
        }

        public ArticleBuilder(Article article) {
            this.id = article.id;
            this.title = article.title;
            this.additionDate = article.additionDate;
            this.reviewDate = article.reviewDate;
        }

        public ArticleBuilder setId(Integer id) {
            this.id = id;
            return this;
        }

        public ArticleBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public ArticleBuilder setAdditionDate(Timestamp additionDate) {
            this.additionDate = additionDate;
            return this;
        }

        public ArticleBuilder setReviewDate(Timestamp reviewDate) {
            this.reviewDate = reviewDate;
            return this;
        }

        public Article createArticle() {
            return new Article(id, title, additionDate, reviewDate);
        }

    }

}
