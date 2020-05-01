package es.bvalero.replacer.finder.benchmark.template;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
public class TemplateFinderBenchmark extends BaseFinderBenchmark {
    @Resource
    private List<String> templateNames;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new TemplateRegexIteratedFinder(templateNames));
        finders.add(new TemplateRegexAlternateFinder(templateNames));
        finders.add(new TemplateAutomatonIteratedFinder(templateNames));
        finders.add(new TemplateAutomatonAlternateFinder(templateNames));

        runBenchmark(finders);

        Assertions.assertTrue(true);
    }
}