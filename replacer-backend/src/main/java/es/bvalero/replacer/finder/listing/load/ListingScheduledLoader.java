package es.bvalero.replacer.finder.listing.load;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@SuppressWarnings("rawtypes")
@Slf4j
@Component
public class ListingScheduledLoader {

    @Autowired
    List<ListingLoader> listingLoaders;

    @Scheduled(fixedDelayString = "${replacer.parse.file.delay}")
    public void scheduledItemListUpdate() {
        for (ListingLoader loader : listingLoaders) {
            LOGGER.info("Scheduled loading of {} listings", loader.getLabel());
            loader.load();
        }
    }
}
