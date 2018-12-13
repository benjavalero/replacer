package es.bvalero.replacer.persistence;

import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A replacement in the database related to an article.
 * TODO : Rename DB table
 * TODO : Add the unique constraint to DB and redo the indexes
 */
@Entity
@Immutable
@Table(name = "potentialerror", uniqueConstraints = @UniqueConstraint(columnNames = {"articleid", "type", "text"}))
public class Replacement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Removed relationship to improve performance on bulk indexing
    // @ManyToOne(fetch = FetchType.EAGER)
    // @JoinColumn(name = "articleid")
    @Column(name = "articleid", nullable = false)
    private int articleId;

    @Column(name = "type", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private ReplacementType type;

    @Column(name = "text", nullable = false, length = 30)
    private String text;

    @SuppressWarnings("unused")
    public Replacement() {
        // Needed by JPA
    }

    private Replacement(int articleId, ReplacementType type, String text) {
        this.articleId = articleId;
        this.type = type;
        this.text = text;
    }

    public static Replacement.ReplacementBuilder builder() {
        return new Replacement.ReplacementBuilder();
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Replacement that = (Replacement) o;
        return articleId == that.articleId &&
                type == that.type &&
                text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId, type, text);
    }

    @Override
    public String toString() {
        return "Replacement{" +
                "articleId=" + articleId +
                ", type=" + type +
                ", text='" + text + '\'' +
                '}';
    }

    public static class ReplacementBuilder {
        private int articleId;
        private ReplacementType type;
        private String text;

        public Replacement.ReplacementBuilder setArticleId(int articleId) {
            this.articleId = articleId;
            return this;
        }

        public Replacement.ReplacementBuilder setType(ReplacementType type) {
            this.type = type;
            return this;
        }

        public Replacement.ReplacementBuilder setText(String text) {
            this.text = text;
            return this;
        }

        public Replacement build() {
            return new Replacement(articleId, type, text);
        }

    }

}

