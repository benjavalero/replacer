package es.bvalero.replacer.checkwikipedia;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import lombok.Value;

@Value(staticConstructor = "of")
public class CheckWikipediaFixEvent {

    WikipediaLanguage lang;
    String pageTitle;
    CheckWikipediaAction action;
}
