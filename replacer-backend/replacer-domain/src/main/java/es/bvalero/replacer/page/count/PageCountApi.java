package es.bvalero.replacer.page.count;

import es.bvalero.replacer.common.domain.ResultCount;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.user.User;
import java.util.Collection;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
interface PageCountApi {
    /** Count the number of pages to review grouped by replacement type */
    Collection<ResultCount<StandardType>> countNotReviewedGroupedByType(User user);
}
