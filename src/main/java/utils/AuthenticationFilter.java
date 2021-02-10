package utils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthenticationFilter implements Filter {

    private ServletContext context;

    public void init(FilterConfig fConfig) throws ServletException {
        System.out.println("Auth filter initialized!");
        this.context = fConfig.getServletContext();
        this.context.log("demo.AuthenticationFilter initialized");
    }

    /*
     * This method filters any attempts at files under /content directory
     * checks if session is null or if current session user is incorrect.
     * if everything is correct, redirects to content/BaggageDemo.html
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);

        if ((session == null) ||
                (req.getSession(false).getAttribute("user") != Login.DEMO_USER_NAME))
        {
            this.context.log("Unauthorized access request");
            res.sendRedirect(req.getContextPath() + "/index.jsp");
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    public void destroy()
    {
        //close any resources here
    }
}
