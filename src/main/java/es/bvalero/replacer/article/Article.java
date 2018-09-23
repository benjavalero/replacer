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

    public Article(Integer id, String title) {
        this.id = id;
        this.title = title;
        this.additionDate = new Timestamp(System.currentTimeMillis());
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Timestamp getAdditionDate() {
        return additionDate;
    }

    public void setAdditionDate(Timestamp additionDate) {
        this.additionDate = additionDate;
    }

    public Timestamp getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(Timestamp reviewDate) {
        this.reviewDate = reviewDate;
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

}
