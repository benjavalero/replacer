package es.bvalero.replacer.wikipedia;

import org.springframework.core.convert.converter.Converter;

public class StringToWikipediaLanguageConverter implements Converter<String, WikipediaLanguage> {

    @Override
    public WikipediaLanguage convert(String source) {
        return WikipediaLanguage.forValues(source.toLowerCase());
    }
}
