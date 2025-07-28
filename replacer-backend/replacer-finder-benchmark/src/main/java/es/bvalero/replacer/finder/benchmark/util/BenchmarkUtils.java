package es.bvalero.replacer.finder.benchmark.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.common.util.FileOfflineUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.page.PageKey;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BenchmarkUtils {

    public List<FinderPage> findSampleContents() throws ReplacerException {
        return findSamplePages().stream().map(BenchmarkUtils::convert).toList();
    }

    public List<WikipediaApiResponse.Page> findSamplePages() throws ReplacerException {
        try {
            String jsonResponse = FileOfflineUtils.getFileContent(
                "es/bvalero/replacer/finder/benchmark/page-samples.json"
            );

            ObjectMapper jsonMapper = new ObjectMapper();
            WikipediaApiResponse apiResponse = jsonMapper.readValue(jsonResponse, WikipediaApiResponse.class);
            return new ArrayList<>(apiResponse.getQuery().getPages());
        } catch (ReplacerException | JsonProcessingException e) {
            throw new ReplacerException("Error loading sample contents", e);
        }
    }

    private FinderPage convert(WikipediaApiResponse.Page page) {
        return FinderPage.of(
            PageKey.of(WikipediaLanguage.getDefault(), page.getPageid()),
            page.getTitle(),
            page.getRevisions().stream().findFirst().orElseThrow().getSlots().getMain().getContent()
        );
    }
}
