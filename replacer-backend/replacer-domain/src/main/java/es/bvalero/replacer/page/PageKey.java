package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;

/** A key to identify a page in Wikipedia */
@Value(staticConstructor = "of")
public class PageKey implements Comparable<PageKey> {

    @NonNull
    WikipediaLanguage lang;

    int pageId;

    @Override
    public String toString() {
        return String.format("%s - %d", this.lang, this.pageId);
    }

    @Override
    public int compareTo(@NotNull PageKey that) {
        return Integer.compare(this.pageId, that.pageId);
    }
}
