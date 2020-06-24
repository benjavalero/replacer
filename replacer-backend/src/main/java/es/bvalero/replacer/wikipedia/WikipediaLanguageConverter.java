package es.bvalero.replacer.wikipedia;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WikipediaLanguageConverter implements Converter<String, WikipediaLanguage> {

    @Override
    public WikipediaLanguage convert(@NotNull String code) {
        return WikipediaLanguage.forValues(code);
    }
}
