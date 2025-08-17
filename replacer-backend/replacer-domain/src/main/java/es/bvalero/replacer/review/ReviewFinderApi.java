package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.PageTitle;
import java.util.Collection;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
interface ReviewFinderApi {
    /** Find a page/section review for the given search options (if any) */
    Optional<Review> findRandomPageReview(ReviewOptions options);

    /** This step can be called independently in case we already know the ID of the page to review */
    Optional<Review> findPageReview(PageKey pageKey, ReviewOptions options);

    /** Find the title of pages to be reviewed for the given search options */
    Collection<PageTitle> findPageTitlesToReviewByType(ReviewOptions options);
}
