package es.bvalero.replacer.common.util;

import es.bvalero.replacer.common.exception.ReplacerException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class FileOfflineUtils {

    public String getFileContent(String fileName) throws ReplacerException {
        LOGGER.debug("Load fake content from file: {} ...", fileName);
        try (InputStream is = FileOfflineUtils.class.getClassLoader().getResourceAsStream(fileName)) {
            return new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Error loading resource file", e);
            throw new ReplacerException(e);
        }
    }
}
