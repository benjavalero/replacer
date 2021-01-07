package es.bvalero.replacer.page;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaSection;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
class SectionReviewService {

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ModelMapper modelMapper;

    /**
     * @param review The review of the complete page.
     * @return The review of a section of the page, or empty if there is no such section.
     */
    Optional<PageReview> findSectionReview(PageReview review) {
        assert review.getSection() == null;

        // Get the sections from the Wikipedia API (better than calculating them by ourselves)
        try {
            List<WikipediaSection> sections = new ArrayList<>(
                wikipediaService.getPageSections(review.getId(), review.getLang())
            );

            // Find the smallest section containing all the replacements
            Optional<WikipediaSection> smallestSection = getSmallestSectionContainingAllReplacements(
                sections,
                review.getReplacements()
            );
            if (smallestSection.isPresent()) {
                // Retrieve the section from Wikipedia API. Better than calculating it by ourselves, just in case.
                Optional<WikipediaPage> pageSection = wikipediaService.getPageByIdAndSection(
                    review.getId(),
                    smallestSection.get(),
                    review.getLang()
                );
                if (pageSection.isPresent()) {
                    // Modify the start position of the replacements according to the section start
                    List<PageReplacement> sectionReplacements = translateReplacementsByOffset(
                        review.getReplacements(),
                        smallestSection.get().getByteOffset()
                    );
                    // We need to check some rare cases where the byte-offset doesn't match with the section position
                    if (
                        sectionReplacements
                            .stream()
                            .allMatch(rep -> validatePageReplacement(rep, pageSection.get().getContent()))
                    ) {
                        LOGGER.debug(
                            "Found section for page {} - {} - {} => {} - {}",
                            pageSection.get().getLang(),
                            pageSection.get().getId(),
                            pageSection.get().getTitle(),
                            pageSection.get().getSection(),
                            pageSection.get().getAnchor()
                        );
                        return Optional.of(
                            buildPageReview(
                                pageSection.get(),
                                translateReplacementsByOffset(
                                    review.getReplacements(),
                                    smallestSection.get().getByteOffset()
                                ),
                                review
                            )
                        );
                    } else {
                        LOGGER.warn(
                            "Not valid byte-offset in page section: {} - {} - {} - {} - {}",
                            pageSection.get().getLang(),
                            pageSection.get().getId(),
                            pageSection.get().getTitle(),
                            smallestSection.get().getAnchor(),
                            smallestSection.get().getByteOffset()
                        );
                    }
                }
            }
        } catch (ReplacerException e) {
            LOGGER.error(
                "Error finding section in page {} - {} - {}",
                review.getLang(),
                review.getId(),
                review.getTitle(),
                e
            );
        }

        LOGGER.debug("No section found in page: {} - {} - {}", review.getLang(), review.getId(), review.getTitle());
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

    private List<PageReplacement> translateReplacementsByOffset(List<PageReplacement> replacements, int offset) {
        return replacements.stream().map(rep -> rep.withStart(rep.getStart() - offset)).collect(Collectors.toList());
    }

    private boolean validatePageReplacement(PageReplacement replacement, String text) {
        if (replacement.getEnd() > text.length()) {
            return false;
        }
        return replacement.getText().equals(text.substring(replacement.getStart(), replacement.getEnd()));
    }

    private PageReview buildPageReview(WikipediaPage page, List<PageReplacement> replacements, PageReview pageReview) {
        PageReview review = modelMapper.map(page, PageReview.class);
        review.setReplacements(replacements);
        review.setNumPending(pageReview.getNumPending());
        return review;
    }
}
