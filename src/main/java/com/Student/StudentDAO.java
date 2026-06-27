package com.Student;

import com.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    /* ── Login ──────────────────────────────────────────────────── */
    private static final String LOGIN_SQL =
            "SELECT 1 FROM students WHERE student_id = ? AND TRIM(password) = ?";

    public boolean login(int studentID, String password) throws SQLException {
        String normalizedPassword = password == null ? "" : password.trim();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(LOGIN_SQL)) {
            ps.setInt(1, studentID);
            ps.setString(2, normalizedPassword);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /* ── Duplicate student_id check ─────────────────────────────── */
    private static final String EXISTS_SQL =
            "SELECT 1 FROM students WHERE student_id = ?";

    public boolean studentIdExists(int studentId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(EXISTS_SQL)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("❌ DB Error (exists check): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /* ── Insert new student ─────────────────────────────────────── */
    private static final String INSERT_SQL =
            "INSERT INTO students (name, student_id, date_of_birth, address, gender, " +
                    "batch, phone, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public boolean saveStudent(Students student) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
            ps.setString(1, student.getName());
            ps.setInt(2, student.getStudent_id());
            ps.setString(3, student.getDate_of_birth());
            ps.setString(4, student.getAddress());
            ps.setString(5, student.getGender());
            ps.setInt(6, student.getBatch());
            ps.setString(7, student.getPhone());
            ps.setString(8, student.getPassword());
            int rows = ps.executeUpdate();
            System.out.println("✅ Rows inserted: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ DB Error Code: " + e.getErrorCode());
            System.err.println("❌ SQL State    : " + e.getSQLState());
            System.err.println("❌ DB Error     : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /* ── Fetch all students ──────────────────────────────────────── */
    private static final String SELECT_ALL_SQL =
            "SELECT name, student_id, date_of_birth, address, gender, batch, phone, password " +
                    "FROM students ORDER BY batch ASC, student_id ASC";

    public List<Students> getAllStudents() {
        List<Students> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Students s = new Students();
                s.setName(rs.getString("name"));
                s.setStudent_id(rs.getInt("student_id"));
                s.setDate_of_birth(rs.getString("date_of_birth"));
                s.setAddress(rs.getString("address"));
                s.setGender(rs.getString("gender"));
                s.setBatch(rs.getInt("batch"));
                s.setPhone(rs.getString("phone"));
                s.setPassword(rs.getString("password"));
                list.add(s);
            }

        } catch (SQLException e) {
            System.err.println("❌ DB Error (getAllStudents): " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /* ── Update existing student ─────────────────────────────────── */
    private static final String UPDATE_SQL_NO_PASSWORD =
            "UPDATE students SET name=?, date_of_birth=?, address=?, gender=?, " +
                    "batch=?, phone=? WHERE student_id=?";

    private static final String UPDATE_SQL_WITH_PASSWORD =
            "UPDATE students SET name=?, date_of_birth=?, address=?, gender=?, " +
                    "batch=?, phone=?, password=? WHERE student_id=?";

    public boolean updateStudent(Students student) {
        boolean changePassword = student.getPassword() != null
                && !student.getPassword().trim().isEmpty();

        String sql = changePassword ? UPDATE_SQL_WITH_PASSWORD : UPDATE_SQL_NO_PASSWORD;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int idx = 1;
            ps.setString(idx++, student.getName());
            ps.setString(idx++, student.getDate_of_birth());
            ps.setString(idx++, student.getAddress());
            ps.setString(idx++, student.getGender());
            ps.setInt(idx++, student.getBatch());
            ps.setString(idx++, student.getPhone());

            if (changePassword) {
                ps.setString(idx++, student.getPassword());
            }

            ps.setInt(idx, student.getStudent_id());

            int rows = ps.executeUpdate();
            System.out.println("✅ Rows updated: " + rows);
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ DB Error Code: " + e.getErrorCode());
            System.err.println("❌ SQL State    : " + e.getSQLState());
            System.err.println("❌ DB Error     : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /* ── Delete student ──────────────────────────────────────────── */
    private static final String DELETE_SQL =
            "DELETE FROM students WHERE student_id=?";

    public boolean deleteStudent(int studentId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, studentId);
            int rows = ps.executeUpdate();
            System.out.println("✅ Rows deleted: " + rows);
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ DB Error Code: " + e.getErrorCode());
            System.err.println("❌ SQL State    : " + e.getSQLState());
            System.err.println("❌ DB Error     : " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /* ── Fetch a single student by ID ────────────────────────────── */
    private static final String SELECT_BY_ID_SQL =
            "SELECT name, student_id, date_of_birth, address, gender, batch, phone, " +
                    "password, email, registration_date " +
                    "FROM students WHERE student_id = ?";

    public Students getStudentById(int studentId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Students s = new Students();
                    s.setName(rs.getString("name"));
                    s.setStudent_id(rs.getInt("student_id"));
                    s.setDate_of_birth(rs.getString("date_of_birth"));
                    s.setAddress(rs.getString("address"));
                    s.setGender(rs.getString("gender"));
                    s.setBatch(rs.getInt("batch"));
                    s.setPhone(rs.getString("phone"));
                    s.setPassword(rs.getString("password"));
                    s.setEmail(rs.getString("email"));

                    Timestamp regTs = rs.getTimestamp("registration_date");
                    s.setRegistrationDate(regTs != null ? regTs.toString() : null);

                    return s;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ DB Error (getStudentById): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /* ── Fetch subjects enrolled by a specific student ───────────── */
    private static final String SELECT_SUBJECTS_FOR_STUDENT_SQL =
            "SELECT s.id, s.subject_code, s.subject_name " +
                    "FROM subjects s " +
                    "INNER JOIN enrollments e ON s.id = e.subject_id " +
                    "WHERE e.student_id = ? " +
                    "ORDER BY s.subject_name ASC";

    public List<Subject> getSubjectsForStudent(int studentId) {
        List<Subject> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_SUBJECTS_FOR_STUDENT_SQL)) {

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
            System.err.println("❌ DB Error (getSubjectsForStudent): " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /* ── Fetch all subjects ───────────────────────────────────────── */
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

    /* ── Check if subject code already exists ────────────────────── */
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

    /* ── Insert new subject ───────────────────────────────────────── */
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

    /* ── Delete subject by id ─────────────────────────────────────── */
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
}