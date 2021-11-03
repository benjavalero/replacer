package es.bvalero.replacer.dump;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Value;
import org.springframework.lang.Nullable;

@ApiModel(description = "Status of the current (or the last) dump indexing")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
@Builder
class DumpIndexingStatus {

    @ApiModelProperty(value = "If the indexing is running", required = true, example = "false")
    boolean running;

    @ApiModelProperty(value = "Number of processable pages read", example = "251934")
    @Nullable
    Long numPagesRead;

    @ApiModelProperty(value = "Number of processable pages processed", example = "5016")
    @Nullable
    Long numPagesProcessed;

    @ApiModelProperty(value = "Estimated number of processable pages", example = "249805")
    @Nullable
    Long numPagesEstimated;

    @ApiModelProperty(value = "Filename of the indexed dump", example = "glwiki-20210320-pages-articles.xml.bz2")
    @Nullable
    String dumpFileName;

    @ApiModelProperty(value = "Indexing start time (in ms)", example = "1616315965367")
    @Nullable
    Long start;

    @ApiModelProperty(value = "Indexing end time (in ms)", example = "1616317331756")
    @Nullable
    Long end;

    static DumpIndexingStatus ofEmpty() {
        return DumpIndexingStatus.builder().build();
    }
}
