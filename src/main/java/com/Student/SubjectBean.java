package com.Student;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Named("subjectBean")
@ViewScoped
public class SubjectBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // ═══════════════════════════════════════════════════════════════
    //  SubjectEntry state  (Subject CRUD)
    // ═══════════════════════════════════════════════════════════════
    private List<Subject> allSubjects;
    private Subject       newSubject   = new Subject();
    private String        codeError    = null;
    private boolean       showAddModal = false;
    private boolean       showConfirm  = false;
    private Subject       deleteTarget = null;

    // ═══════════════════════════════════════════════════════════════
    //  StudentSubjectList state  (Enrollment)
    // ═══════════════════════════════════════════════════════════════
    private List<StudentSubjectRow> rows;
    private boolean                 showEnrollModal     = false;
    private StudentSubjectRow       addTarget           = null;
    private int                     selectedSubjectId   = 0;
    private boolean                 showRemoveConfirm   = false;
    private StudentSubjectRow       removeStudentTarget = null;
    private Subject                 removeSubjectTarget = null;

    private final SubjectDAO subjectDAO = new SubjectDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    // ═══════════════════════════════════════════════════════════════
    //  Init
    // ═══════════════════════════════════════════════════════════════
    @PostConstruct
    public void init() {
        allSubjects = subjectDAO.getAllSubjects();
        loadRows();
    }

    private void loadRows() {
        List<Students> students = studentDAO.getAllStudents();
        rows = students.stream()
                .map(s -> new StudentSubjectRow(
                        s,
                        subjectDAO.getSubjectsForStudent(s.getStudent_id())
                ))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════
    //  SubjectEntry — Subject CRUD methods
    // ═══════════════════════════════════════════════════════════════

    public void openAddModal() {
        newSubject   = new Subject();
        codeError    = null;
        showAddModal = true;
    }

    public void closeModal() {
        newSubject   = new Subject();
        codeError    = null;
        showAddModal = false;
    }

    public void saveSubject() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        codeError = null;

        String code = newSubject.getSubjectCode();
        String name = newSubject.getSubjectName();

        if (code == null || code.trim().isEmpty()) {
            ctx.addMessage("subjectCode",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Subject Code is required.", null));
            return;
        }
        if (name == null || name.trim().isEmpty()) {
            ctx.addMessage("subjectName",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Subject Name is required.", null));
            return;
        }
        if (subjectDAO.codeExists(code.trim())) {
            codeError = "Subject Code already exists.";
            return;
        }

        boolean saved = subjectDAO.addSubject(newSubject);
        if (saved) {
            allSubjects  = subjectDAO.getAllSubjects();
            showAddModal = false;
            newSubject   = new Subject();
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Subject added successfully.", null));
        } else {
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Failed to save subject. Please try again.", null));
        }
    }

    public void confirmDelete(Subject subject) {
        deleteTarget = subject;
        showConfirm  = true;
    }

    public void cancelDelete() {
        deleteTarget = null;
        showConfirm  = false;
    }

    public void deleteSubject() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (deleteTarget == null) return;

        boolean deleted = subjectDAO.deleteSubject(deleteTarget.getId());
        showConfirm  = false;
        deleteTarget = null;

        if (deleted) {
            allSubjects = subjectDAO.getAllSubjects();
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Subject deleted successfully.", null));
        } else {
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Failed to delete subject. Please try again.", null));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  StudentSubjectList — Enrollment methods
    // ═══════════════════════════════════════════════════════════════

    public void openEnrollModal(StudentSubjectRow row) {
        addTarget           = row;
        selectedSubjectId   = 0;
        showEnrollModal     = true;
    }

    public void closeEnrollModal() {
        showEnrollModal = false;
        addTarget       = null;
    }

    public void assignSubject() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (selectedSubjectId == 0) {
            ctx.addMessage("addSubjectMsg",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Please select a subject.", null));
            return;
        }

        Subject selected = allSubjects.stream()
                .filter(s -> s.getId() == selectedSubjectId)
                .findFirst()
                .orElse(null);

        if (selected == null) {
            ctx.addMessage("addSubjectMsg",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Selected subject not found.", null));
            return;
        }

        int studentId = addTarget.getStudent().getStudent_id();

        if (subjectDAO.isEnrolled(studentId, selected.getSubjectCode())) {
            ctx.addMessage("addSubjectMsg",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "This subject is already assigned to the student.", null));
            return;
        }

        boolean saved = subjectDAO.enrollStudent(studentId, selected.getSubjectCode());
        if (saved) {
            loadRows();
            allSubjects     = subjectDAO.getAllSubjects();
            showEnrollModal = false;
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Subject assigned successfully.", null));
        } else {
            ctx.addMessage("addSubjectMsg",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Failed to assign subject. Please try again.", null));
        }
    }

    public void confirmRemove(StudentSubjectRow row, Subject subject) {
        removeStudentTarget = row;
        removeSubjectTarget = subject;
        showRemoveConfirm   = true;
    }

    public void cancelRemove() {
        removeStudentTarget = null;
        removeSubjectTarget = null;
        showRemoveConfirm   = false;
    }

    public void removeSubject() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (removeStudentTarget == null || removeSubjectTarget == null) return;

        boolean deleted = subjectDAO.unenrollStudent(
                removeStudentTarget.getStudent().getStudent_id(),
                removeSubjectTarget.getSubjectCode()
        );

        showRemoveConfirm = false;
        if (deleted) {
            loadRows();
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Subject removed successfully.", null));
        } else {
            ctx.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Remove failed. Please try again.", null));
        }

        removeStudentTarget = null;
        removeSubjectTarget = null;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Getters / Setters
    // ═══════════════════════════════════════════════════════════════

    // Subject CRUD
    public List<Subject> getAllSubjects()           { return allSubjects; }
    public Subject       getNewSubject()            { return newSubject; }
    public void          setNewSubject(Subject s)   { this.newSubject = s; }
    public String        getCodeError()             { return codeError; }
    public boolean       isShowAddModal()           { return showAddModal; }
    public boolean       isShowConfirm()            { return showConfirm; }
    public Subject       getDeleteTarget()          { return deleteTarget; }

    // Enrollment
    public List<StudentSubjectRow> getRows()              { return rows; }
    public boolean                 isShowEnrollModal()    { return showEnrollModal; }
    public boolean                 isShowRemoveConfirm()  { return showRemoveConfirm; }
    public StudentSubjectRow       getAddTarget()         { return addTarget; }
    public Subject                 getRemoveSubjectTarget() { return removeSubjectTarget; }
    public int                     getSelectedSubjectId()   { return selectedSubjectId; }
    public void                    setSelectedSubjectId(int id) { this.selectedSubjectId = id; }

    // ═══════════════════════════════════════════════════════════════
    //  Inner DTO
    // ═══════════════════════════════════════════════════════════════
    public static class StudentSubjectRow implements Serializable {

        private final Students      student;
        private       List<Subject> subjects;

        public StudentSubjectRow(Students student, List<Subject> subjects) {
            this.student  = student;
            this.subjects = subjects;
        }

        public Students      getStudent()                 { return student; }
        public List<Subject> getSubjects()                { return subjects; }
        public void          setSubjects(List<Subject> s) { this.subjects = s; }

        public String getSubjectNamesDisplay() {
            if (subjects == null || subjects.isEmpty()) return "—";
            StringBuilder sb = new StringBuilder();
            for (Subject s : subjects) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(s.getSubjectName());
            }
            return sb.toString();
        }
    }
}