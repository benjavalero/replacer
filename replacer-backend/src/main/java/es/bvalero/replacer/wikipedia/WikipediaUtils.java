package es.bvalero.replacer.wikipedia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.ReplacerException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.TestOnly;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WikipediaUtils {

    private static final String WIKIPEDIA_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final DateTimeFormatter WIKIPEDIA_DATE_FORMATTER = DateTimeFormatter.ofPattern(
        WIKIPEDIA_DATE_PATTERN
    );

    public static String getFileContent(String fileName) throws ReplacerException {
        LOGGER.debug("Load fake content from file: {}", fileName);
        try {
            return Files.readString(Paths.get(WikipediaUtils.class.getResource(fileName).toURI()));
        } catch (IOException | URISyntaxException e) {
            throw new ReplacerException(e);
        }
    }

    @TestOnly
    public static List<String> findSampleContents() throws ReplacerException {
        String jsonResponse = WikipediaUtils.getFileContent("/es/bvalero/replacer/finder/benchmark/page-samples.json");
        try {
            ObjectMapper jsonMapper = new ObjectMapper();
            WikipediaApiResponse apiResponse = jsonMapper.readValue(jsonResponse, WikipediaApiResponse.class);
            return apiResponse
                .getQuery()
                .getPages()
                .stream()
                .map(page -> page.getRevisions().get(0).getSlots().getMain().getContent())
                .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            throw new ReplacerException(e);
        }
    }

    public static LocalDate parseWikipediaTimestamp(String timestamp) {
        return LocalDate.from(WIKIPEDIA_DATE_FORMATTER.parse(timestamp));
    }

    static String formatWikipediaTimestamp(LocalDateTime localDateTime) {
        return WIKIPEDIA_DATE_FORMATTER.format(localDateTime);
    }
}
