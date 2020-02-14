package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class UrlFinderTest {

    @Test
    public void testRegexUrl() {
        String url1 = "https://google.es?u=aj&t2+rl=http://www.marca.com#!page~2,3";
        String url2 = "http://www.marca.com";
        String text = String.format("[%s Google] [%s Marca]", url1, url2);

        ImmutableFinder urlFinder = new UrlFinder();

        List<Immutable> matches = urlFinder.findList(text);
        Assert.assertFalse(matches.isEmpty());
        Assert.assertEquals(url1, matches.get(0).getText());
        Assert.assertEquals(url2, matches.get(1).getText());
    }
}