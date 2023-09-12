package es.bvalero.replacer.page.review;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

class PageContentSerializer extends JsonSerializer<String> {

    private static final int SHORT_CONTENT_LENGTH = 50;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(StringUtils.abbreviate(value, SHORT_CONTENT_LENGTH));
    }
}
