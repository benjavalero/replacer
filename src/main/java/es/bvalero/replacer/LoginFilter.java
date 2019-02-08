package es.bvalero.replacer;

import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoginFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFilter.class);
    private static final String LOGIN_PAGE = "/login.html";

    @Autowired
    private IWikipediaFacade wikipediaFacade;

    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.debug("Initializing filter :{}", this);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        LOGGER.debug("Logging Request  {} : {} : {}", req.getMethod(), req.getRequestURI(), req.getRequestURL());

        // Avoid infinite loop redirecting to login page
        if (!wikipediaFacade.isAuthenticated(req) && isUriFilterable(req.getRequestURI())) {
            req.getSession().setAttribute(LoginController.REDIRECT_URL, req.getRequestURL());
            res.sendRedirect(LOGIN_PAGE);
        }

        chain.doFilter(request, response);
    }

    private boolean isUriFilterable(String uri) {
        return uri.endsWith(".html") && !uri.equals(LOGIN_PAGE);
    }

    @Override
    public void destroy() {
        LOGGER.warn("Destructing filter :{}", this);
    }

}
