package es.bvalero.replacer.common;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

// This converter is need because of a bug on Jackson library.
// See https://github.com/FasterXML/jackson-databind/issues/1850
@Component
public class WikipediaLanguageConverter implements Converter<String, WikipediaLanguage> {

    @Override
    public WikipediaLanguage convert(String code) {
        return WikipediaLanguage.valueOfCode(code);
    }
}
