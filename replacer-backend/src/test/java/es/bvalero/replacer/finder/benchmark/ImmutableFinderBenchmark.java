package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.immutable.CompleteTagFinder;
import es.bvalero.replacer.finder.immutable.DomainFinder;
import es.bvalero.replacer.finder.immutable.FileNameFinder;
import es.bvalero.replacer.finder.immutable.InterLanguageLinkFinder;
import es.bvalero.replacer.finder.immutable.LinkAliasedFinder;
import es.bvalero.replacer.finder.immutable.LinkSuffixedFinder;
import es.bvalero.replacer.finder.immutable.QuotesAngularFinder;
import es.bvalero.replacer.finder.immutable.QuotesFinder;
import es.bvalero.replacer.finder.immutable.QuotesTypographicFinder;
import es.bvalero.replacer.finder.immutable.TemplateNameFinder;
import es.bvalero.replacer.finder.immutable.TemplateParamFinder;
import es.bvalero.replacer.finder.immutable.UrlFinder;
import es.bvalero.replacer.finder.immutable.XmlTagFinder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class ImmutableFinderBenchmark extends BaseFinderBenchmark {
    private static final int ITERATIONS = 1000;

    @Test
    public void testBenchmark() throws IOException, URISyntaxException {
        // Load the finders
        List<ImmutableFinder> finders = new ArrayList<>();
        finders.add(new UrlFinder());
        finders.add(new XmlTagFinder());
        finders.add(new CompleteTagFinder());
        finders.add(new DomainFinder());
        finders.add(new TemplateNameFinder());
        finders.add(new TemplateParamFinder());
        finders.add(new QuotesFinder());
        finders.add(new QuotesTypographicFinder());
        finders.add(new QuotesAngularFinder());
        finders.add(new FileNameFinder());
        finders.add(new LinkSuffixedFinder());
        finders.add(new LinkAliasedFinder());
        finders.add(new InterLanguageLinkFinder());

        System.out.println();
        System.out.println("FINDER\tTIME");
        findSampleContents()
            .forEach(
                value -> {
                    for (ImmutableFinder finder : finders) {
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < ITERATIONS; i++) {
                            finder.findList(value);
                        }
                        long end = System.currentTimeMillis() - start;
                        System.out.println(finder.getClass().getSimpleName() + "\t" + end);
                    }
                }
            );

        Assert.assertTrue(true);
    }

    @Test
    public void testMatches() throws IOException, URISyntaxException {
        // Load the finders
        List<ImmutableFinder> finders = new ArrayList<>();
        finders.add(new UrlFinder());
        finders.add(new XmlTagFinder());
        finders.add(new CompleteTagFinder());
        finders.add(new DomainFinder());
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

        finders.forEach(finder -> {
            System.out.println("FINDER: " + finder.getClass().getSimpleName());
            sampleContents.forEach(content -> {
                finder.find(content).forEach(result -> {
                    System.out.println("==> " + result.getText());
                });
                System.out.println("----------");
            });
            System.out.println();
        });
        Assert.assertTrue(true);
    }
}
