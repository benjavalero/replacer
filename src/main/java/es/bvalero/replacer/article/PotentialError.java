package es.bvalero.replacer.article;

import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A potential error in the database related to an article.
 */
@Entity
@Immutable
@Table(name = "potentialerror", uniqueConstraints = @UniqueConstraint(columnNames = {"articleid", "type", "text"}))
public class PotentialError implements Serializable {
    // TODO Rename table

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articleid")
    private Article article;

    @Column(name = "type", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private PotentialErrorType type;

    // TODO Add the unique constraint to the MariaDB database and redo the indexes
    @Column(name = "text", nullable = false, length = 30)
    private String text;

    public PotentialError() {
        // Needed by JPA
    }

    public PotentialError(Article article, PotentialErrorType type, String text) {
        this.article = article;
        this.type = type;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotentialError that = (PotentialError) o;
        return Objects.equals(article, that.article) &&
                type == that.type &&
                Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(article, type, text);
    }

    @Override
    public String toString() {
        return "PotentialError{" +
                "article=" + article +
                ", type=" + type +
                ", text='" + text + '\'' +
                '}';
    }

}

