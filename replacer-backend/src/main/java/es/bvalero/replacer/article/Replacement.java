package es.bvalero.replacer.article;

import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A replacement in the database related to an article.
 */
@Entity
@Immutable
@Table(name = "replacement")
public class Replacement implements Serializable {

    private static final long serialVersionUID = -6766305982117992712L;
    private static final int MIGRATED_POSITION = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "articleid", nullable = false)
    private int articleId;

    @Column(name = "type", nullable = false, length = 25)
    private String type;

    @Column(name = "subtype", nullable = false, length = 30)
    private String subtype;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "status", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private ReplacementStatus status;

    @Column(name = "lastupdate", nullable = false)
    private LocalDate lastUpdate;

    @Column(name = "reviewer")
    private String reviewer;

    public Replacement(int articleId, String type, String subtype, int position) {
        this.articleId = articleId;
        this.type = type;
        this.subtype = subtype;
        this.position = position;
        this.status = ReplacementStatus.TO_REVIEW;
        this.lastUpdate = LocalDate.now();
        this.reviewer = null;
    }

    int getArticleId() {
        return articleId;
    }

    ReplacementStatus getStatus() {
        return status;
    }

    public LocalDate getLastUpdate() {
        return lastUpdate;
    }

    Replacement withStatus(ReplacementStatus newStatus) {
        Replacement newRep = new Replacement(articleId, type, subtype, position);
        newRep.id = id;
        newRep.status = newStatus;
        newRep.lastUpdate = lastUpdate;
        newRep.reviewer = reviewer;
        return newRep;
    }

    public Replacement withLastUpdate(LocalDate newLastUpdate) {
        Replacement newRep = new Replacement(articleId, type, subtype, position);
        newRep.id = id;
        newRep.status = status;
        newRep.lastUpdate = newLastUpdate;
        newRep.reviewer = reviewer;
        return newRep;
    }

    Replacement withReviewer(String newReviewer) {
        Replacement newRep = new Replacement(articleId, type, subtype, position);
        newRep.id = id;
        newRep.status = status;
        newRep.lastUpdate = lastUpdate;
        newRep.reviewer = newReviewer;
        return newRep;
    }

    boolean isToBeReviewed() {
        return this.status == ReplacementStatus.TO_REVIEW;
    }

    boolean isReviewed() {
        return this.status == ReplacementStatus.REVIEWED;
    }

    boolean isFixed() {
        return this.status == ReplacementStatus.FIXED;
    }

    boolean isMigrated() {
        return this.position == MIGRATED_POSITION;
    }

    Replacement migrate() {
        Replacement newRep = new Replacement(articleId, type, subtype, MIGRATED_POSITION);
        newRep.id = id;
        newRep.status = status;
        newRep.lastUpdate = lastUpdate;
        newRep.reviewer = reviewer;
        return newRep;
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
                status == that.status &&
                lastUpdate.equals(that.lastUpdate) &&
                Objects.equals(reviewer, that.reviewer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, articleId, type, subtype, position, status, lastUpdate, reviewer);
    }


    @Override
    public String toString() {
        return "Replacement{" +
                "id=" + id +
                ", articleId=" + articleId +
                ", type='" + type + '\'' +
                ", subtype='" + subtype + '\'' +
                ", position=" + position +
                ", status=" + status +
                ", lastUpdate=" + lastUpdate +
                ", reviewer='" + reviewer + '\'' +
                '}';
    }

}

