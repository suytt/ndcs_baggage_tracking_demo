package utils;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;


/**
 *  Servlet for authenticating to the demo application
 */
//@WebServlet(urlPatterns = {"/login"})
public class Login extends HttpServlet {
    public static final String DEMO_USER_NAME = "ndcsDemoUser";

  /**
   * demo.Login method
   * if username and password are correct, invalidate any old session and
   * and create new session to last 5 mins, then redirect to content/BaggageDemo.html
   * if incorrect, go back to login page.
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if(username.equals(DEMO_USER_NAME) && password.equals("nosqlOracle!!"))
        {
            //get the old session and invalidate
            HttpSession oldSession = request.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            //generate a new session
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("user", DEMO_USER_NAME);

            //setting session to expiry in 5 mins
            newSession.setMaxInactiveInterval(5*60);

            RequestDispatcher rs = request.getRequestDispatcher("/content/BaggageDemo.html");
            rs.forward(request, response);
        }
        else
        {
           System.out.println("Now login session, redirecting to /index.jsp");
           String message = "Username or password incorrect.";
           request.setAttribute("message", message);
           RequestDispatcher rs = request.getRequestDispatcher("/index.jsp");
           rs.include(request, response);
        }
    }
}
