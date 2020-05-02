package es.bvalero.replacer.wikipedia;

import java.util.stream.Stream;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class WikipediaLanguageConverter implements AttributeConverter<WikipediaLanguage, String> {

    @Override
    public String convertToDatabaseColumn(WikipediaLanguage lang) {
        if (lang == null) {
            return null;
        }
        return lang.getCode();
    }

    @Override
    public WikipediaLanguage convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return Stream
            .of(WikipediaLanguage.values())
            .filter(c -> c.getCode().equals(code))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
