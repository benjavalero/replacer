package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.PageReplacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaSection;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class PageReviewSectionFinder {

    @Autowired
    private WikipediaService wikipediaService;

    /**
     * Find (if any) the smallest section in a page containing the given replacements.
     *
     * In case such a section is found, then return a review containing the page fragment corresponding to the section
     * with the replacements translated accordingly.
     */
    Optional<PageReview> findPageReviewSection(PageReview review) {
        assert review.getSection() == null;

        try {
            // Get the sections from the Wikipedia API (better than calculating them by ourselves)
            Collection<WikipediaSection> sections = wikipediaService.getPageSections(review.getPage().getId());

            // Find the smallest section containing all the replacements
            Optional<WikipediaSection> smallestSection = getSmallestSectionContainingAllReplacements(
                sections,
                review.getReplacements()
            );
            if (smallestSection.isPresent()) {
                // Get the section from Wikipedia API (better than calculating it by ourselves)
                Optional<WikipediaPage> pageSection = wikipediaService.getPageSection(
                    review.getPage().getId(),
                    smallestSection.get()
                );
                if (pageSection.isPresent()) {
                    // We need to modify the start position of the replacements according to the section start
                    Collection<PageReplacement> sectionReplacements = translateReplacementsByOffset(
                        review.getReplacements(),
                        smallestSection.get().getByteOffset()
                    );

                    // We need to check some rare cases where the byte-offset doesn't match with the section position,
                    // usually because of emojis or other strange Unicode characters
                    validateTranslatedReplacements(sectionReplacements, pageSection.get());

                    LOGGER.debug(
                        "Found section for page {} - {} => {}",
                        pageSection.get().getId(),
                        pageSection.get().getTitle(),
                        smallestSection.get()
                    );

                    // Finally, we build the section review based on the page review.
                    PageReview sectionReview = PageReview.of(
                        pageSection.get(),
                        smallestSection.get(),
                        sectionReplacements,
                        review.getNumPending()
                    );
                    return Optional.of(sectionReview);
                }
            }
        } catch (WikipediaException e) {
            // No need to log the details as they are logged at the end of the method
            LOGGER.warn("Error finding section", e);
        }

        LOGGER.debug("No section found in page: {} - {}", review.getPage().getId(), review.getPage().getTitle());
        return Optional.empty();
    }

    private Optional<WikipediaSection> getSmallestSectionContainingAllReplacements(
        Collection<WikipediaSection> sections,
        Collection<PageReplacement> replacements
    ) {
        // We sort the collection of sections
        // In theory they are already sorted as returned from Wikipedia API,
        // so we use an ArrayList assuming there will not be actually sorting.
        List<WikipediaSection> sortedSections = new ArrayList<>(sections);
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
        Collection<PageReplacement> replacements,
        Integer start,
        @Nullable Integer end
    ) {
        return replacements.stream().allMatch(rep -> isReplacementContainedInInterval(rep, start, end));
    }

    private boolean isReplacementContainedInInterval(
        PageReplacement replacement,
        Integer start,
        @Nullable Integer end
    ) {
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

    private Collection<PageReplacement> translateReplacementsByOffset(
        Collection<PageReplacement> replacements,
        int sectionOffset
    ) {
        return replacements
            .stream()
            .map(rep -> rep.withStart(rep.getStart() - sectionOffset))
            .collect(Collectors.toUnmodifiableList());
    }

    private void validateTranslatedReplacements(Collection<PageReplacement> replacements, WikipediaPage pageSection)
        throws WikipediaException {
        if (replacements.stream().anyMatch(rep -> !validatePageReplacement(rep, pageSection.getContent()))) {
            LOGGER.warn("Not valid byte-offset in page section: {} - {}", pageSection.getId(), pageSection.getTitle());
            throw new WikipediaException("Not valid byte-offset in page section");
        }
    }

    private boolean validatePageReplacement(PageReplacement replacement, String text) {
        if (replacement.getEnd() > text.length()) {
            return false;
        }
        return replacement.getText().equals(text.substring(replacement.getStart(), replacement.getEnd()));
    }
}
