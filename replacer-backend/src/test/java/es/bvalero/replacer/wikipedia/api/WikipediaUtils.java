package es.bvalero.replacer.wikipedia.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.common.util.FileOfflineUtils;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class WikipediaUtils {

    public List<String> findSampleContents() throws WikipediaException {
        try {
            String jsonResponse = FileOfflineUtils.getFileContent(
                "/es/bvalero/replacer/finder/benchmark/page-samples.json"
            );

            ObjectMapper jsonMapper = new ObjectMapper();
            WikipediaApiResponse apiResponse = jsonMapper.readValue(jsonResponse, WikipediaApiResponse.class);
            return apiResponse
                .getQuery()
                .getPages()
                .stream()
                .map(page -> page.getRevisions().get(0).getSlots().getMain().getContent())
                .collect(Collectors.toList());
        } catch (ReplacerException | JsonProcessingException e) {
            throw new WikipediaException("Error loading sample contents");
        }
    }
}
