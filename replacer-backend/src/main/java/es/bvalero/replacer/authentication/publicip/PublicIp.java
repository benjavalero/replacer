package es.bvalero.replacer.authentication.publicip;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

@Schema
@Value(staticConstructor = "of")
public class PublicIp {

    @Schema(required = true)
    String ip;
}
