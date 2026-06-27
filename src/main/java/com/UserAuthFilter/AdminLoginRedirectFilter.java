package com.UserAuthFilter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebFilter("/index.xhtml")
public class AdminLoginRedirectFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  httpRequest  = (HttpServletRequest)  request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession         session      = httpRequest.getSession(false);

        boolean adminLoggedIn = session != null
                && Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"))
                && "ADMIN".equals(session.getAttribute("userRole"));

        if (adminLoggedIn) {
            httpResponse.sendRedirect(
                    httpRequest.getContextPath() + "/admin_page.xhtml"
            );
            return;
        }

        chain.doFilter(request, response);
    }
}