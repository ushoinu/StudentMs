package com.Student;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class StudentBean implements Serializable {

    /* ── Confirm password (registration form) ─────────────────── */
    private String confirmPassword;
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    /* ── New student (registration form) ──────────────────────── */
    private Students student = new Students();
    public Students getStudent() { return student; }
    public void setStudent(Students student) { this.student = student; }

    public void saveStudent() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        StudentDAO dao   = new StudentDAO();

        // 1. Password confirmation check
        if (!student.getPassword().equals(confirmPassword)) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Passwords do not match.", null));
            return;
        }

        // 2. Duplicate student_id check
        if (dao.studentIdExists(student.getStudent_id())) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "This Student ID is already registered.", null));
            return;
        }

        // 3. Insert
        boolean success = dao.saveStudent(student);
        if (success) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Success!", "Student saved successfully."));
            student         = new Students();
            confirmPassword = null;
            students = dao.getAllStudents(); // keep list in sync
        } else {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Failed!", "Could not save student. Check logs."));
        }
    }

    /* ── Student List ──────────────────────────────────────────── */
    private List<Students> students;

    @jakarta.annotation.PostConstruct
    public void init() {
        StudentDAO dao = new StudentDAO();
        students = dao.getAllStudents();
    }

    public List<Students> getStudents() {
        return students;
    }

    public int getTotalStudents() {
        return students == null ? 0 : students.size();
    }

    public void refresh() {
        StudentDAO dao = new StudentDAO();
        students = dao.getAllStudents();
    }

    /* ── Edit / Update ─────────────────────────────────────────── */
    private Students editStudent = new Students();

    public Students getEditStudent() { return editStudent; }
    public void setEditStudent(Students editStudent) { this.editStudent = editStudent; }

    /**
     * Called when the admin clicks "Edit" on a row.
     * Loads that student's data into editStudent so the modal form
     * can be populated.
     */
    public void prepareEdit(Students s) {
        Students copy = new Students();
        copy.setStudent_id(s.getStudent_id());
        copy.setName(s.getName());
        copy.setDate_of_birth(s.getDate_of_birth());
        copy.setAddress(s.getAddress());
        copy.setGender(s.getGender());
        copy.setBatch(s.getBatch());
        copy.setPhone(s.getPhone());
        copy.setPassword("");   // blank = "keep current password"
        this.editStudent = copy;
    }

    /**
     * Called when the admin clicks "Update" inside the edit modal.
     */
    public void updateStudent() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        StudentDAO dao   = new StudentDAO();

        boolean success = dao.updateStudent(editStudent);
        if (success) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Success!", "Student updated successfully."));
            students = dao.getAllStudents();
        } else {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Failed!", "Could not update student. Check logs."));
        }
    }

    /* ── Delete ────────────────────────────────────────────────── */

    /**
     * Called when the admin confirms "Delete" on a row.
     */
    public void deleteStudent(int studentId) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        StudentDAO dao   = new StudentDAO();

        boolean success = dao.deleteStudent(studentId);
        if (success) {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Deleted!", "Student record deleted successfully."));
        } else {
            ctx.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Failed!", "Could not delete student. Check logs."));
        }
        students = dao.getAllStudents();
    }
}