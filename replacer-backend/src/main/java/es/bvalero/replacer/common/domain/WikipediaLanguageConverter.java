package es.bvalero.replacer.common.domain;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

// This converter is needed because Spring doesn't use Jackson for web-bindings,
// only for the body request and the response.
// See https://github.com/spring-projects/spring-boot/issues/24233#issuecomment-733001230
@Component
public class WikipediaLanguageConverter implements Converter<String, WikipediaLanguage> {

    @Override
    public WikipediaLanguage convert(String code) {
        return WikipediaLanguage.valueOfCode(code);
    }
}
