package es.bvalero.replacer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * In the frontend all internal redirects are managed by Angular.
 * However if we put directly an URL in the browser the request is handled by SpringBoot.
 * Using this trick we can redirect all requests to Angular.
 * This is checked after the other controllers so the REST controllers are not broken.
 */
@Controller
public class ForwardController {

    // https://spring.io/blog/2015/05/13/modularizing-the-client-angular-js-and-spring-security-part-vii
    @GetMapping(value = "/**/{[path:[^\\.]*}")
    public String redirect() {
        return "forward:/";
    }

}
