package es.bvalero.replacer.finder.benchmark.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.common.exception.WikipediaException;
import es.bvalero.replacer.common.util.FileOfflineUtils;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.page.find.WikipediaNamespace;
import es.bvalero.replacer.page.find.WikipediaPage;
import es.bvalero.replacer.page.find.WikipediaTimestamp;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class BenchmarkUtils {

    public List<WikipediaPage> findSampleContents() throws WikipediaException {
        try {
            String jsonResponse = FileOfflineUtils.getFileContent(
                "es/bvalero/replacer/finder/benchmark/page-samples.json"
            );

            ObjectMapper jsonMapper = new ObjectMapper();
            WikipediaApiResponse apiResponse = jsonMapper.readValue(jsonResponse, WikipediaApiResponse.class);
            return apiResponse.getQuery().getPages().stream().map(BenchmarkUtils::convert).collect(Collectors.toList());
        } catch (ReplacerException | JsonProcessingException e) {
            throw new WikipediaException("Error loading sample contents", e);
        }
    }

    private WikipediaPage convert(WikipediaApiResponse.Page page) {
        return WikipediaPage
            .builder()
            .pageKey(PageKey.of(WikipediaLanguage.getDefault(), page.getPageid()))
            .namespace(WikipediaNamespace.valueOf(page.getNs()))
            .title(page.getTitle())
            .content(page.getRevisions().stream().findFirst().orElseThrow().getSlots().getMain().getContent())
            .lastUpdate(WikipediaTimestamp.of(page.getRevisions().stream().findFirst().orElseThrow().getTimestamp()))
            .queryTimestamp(WikipediaTimestamp.now())
            .build();
    }
}
