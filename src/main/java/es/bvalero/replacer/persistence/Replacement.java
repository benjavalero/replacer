package es.bvalero.replacer.persistence;

import org.hibernate.annotations.Immutable;
import org.jetbrains.annotations.NonNls;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A replacement in the database related to an article.
 */
@Entity
@Immutable
@Table(name = "replacement", uniqueConstraints = @UniqueConstraint(columnNames = {"articleid", "type", "text"}))
public class Replacement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "articleid", nullable = false)
    private Article article;

    @Column(name = "type", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private ReplacementType type;

    @Column(name = "text", nullable = false, length = 30)
    private String text;

    @SuppressWarnings("unused")
    public Replacement() {
        // Needed by JPA
    }

    private Replacement(Article article, ReplacementType type, String text) {
        this.article = article;
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Replacement that = (Replacement) obj;
        return article.equals(that.article) &&
                type == that.type &&
                text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(article, type, text);
    }

    @NonNls
    @Override
    public String toString() {
        return "Replacement{" +
                "article=" + article +
                ", type=" + type +
                ", text='" + text + '\'' +
                '}';
    }

    public static class ReplacementBuilder {
        private Article article;
        private ReplacementType type;
        private String text;

        public Replacement.ReplacementBuilder setArticle(Article article) {
            this.article = article;
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
            return new Replacement(article, type, text);
        }

    }

}

