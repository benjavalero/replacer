package es.bvalero.replacer.common.domain;

import lombok.Value;

@Value(staticConstructor = "of")
public class PageTitle {

    int pageId;
    String title;
}
