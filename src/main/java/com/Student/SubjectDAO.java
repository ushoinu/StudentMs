package com.Student;

import com.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    // ═══════════════════════════════════════════════════════════════
    //  SUBJECT CRUD  (subjects table)
    // ═══════════════════════════════════════════════════════════════

    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT id, subject_code, subject_name FROM subjects ORDER BY id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Subject(
                        rs.getInt("id"),
                        rs.getString("subject_code"),
                        rs.getString("subject_name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean codeExists(String code) {
        String sql = "SELECT COUNT(*) FROM subjects WHERE subject_code = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, code.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addSubject(Subject subject) {
        String sql = "INSERT INTO subjects (subject_code, subject_name) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, subject.getSubjectCode().trim());
            ps.setString(2, subject.getSubjectName().trim());
            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSubject(int id) {
        String sql = "DELETE FROM subjects WHERE id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  ENROLLMENT  (student_subjects join table)
    //  columns: id | student_id | subject_code
    // ═══════════════════════════════════════════════════════════════

    /** student_subjects এ row INSERT করে — enrollment */
    public boolean enrollStudent(int studentId, String subjectCode) {
        String sql = "INSERT INTO student_subjects (student_id, subject_code) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setString(2, subjectCode.trim());
            return ps.executeUpdate() > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // duplicate — already enrolled
        } catch (SQLException e) {
            System.err.println("❌ DB Error (enrollStudent): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** student_subjects থেকে row DELETE করে — unenroll */
    public boolean unenrollStudent(int studentId, String subjectCode) {
        String sql = "DELETE FROM student_subjects WHERE student_id = ? AND subject_code = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setString(2, subjectCode.trim());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ DB Error (unenrollStudent): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** একজন student এর সব enrolled subject fetch করে */
    public List<Subject> getSubjectsForStudent(int studentId) {
        List<Subject> list = new ArrayList<>();
        String sql = """
                SELECT s.id, s.subject_code, s.subject_name
                FROM subjects s
                JOIN student_subjects ss ON s.subject_code = ss.subject_code
                WHERE ss.student_id = ?
                ORDER BY s.id
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Subject(
                            rs.getInt("id"),
                            rs.getString("subject_code"),
                            rs.getString("subject_name")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Already enrolled কিনা check করে */
    public boolean isEnrolled(int studentId, String subjectCode) {
        String sql = "SELECT COUNT(*) FROM student_subjects WHERE student_id = ? AND subject_code = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setString(2, subjectCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}