package com.Student;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named
@ViewScoped
public class StudentLoginBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER =
            Logger.getLogger(StudentLoginBean.class.getName());

    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;

    private String studentId;
    private String password;

    public String login() {
        int parsedStudentId;
        try {
            parsedStudentId = Integer.parseInt(
                    studentId == null ? "" : studentId.trim()
            );
        } catch (NumberFormatException e) {
            addErrorMessage("Login Failed", "Student ID must be a valid number");
            return null;
        }

        StudentDAO dao = new StudentDAO();
        try {
            boolean validLogin = dao.login(parsedStudentId, password);

            if (validLogin) {
                ExternalContext externalContext = getExternalContext();

                // Invalidate old session first (session fixation protection)
                externalContext.invalidateSession();

                // Get fresh session
                externalContext.getSessionMap().put("loggedInStudentId", parsedStudentId);
                externalContext.getSessionMap().put("studentLoggedIn", Boolean.TRUE);
                externalContext.getSessionMap().put("userRole", "STUDENT");

                // Set session timeout
                Object session = externalContext.getSession(true);
                if (session instanceof jakarta.servlet.http.HttpSession httpSession) {
                    httpSession.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
                }

                // Clear sensitive data
                this.password = null;

                return "/student_page?faces-redirect=true";
            }

            addErrorMessage("Login Failed", "Invalid Student ID or Password");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during student login", e);
            addErrorMessage("Database Error", "Unable to verify login credentials");
        }

        return null;
    }

    public String logout() {
        ExternalContext externalContext = getExternalContext();
        externalContext.getSessionMap().remove("loggedInStudentId");
        externalContext.getSessionMap().remove("studentLoggedIn");
        externalContext.getSessionMap().remove("userRole");
        externalContext.invalidateSession();
        return "/student_login?faces-redirect=true";
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

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}