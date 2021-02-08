package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.finder.common.FinderResult;
import lombok.Builder;
import lombok.Value;

/**
 * A <strong>cosmetic</strong> is a special type of replacement which can be applied automatically,
 * concerning cosmetic modifications, visible or not, e.g. replacing <code>[[Asia|Asia]]</code>
 * by <code>[[Asia]]</code>.
 */
@Value
@Builder
class Cosmetic implements FinderResult {

    int start;
    String text;
    String fix;
    CosmeticFinder finder;
}
