package es.bvalero.replacer.article;

import es.bvalero.replacer.utils.RegexMatchType;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "potentialerror")
public class PotentialError implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "articleid")
    private Article article;

    @Column(name = "type", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private RegexMatchType type;

    @Column(name = "text", nullable = false, length = 30)
    private String text;

    public PotentialError() {
    }

    public PotentialError(Article article, RegexMatchType type, String text) {
        this.article = article;
        this.type = type;
        this.text = text;
    }

    public Article getArticle() {
        return article;
    }

    void setArticle(Article article) {
        this.article = article;
    }

    RegexMatchType getType() {
        return type;
    }

    void setType(RegexMatchType type) {
        this.type = type;
    }

    String getText() {
        return text;
    }

    void setText(String text) {
        this.text = text;
    }

}

