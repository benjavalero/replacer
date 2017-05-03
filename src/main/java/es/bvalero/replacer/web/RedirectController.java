package es.bvalero.replacer.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class RedirectController {

    @RequestMapping("/")
    public ModelAndView redirectToIndex(ModelMap model) {
        return new ModelAndView("redirect:/index.html", model);
    }

}
