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

    /* ── Confirm password ──────────────────────────────────────── */
    private String confirmPassword;
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    /* ── New student form ──────────────────────────────────────── */
    private Students student = new Students();
    public Students getStudent() { return student; }
    public void setStudent(Students student) { this.student = student; }

    /* ── Student list ──────────────────────────────────────────── */
    private List<Students> students;

    @jakarta.annotation.PostConstruct
    public void init() {
        students = new StudentDAO().getAllStudents();
    }

    public List<Students> getStudents()  { return students; }
    public int getTotalStudents()        { return students == null ? 0 : students.size(); }

    public void refresh() {
        students = new StudentDAO().getAllStudents();
    }

    /* ── Edit / Update ─────────────────────────────────────────── */
    private Students editStudent = new Students();

    public Students getEditStudent()                     { return editStudent; }
    public void     setEditStudent(Students editStudent) { this.editStudent = editStudent; }

    /**
     * Flag read by JS after the Update ajax call completes.
     *
     * true  → update succeeded → JS closes the modal, green message shows on page
     * false → error/warn       → JS leaves modal open, message shown inside modal
     */
    private boolean updateSuccess = false;
    public boolean isUpdateSuccess() { return updateSuccess; }

    /* ── Message helpers ───────────────────────────────────────── */
    private void addError(FacesContext ctx, String msg) {
        ctx.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
    }

    private void addSuccess(FacesContext ctx, String msg) {
        ctx.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg));
    }

    private void addWarning(FacesContext ctx, String msg) {
        ctx.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg));
    }

    /* ── Phone validation ──────────────────────────────────────── */
    private boolean isPhoneInvalid(String phone, FacesContext ctx) {
        if (phone == null || phone.trim().isEmpty()) {
            addError(ctx, "Phone number is required.");
            return true;
        }
        String p = phone.trim();
        if (!p.startsWith("01")) {
            addError(ctx, "Phone number must start with 01 (01XXXXXXXXX).");
            return true;
        }
        if (!p.matches("\\d{11}")) {
            addError(ctx, "Phone number must be exactly 11 digits.");
            return true;
        }
        return false;
    }

    /* ── Null-safe trim helper ─────────────────────────────────── */
    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /* ══════════════════════════════════════════════════════════════
       SAVE (Registration)
       ══════════════════════════════════════════════════════════════ */
    public void saveStudent() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        StudentDAO   dao = new StudentDAO();
        boolean hasError = false;

        if (!student.getPassword().equals(confirmPassword)) {
            addError(ctx, "Passwords do not match.");
            hasError = true;
        }

        if (isPhoneInvalid(student.getPhone(), ctx)) {
            hasError = true;
        }

        if (hasError) return;

        if (dao.studentIdExists(student.getStudent_id())) {
            addError(ctx, "This Student ID is already registered.");
            return;
        }

        if (dao.saveStudent(student)) {
            addSuccess(ctx, "Student saved successfully.");
            student         = new Students();
            confirmPassword = null;
            students        = dao.getAllStudents();
        } else {
            addError(ctx, "Could not save student. Please try again.");
        }
    }

    /* ══════════════════════════════════════════════════════════════
       PREPARE EDIT — copies the selected row into editStudent
       ══════════════════════════════════════════════════════════════ */
    public void prepareEdit(Students s) {
        // Reset success flag every time we open the modal
        updateSuccess = false;

        Students copy = new Students();
        copy.setStudent_id    (s.getStudent_id());
        copy.setName          (s.getName());
        copy.setDate_of_birth (s.getDate_of_birth());
        copy.setAddress       (s.getAddress());
        copy.setGender        (s.getGender());
        copy.setBatch         (s.getBatch());   // String -> String (no type mismatch)
        copy.setPhone         (s.getPhone());
        copy.setPassword      ("");             // blank = keep existing password
        this.editStudent = copy;
    }

    /* ══════════════════════════════════════════════════════════════
       UPDATE
       ══════════════════════════════════════════════════════════════ */
    public void updateStudent() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        StudentDAO   dao = new StudentDAO();

        // Always reset flag at the start of each attempt
        updateSuccess = false;

        if (isPhoneInvalid(editStudent.getPhone(), ctx)) return;

        /* ── No-change detection ─────────────────────────────── */
        Students current = dao.getStudentById(editStudent.getStudent_id());
        if (current != null) {
            boolean nameChanged    = !trim(editStudent.getName())         .equals(trim(current.getName()));
            boolean genderChanged  = !trim(editStudent.getGender())       .equals(trim(current.getGender()));
            boolean phoneChanged   = !trim(editStudent.getPhone())        .equals(trim(current.getPhone()));
            boolean batchChanged   = !trim(editStudent.getBatch())        .equals(trim(current.getBatch()));
            boolean dobChanged     = !trim(editStudent.getDate_of_birth()).equals(trim(current.getDate_of_birth()));
            boolean addressChanged = !trim(editStudent.getAddress())      .equals(trim(current.getAddress()));
            boolean passChanged    = editStudent.getPassword() != null
                    && !editStudent.getPassword().isEmpty();

            boolean anyChanged = nameChanged || genderChanged || phoneChanged
                    || batchChanged || dobChanged || addressChanged || passChanged;

            if (!anyChanged) {
                // WARN -> stays inside modal
                addWarning(ctx, "No changes detected. Please update at least one field.");
                return;
            }
        }

        if (dao.updateStudent(editStudent)) {
            // SUCCESS -> set flag so JS can close the modal
            updateSuccess = true;
            addSuccess(ctx, "Student updated successfully.");
            students = dao.getAllStudents();
        } else {
            // ERROR -> stays inside modal
            addError(ctx, "Could not update student. Please try again.");
        }
    }

    /* ══════════════════════════════════════════════════════════════
       DELETE
       ══════════════════════════════════════════════════════════════ */
    public void deleteStudent(int studentId) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        StudentDAO   dao = new StudentDAO();

        if (dao.deleteStudent(studentId)) {
            addSuccess(ctx, "Student record deleted successfully.");
        } else {
            addError(ctx, "Could not delete student. Please try again.");
        }
        students = dao.getAllStudents();
    }
}