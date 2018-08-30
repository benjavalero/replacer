package es.bvalero.replacer.article;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A potential error in the database related to an article.
 */
@Entity
@Table(name = "potentialerror")
public class PotentialError implements Serializable {

    @SuppressWarnings("unused")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "articleid")
    private Article article;

    @Column(name = "type", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private PotentialErrorType type;

    @Column(name = "text", nullable = false, length = 30)
    private String text;

    @SuppressWarnings("unused")
    public PotentialError() {
    }

    public PotentialError(PotentialErrorType type, String text) {
        this.type = type;
        this.text = text;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    PotentialErrorType getType() {
        return type;
    }

    void setType(PotentialErrorType type) {
        this.type = type;
    }

    String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotentialError that = (PotentialError) o;
        return type == that.type && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, text);
    }

    @Override
    public String toString() {
        return "PotentialError{" +
                "type=" + type +
                ", text='" + text + '\'' +
                '}';
    }

}

