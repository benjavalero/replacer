package es.bvalero.replacer.wikipedia.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaNamespace;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.common.util.FileOfflineUtils;
import es.bvalero.replacer.common.util.WikipediaDateUtils;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class WikipediaUtils {

    public List<WikipediaPage> findSampleContents() throws WikipediaException {
        try {
            String jsonResponse = FileOfflineUtils.getFileContent(
                "/es/bvalero/replacer/finder/benchmark/page-samples.json"
            );

            ObjectMapper jsonMapper = new ObjectMapper();
            WikipediaApiResponse apiResponse = jsonMapper.readValue(jsonResponse, WikipediaApiResponse.class);
            return apiResponse.getQuery().getPages().stream().map(WikipediaUtils::convert).collect(Collectors.toList());
        } catch (ReplacerException | JsonProcessingException e) {
            throw new WikipediaException("Error loading sample contents");
        }
    }

    private WikipediaPage convert(WikipediaApiResponse.Page page) {
        return WikipediaPage
            .builder()
            .id(WikipediaPageId.of(WikipediaLanguage.getDefault(), page.getPageid()))
            .namespace(WikipediaNamespace.valueOf(page.getNs()))
            .title(page.getTitle())
            .content(page.getRevisions().get(0).getSlots().getMain().getContent())
            .lastUpdate(WikipediaDateUtils.parseWikipediaTimestamp(page.getRevisions().get(0).getTimestamp()))
            .queryTimestamp(LocalDateTime.now())
            .build();
    }
}
