package es.bvalero.replacer.page;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.replacement.CustomOptions;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import java.util.ArrayList;
import java.util.List;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
public class PageReviewOptions {

    WikipediaLanguage lang;
    String type;
    String subtype;
    String suggestion;
    boolean caseSensitive;

    WikipediaLanguage getLang() {
        return lang == null ? WikipediaLanguage.getDefault() : lang;
    }

    static PageReviewOptions ofNoType(WikipediaLanguage lang) {
        return new PageReviewOptions(lang, null, null, null, false);
    }

    static PageReviewOptions ofTypeSubtype(WikipediaLanguage lang, String type, String subtype) {
        return new PageReviewOptions(lang, type, subtype, null, false);
    }

    static PageReviewOptions ofCustom(
        WikipediaLanguage lang,
        String replacement,
        String suggestion,
        boolean caseSensitive
    ) {
        return new PageReviewOptions(lang, ReplacementType.CUSTOM, replacement, suggestion, caseSensitive);
    }

    CustomOptions toCustomOptions() {
        return CustomOptions.of(subtype, caseSensitive, suggestion);
    }

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        list.add(lang.toString());

        if (type == null) {
            list.add("NO TYPE");
        } else {
            list.add(type);
            list.add(subtype);
        }

        return StringUtils.join(list, " - ");
    }
}
