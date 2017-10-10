package es.bvalero.replacer.article;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "article")
public class Article implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false, length = 190)
    private String title;

    @Column(name = "dtadd", nullable = false, columnDefinition = "TIMESTAMP")
    private Timestamp additionDate;

    @Column(name = "dtreview", columnDefinition = "TIMESTAMP")
    private Timestamp reviewDate;

    @OneToMany(mappedBy = "article", cascade = {CascadeType.ALL})
    private List<PotentialError> potentialErrors = new ArrayList<>();

    public Article() {
    }

    public Article(Integer id, String title) {
        this.id = id;
        this.title = title;
        this.additionDate = new Timestamp(new Date().getTime());
        this.potentialErrors = new ArrayList<>();
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

    void setTitle(String title) {
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

    void setPotentialErrors(List<PotentialError> potentialErrors) {
        this.potentialErrors = potentialErrors;
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
