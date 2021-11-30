package es.bvalero.replacer.dump;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@ApiModel(description = "Response DTO containing the status of the current (or the last) dump indexing")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
@Builder
class DumpIndexingStatus {

    @ApiModelProperty(value = "If the indexing is running", required = true, example = "false")
    @NonNull
    Boolean running;

    @ApiModelProperty(value = "Number of indexable pages read", example = "251934")
    @Nullable
    Long numPagesRead;

    @ApiModelProperty(value = "Number of indexable pages indexed", example = "5016")
    @Nullable
    Long numPagesIndexed;

    @ApiModelProperty(value = "Estimated number of indexable pages", example = "249805")
    @Nullable
    Long numPagesEstimated;

    @ApiModelProperty(value = "Filename of the indexed dump", example = "glwiki-20210320-pages-articles.xml.bz2")
    @Nullable
    String dumpFileName;

    @ApiModelProperty(value = "Indexing start time (in ms)", example = "1616315965367")
    @Nullable
    @JsonSerialize(using = DumpLocalDateTimeSerializer.class)
    LocalDateTime start;

    @ApiModelProperty(value = "Indexing end time (in ms)", example = "1616317331756")
    @Nullable
    @JsonSerialize(using = DumpLocalDateTimeSerializer.class)
    LocalDateTime end;

    static DumpIndexingStatus ofEmpty() {
        return DumpIndexingStatus.builder().running(false).build();
    }
}
