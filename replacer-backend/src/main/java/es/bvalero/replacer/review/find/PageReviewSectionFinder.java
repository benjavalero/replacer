package es.bvalero.replacer.review.find;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaSection;
import es.bvalero.replacer.wikipedia.WikipediaPageRepository;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class PageReviewSectionFinder {

    @Autowired
    private WikipediaPageRepository wikipediaPageRepository;

    /**
     * Find (if any) the smallest section in a page containing the given replacements.
     *
     * In case such a section is found, then return a review containing the page fragment corresponding to the section
     * with the replacements translated accordingly.
     */
    Optional<Review> findPageReviewSection(Review review) {
        assert review.getSection() == null;

        // Find the sections from the Wikipedia API (better than calculating them by ourselves)
        Collection<WikipediaSection> sections = wikipediaPageRepository.findSectionsInPage(review.getPage().getId());

        // Find the smallest section containing all the replacements
        Optional<WikipediaSection> smallestSection = getSmallestSectionContainingAllReplacements(
            sections,
            review.getReplacements()
        );
        if (smallestSection.isPresent()) {
            // Find the section from Wikipedia API (better than calculating it by ourselves)
            Optional<WikipediaPage> pageSection = wikipediaPageRepository.findPageSection(
                review.getPage().getId(),
                smallestSection.get()
            );
            if (pageSection.isPresent()) {
                // We need to modify the start position of the replacements according to the section start
                Collection<Replacement> sectionReplacements = translateReplacementsByOffset(
                    review.getReplacements(),
                    smallestSection.get().getByteOffset()
                );

                // We need to check some rare cases where the byte-offset doesn't match with the section position,
                // usually because of emojis or other strange Unicode characters
                if (!validateTranslatedReplacements(sectionReplacements, pageSection.get())) {
                    LOGGER.info(
                        "Not valid byte-offset in page section: {} - {}",
                        pageSection.get().getId(),
                        pageSection.get().getTitle()
                    );
                    return Optional.empty();
                }

                LOGGER.debug(
                    "Found section for page {} - {} => {}",
                    pageSection.get().getId(),
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

        LOGGER.debug("No section found in page: {} - {}", review.getPage().getId(), review.getPage().getTitle());
        return Optional.empty();
    }

    private Optional<WikipediaSection> getSmallestSectionContainingAllReplacements(
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
        if (replacement.getStart() >= start) {
            if (end == null) {
                return true;
            } else {
                return replacement.getEnd() <= end;
            }
        } else {
            return false;
        }
    }

    private Collection<Replacement> translateReplacementsByOffset(
        Collection<Replacement> replacements,
        int sectionOffset
    ) {
        return replacements
            .stream()
            .map(rep -> rep.withStart(rep.getStart() - sectionOffset))
            .collect(Collectors.toUnmodifiableList());
    }

    private boolean validateTranslatedReplacements(Collection<Replacement> replacements, WikipediaPage pageSection) {
        return replacements.stream().allMatch(rep -> validateReplacement(rep, pageSection.getContent()));
    }

    private boolean validateReplacement(Replacement replacement, String text) {
        if (replacement.getEnd() > text.length()) {
            return false;
        }
        return replacement.getText().equals(text.substring(replacement.getStart(), replacement.getEnd()));
    }
}