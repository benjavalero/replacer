package es.bvalero.replacer.replacement;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.CustomType;
import es.bvalero.replacer.finder.ReplacementFindApi;
import es.bvalero.replacer.finder.StandardType;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class CustomMigrationService {

    // Dependency injection
    private final ReplacementFindApi replacementFindApi;
    private final CustomRepository customRepository;

    CustomMigrationService(ReplacementFindApi replacementFindApi, CustomRepository customRepository) {
        this.replacementFindApi = replacementFindApi;
        this.customRepository = customRepository;
    }

    void migrate() {
        LOGGER.debug("START Custom replacement migration");
        int totalTermsMigrated = 0;
        int totalRowsMoved = 0;

        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            Collection<CustomType> customTypes = customRepository.findDistinctCustomTypes(lang);
            LOGGER.debug("Found {} distinct custom types for language {}", customTypes.size(), lang);

            // Accumulate the matches: custom type -> standard type
            Map<CustomType, StandardType> migrations = new LinkedHashMap<>();
            for (CustomType customType : customTypes) {
                Optional<StandardType> standardType = replacementFindApi.findReplacementType(
                    lang,
                    customType.getSubtype(),
                    customType.isCaseSensitive()
                );
                standardType.ifPresent(type -> migrations.put(customType, type));
            }

            if (!migrations.isEmpty()) {
                int rowsMoved = customRepository.migrateToReplacement(lang, migrations);
                LOGGER.info(
                    "Migrated {} custom types for language {}: {} total rows moved",
                    migrations.size(),
                    lang,
                    rowsMoved
                );
                totalTermsMigrated += migrations.size();
                totalRowsMoved += rowsMoved;
            }
        }

        LOGGER.debug(
            "END Custom replacement migration: {} terms migrated, {} total rows moved",
            totalTermsMigrated,
            totalRowsMoved
        );
    }
}
