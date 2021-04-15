package es.bvalero.replacer.replacement;

import lombok.Value;

@Value(staticConstructor = "of")
public class PageEntity {

    String lang;
    int pageId;
    String title;
}
