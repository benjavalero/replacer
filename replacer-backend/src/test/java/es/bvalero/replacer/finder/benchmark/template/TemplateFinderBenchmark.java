package es.bvalero.replacer.finder.benchmark.template;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
class TemplateFinderBenchmark extends BaseFinderBenchmark {

    @Resource
    private List<String> templateNames;

    @Test
    void testBenchmark() throws Exception {
        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new TemplateRegexIteratedFinder(templateNames));
        finders.add(new TemplateRegexAlternateFinder(templateNames));
        finders.add(new TemplateAutomatonIteratedFinder(templateNames));
        finders.add(new TemplateAutomatonAlternateFinder(templateNames));

        runBenchmark(finders);

        MatcherAssert.assertThat(true, is(true));
    }
}
