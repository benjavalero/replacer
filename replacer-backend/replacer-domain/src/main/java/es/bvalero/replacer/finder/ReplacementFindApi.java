package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.util.Collection;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface ReplacementFindApi {
    /** Find all replacements in the page content ignoring the ones contained in immutables */
    Collection<Replacement> findReplacements(FinderPage page);

    /** Find a known standard type matching with the given replacement and case-sensitive option */
    Optional<StandardType> findReplacementType(WikipediaLanguage lang, String replacement, boolean caseSensitive);
}
