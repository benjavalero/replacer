package es.bvalero.replacer.review;

import es.bvalero.replacer.common.domain.PageKey;
import java.util.Optional;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
interface ReviewFinderApi {
    /** Find a page/section review for the given search options (if any) */
    Optional<Review> findRandomPageReview(ReviewOptions options);

    /** This step can be called independently in case we already know the ID of the page to review */
    Optional<Review> findPageReview(PageKey pageKey, ReviewOptions options);
}
