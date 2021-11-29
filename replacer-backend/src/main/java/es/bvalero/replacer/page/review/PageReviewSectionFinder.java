package es.bvalero.replacer.page.review;

import es.bvalero.replacer.common.domain.Replacement;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaSection;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.page.PageReview;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PageReviewSectionFinder {

    // TODO: Public while refactoring

    @Autowired
    private WikipediaService wikipediaService;

    /**
     * Find (if any) the smallest section in a page containing the given replacements.
     *
     * In case such a section is found, then return a review containing the page fragment corresponding to the section
     * with the replacements translated accordingly.
     */
    // TODO: Public while refactoring
    public Optional<PageReview> findPageReviewSection(
        PageReview review,
        WikipediaPage page,
        Collection<Replacement> replacements
    ) {
        assert review.getSection() == null;

        try {
            // Get the sections from the Wikipedia API (better than calculating them by ourselves)
            List<WikipediaSection> sections = wikipediaService.getPageSections(page.getId());

            // Find the smallest section containing all the replacements
            Optional<WikipediaSection> smallestSection = getSmallestSectionContainingAllReplacements(
                sections,
                replacements
            );
            if (smallestSection.isPresent()) {
                // Get the section from Wikipedia API (better than calculating it by ourselves)
                Optional<WikipediaPage> pageSection = wikipediaService.getPageSection(
                    page.getId(),
                    smallestSection.get()
                );
                if (pageSection.isPresent()) {
                    // We need to modify the start position of the replacements according to the section start
                    Collection<Replacement> sectionReplacements = translateReplacementsByOffset(
                        replacements,
                        smallestSection.get().getByteOffset(),
                        pageSection.get()
                    );

                    LOGGER.debug(
                        "Found section for page {} - {} => {}",
                        pageSection.get().getId(),
                        pageSection.get().getTitle(),
                        smallestSection.get()
                    );
                    return Optional.of(
                        buildPageReview(pageSection.get(), smallestSection.get(), sectionReplacements, review)
                    );
                }
            }
        } catch (ReplacerException e) {
            // No need to log the details as they are logged at the end of the method
            LOGGER.warn("Error finding section", e);
        }

        LOGGER.debug("No section found in page: {} - {}", page.getId(), page.getTitle());
        return Optional.empty();
    }

    private Optional<WikipediaSection> getSmallestSectionContainingAllReplacements(
        List<WikipediaSection> sections,
        Collection<Replacement> replacements
    ) {
        Collections.sort(sections); // Although in theory they are already sorted as returned from Wikipedia API
        WikipediaSection smallest = null;
        for (int i = 0; i < sections.size(); i++) {
            WikipediaSection section = sections.get(i);
            int start = section.getByteOffset();
            Integer end = null;
            for (int j = i + 1; j < sections.size() && end == null; j++) {
                if (sections.get(j).getLevel() <= section.getLevel()) {
                    end = sections.get(j).getByteOffset();
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
        Integer start,
        @Nullable Integer end
    ) {
        return replacements.stream().allMatch(rep -> isReplacementContainedInInterval(rep, start, end));
    }

    private boolean isReplacementContainedInInterval(Replacement replacement, Integer start, @Nullable Integer end) {
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
        int sectionOffset,
        WikipediaPage pageSection
    ) throws ReplacerException {
        List<Replacement> translated = replacements
            .stream()
            .map(rep -> rep.withStart(rep.getStart() - sectionOffset))
            .collect(Collectors.toUnmodifiableList());

        // We need to check some rare cases where the byte-offset doesn't match with the section position,
        // usually because of emojis or other strange Unicode characters
        if (translated.stream().anyMatch(rep -> !validatePageReplacement(rep, pageSection.getContent()))) {
            LOGGER.warn(
                "Not valid byte-offset in page section: {} - {} - {}",
                pageSection.getId(),
                pageSection.getTitle(),
                sectionOffset
            );
            throw new ReplacerException("Not valid byte-offset in page section");
        }

        return translated;
    }

    private boolean validatePageReplacement(Replacement replacement, String text) {
        if (replacement.getEnd() > text.length()) {
            return false;
        }
        return replacement.getText().equals(text.substring(replacement.getStart(), replacement.getEnd()));
    }

    private PageReview buildPageReview(
        WikipediaPage page,
        @Nullable WikipediaSection section,
        Collection<Replacement> replacements,
        PageReview pageReview
    ) {
        return PageReview.of(page, section, replacements, pageReview.getSearch());
    }
}
