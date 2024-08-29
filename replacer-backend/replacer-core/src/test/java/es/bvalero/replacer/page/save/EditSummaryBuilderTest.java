package es.bvalero.replacer.page.save;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.bvalero.replacer.common.domain.CustomType;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.StandardType;
import java.util.List;
import org.junit.jupiter.api.Test;

class EditSummaryBuilderTest {

    @Test
    void testBuildEditSummary() {
        ReplacementType r1 = StandardType.of(ReplacementKind.SIMPLE, "1");
        ReplacementType r2 = StandardType.of(ReplacementKind.COMPOSED, "2");
        ReplacementType r3 = CustomType.of("3", false);
        ReplacementType r4 = StandardType.DATE;
        List<ReplacementType> fixedReplacementTypes = List.of(r1, r2, r3, r4);

        String summary = EditSummaryBuilder.build(fixedReplacementTypes, false);
        assertTrue(summary.contains("«1»"));
        assertTrue(summary.contains("«2»"));
        assertTrue(summary.contains("«3»"));
        assertFalse(summary.contains("«Fechas»"));
        assertTrue(summary.contains("Fechas"));
    }
}
