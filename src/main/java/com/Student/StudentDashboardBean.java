package com.Student;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Named("studentDashboardBean")
@ViewScoped
public class StudentDashboardBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Students      student;
    private List<Subject> subjects = Collections.emptyList();

    @PostConstruct
    public void init() {
        FacesContext    ctx = FacesContext.getCurrentInstance();
        ExternalContext ext = ctx.getExternalContext();

        Object sessionId = ext.getSessionMap().get("loggedInStudentId");
        if (sessionId == null) {
            NavigationHandler nav = ctx.getApplication().getNavigationHandler();
            nav.handleNavigation(ctx, null, "student_login?faces-redirect=true");
            return;
        }

        int studentId = (Integer) sessionId;

        StudentDAO studentDAO = new StudentDAO();
        SubjectDAO subjectDAO = new SubjectDAO();

        this.student  = studentDAO.getStudentById(studentId);
        this.subjects = subjectDAO.getSubjectsForStudent(studentId);

        if (this.subjects == null) {
            this.subjects = Collections.emptyList();
        }
    }

    public Students      getStudent()       { return student; }
    public List<Subject> getSubjects()      { return subjects; }
    public int           getTotalSubjects() { return subjects == null ? 0 : subjects.size(); }
}