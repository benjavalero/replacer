package es.bvalero.replacer.article;

import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A replacement in the database related to an article.
 */
@Entity
@Table(name = "replacement2",
        uniqueConstraints = @UniqueConstraint(columnNames = {"articleId", "type", "subtype", "position"}))
public class Replacement implements Serializable {

    private static final long serialVersionUID = -6766305982117992712L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private int articleId;

    @Column(nullable = false, length = 25)
    private String type;

    @Column(nullable = false, length = 30)
    private String subtype;

    @Column
    @ColumnDefault(value = "0")
    private int position;

    @Column(nullable = false)
    private LocalDate lastUpdate;

    @Column
    private String reviewer;

    public Replacement() {
        // Needed by JPA
    }

    Replacement(int articleId, String type, String subtype, int position) {
        this.articleId = articleId;
        this.type = type;
        this.subtype = subtype;
        this.position = position;
        this.lastUpdate = LocalDate.now();
        this.reviewer = null;
    }

    int getArticleId() {
        return articleId;
    }

    LocalDate getLastUpdate() {
        return lastUpdate;
    }

    Replacement withLastUpdate(LocalDate newLastUpdate) {
        Replacement newRep = new Replacement(articleId, type, subtype, position);
        newRep.id = id;
        newRep.lastUpdate = newLastUpdate;
        newRep.reviewer = reviewer;
        return newRep;
    }

    Replacement withReviewer(String newReviewer) {
        Replacement newRep = new Replacement(articleId, type, subtype, position);
        newRep.id = id;
        newRep.lastUpdate = lastUpdate;
        newRep.reviewer = newReviewer;
        return newRep;
    }

    boolean isToBeReviewed() {
        return this.reviewer == null;
    }

    boolean isSame(Replacement that) {
        return articleId == that.articleId &&
                position == that.position &&
                type.equals(that.type) &&
                subtype.equals(that.subtype);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Replacement that = (Replacement) o;
        return id == that.id &&
                articleId == that.articleId &&
                position == that.position &&
                type.equals(that.type) &&
                subtype.equals(that.subtype) &&
                lastUpdate.equals(that.lastUpdate) &&
                Objects.equals(reviewer, that.reviewer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, articleId, type, subtype, position, lastUpdate, reviewer);
    }


    @Override
    public String toString() {
        return "Replacement{" +
                "id=" + id +
                ", articleId=" + articleId +
                ", type='" + type + '\'' +
                ", subtype='" + subtype + '\'' +
                ", position=" + position +
                ", lastUpdate=" + lastUpdate +
                ", reviewer='" + reviewer + '\'' +
                '}';
    }

}

