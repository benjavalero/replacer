package es.bvalero.replacer.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@IdClass(ReplacementPK.class)
@Table(name = "replacement")
public class ReplacementBD implements Serializable {

    @Id
    @Column(name = "title", nullable = false, length = 190)
    private String title;

    @Id
    @Column(name = "word", nullable = false, length = 190)
    private String word;

    @Column(name = "dtfixed", columnDefinition = "TIMESTAMP")
    private Timestamp lastReviewed;

    public ReplacementBD() {
    }

    public ReplacementBD(String title, String word) {
        this.title = title;
        this.word = word;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Timestamp getLastReviewed() {
        return lastReviewed;
    }

    public void setLastReviewed(Timestamp lastReviewed) {
        this.lastReviewed = lastReviewed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReplacementBD that = (ReplacementBD) o;

        if (!title.equals(that.title)) return false;
        return word.equals(that.word);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + word.hashCode();
        return result;
    }

}
