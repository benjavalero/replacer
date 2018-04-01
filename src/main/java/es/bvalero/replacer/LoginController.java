package es.bvalero.replacer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/*
 * I was having some issues with encoding when accessing the application just through the parent "/".
 * With this trick, these accesses are redirected to the expected index.html.
 */
@Controller
public class LoginController {

    @RequestMapping("/")
    public ModelAndView redirectToIndex(ModelMap model) {
        return new ModelAndView("redirect:/index.html", model);
    }

}
