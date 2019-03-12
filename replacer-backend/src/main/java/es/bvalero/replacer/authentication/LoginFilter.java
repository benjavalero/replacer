package es.bvalero.replacer.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class LoginFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        LOGGER.debug("Initializing filter :{}", this);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        LOGGER.debug("Logging Request  {} : {} : {}", req.getMethod(), req.getRequestURI(), req.getRequestURL());

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOGGER.warn("Destructing filter :{}", this);
    }

}
