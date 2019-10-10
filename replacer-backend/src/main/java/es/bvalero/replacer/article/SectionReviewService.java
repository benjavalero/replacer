package es.bvalero.replacer.article;

import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaSection;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
class SectionReviewService {

    @Autowired
    private WikipediaService wikipediaService;

    /**
     * @param review The review of the complete article.
     * @return The review of a section of the article, or empty if there is no such section.
     */
    Optional<ArticleReview> findSectionReview(ArticleReview review) {
        if (review.getSection() != null) {
            throw new IllegalArgumentException("The article review already contains a section");
        }

        // Get the sections from the Wikipedia API (better than calculating them by ourselves)
        try {
            List<WikipediaSection> sections = new ArrayList<>(wikipediaService.getPageSections(review.getId()));

            // Find the smallest section containing all the replacements
            Optional<WikipediaSection> smallestSection =
                    getSmallestSectionContainingAllReplacements(sections, review.getReplacements());
            if (smallestSection.isPresent()) {
                // Retrieve the section from Wikipedia API. Better than calculating it by ourselves, just in case.
                Optional<WikipediaPage> pageSection = wikipediaService.getPageByIdAndSection(review.getId(), smallestSection.get().getIndex());
                if (pageSection.isPresent()) {
                    // Modify the start position of the replacements according to the section start
                    List<ArticleReplacement> sectionReplacements =
                            translateReplacementsByOffset(review.getReplacements(), smallestSection.get().getByteOffset());
                    // We need to check some rare cases where the byte-offset doesn't match with the section position
                    if (sectionReplacements.stream().allMatch(rep -> validateArticleReplacement(rep, pageSection.get().getContent()))) {
                        return Optional.of(review
                                .withSection(pageSection.get().getSection())
                                .withReplacements(translateReplacementsByOffset(review.getReplacements(), smallestSection.get().getByteOffset())));
                    } else {
                        LOGGER.warn("Not valid byte-offset in section {} of article: {}",
                                smallestSection.get().getIndex(), pageSection.get().getTitle());
                    }
                }
            }
        } catch (WikipediaException e) {
            LOGGER.error("Error getting section review", e);
        }

        return Optional.empty();
    }

    private Optional<WikipediaSection> getSmallestSectionContainingAllReplacements(
            List<WikipediaSection> sections, List<ArticleReplacement> replacements) {
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
            List<ArticleReplacement> replacements, Integer start, @Nullable Integer end) {
        return replacements.stream().allMatch(rep -> isReplacementContainedInInterval(rep, start, end));
    }

    private boolean isReplacementContainedInInterval(
            ArticleReplacement replacement, Integer start, @Nullable Integer end) {
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

    private List<ArticleReplacement> translateReplacementsByOffset(List<ArticleReplacement> replacements, int offset) {
        return replacements.stream().map(rep -> rep.withStart(rep.getStart() - offset)).collect(Collectors.toList());
    }

    private boolean validateArticleReplacement(ArticleReplacement replacement, String text) {
        return replacement.getText().equals(text.substring(replacement.getStart(), replacement.getEnd()));
    }

}
