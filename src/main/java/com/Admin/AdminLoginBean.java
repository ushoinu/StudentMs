package com.Admin;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class AdminLoginBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER =
            Logger.getLogger(AdminLoginBean.class.getName());

    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

    private String email;
    private String password;

    public String login() {
        AdminDAO dao = new AdminDAO();
        try {
            if (dao.login(email, password)) {
                ExternalContext externalContext = getExternalContext();

                // Invalidate old session first (session fixation protection)
                externalContext.invalidateSession();

                // Set admin session attributes
                externalContext.getSessionMap().put("adminLoggedIn", Boolean.TRUE);
                externalContext.getSessionMap().put("userRole", "ADMIN");

                // Set session timeout
                Object session = externalContext.getSession(true);
                if (session instanceof HttpSession httpSession) {
                    httpSession.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
                }

                // Clear sensitive data
                this.password = null;

                return "/admin_page.xhtml?faces-redirect=true";
            }

            addErrorMessage("Login Failed", "Invalid Email or Password");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during admin login", e);
            addErrorMessage("Database Error", "Could not verify login. Please check database connection.");
        }

        return null;
    }

    public String logout() {
        ExternalContext externalContext = getExternalContext();
        externalContext.getSessionMap().remove("adminLoggedIn");
        externalContext.getSessionMap().remove("userRole");
        externalContext.invalidateSession();
        return "/index.xhtml?faces-redirect=true";
    }

    // ==========================
    // HELPERS
    // ==========================

    private ExternalContext getExternalContext() {
        return FacesContext.getCurrentInstance().getExternalContext();
    }

    private void addErrorMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail)
        );
    }

    // ==========================
    // GETTERS & SETTERS
    // ==========================

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}