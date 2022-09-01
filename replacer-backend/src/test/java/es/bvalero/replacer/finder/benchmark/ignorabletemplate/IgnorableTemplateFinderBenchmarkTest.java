package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.config.XmlConfiguration;
import es.bvalero.replacer.finder.benchmark.BaseFinderBenchmark;
import es.bvalero.replacer.finder.benchmark.BenchmarkFinder;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
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

        runBenchmark(finders, WARM_UP / 5, ITERATIONS / 5);

        assertTrue(true);
    }

    public static void main(String[] args) throws URISyntaxException {
        generateBoxplot("ignorabletemplate/ignorable-template-benchmark.csv", "Ignorable Template");
    }
}
