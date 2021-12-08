package es.bvalero.replacer.common.util;

import es.bvalero.replacer.common.exception.ReplacerException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class FileOfflineUtils {

    public String getFileContent(String fileName) throws ReplacerException {
        LOGGER.debug("Load fake content from file: {} ...", fileName);
        try {
            return Files.readString(
                Paths.get(Objects.requireNonNull(FileOfflineUtils.class.getResource(fileName)).toURI())
            );
        } catch (IOException | URISyntaxException e) {
            throw new ReplacerException(e);
        }
    }
}
