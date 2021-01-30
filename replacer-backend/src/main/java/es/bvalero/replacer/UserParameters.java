package es.bvalero.replacer;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import lombok.Data;

@Data
public class UserParameters {

    WikipediaLanguage lang;
    String user;

    @Override
    public String toString() {
        return "lang=" + this.getLang() + ", user=" + this.getUser();
    }
}
