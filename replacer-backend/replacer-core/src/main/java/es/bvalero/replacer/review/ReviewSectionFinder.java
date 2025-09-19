package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.User;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import es.bvalero.replacer.wikipedia.WikipediaSection;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class ReviewSectionFinder {

    // Dependency injection
    private final WikipediaPageRepository wikipediaPageRepository;

    ReviewSectionFinder(WikipediaPageRepository wikipediaPageRepository) {
        this.wikipediaPageRepository = wikipediaPageRepository;
    }

    /**
     * Find (if any) the smallest section in a page containing the given replacements.
     * In case such a section is found, then return a review containing the page fragment corresponding to the section
     * with the replacements translated accordingly.
     */
    Optional<Review> findPageReviewSection(Review review, User user) {
        assert review.getSection() == null;

        // Find the sections from the Wikipedia API (better than calculating them by ourselves)
        Collection<WikipediaSection> sections = wikipediaPageRepository.findSectionsInPage(
            review.getPage().getPageKey(),
            user.getAccessToken()
        );

        // Find the smallest section containing all the replacements
        Optional<WikipediaSection> smallestSection = findSmallestSectionContainingAllReplacements(
            sections,
            review.getReplacements()
        );
        if (smallestSection.isPresent()) {
            // Find the section from Wikipedia API (better than calculating it by ourselves)
            Optional<WikipediaPage> pageSection = wikipediaPageRepository.findPageSection(
                smallestSection.get(),
                user.getAccessToken()
            );
            if (pageSection.isPresent()) {
                // We need to modify the start position of the replacements according to the section start
                Collection<Replacement> sectionReplacements = translateReplacementsByOffset(
                    review.getReplacements(),
                    smallestSection.get().getByteOffset()
                );

                // We need to check some rare cases where the byte-offset doesn't match with the section position,
                // usually because of emojis or other strange Unicode characters like runes.
                if (!validateTranslatedReplacements(sectionReplacements, pageSection.get())) {
                    LOGGER.info(
                        "Not valid byte-offset in page section: {} - {}",
                        pageSection.get().getPageKey(),
                        pageSection.get().getTitle()
                    );
                    return Optional.empty();
                }

                LOGGER.debug(
                    "Found section for page {} - {} => {}",
                    pageSection.get().getPageKey(),
                    pageSection.get().getTitle(),
                    smallestSection.get()
                );

                // Finally, we build the section review based on the page review.
                Review sectionReview = Review.of(
                    pageSection.get(),
                    smallestSection.get(),
                    sectionReplacements,
                    review.getNumPending()
                );
                return Optional.of(sectionReview);
            }
        }

        LOGGER.debug("No section found in page: {} - {}", review.getPage().getPageKey(), review.getPage().getTitle());
        return Optional.empty();
    }

    private Optional<WikipediaSection> findSmallestSectionContainingAllReplacements(
        Collection<WikipediaSection> sections,
        Collection<Replacement> replacements
    ) {
        // We sort the collection of sections
        // In theory they are already sorted as returned from Wikipedia API,
        // so we use an ArrayList assuming there will not be actually sorting.
        List<WikipediaSection> sortedSections = new ArrayList<>(sections);
        Collections.sort(sortedSections);
        WikipediaSection smallest = null;
        for (int i = 0; i < sortedSections.size(); i++) {
            WikipediaSection section = sortedSections.get(i);
            int start = section.getByteOffset();
            Integer end = null;
            for (int j = i + 1; j < sortedSections.size() && end == null; j++) {
                if (sortedSections.get(j).getLevel() <= section.getLevel()) {
                    end = sortedSections.get(j).getByteOffset();
                }
            }

            // Check if all replacements are contained in the current section
            if (areAllReplacementsContainedInInterval(replacements, start, end)) {
                smallest = section;
            }
        }
        return Optional.ofNullable(smallest);
    }

    private boolean areAllReplacementsContainedInInterval(
        Collection<Replacement> replacements,
        int start,
        @Nullable Integer end
    ) {
        return replacements.stream().allMatch(rep -> isReplacementContainedInInterval(rep, start, end));
    }

    private boolean isReplacementContainedInInterval(Replacement replacement, int start, @Nullable Integer end) {
        if (replacement.start() >= start) {
            if (end == null) {
                return true;
            } else {
                return replacement.end() <= end;
            }
        } else {
            return false;
        }
    }

    private Collection<Replacement> translateReplacementsByOffset(
        Collection<Replacement> replacements,
        int sectionOffset
    ) {
        return replacements.stream().map(rep -> rep.withStart(rep.start() - sectionOffset)).toList();
    }

    private boolean validateTranslatedReplacements(Collection<Replacement> replacements, WikipediaPage pageSection) {
        return replacements.stream().allMatch(rep -> validateReplacement(rep, pageSection.getContent()));
    }

    private boolean validateReplacement(Replacement replacement, String text) {
        if (replacement.end() > text.length()) {
            return false;
        }
        return replacement.text().equals(text.substring(replacement.start(), replacement.end()));
    }
}
