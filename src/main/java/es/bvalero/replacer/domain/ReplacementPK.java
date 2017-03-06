package es.bvalero.replacer.domain;

import java.io.Serializable;

public class ReplacementPK implements Serializable {

    private String title;
    private String word;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReplacementPK that = (ReplacementPK) o;

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
