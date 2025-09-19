package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Optional;
import java.util.SortedSet;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface ReplacementFindApi {
    /** Find all replacements in the page content ignoring the ones contained in immutables */
    SortedSet<Replacement> findReplacements(FinderPage page);

    /**
     * Find all replacements in the page content ignoring the ones contained in immutables.
     * They are retrieved without suggestions in order to improve the performance.
     */
    SortedSet<Replacement> findReplacementsWithoutSuggestions(FinderPage page);

    /** Find a known standard type matching with the given replacement and case-sensitive option */
    Optional<StandardType> findReplacementType(WikipediaLanguage lang, String replacement, boolean caseSensitive);
}
