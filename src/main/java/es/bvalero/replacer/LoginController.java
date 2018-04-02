package es.bvalero.replacer;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class LoginController {

    @Autowired
    private IWikipediaFacade wikipediaFacade;

    @RequestMapping("/isAuthenticated")
    public boolean isAuthenticated(HttpSession session) {
        // Load the OAuth service especially for offline testing
        wikipediaFacade.getOAuthService();
        return session.getAttribute("accessToken") != null;
    }

    @RequestMapping("/login")
    public ModelAndView login(HttpSession session) throws InterruptedException, ExecutionException, IOException {
        // TODO Controlar excepciones por ejemplo que no haya acceso a Internet
        // Obtain the Request Token
        OAuth1RequestToken requestToken = wikipediaFacade.getOAuthService().getRequestToken();

        // Go to authorization page
        // Add the request token to the session to use it when getting back
        session.setAttribute("requestToken", requestToken);
        return new ModelAndView("redirect:" + wikipediaFacade.getOAuthService().getAuthorizationUrl(requestToken));
    }

    @RequestMapping("/")
    public ModelAndView checkLogin(HttpServletRequest request) throws InterruptedException, ExecutionException, IOException {
        // Check if we come from the authorization page
        String oauthVerifier = request.getParameter("oauth_verifier");
        Object requestToken = request.getSession().getAttribute("requestToken");

        if (oauthVerifier != null && requestToken != null) {
            // Trade the Request Token and Verify for the Access Token
            OAuth1AccessToken accessToken = wikipediaFacade.getOAuthService()
                    .getAccessToken((OAuth1RequestToken) requestToken, oauthVerifier);

            request.getSession().setAttribute("accessToken", accessToken);
            request.getSession().removeAttribute("requestToken");
        }

        return new ModelAndView("redirect:/index.html");
    }

}
