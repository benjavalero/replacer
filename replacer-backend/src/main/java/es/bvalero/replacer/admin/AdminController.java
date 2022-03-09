package es.bvalero.replacer.admin;

import com.github.rozidan.springboot.logger.Loggable;
import es.bvalero.replacer.common.dto.CommonQueryParameters;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.user.validate.ValidateAdminUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/** REST controller to perform administration operations */
@Tag(name = "Administration")
@Loggable
@RestController
@RequestMapping("api/admin")
public class AdminController {

    @Autowired
    private PublicIpService publicIpService;

    @Operation(summary = "Find the public IP of the tool")
    @ValidateAdminUser
    @GetMapping(value = "/public-ip")
    public PublicIp getPublicIp(@Valid CommonQueryParameters queryParameters) throws ReplacerException {
        return publicIpService.getPublicIp();
    }
}
