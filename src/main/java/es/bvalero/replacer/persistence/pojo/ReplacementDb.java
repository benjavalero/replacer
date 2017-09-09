package es.bvalero.replacer.persistence.pojo;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@IdClass(ReplacementDbPk.class)
@Table(name = "replacement")
public class ReplacementDb implements Serializable {

    @Id
    @Column(name = "title", nullable = false, length = 190)
    private String title;

    @Id
    @Column(name = "word", nullable = false, length = 190)
    private String word;

    @Column(name = "dtfixed", columnDefinition = "TIMESTAMP")
    private Timestamp lastReviewed;

    public ReplacementDb() {
    }

    public ReplacementDb(String title, String word) {
        this.title = title;
        this.word = word;
        this.lastReviewed = null;
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

        ReplacementDb that = (ReplacementDb) o;

        return title.equals(that.title) && word.equals(that.word);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + word.hashCode();
        return result;
    }

}
