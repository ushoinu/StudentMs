package com.UserAuthFilter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebFilter({"/admin_page.xhtml", "/studentRegistration.xhtml","/studentSubjectList.xhtml",
        "/subjectEntry.xhtml","/studentList.xhtml", "/admin/*"})
public class AdminAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession         session      = httpRequest.getSession(false);

        boolean adminLoggedIn = session != null
                && Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))
                && "ADMIN".equals(session.getAttribute("userRole"));

        if (!adminLoggedIn) {
            setNoCacheHeaders(httpResponse);
            httpResponse.sendRedirect(
                    httpRequest.getContextPath() + "/index.xhtml"
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