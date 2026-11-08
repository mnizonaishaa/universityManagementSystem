package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Reports {

    // ================================
    // COMMON UTIL 
    // ================================
    private String safe(String s) {
        return (s == null) ? "-" : s;
    }

    private String truncate(String str, int length) {
        if (str == null) return "-";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }

    // ================================
    // STUDENT REPORT 
    // ================================
    public void generateStudentReport() {

        JFrame frame = new JFrame("Student Report");
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);

        try {
            Conn conn = new Conn();

            String query =
                    "SELECT s.rollno, s.name, s.program, d.deptName, s.semester, " +
                    "COALESCE(COUNT(sc.courseId),0) AS enrolled_courses " +
                    "FROM student s " +
                    "LEFT JOIN department d ON s.deptId = d.deptId " +
                    "LEFT JOIN student_course sc ON s.rollno = sc.rollno " +
                    "GROUP BY s.rollno, s.name, s.program, d.deptName, s.semester " +
                    "ORDER BY s.rollno";

            ResultSet rs = conn.s.executeQuery(query);

            JTextArea ta = new JTextArea();
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            ta.setEditable(false);

            StringBuilder sb = new StringBuilder();

            sb.append("\n═══════════════════════════════════════════════════════════════════════════════\n");
            sb.append(String.format("%40s\n", "STUDENT REPORT"));
            sb.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

            sb.append(String.format("%-12s %-20s %-10s %-20s %-10s %s\n",
                    "Roll", "Name", "Prog", "Dept", "Sem", "Courses"));
            sb.append("-------------------------------------------------------------------------------\n");

            while (rs.next()) {
                sb.append(String.format("%-12s %-20s %-10s %-20s %-10s %d\n",
                        rs.getString("rollno"),
                        truncate(rs.getString("name"), 20),
                        safe(rs.getString("program")),
                        truncate(rs.getString("deptName"), 20),
                        safe(rs.getString("semester")),
                        rs.getInt("enrolled_courses")
                ));
            }

            ta.setText(sb.toString());
            frame.add(new JScrollPane(ta));

        } catch (Exception e) {
            e.printStackTrace();
        }

        frame.setVisible(true);
    }

    // ================================
    // TEACHER REPORT 
    // ================================
    public void generateTeacherReport() {

        JFrame frame = new JFrame("Teacher Report");
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);

        try {
            Conn conn = new Conn();

            String query =
                    "SELECT t.empId, t.name, t.education, d.deptName, " +
                    "COALESCE(COUNT(tc.courseId),0) AS assigned_courses " +
                    "FROM teacher t " +
                    "LEFT JOIN department d ON t.deptId = d.deptId " +
                    "LEFT JOIN teacher_course tc ON t.empId = tc.empId " +
                    "GROUP BY t.empId, t.name, t.education, d.deptName " +
                    "ORDER BY t.empId";

            ResultSet rs = conn.s.executeQuery(query);

            JTextArea ta = new JTextArea();
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            ta.setEditable(false);

            StringBuilder sb = new StringBuilder();

            sb.append("\n══════════════════════════════════════════════════════════════\n");
            sb.append(String.format("%30s\n", "TEACHER REPORT"));
            sb.append("══════════════════════════════════════════════════════════════\n\n");

            sb.append(String.format("%-10s %-20s %-15s %-20s %s\n",
                    "EmpID", "Name", "Education", "Dept", "Courses"));
            sb.append("--------------------------------------------------------------\n");

            while (rs.next()) {
                sb.append(String.format("%-10s %-20s %-15s %-20s %d\n",
                        rs.getString("empId"),
                        truncate(rs.getString("name"), 20),
                        safe(rs.getString("education")),
                        truncate(rs.getString("deptName"), 20),
                        rs.getInt("assigned_courses")
                ));
            }

            ta.setText(sb.toString());
            frame.add(new JScrollPane(ta));

        } catch (Exception e) {
            e.printStackTrace();
        }

        frame.setVisible(true);
    }

    // ================================
    // COURSE REPORT 
    // ================================
    public void generateCourseReport() {

        JFrame frame = new JFrame("Course Report");
        frame.setSize(900, 500);
        frame.setLocationRelativeTo(null);

        try {
            Conn conn = new Conn();

            String query =
                    "SELECT c.courseId, c.courseName, d.deptName, c.creditHours, " +
                    "COALESCE(COUNT(DISTINCT tc.empId),0) AS teachers, " +
                    "COALESCE(COUNT(DISTINCT sc.rollno),0) AS students " +
                    "FROM course c " +
                    "LEFT JOIN department d ON c.deptId = d.deptId " +
                    "LEFT JOIN teacher_course tc ON c.courseId = tc.courseId " +
                    "LEFT JOIN student_course sc ON c.courseId = sc.courseId " +
                    "GROUP BY c.courseId, c.courseName, d.deptName, c.creditHours " +
                    "ORDER BY c.courseId";

            ResultSet rs = conn.s.executeQuery(query);

            JTextArea ta = new JTextArea();
            ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
            ta.setEditable(false);

            StringBuilder sb = new StringBuilder();

            sb.append("\n══════════════════════════════════════════════════════════════\n");
            sb.append(String.format("%30s\n", "COURSE REPORT"));
            sb.append("══════════════════════════════════════════════════════════════\n\n");

            sb.append(String.format("%-10s %-25s %-15s %-8s %-8s %-8s\n",
                    "ID", "Course", "Dept", "Cr", "T", "S"));
            sb.append("--------------------------------------------------------------\n");

            while (rs.next()) {
                sb.append(String.format("%-10s %-25s %-15s %-8d %-8d %-8d\n",
                        rs.getString("courseId"),
                        truncate(rs.getString("courseName"), 25),
                        truncate(rs.getString("deptName"), 15),
                        rs.getInt("creditHours"),
                        rs.getInt("teachers"),
                        rs.getInt("students")
                ));
            }

            ta.setText(sb.toString());
            frame.add(new JScrollPane(ta));

        } catch (Exception e) {
            e.printStackTrace();
        }

        frame.setVisible(true);
    }

    // ================================
    // ATTENDANCE REPORT 
    // ================================
   public void generateAttendanceReport() {
    JFrame frame = new JFrame("Low Attendance Warning (<75%)");
    frame.setSize(900, 600);
    frame.setLocationRelativeTo(null);

    try {
        Conn conn = new Conn();

        // This query calculates percentage: (Count of Present / Total Days) * 100
        String query = 
            "SELECT a.rollno, s.name, a.courseId, " +
            "SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) as attended, " +
            "COUNT(a.status) as total_days, " +
            "(SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) / COUNT(a.status) * 100) as perc " +
            "FROM attendance a " +
            "JOIN student s ON a.rollno = s.rollno " +
            "GROUP BY a.rollno, s.name, a.courseId " +
            "HAVING perc < 75 " + 
            "ORDER BY perc ASC";

        ResultSet rs = conn.s.executeQuery(query);

        JTextArea ta = new JTextArea();
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setEditable(false);

        StringBuilder sb = new StringBuilder();
        sb.append("\n" + "=".repeat(70) + "\n");
        sb.append(String.format("%45s\n", "ATTENDANCE DEFICIT REPORT"));
        sb.append("=".repeat(70) + "\n\n");
        sb.append(String.format("%-12s %-20s %-12s %-8s %-8s %-5s\n", 
                  "Roll No", "Student Name", "Course", "Pres", "Total", "%"));
        sb.append("-".repeat(70) + "\n");

        while (rs.next()) {
            sb.append(String.format("%-12s %-20s %-12s %-8d %-8d %.1f%%\n",
                rs.getString("rollno"),
                truncate(rs.getString("name"), 20),
                rs.getString("courseId"),
                rs.getInt("attended"),
                rs.getInt("total_days"),
                rs.getDouble("perc")
            ));
        }

        ta.setText(sb.toString());
        frame.add(new JScrollPane(ta));

    } catch (Exception e) {
        e.printStackTrace();
    }
    frame.setVisible(true);
}
}