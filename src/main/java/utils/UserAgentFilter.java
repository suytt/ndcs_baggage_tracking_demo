package utils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UserAgentFilter implements Filter {

    private ServletContext context;

    private static final String DEVICE_MOBILE = "Mobile".toLowerCase();
    private static final String DEVICE_NAME_ANDROID = "Android".toLowerCase();
    private static final String DEVICE_NAME_WEB_OS = "webOS".toLowerCase();
    private static final String DEVICE_NAME_IPHONE = "iPhone".toLowerCase();
    private static final String DEVICE_NAME_IPAD = "iPad".toLowerCase();
    private static final String DEVICE_NAME_IPOD = "iPod".toLowerCase();
    private static final String DEVICE_NAME_BLACKBERRY = "BlackBerry".toLowerCase();
    private static final String DEVICE_NAME_IEMOBILE = "IEMobile".toLowerCase();
    private static final String DEVICE_NME_OPERA_MINI = "Opera Mini".toLowerCase();

    public void init(FilterConfig fConfig) throws ServletException {
        System.out.println("User Agent filter initialization!");
        this.context = fConfig.getServletContext();
        this.context.log("demo.UserAgentFilter initialized");
    }

    private static boolean isMobileUAString(String uaString) {
        String userAgent = uaString.toLowerCase();
        System.out.println("User Agent = " + userAgent);
        return(userAgent.contains(DEVICE_NAME_ANDROID) ||
                userAgent.contains(DEVICE_NAME_WEB_OS) ||
                userAgent.contains(DEVICE_NAME_IPHONE) ||
                userAgent.contains(DEVICE_NAME_IPAD) ||
                userAgent.contains(DEVICE_NAME_IPOD) ||
                userAgent.contains(DEVICE_NAME_BLACKBERRY) ||
                userAgent.contains(DEVICE_NAME_IEMOBILE) ||
                userAgent.contains(DEVICE_NME_OPERA_MINI) ||
                userAgent.contains(DEVICE_MOBILE));
    }

    /*
     * This method filters
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        System.out.println("Context path = " + req.getContextPath());
        System.out.println("Request URL = " + req.getRequestURL());
        if (isMobileUAString(req.getHeader("User-Agent"))) {
            System.out.println("Mobile caller detected!");
            System.out.println("Request from mobile device, sending redirect to /demo/mobileApps/baggagedemocustomer");
            res.sendRedirect(req.getContextPath() + "/demo/mobileApps/baggagedemocustomer");
        }
    }

    public void destroy()
    {
        //close any resources here
    }
}

