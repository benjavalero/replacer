package es.bvalero.replacer.finder.listing.load;

import com.github.rozidan.springboot.logger.Loggable;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@SuppressWarnings("rawtypes")
@Component
public class ListingScheduledLoader {

    @Autowired
    private List<ListingLoader> listingLoaders;

    @Loggable(value = LogLevel.DEBUG, skipArgs = true, skipResult = true)
    @Scheduled(fixedDelayString = "${replacer.parse.file.delay}")
    public void scheduledItemListUpdate() {
        for (ListingLoader loader : listingLoaders) {
            loader.load();
        }
    }
}
