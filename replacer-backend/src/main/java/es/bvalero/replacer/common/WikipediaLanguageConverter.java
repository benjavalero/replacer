package es.bvalero.replacer.common;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WikipediaLanguageConverter implements Converter<String, WikipediaLanguage> {

    @Override
    public WikipediaLanguage convert(String code) {
        return WikipediaLanguage.forValues(code);
    }
}
