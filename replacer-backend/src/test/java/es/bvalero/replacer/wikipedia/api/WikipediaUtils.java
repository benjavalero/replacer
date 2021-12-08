package es.bvalero.replacer.wikipedia.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.common.util.FileOfflineUtils;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WikipediaUtils {

    public List<String> findSampleContents() throws ReplacerException {
        String jsonResponse = FileOfflineUtils.getFileContent(
            "/es/bvalero/replacer/finder/benchmark/page-samples.json"
        );
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
}
