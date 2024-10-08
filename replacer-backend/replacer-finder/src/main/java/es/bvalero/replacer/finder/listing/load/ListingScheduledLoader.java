package es.bvalero.replacer.finder.listing.load;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@SuppressWarnings("rawtypes")
@Slf4j
@Component
public class ListingScheduledLoader {

    // Dependency injection
    private final List<ListingLoader> listingLoaders;

    public ListingScheduledLoader(List<ListingLoader> listingLoaders) {
        this.listingLoaders = listingLoaders;
    }

    @Scheduled(initialDelay = 0, fixedDelayString = "${replacer.parse.file.delay}")
    public void scheduledItemListUpdate() {
        MDC.put("user", "system");
        LOGGER.debug("START Scheduled Item List update...");
        for (ListingLoader loader : listingLoaders) {
            loader.load();
        }
        MDC.remove("user");
    }
}
