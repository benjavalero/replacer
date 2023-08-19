package es.bvalero.replacer.dump;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import com.fasterxml.jackson.annotation.JsonInclude;
import es.bvalero.replacer.common.util.ReplacerUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.Nullable;

@Schema(description = "Status of the current (or the last) dump indexing", name = "DumpStatus")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
@Builder
public class DumpStatusDto {

    // Even when it is quite similar to the domain object
    // we prefer to create this copy without the logic and with the different response types

    @Schema(description = "If the indexing is running", requiredMode = REQUIRED, example = "false")
    boolean running;

    @Schema(description = "Number of indexable pages read", requiredMode = REQUIRED, example = "251934")
    int numPagesRead;

    @Schema(description = "Number of indexable pages indexed", requiredMode = REQUIRED, example = "5016")
    int numPagesIndexed;

    @Schema(description = "Estimated number of indexable pages", requiredMode = REQUIRED, example = "249805")
    int numPagesEstimated;

    @Schema(
        description = "Filename of the indexed dump",
        requiredMode = REQUIRED,
        example = "glwiki-20210320-pages-articles.xml.bz2"
    )
    String dumpFileName;

    @Schema(
        description = "Indexing start time (in ms)",
        type = "number",
        requiredMode = REQUIRED,
        example = "1616315965367"
    )
    long start;

    @Schema(description = "Indexing end time (in ms)", type = "number", example = "1616317331756")
    @Nullable
    Long end;

    @Override
    public String toString() {
        return ReplacerUtils.toJson(this);
    }
}
