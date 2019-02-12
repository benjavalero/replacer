package es.bvalero.replacer.authentication;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuth1RequestToken;
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

    private static final String TOKEN_VERIFIER = "oauth_verifier";

    @Autowired
    private AuthenticationService authenticationService;

    @GetMapping(value = "/")
    public ModelAndView redirectToIndex(HttpServletRequest request)
            throws InterruptedException, ExecutionException, IOException {
        // Check if we come from the authorization page
        String oauthVerifier = request.getParameter(TOKEN_VERIFIER);
        OAuth1RequestToken requestToken = authenticationService.getRequestTokenInSession();

        if (oauthVerifier != null && requestToken != null) {
            // Trade the Request Token and Verify for the Access Token
            OAuth1AccessToken accessToken = authenticationService.getAccessToken(requestToken, oauthVerifier);
            authenticationService.setAccessTokenInSession(accessToken);
            authenticationService.removeRequestTokenInSession();

            // Redirect to the previous URL
            String redirectUrl = authenticationService.getRedirectUrlInSession();
            if (redirectUrl != null) {
                authenticationService.removeRedirectUrlInSession();
                return new ModelAndView("redirect:" + redirectUrl);
            }
        }

        return new ModelAndView("redirect:/index.html");
    }

    @GetMapping(value = "/authenticate")
    public ModelAndView authenticate(HttpSession session)
            throws InterruptedException, ExecutionException, IOException {
        // Obtain the Request Token
        OAuth1RequestToken requestToken = authenticationService.getRequestToken();

        // Go to authorization page
        // Add the request token to the session to use it when getting back
        authenticationService.setRequestTokenInSession(requestToken);
        return new ModelAndView("redirect:" + authenticationService.getAuthorizationUrl(requestToken));
    }

}
