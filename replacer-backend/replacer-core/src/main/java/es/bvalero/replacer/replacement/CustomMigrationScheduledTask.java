package es.bvalero.replacer.replacement;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Task to migrate custom replacements to the standard table when the term exists in general listings */
@Slf4j
@Component
class CustomMigrationScheduledTask {

    // Dependency injection
    private final CustomMigrationService customMigrationService;

    CustomMigrationScheduledTask(CustomMigrationService customMigrationService) {
        this.customMigrationService = customMigrationService;
    }

    @Scheduled(cron = "0 0 4 * * MON")
    void scheduledMigrateCustomReplacements() {
        MDC.put("user", "system");
        LOGGER.debug("START Scheduled Custom Replacement Migration...");
        customMigrationService.migrate();
        LOGGER.debug("END Scheduled Custom Replacement Migration");
        MDC.remove("user");
    }
}
