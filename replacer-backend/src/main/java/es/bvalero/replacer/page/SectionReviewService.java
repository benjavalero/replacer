package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.WikipediaPageSection;
import es.bvalero.replacer.common.domain.WikipediaSection;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaService;
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
class SectionReviewService {

    @Autowired
    private WikipediaService wikipediaService;

    /**
     * @return The review of a section of the page, or empty if there is no such section.
     */
    Optional<PageReview> findSectionReview(PageReview review) {
        assert review.getPage().getSection() == null;

        try {
            // Get the sections from the Wikipedia API (better than calculating them by ourselves)
            List<WikipediaSection> sections = wikipediaService.getPageSections(
                review.getPage().getLang(),
                review.getPage().getId()
            );

            // Find the smallest section containing all the replacements
            Optional<WikipediaSection> smallestSection = getSmallestSectionContainingAllReplacements(
                sections,
                review.getReplacements()
            );
            if (smallestSection.isPresent()) {
                // Get the section from Wikipedia API (better than calculating it by ourselves)
                Optional<WikipediaPageSection> pageSection = wikipediaService.getPageSection(
                    review.getPage().getLang(),
                    review.getPage().getId(),
                    smallestSection.get()
                );
                if (pageSection.isPresent()) {
                    // We need to modify the start position of the replacements according to the section start
                    List<PageReplacement> sectionReplacements = translateReplacementsByOffset(
                        review.getReplacements(),
                        smallestSection.get().getByteOffset(),
                        pageSection.get()
                    );

                    LOGGER.debug(
                        "Found section for page {} - {} => {}",
                        pageSection.get().getId(),
                        pageSection.get().getTitle(),
                        pageSection.get().getSection()
                    );
                    return Optional.of(buildPageReview(pageSection.get(), sectionReplacements, review));
                }
            }
        } catch (ReplacerException e) {
            // No need to log the details as they are logged at the end of the method
            LOGGER.warn("Error finding section", e);
        }

        LOGGER.debug(
            "No section found in page: {} - {} - {}",
            review.getPage().getLang(),
            review.getPage().getId(),
            review.getPage().getTitle()
        );
        return Optional.empty();
    }

    private Optional<WikipediaSection> getSmallestSectionContainingAllReplacements(
        List<WikipediaSection> sections,
        List<PageReplacement> replacements
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
        List<PageReplacement> replacements,
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

    private List<PageReplacement> translateReplacementsByOffset(
        List<PageReplacement> replacements,
        int sectionOffset,
        WikipediaPageSection pageSection
    ) throws ReplacerException {
        List<PageReplacement> translated = replacements
            .stream()
            .map(rep -> rep.withStart(rep.getStart() - sectionOffset))
            .collect(Collectors.toList());

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

    private boolean validatePageReplacement(PageReplacement replacement, String text) {
        if (replacement.getEnd() > text.length()) {
            return false;
        }
        return replacement.getText().equals(text.substring(replacement.getStart(), replacement.getEnd()));
    }

    private PageReview buildPageReview(
        WikipediaPageSection page,
        List<PageReplacement> replacements,
        PageReview pageReview
    ) {
        return PageReview.of(page, replacements, pageReview.getSearch());
    }
}
