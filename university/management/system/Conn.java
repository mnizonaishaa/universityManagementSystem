package university.management.system;

import java.sql.*;

/**
 * Conn — single-use DB connection wrapper.
 * One Conn object = one Statement; close after use.
 * Password: update to match your MySQL root password.
 */
public class Conn {

    Connection c;
    Statement  s;

    Conn() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            c = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/universitymanagementsystem", "root", "134340");
            s = c.createStatement();
              System.out.println("Database connected successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}