package es.bvalero.replacer.dump;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.lang.Nullable;

class DumpLocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

    public DumpLocalDateTimeSerializer() {
        this(null);
    }

    public DumpLocalDateTimeSerializer(@Nullable Class<LocalDateTime> t) {
        super(t);
    }

    @Override
    public void serialize(
        LocalDateTime localDateTime,
        JsonGenerator jsonGenerator,
        SerializerProvider serializerProvider
    ) throws IOException {
        Long milliseconds = convertLocalDateTimeToMilliseconds(localDateTime);
        if (milliseconds == null) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeNumber(milliseconds);
        }
    }

    @Nullable
    static Long convertLocalDateTimeToMilliseconds(@Nullable LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        return zdt.toInstant().toEpochMilli();
    }
}
