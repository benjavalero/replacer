package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.CustomType;
import es.bvalero.replacer.finder.StandardType;
import java.util.Collection;
import java.util.Map;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface CustomRepository {
    /** Find the pages reviewed for the given custom replacement and return the IDs */
    Collection<PageKey> findPagesReviewed(WikipediaLanguage lang, CustomType type);

    /** Find all distinct custom types (replacement + case-sensitivity) for the given language */
    Collection<CustomType> findDistinctCustomTypes(WikipediaLanguage lang);

    /**
     * Atomically migrate a batch of custom replacements to the standard replacement table.
     * Each entry maps a CustomType to the StandardType it should become.
     * Optimized for MariaDB with rewriteBatchedStatements=true.
     * @return the total number of rows migrated
     */
    int migrateToReplacement(WikipediaLanguage lang, Map<CustomType, StandardType> migrations);
}
