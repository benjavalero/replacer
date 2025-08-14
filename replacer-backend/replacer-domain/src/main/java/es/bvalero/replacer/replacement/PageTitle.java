package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.PageKey;
import lombok.Value;

@Value(staticConstructor = "of")
public class PageTitle {

    PageKey pageKey;
    String title;
}
