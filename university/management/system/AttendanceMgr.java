package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import net.proteanit.sql.DbUtils;

/**
 * Attendance Management System
 * Features:
 * - Add attendance
 * - View attendance
 * - Update attendance ONLY before 24 hrs
 * - Delete attendance
 * - Short attendance warning
 */

public class AttendanceMgr {
String selectedRoll = null;
String selectedCourse = null;

     private final String loggedInId;

    public AttendanceMgr(String loggedInId) {
        this.loggedInId = loggedInId;
    }
    
    // =========================================================
    // MARK ATTENDANCE
    // =========================================================
public void markAttendance() {

    JFrame frame = createFrame("Mark Attendance", 850, 600);

    JLabel heading = heading("MARK ATTENDANCE");
    heading.setBounds(0, 10, 850, 35);
    frame.add(heading);

    // COURSE DROPDOWN
    Choice courseChoice = new Choice();
    courseChoice.setBounds(220, 60, 250, 25);
    frame.add(courseChoice);

    loadTeacherCourses(courseChoice);

    // PANEL
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JScrollPane scroll = new JScrollPane(panel);
    scroll.setBounds(40, 120, 760, 330);
    frame.add(scroll);

    JButton loadBtn = button("LOAD STUDENTS", new Color(0,102,51));
    loadBtn.setBounds(120, 500, 180, 35);
    frame.add(loadBtn);

    JButton saveBtn = button("SAVE ATTENDANCE", new Color(0,70,140));
    saveBtn.setBounds(420, 500, 220, 35);
    frame.add(saveBtn);

    HashMap<String, JComboBox<String>> map = new HashMap<>();

    // LOAD STUDENTS
   loadBtn.addActionListener(e -> {

    panel.removeAll();
    map.clear();

    try {

        String selected = courseChoice.getSelectedItem();
        String courseId = selected.split(" - ")[0].trim();

        Conn c = new Conn();

        // GET STUDENTS BASED ONLY ON COURSE
        PreparedStatement ps = c.c.prepareStatement(
            "SELECT s.rollno, s.name " +
            "FROM student s " +
            "JOIN student_course sc ON s.rollno = sc.rollno " +
            "WHERE sc.courseId = ?"
        );

        ps.setString(1, courseId);

        ResultSet rs = ps.executeQuery();

        boolean found = false;

        while (rs.next()) {

            found = true;

            String roll = rs.getString("rollno");

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JLabel lbl = new JLabel(
                    roll + " - " + rs.getString("name")
            );

            JComboBox<String> status =
                    new JComboBox<>(new String[]{"Present", "Absent"});

            map.put(roll, status);

            row.add(lbl);
            row.add(status);

            panel.add(row);
        }

        if (!found) {
            JOptionPane.showMessageDialog(frame,
                    "No students found for this course");
        }

        panel.revalidate();
        panel.repaint();

    } catch (Exception ex) {
        ex.printStackTrace();
    }
});
    // SAVE ATTENDANCE
    saveBtn.addActionListener(e -> saveAttendance(map, courseChoice));

    frame.setVisible(true);
}
    // =========================================================
    // VIEW ATTENDANCE
    // =========================================================

    public void viewAttendance() {

        JFrame frame = createFrame("View Attendance", 850, 550);

        JLabel heading = heading("ATTENDANCE RECORD");
        heading.setBounds(0, 10, 850, 35);
        frame.add(heading);

        JLabel lblRoll = label("Student Roll No");
        lblRoll.setBounds(25, 65, 130, 25);
        frame.add(lblRoll);

        Choice cRoll = new Choice();
        cRoll.setBounds(160, 65, 170, 25);
        loadRollNumbers(cRoll);
        frame.add(cRoll);

        JButton search = button("SEARCH", Color.BLACK);
        search.setBounds(360, 63, 110, 30);
        frame.add(search);

        JButton showAll = button("SHOW ALL",
                new Color(0, 70, 140));
        showAll.setBounds(490, 63, 120, 30);
        frame.add(showAll);

        JButton close = button("CLOSE",new Color(120, 0, 0));
        close.setBounds(630, 63, 110, 30);
        frame.add(close);

        JTable table = new JTable();

        JScrollPane jsp = new JScrollPane(table);
        jsp.setBounds(0, 120, 850, 390);

        frame.add(jsp);

        loadAttendanceTable(table);

        search.addActionListener(e -> { 
            try {
      Conn conn = new Conn();

        String query = "SELECT a.attId AS 'ID', a.rollno AS 'Roll No', " +
                       "a.courseId AS 'Course ID', a.semester AS 'Semester', " +
                       "a.attendanceDate AS 'Date', a.status AS 'Status' " +
                       "FROM attendance a WHERE a.rollno = ?";

        PreparedStatement ps = conn.c.prepareStatement(query);
        
        ps.setString(1, cRoll.getSelectedItem());

        ResultSet rs = ps.executeQuery();

        table.setModel(DbUtils.resultSetToTableModel(rs));

        table.setRowHeight(25);
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Roll No
        table.getColumnModel().getColumn(2).setPreferredWidth(120); // Course ID
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Date

        ps.close();
        conn.c.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        showAll.addActionListener( e -> loadAttendanceTable(table));

        close.addActionListener( e -> frame.dispose());

        frame.setVisible(true);
    }
    // =========================================================
    // VIEW MY ATTENDANCE (STUDENT)
    // =========================================================
    public void viewMyAttendance() {
    JFrame frame = new JFrame("My Attendance Records");
    frame.setSize(900, 600); // Widened frame
    frame.setLocationRelativeTo(null);
    frame.setLayout(null);
    frame.getContentPane().setBackground(Color.WHITE);

    JLabel heading = new JLabel("VIEW ATTENDANCE", SwingConstants.CENTER);
    heading.setBounds(0, 20, 900, 30);
    heading.setFont(new Font("Tahoma", Font.BOLD, 22));
    frame.add(heading);

    // --- COURSE SELECTION ---
    JLabel lblCourse = new JLabel("Select Course:");
    lblCourse.setBounds(50, 80, 120, 25);
    lblCourse.setFont(new Font("Tahoma", Font.BOLD, 14));
    frame.add(lblCourse);

    JComboBox<String> cbCourse = new JComboBox<>();
    cbCourse.setBounds(170, 80, 250, 25);
    frame.add(cbCourse);

    // Load only courses the student is actually enrolled in
    try {
        Conn c = new Conn();
        PreparedStatement ps = c.c.prepareStatement(
            "SELECT DISTINCT courseId FROM student_course WHERE rollno = ?"
        );
        ps.setString(1, loggedInId); // Student's Roll No
        ResultSet rs = ps.executeQuery();
        cbCourse.addItem("--- All Courses ---");
        while (rs.next()) {
            cbCourse.addItem(rs.getString("courseId"));
        }
    } catch (Exception e) { e.printStackTrace(); }

    // --- TABLE SETUP ---
    JTable table = new JTable();
    table.setRowHeight(30);
    table.setFont(new Font("Tahoma", Font.PLAIN, 13));
    
    JScrollPane jsp = new JScrollPane(table);
    jsp.setBounds(50, 130, 800, 350);
    frame.add(jsp);

    // --- SEARCH LOGIC ---
    // Function to load data based on selected course
    ActionListener loadAttendance = ae -> {
        String selectedCourse = (String) cbCourse.getSelectedItem();
        try {
            Conn c = new Conn();
            String query = "SELECT a.attendanceDate AS 'Date', " +
                           "c.courseName AS 'Course', " +
                           "t.name AS 'Teacher', " + // Joined to get Teacher Name
                           "a.status AS 'Attendance Status' " +
                           "FROM attendance a " +
                           "JOIN course c ON a.courseId = c.courseId " +
                           "JOIN teacher t ON a.empId = t.empId " + // Joining with Teacher table
                           "WHERE a.rollno = ?";

            // If a specific course is selected, add to query
            if (selectedCourse != null && !selectedCourse.equals("--- All Courses ---")) {
                query += " AND a.courseId = '" + selectedCourse + "'";
            }
            
            query += " ORDER BY a.attendanceDate DESC";

            PreparedStatement ps = c.c.prepareStatement(query);
            ps.setString(1, loggedInId);
            ResultSet rs = ps.executeQuery();
            
            table.setModel(DbUtils.resultSetToTableModel(rs));
            
            table.getColumnModel().getColumn(0).setPreferredWidth(100); // Date
            table.getColumnModel().getColumn(1).setPreferredWidth(250); // Course Name
            table.getColumnModel().getColumn(2).setPreferredWidth(180); // Teacher Name
            table.getColumnModel().getColumn(3).setPreferredWidth(120); // Status
            
        } catch (Exception e) { e.printStackTrace(); }
    };

    cbCourse.addActionListener(loadAttendance);
    loadAttendance.actionPerformed(null); // Initial load

    JButton close = new JButton("Close");
    close.setBounds(390, 500, 120, 30);
    close.setBackground(Color.BLACK);
    close.setForeground(Color.WHITE);
    close.addActionListener(e -> frame.dispose());
    frame.add(close);

    frame.setVisible(true);
}

    // =========================================================
    // UPDATE ATTENDANCE
    // =========================================================

public void updateAttendance() {

    JFrame frame = createFrame("Update Attendance", 850, 600);

    JLabel heading = heading("UPDATE ATTENDANCE");
    heading.setBounds(0, 10, 850, 35);
    frame.add(heading);

    // COURSE CHOICE
    Choice courseChoice = new Choice();
    courseChoice.setBounds(220, 60, 250, 25);
    frame.add(courseChoice);

    loadTeacherCourses(courseChoice);

    // PANEL FOR STUDENTS
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JScrollPane scroll = new JScrollPane(panel);
    scroll.setBounds(40, 120, 760, 330);
    frame.add(scroll);

    JButton loadBtn = button("LOAD ATTENDANCE",
            new Color(0,102,51));

    loadBtn.setBounds(120, 500, 200, 35);
    frame.add(loadBtn);

    JButton updateBtn = button("UPDATE",
            new Color(0,70,140));

    updateBtn.setBounds(420, 500, 200, 35);
    frame.add(updateBtn);

    HashMap<Integer, JComboBox<String>> map = new HashMap<>();

    // =====================================================
    // LOAD ATTENDANCE
    // =====================================================
   loadBtn.addActionListener(e -> {

    panel.removeAll();
    map.clear();

    try {

        Conn conn = new Conn();

        String courseId =
                courseChoice.getSelectedItem()
                        .split(" - ")[0]
                        .trim();

        PreparedStatement ps = conn.c.prepareStatement(
    "SELECT a.attId, a.rollno, s.name, a.status " +
    "FROM attendance a " +
    "JOIN student s ON a.rollno = s.rollno " +
    "WHERE a.courseId = ? AND a.empId = ? " +
    "AND a.attendanceDate = CURDATE()"
);

         ps.setString(1, courseId);
         ps.setString(2, loggedInId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {

            int attId = rs.getInt("attId");

            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));

            JLabel lbl = new JLabel(
                rs.getString("rollno") + " - " + rs.getString("name")
            );

            JComboBox<String> status =
                new JComboBox<>(new String[]{"Present", "Absent"});

            status.setSelectedItem(rs.getString("status"));

            map.put(attId, status);

            row.add(lbl);
            row.add(status);

            panel.add(row);
        }

        panel.revalidate();
        panel.repaint();

    } catch (Exception ex) {
        ex.printStackTrace();
    }
});
    // =====================================================
    // UPDATE BUTTON
    // =====================================================
    updateBtn.addActionListener(e -> {

        try {

            Conn conn = new Conn();

            for (Integer attId : map.keySet()) {

                // CHECK 24 HOURS
                PreparedStatement check =
                        conn.c.prepareStatement(

                    "SELECT markedAt " +
                    "FROM attendance " +
                    "WHERE attId=?"
                );

                check.setInt(1, attId);

                ResultSet rs = check.executeQuery();

                if (rs.next()) {

                    Timestamp time =
                            rs.getTimestamp("markedAt");

                    long hours =
                            (System.currentTimeMillis()
                            - time.getTime())
                            / (1000 * 60 * 60);

                    // IF MORE THAN 24 HOURS
                    if (hours > 24) {

                        JOptionPane.showMessageDialog(
                                frame,
                                "24 hour limit passed for attendance ID: "
                                        + attId
                        );

                        continue;
                    }

                    // UPDATE
                    PreparedStatement ps =
                            conn.c.prepareStatement(

                        "UPDATE attendance " +
                        "SET status=? " +
                        "WHERE attId=?"
                    );

                    ps.setString(
                            1,
                            map.get(attId)
                                    .getSelectedItem()
                                    .toString()
                    );

                    ps.setInt(2, attId);

                    ps.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(
                    frame,
                    "Attendance Updated Successfully"
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    frame.setVisible(true);
}
    // =========================================================
    // VIEW SUMMARY
    // =========================================================
    public void viewSummary() {

    JFrame frame = createFrame("Attendance Summary", 600, 420);

    JLabel heading = heading("ATTENDANCE SUMMARY");

    heading.setBounds(0, 20, 600, 35);

    frame.add(heading);

    JTextArea area = new JTextArea();

    area.setEditable(false);

    area.setFont(new Font("Monospaced", Font.BOLD, 15));

    JScrollPane jsp = new JScrollPane(area);

    jsp.setBounds(40, 90, 500, 240);

    frame.add(jsp);

    try {

        Conn conn = new Conn();

        ResultSet rs1 = conn.s.executeQuery(

            "SELECT COUNT(DISTINCT rollno) FROM attendance"
        );

        int totalStudents = 0;

        if (rs1.next()) {

            totalStudents = rs1.getInt(1);
        }

        ResultSet rs2 = conn.s.executeQuery(

            "SELECT COUNT(DISTINCT rollno) " +
            "FROM attendance " +
            "WHERE status='Absent'"
        );

        int absent = 0;

        if (rs2.next()) {

            absent = rs2.getInt(1);
        }

        ResultSet rs3 = conn.s.executeQuery(

            "SELECT COUNT(*) FROM attendance " +
            "WHERE attendanceDate=CURDATE()"
        );

        int today = 0;

        if (rs3.next()) {

            today = rs3.getInt(1);
        }

        area.setText(

            "\nTotal Students : " + totalStudents +

            "\n\nAbsent Students : " + absent +

            "\n\nToday's Attendance Entries : " + today
        );

    } catch (Exception e) {

        e.printStackTrace();
    }

    frame.setVisible(true);
}
    // =========================================================
    // HELPERS
    // =========================================================
   private void saveAttendance(HashMap<String, JComboBox<String>> map, Choice courseChoice) {
    try {
        Conn c = new Conn();
        String selected = courseChoice.getSelectedItem();
        String courseId = selected.split(" - ")[0].trim();
        
        String semester = "1st Semester"; 
        if (selected.split(" - ").length > 2) {
            semester = selected.split(" - ")[2].trim();
        }

        for (String roll : map.keySet()) {
            String status = map.get(roll).getSelectedItem().toString();

            // This query handles the "Unique Constraint" automatically
            String query = "INSERT INTO attendance " +
                           "(rollno, courseId, semester, empId, status, attendanceDate, markedAt) " +
                           "VALUES (?, ?, ?, ?, ?, CURDATE(), NOW()) " +
                           "ON DUPLICATE KEY UPDATE status = ?, markedAt = NOW()";

            PreparedStatement ps = c.c.prepareStatement(query);
            ps.setString(1, roll);
            ps.setString(2, courseId);
            ps.setString(3, semester);
            ps.setString(4, loggedInId);
            ps.setString(5, status); // For the INSERT
            ps.setString(6, status); // For the UPDATE if duplicate found

            ps.executeUpdate();
        }
        JOptionPane.showMessageDialog(null, "Attendance Saved Successfully!");

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage());
    }
}
    private void loadTeacherCourses(Choice courseChoice) {

    try {

        Conn c = new Conn();

        PreparedStatement ps = c.c.prepareStatement(
                "SELECT tc.courseId, c.courseName " +
                "FROM teacher_course tc " +
                "JOIN course c ON tc.courseId = c.courseId " +
                "WHERE tc.empId = ?"
        );

        ps.setString(1, loggedInId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {

          String item =
        rs.getString("courseId")
        + " - "
        + rs.getString("courseName");

            courseChoice.add(item);
        }

        rs.close();
        ps.close();
        c.c.close();

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    private void loadRollNumbers(Choice c) {

        try {

            Conn conn = new Conn();

            ResultSet rs =
                    conn.s.executeQuery(
                            "select rollno from student");

            while (rs.next()) {

                c.add(rs.getString("rollno"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadAttendanceTable(JTable table) {

        try {

            Conn conn = new Conn();

            ResultSet rs =
                    conn.s.executeQuery(
                            "select * from attendance");

            table.setModel(
                    DbUtils.resultSetToTableModel(rs));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private JFrame createFrame(
            String title,
            int width,
            int height) {

        JFrame frame = new JFrame(title);

        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);

        return frame;
    }

    private JLabel heading(String text) {

        JLabel lbl = new JLabel(text, SwingConstants.CENTER);

        lbl.setFont( new Font("Tahoma",Font.BOLD, 24));
        return lbl;
    }

    private JLabel label(String text) {

        JLabel lbl = new JLabel(text);

        lbl.setFont( new Font("Tahoma", Font.BOLD,14));

        return lbl;
    }

    private JButton button(String text, Color bg) {

        JButton btn = new JButton(text);

        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);

        btn.setFont( new Font("Tahoma",Font.BOLD,13));

        return btn;
    }
}