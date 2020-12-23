package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LinkAliasedFinderTest {

    @Test
    void testLinkAliased() {
        String aliased1 = "brasil";
        String aliased2 = " reacción química ";
        String noAliased = "Text";
        String withNewLine = "one\nexample";
        String file = "File:file.jpg";
        String aliasedAnnex = "Anexo:Países";
        String category = "Categoría:Países";
        String interWiki = "s:es:Corán";
        String text = String.format(
            "[[%s|Brasil]] [[%s]] [[%s|reacción]] [[%s|example]] [[%s|thumb]] [[%s|Países]] [[%s| ]] [[%s|Corán]].",
            aliased1,
            noAliased,
            aliased2,
            withNewLine,
            file,
            aliasedAnnex,
            category,
            interWiki
        );

        ImmutableFinder linkAliasedFinder = new LinkAliasedFinder();
        List<Immutable> matches = linkAliasedFinder.findList(text);

        Set<String> expected = new HashSet<>(
            Arrays.asList(aliased1, aliased2, file, aliasedAnnex, category, interWiki)
        );
        Set<String> actual = matches.stream().map(Immutable::getText).collect(Collectors.toSet());
        Assertions.assertEquals(expected, actual);
    }
}
