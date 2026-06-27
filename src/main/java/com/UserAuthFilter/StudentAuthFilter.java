package com.UserAuthFilter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebFilter({"/student_page.xhtml", "/student/*"})
public class StudentAuthFilter implements Filter {

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

        if (!studentLoggedIn) {
            setNoCacheHeaders(httpResponse);
            httpResponse.sendRedirect(
                    httpRequest.getContextPath() + "/student_login.xhtml"
            );
            return;
        }

        setSecurityHeaders(httpResponse);
        chain.doFilter(request, response);
    }

    private void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private void setSecurityHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
    }
}