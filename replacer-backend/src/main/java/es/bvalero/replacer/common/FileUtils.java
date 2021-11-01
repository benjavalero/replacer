package es.bvalero.replacer.common;

import es.bvalero.replacer.domain.ReplacerException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

    public static String getFileContent(String fileName) throws ReplacerException {
        LOGGER.debug("Load fake content from file: {}", fileName);
        try {
            return Files.readString(Paths.get(FileUtils.class.getResource(fileName).toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new ReplacerException(e);
        }
    }
}
