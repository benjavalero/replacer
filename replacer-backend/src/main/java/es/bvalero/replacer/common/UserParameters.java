package es.bvalero.replacer.common;

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
