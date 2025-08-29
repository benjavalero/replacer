package es.bvalero.replacer.page.save;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.bvalero.replacer.common.domain.User;
import es.bvalero.replacer.common.domain.UserId;
import es.bvalero.replacer.wikipedia.WikipediaException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class EditionsPerMinuteValidator {

    @Setter(onMethod_ = @TestOnly)
    @Value("${replacer.max-editions-per-minute}")
    private int maxEditionsPerMinute;

    private final Cache<UserId, CircularFifoQueue<LocalDateTime>> cachedUserEditions = Caffeine.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build();

    void validate(User user) throws WikipediaException {
        if (user.isBot()) {
            // Bots are allowed to perform more editions per minute
            return;
        }

        UserId userId = user.getId();
        CircularFifoQueue<LocalDateTime> userEditions = cachedUserEditions.get(userId, id ->
            new CircularFifoQueue<>(maxEditionsPerMinute)
        );
        assert userEditions != null;

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        if (userEditions.isAtFullCapacity()) {
            LocalDateTime older = userEditions.peek();
            assert older != null;
            LOGGER.debug("Older edition: {}", older);
            LOGGER.debug("Difference in seconds: {}", ChronoUnit.SECONDS.between(older, now));
            long diffMinutes = ChronoUnit.MINUTES.between(older, now);
            if (diffMinutes == 0) {
                LOGGER.error(
                    "Maximum number of editions per minute is {} - {} - {}",
                    maxEditionsPerMinute,
                    userId,
                    older
                );
                // The message is in Spanish to be displayed in an alert in the frontend
                throw new WikipediaException(
                    String.format("Ha sobrepasado el máximo de %d ediciones por minuto.", maxEditionsPerMinute)
                );
            }
        }

        userEditions.add(now);
    }
}
