package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.finder.listing.ListingItem;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ListingScheduledLoader {

    @Autowired
    List<ListingLoader<ListingItem>> listingLoaders;

    @Scheduled(fixedDelayString = "${replacer.parse.file.delay}")
    public void scheduledItemListUpdate() {
        for (ListingLoader<ListingItem> loader : listingLoaders) {
            LOGGER.info("Scheduled loading of {} listings", loader.getLabel());
            loader.load();
        }
    }
}
