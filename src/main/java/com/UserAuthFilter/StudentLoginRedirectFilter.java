package com.UserAuthFilter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebFilter("/student_login.xhtml")
public class StudentLoginRedirectFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession         session      = httpRequest.getSession(false);

        boolean studentLoggedIn = session != null
                && Boolean.TRUE.equals(session.getAttribute("studentLoggedIn"))
                && session.getAttribute("loggedInStudentId") != null
                && "STUDENT".equals(session.getAttribute("userRole"));

        if (studentLoggedIn) {
            httpResponse.sendRedirect(
                    httpRequest.getContextPath() + "/student_page.xhtml"
            );
            return;
        }

        chain.doFilter(request, response);
    }
}