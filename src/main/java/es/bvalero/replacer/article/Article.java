package es.bvalero.replacer.article;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * An article in the database containing potential errors.
 */
@Entity
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

    // LinkedList is better to run iterators and remove items from it
    @OneToMany(mappedBy = "article", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<PotentialError> potentialErrors = new LinkedList<>();

    public Article() {
    }

    public Article(Integer id, String title) {
        this.id = id;
        this.title = title;
        this.additionDate = new Timestamp(System.currentTimeMillis());
    }

    public Integer getId() {
        return id;
    }

    void setId(Integer id) {
        this.id = id;
    }

    String getTitle() {
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

    public List<PotentialError> getPotentialErrors() {
        return potentialErrors;
    }

    @SuppressWarnings("unused")
    void setPotentialErrors(List<PotentialError> potentialErrors) {
        this.potentialErrors = potentialErrors;
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

    public void addPotentialError(PotentialError potentialError) {
        this.potentialErrors.add(potentialError);
        potentialError.setArticle(this);
    }

}
