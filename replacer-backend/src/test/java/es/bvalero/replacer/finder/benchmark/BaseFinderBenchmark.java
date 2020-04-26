package es.bvalero.replacer.finder.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.bvalero.replacer.wikipedia.WikipediaApiResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class BaseFinderBenchmark {
    private static final int WARM_UP = 100;
    private static final int ITERATIONS = 1000;

    public void runBenchmark(List<BenchmarkFinder> finders) throws IOException, URISyntaxException {
        List<String> sampleContents = findSampleContents();

        // Warm-up
        System.out.println("WARM-UP...");
        run(finders, WARM_UP, sampleContents, false);

        // Real run
        run(finders, ITERATIONS, sampleContents, true);
    }

    private void run(List<BenchmarkFinder> finders, int numIterations, List<String> sampleContents, boolean print) {
        if (print) {
            System.out.println();
            System.out.println("FINDER\tTIME");
        }
        sampleContents.forEach(
            text -> {
                for (BenchmarkFinder finder : finders) {
                    long start = System.currentTimeMillis();
                    for (int i = 0; i < numIterations; i++) {
                        finder.findMatches(text);
                    }
                    long end = System.currentTimeMillis() - start;
                    if (print) {
                        System.out.println(finder.getClass().getSimpleName() + "\t" + end);
                    }
                }
            }
        );
    }

    List<String> findSampleContents() throws IOException, URISyntaxException {
        String fileName = "/es/bvalero/replacer/finder/benchmark/page-samples.json";
        String jsonResponse = Files.readString(Paths.get(getClass().getResource(fileName).toURI()));
        ObjectMapper jsonMapper = new ObjectMapper();
        WikipediaApiResponse apiResponse = jsonMapper.readValue(jsonResponse, WikipediaApiResponse.class);
        return apiResponse
            .getQuery()
            .getPages()
            .stream()
            .map(page -> page.getRevisions().get(0).getSlots().getMain().getContent())
            .collect(Collectors.toList());
    }
}
