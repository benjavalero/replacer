package es.bvalero.replacer;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class LoginController {

    static final String REDIRECT_URL = "redirectUrl";
    private static final String TOKEN_REQUEST = "requestToken";
    private static final String TOKEN_VERIFIER = "oauth_verifier";

    @Autowired
    private IWikipediaFacade wikipediaFacade;

    @GetMapping(value = "/")
    public ModelAndView redirectToIndex(HttpServletRequest request)
            throws InterruptedException, ExecutionException, IOException {
        // Check if we come from the authorization page
        String oauthVerifier = request.getParameter(TOKEN_VERIFIER);
        Object requestToken = request.getSession().getAttribute(TOKEN_REQUEST);

        if (oauthVerifier != null && requestToken != null) {
            // Trade the Request Token and Verify for the Access Token
            OAuth1AccessToken accessToken = wikipediaFacade.getOAuthService()
                    .getAccessToken((OAuth1RequestToken) requestToken, oauthVerifier);

            request.getSession().setAttribute(IWikipediaFacade.TOKEN_ACCESS, accessToken);
            request.getSession().removeAttribute(TOKEN_REQUEST);

            // Redirect to the previous URL
            Object redirectUrl = request.getSession().getAttribute(REDIRECT_URL);
            if (redirectUrl != null) {
                request.getSession().removeAttribute(REDIRECT_URL);
                return new ModelAndView("redirect:" + redirectUrl.toString());
            }
        }

        return new ModelAndView("redirect:/index.html");
    }

    @GetMapping(value = "/authenticate")
    public ModelAndView authenticate(HttpSession session) throws
            InterruptedException, ExecutionException, IOException {
        // Obtain the Request Token
        OAuth1RequestToken requestToken = wikipediaFacade.getOAuthService().getRequestToken();

        // Go to authorization page
        // Add the request token to the session to use it when getting back
        session.setAttribute(TOKEN_REQUEST, requestToken);
        return new ModelAndView("redirect:" + wikipediaFacade.getOAuthService().getAuthorizationUrl(requestToken));
    }

}
