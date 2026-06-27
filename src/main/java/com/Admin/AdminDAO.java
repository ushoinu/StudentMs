package com.Admin;
import com.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {

    private static final String LOGIN_SQL =
            "SELECT 1 FROM admin WHERE TRIM(email)=? AND TRIM(password)=?";

    public boolean login(String email, String password) throws SQLException {

        String normalizedEmail = email == null ? "" : email.trim();
        String normalizedPassword = password == null ? "" : password.trim();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(LOGIN_SQL)) {

            ps.setString(1, normalizedEmail);
            ps.setString(2, normalizedPassword);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
