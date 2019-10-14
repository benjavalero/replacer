package es.bvalero.replacer.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.wikipedia.WikipediaApiResponse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

class BaseFinderBenchmark {

    List<String> findSampleContents() throws IOException, URISyntaxException {
        String fileName = "/es/bvalero/replacer/benchmark/page-samples.json";
        String jsonResponse = new String(Files.readAllBytes(Paths.get(getClass().getResource(fileName).toURI())),
                StandardCharsets.UTF_8);
        ObjectMapper jsonMapper = new ObjectMapper();
        WikipediaApiResponse apiResponse = jsonMapper.readValue(jsonResponse, WikipediaApiResponse.class);
        return apiResponse.getQuery().getPages().stream()
                .map(page -> page.getRevisions().get(0).getSlots().getMain().getContent())
                .collect(Collectors.toList());
    }

}
