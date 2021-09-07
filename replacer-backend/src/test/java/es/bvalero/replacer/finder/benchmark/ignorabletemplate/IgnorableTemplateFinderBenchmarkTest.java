package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import static org.hamcrest.Matchers.is;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = XmlConfiguration.class)
class IgnorableTemplateFinderBenchmarkTest extends BaseFinderBenchmark {

    @Resource
    private Set<String> ignorableTemplates;

    @Test
    void testBenchmark() throws ReplacerException {
        // Load the finders
        List<BenchmarkFinder> finders = new ArrayList<>();
        finders.add(new IgnorableTemplateLowercaseContainsFinder(ignorableTemplates));
        finders.add(new IgnorableTemplateRegexFinder(ignorableTemplates));
        finders.add(new IgnorableTemplateRegexInsensitiveFinder(ignorableTemplates));
        finders.add(new IgnorableTemplateAutomatonFinder(ignorableTemplates));

        runBenchmark(finders);

        MatcherAssert.assertThat(true, is(true));
    }
}
