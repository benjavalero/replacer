package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.XmlConfiguration;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.immutables.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = { XmlConfiguration.class, CompleteTagFinder.class, TemplateParamFinder.class, TemplateFinder.class }
)
public class ImmutableFinderBenchmark extends BaseFinderBenchmark {
    @Autowired
    private CompleteTagFinder completeTagFinder;

    @Autowired
    private TemplateParamFinder templateParamFinder;

    @Autowired
    private TemplateFinder templateFinder;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        // Load the finders
        List<ImmutableFinder> finders = new ArrayList<>();
        finders.add(new CategoryFinder());
        finders.add(new UrlFinder());
        finders.add(new XmlTagFinder());
        finders.add(new CommentFinder());
        finders.add(completeTagFinder);
        finders.add(templateFinder);
        finders.add(new TemplateNameFinder());
        finders.add(templateParamFinder);
        finders.add(new CursiveFinder());
        finders.add(new QuotesFinder());
        finders.add(new QuotesTypographicFinder());
        finders.add(new QuotesAngularFinder());
        finders.add(new FileNameFinder());
        finders.add(new LinkSuffixedFinder());
        finders.add(new LinkAliasedFinder());
        finders.add(new InterLanguageLinkFinder());
        // finders.add(new PersonNameFinder());

        run(finders);

        Assertions.assertTrue(true);
    }

    private void run(List<ImmutableFinder> finders) throws IOException, URISyntaxException {
        List<String> sampleContents = findSampleContents();

        // Warm-up
        System.out.println("WARM-UP...");
        run(finders, WARM_UP, sampleContents, false);

        // Real run
        run(finders, ITERATIONS, sampleContents, true);
    }

    private void run(List<ImmutableFinder> finders, int numIterations, List<String> sampleContents, boolean print) {
        if (print) {
            System.out.println();
            System.out.println("FINDER\tTIME");
        }
        sampleContents.forEach(
            text -> {
                for (ImmutableFinder finder : finders) {
                    long start = System.currentTimeMillis();
                    for (int i = 0; i < numIterations; i++) {
                        finder.findList(text);
                    }
                    long end = System.currentTimeMillis() - start;
                    if (print) {
                        System.out.println(finder.getClass().getSimpleName() + "\t" + end);
                    }
                }
            }
        );
    }

    @Test
    public void testMatches() throws IOException, URISyntaxException {
        // Load the finders
        List<ImmutableFinder> finders = new ArrayList<>();
        finders.add(new UrlFinder());
        finders.add(new XmlTagFinder());
        finders.add(new CompleteTagFinder());
        finders.add(new TemplateNameFinder());
        finders.add(new TemplateParamFinder());
        finders.add(new QuotesFinder());
        finders.add(new QuotesTypographicFinder());
        finders.add(new QuotesAngularFinder());
        finders.add(new FileNameFinder());
        finders.add(new LinkSuffixedFinder());
        finders.add(new LinkAliasedFinder());
        finders.add(new InterLanguageLinkFinder());

        List<String> sampleContents = findSampleContents();

        finders.forEach(
            finder -> {
                System.out.println("FINDER: " + finder.getClass().getSimpleName());
                sampleContents.forEach(
                    content -> {
                        finder.find(content).forEach(result -> System.out.println("==> " + result.getText()));
                        System.out.println("----------");
                    }
                );
                System.out.println();
            }
        );
        Assertions.assertTrue(true);
    }
}
