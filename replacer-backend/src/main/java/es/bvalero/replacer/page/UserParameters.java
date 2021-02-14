package es.bvalero.replacer.page;

import es.bvalero.replacer.common.WikipediaLanguage;
import lombok.Data;

@Data
public class UserParameters {

    // This is for the moment only used in PageController.
    // In case it is used in more controllers this class could be moved to common package.

    WikipediaLanguage lang;
    String user;

    @Override
    public String toString() {
        return "lang=" + this.getLang() + ", user=" + this.getUser();
    }
}
