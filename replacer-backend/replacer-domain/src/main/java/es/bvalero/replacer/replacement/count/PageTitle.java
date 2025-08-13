package es.bvalero.replacer.replacement.count;

import es.bvalero.replacer.common.domain.PageKey;
import lombok.Value;

@Value(staticConstructor = "of")
class PageTitle {

    PageKey pageKey;
    String title;
}
