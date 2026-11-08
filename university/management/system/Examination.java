package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import net.proteanit.sql.DbUtils;

public class Examination {

    String loggedInTeacherId;

    public Examination(String teacherId) {

        this.loggedInTeacherId = teacherId;
    }
    //=========================================================
    // VIEW RESULTS ADMIN AND TEACHER
    // =========================================================
    public void examinationDetails() {
        boolean isAdmin = loggedInTeacherId.equalsIgnoreCase("admin");

        JFrame frame = new JFrame(isAdmin ? "All Student Results (Admin Controls)" : "My Student Results (Class Selection)");
        frame.setSize(1200, 650);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // --- NORTH PANEL (Heading + Dynamic Dropdown Layout) ---
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBackground(Color.WHITE);

        JLabel h = new JLabel(isAdmin ? "OVERALL ACADEMIC RESULTS " : "STUDENT RESULTS", SwingConstants.CENTER);
        h.setAlignmentX(Component.CENTER_ALIGNMENT);
        h.setFont(new Font("Tahoma", Font.BOLD, 22));
        h.setForeground(new Color(0, 51, 102));
        h.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        northPanel.add(h);

        // Filter Bar Panel
        JPanel filterBar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 10));
        filterBar.setBackground(Color.WHITE);

        JLabel lblCourse = new JLabel("Select Course Section:");
        lblCourse.setFont(new Font("Tahoma", Font.BOLD, 13));
        filterBar.add(lblCourse);

        JComboBox<String> cbCourse = new JComboBox<>();
        cbCourse.setPreferredSize(new Dimension(350, 28));
        filterBar.add(cbCourse);
        northPanel.add(filterBar);
        
        frame.add(northPanel, BorderLayout.NORTH);

        // --- CENTER PANEL 
        JTable table = new JTable();
        table.setRowHeight(30); 
        table.setFont(new Font("Tahoma", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        
        JScrollPane jsp = new JScrollPane(table);
        jsp.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20)); 
        frame.add(jsp, BorderLayout.CENTER);

        try {
            Conn conn = new Conn();
            String courseQuery;
            PreparedStatement psCourse;

            if (isAdmin) {
                // Admins see all courses registered anywhere
                courseQuery = "SELECT DISTINCT c.courseId, c.courseName, m.semester " +
                              "FROM marks m JOIN course c ON m.courseId = c.courseId ORDER BY c.courseId";
                psCourse = conn.c.prepareStatement(courseQuery);
            } else {
                // Teachers 
                courseQuery = "SELECT tc.courseId, c.courseName, tc.semester " +
                              "FROM teacher_course tc JOIN course c ON tc.courseId = c.courseId " +
                              "WHERE tc.empId = ? ORDER BY tc.courseId";
                psCourse = conn.c.prepareStatement(courseQuery);
                psCourse.setString(1, loggedInTeacherId);
            }

            ResultSet rsCourse = psCourse.executeQuery();
            cbCourse.addItem("--- Select a Course Section ---");
            while (rsCourse.next()) {
                cbCourse.addItem(rsCourse.getString("courseId") + " - " + 
                                 rsCourse.getString("courseName") + " [" + 
                                 rsCourse.getString("semester") + "]");
            }
            rsCourse.close();
            psCourse.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        cbCourse.addActionListener(e -> {
            if (cbCourse.getSelectedIndex() <= 0) {
                table.setModel(new javax.swing.table.DefaultTableModel()); // Clear table if default selected
                return;
            }

            try {
                String selected = cbCourse.getSelectedItem().toString();
                String courseId = selected.split(" - ")[0].trim();
                String semester = selected.substring(selected.indexOf("[") + 1, selected.indexOf("]")).trim();

                Conn conn = new Conn();
                String query;
                PreparedStatement ps;

                if (isAdmin) {
                    // Admin View 
                    query = "SELECT m.rollno AS 'Roll No', " +
                            "s.name AS 'Student Name', " +
                            "COALESCE(t.name, 'Not Assigned') AS 'Assigned Teacher', " +
                            "m.midMarks AS 'Mids (20)', " +
                            "m.assignmentMarks AS 'Assig (30)', " + 
                            "m.finalMarks AS 'Final (50)', " +
                            "m.totalObtained AS 'Total (100)', " +
                            "m.gpa AS 'GPA', " +
                            "m.status AS 'Result Status' " +
                            "FROM marks m " +
                            "JOIN student s ON m.rollno = s.rollno " +
                            "LEFT JOIN teacher_course tc ON m.courseId = tc.courseId AND m.semester = tc.semester " +
                            "LEFT JOIN teacher t ON tc.empId = t.empId " +
                            "WHERE m.courseId = ? AND m.semester = ? " +
                            "ORDER BY m.rollno ASC";
                    
                    ps = conn.c.prepareStatement(query);
                    ps.setString(1, courseId);
                    ps.setString(2, semester);
                } else {
                    // Teacher View 
                    query = "SELECT m.rollno AS 'Roll No', " +
                            "s.name AS 'Student Name', " +
                            "m.midMarks AS 'Mids (20)', " +
                            "m.assignmentMarks AS 'Assig (30)', " + 
                            "m.finalMarks AS 'Final (50)', " +
                            "m.totalObtained AS 'Total (100)', " +
                            "m.gpa AS 'GPA', " +
                            "m.status AS 'Result Status' " +
                            "FROM marks m " +
                            "JOIN student s ON m.rollno = s.rollno " +
                            "JOIN teacher_course tc ON m.courseId = tc.courseId AND m.semester = tc.semester " +
                            "WHERE m.courseId = ? AND m.semester = ? AND tc.empId = ? " +
                            "ORDER BY m.rollno ASC";
                    
                    ps = conn.c.prepareStatement(query);
                    ps.setString(1, courseId);
                    ps.setString(2, semester);
                    ps.setString(3, loggedInTeacherId);
                }

                ResultSet rs = ps.executeQuery();
                table.setModel(DbUtils.resultSetToTableModel(rs));

                table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                if (isAdmin) {
                    table.getColumnModel().getColumn(0).setPreferredWidth(100); // Roll No
                    table.getColumnModel().getColumn(1).setPreferredWidth(180); // Student Name
                    table.getColumnModel().getColumn(2).setPreferredWidth(180); // Teacher Name
                    table.getColumnModel().getColumn(3).setPreferredWidth(85);  // Mids
                    table.getColumnModel().getColumn(4).setPreferredWidth(85);  // Assig
                    table.getColumnModel().getColumn(5).setPreferredWidth(85);  // Final
                    table.getColumnModel().getColumn(6).setPreferredWidth(95);  // Total
                    table.getColumnModel().getColumn(7).setPreferredWidth(65);  // GPA
                    table.getColumnModel().getColumn(8).setPreferredWidth(110); // Status
                } else {
                    table.getColumnModel().getColumn(0).setPreferredWidth(100); // Roll No
                    table.getColumnModel().getColumn(1).setPreferredWidth(200); // Student Name
                    table.getColumnModel().getColumn(2).setPreferredWidth(85);  // Mids
                    table.getColumnModel().getColumn(3).setPreferredWidth(85);  // Assig
                    table.getColumnModel().getColumn(4).setPreferredWidth(85);  // Final
                    table.getColumnModel().getColumn(5).setPreferredWidth(95);  // Total
                    table.getColumnModel().getColumn(6).setPreferredWidth(65);  // GPA
                    table.getColumnModel().getColumn(7).setPreferredWidth(110); // Status
                }

                rs.close();
                ps.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        frame.setVisible(true);
    }
    // =========================================================
    // SHOW SINGLE STUDENT RESULT
    // =========================================================
public void showMarks(String rollno) {
    JFrame f = new JFrame("Official Result Card — " + rollno);
    f.setSize(750, 600); 
    f.setLocationRelativeTo(null);
    f.setLayout(null); 
    f.getContentPane().setBackground(Color.white);

    // --- HEADER ---
    JLabel uni = new JLabel("BAHRIA UNIVERSITY KARACHI CAMPUS", SwingConstants.CENTER);
    uni.setBounds(0, 20, 750, 28); 
    uni.setFont(new Font("Tahoma", Font.BOLD, 20));
    uni.setForeground(new Color(0, 51, 102)); 
    f.add(uni);

    JLabel semHeader = new JLabel("", SwingConstants.CENTER);
    semHeader.setBounds(0, 52, 750, 22); 
    semHeader.setFont(new Font("Tahoma", Font.BOLD, 14)); 
    f.add(semHeader);

    JSeparator s1 = new JSeparator(); 
    s1.setBounds(30, 85, 690, 2); 
    f.add(s1);

    // --- STUDENT INFO ---
    JLabel lRoll = new JLabel("Roll No : " + rollno); 
    lRoll.setBounds(40, 95, 300, 24);
    lRoll.setFont(new Font("Tahoma", Font.BOLD, 13)); 
    f.add(lRoll);

    JLabel lName = new JLabel("Name : "); 
    lName.setBounds(40, 120, 580, 24);
    lName.setFont(new Font("Tahoma", Font.BOLD, 13)); 
    f.add(lName);

    JSeparator s2 = new JSeparator(); 
    s2.setBounds(30, 150, 690, 2); 
    f.add(s2);

    // --- COLUMN HEADERS ---
    JLabel hSub = new JLabel("Subject Name"); hSub.setBounds(40, 160, 200, 24);
    hSub.setFont(new Font("Tahoma", Font.BOLD, 13)); f.add(hSub);

    JLabel hMid = new JLabel("Mid(20)"); hMid.setBounds(250, 160, 60, 24);
    hMid.setFont(new Font("Tahoma", Font.BOLD, 13)); f.add(hMid);

    JLabel hAss = new JLabel("Ass(30)"); hAss.setBounds(320, 160, 60, 24);
    hAss.setFont(new Font("Tahoma", Font.BOLD, 13)); f.add(hAss);

    JLabel hFin = new JLabel("Fin(50)"); hFin.setBounds(390, 160, 60, 24);
    hFin.setFont(new Font("Tahoma", Font.BOLD, 13)); f.add(hFin);

   JLabel hTot = new JLabel("Tot(100)"); hTot.setBounds(460, 160, 70, 24);
   hTot.setFont(new Font("Tahoma", Font.BOLD, 13)); f.add(hTot);

   JLabel hGPA = new JLabel("GPA"); hGPA.setBounds(540, 160, 50, 24);
   hGPA.setFont(new Font("Tahoma", Font.BOLD, 13)); f.add(hGPA);

   JLabel hStat = new JLabel("Status"); hStat.setBounds(620, 160, 100, 24);
   hStat.setFont(new Font("Tahoma", Font.BOLD, 13)); f.add(hStat);

    JSeparator s3 = new JSeparator(); 
    s3.setBounds(30, 185, 690, 2); 
    f.add(s3);

    // --- DATA ROWS ---
    boolean hasFail = false;
    int y = 195;
    try {
        Conn c = new Conn();
        
        ResultSet rsS = c.s.executeQuery("SELECT name, semester FROM student WHERE rollno='" + rollno + "'");
        if (rsS.next()) {
            lName.setText("Name : " + rsS.getString("name"));
            semHeader.setText("Examination Result — " + rsS.getString("semester"));
        }

        ResultSet rs = c.s.executeQuery(
    "SELECT co.courseName, m.midMarks, m.assignmentMarks, m.finalMarks, m.totalObtained, m.gpa, m.status " +
    "FROM marks m JOIN course co ON m.courseId=co.courseId " +
    "WHERE m.rollno='" + rollno + "' ORDER BY co.courseName");
        
        double totalGPA = 0; 
        int cnt = 0;

        while (rs.next()) {
          String status = rs.getString("status");
    
    // Subject
    JLabel lS = new JLabel(rs.getString("courseName")); lS.setBounds(40, y, 200, 26);
    lS.setFont(new Font("Tahoma", Font.PLAIN, 12)); f.add(lS);
    
    // Mid (20)
    JLabel lM = new JLabel(String.format("%.0f", rs.getDouble("midMarks"))); 
    lM.setBounds(250, y, 60, 26); f.add(lM);
    
    // Assignment (30) - NEW
    JLabel lA = new JLabel(String.format("%.0f", rs.getDouble("assignmentMarks"))); 
    lA.setBounds(320, y, 60, 26); f.add(lA);
    
    // Final (50)
    JLabel lF = new JLabel(String.format("%.0f", rs.getDouble("finalMarks"))); 
    lF.setBounds(390, y, 60, 26); f.add(lF);
    
    // Total (100)
    JLabel lT = new JLabel(String.format("%.0f", rs.getDouble("totalObtained"))); 
    lT.setBounds(460, y, 70, 26);
    lT.setFont(new Font("Tahoma", Font.BOLD, 12)); f.add(lT);

    // GPA
    JLabel lG = new JLabel(String.format("%.1f", rs.getDouble("gpa"))); 
    lG.setBounds(540, y, 50, 26); f.add(lG);

    // Pass/Fail
    JLabel lSt = new JLabel(status.equals("PASS") ? " PASS" : " FAIL");
    lSt.setBounds(620, y, 100, 26); 
    lSt.setFont(new Font("Tahoma", Font.BOLD, 12));
    lSt.setForeground(status.equals("PASS") ? new Color(0, 120, 0) : Color.RED); 
    f.add(lSt);

    JSeparator line = new JSeparator(); 
    line.setBounds(30, y + 26, 690, 1); 
    f.add(line);

    y += 35; 
    totalGPA += rs.getDouble("gpa"); 
    cnt++;
    if (status.equals("FAIL")) hasFail = true;
        }

        if (hasFail) {
            JLabel warn = new JLabel(" Warning: You must clear failing subjects to maintain promotion eligibility.");
            warn.setBounds(40, y, 690, 26); 
            warn.setFont(new Font("Tahoma", Font.BOLD, 12));
            warn.setForeground(Color.RED); 
            f.add(warn); 
            y += 40;
        }

    } catch (Exception ex) { ex.printStackTrace(); }

    JButton close = new JButton("Close Window"); 
    close.setBounds(300, y + 20, 150, 35);
    close.setBackground(new Color(50, 50, 50)); 
    close.setForeground(Color.white);
    close.setFont(new Font("Tahoma", Font.BOLD, 12));
    close.addActionListener(e -> f.dispose()); 
    f.add(close);

    f.setSize(750, y + 120); 
    f.setVisible(true);
}
    // =========================================================
    // ENTER MARKS
    // =========================================================
public void enterMarks() {

    JFrame frame = new JFrame("Enter Marks");
    frame.setSize(850, 550);
    frame.setLocationRelativeTo(null);
    frame.setLayout(null);
    frame.getContentPane().setBackground(Color.WHITE);

    JLabel heading = new JLabel("ENTER STUDENT MARKS");
    heading.setBounds(240, 20, 400, 35);
    heading.setFont(new Font("Tahoma", Font.BOLD, 24));
    frame.add(heading);

    JLabel lblCourse = new JLabel("Select Course");
    lblCourse.setBounds(40, 90, 120, 25);
    frame.add(lblCourse);

    JComboBox<String> cbCourse = new JComboBox<>();
    cbCourse.setBounds(180, 90, 300, 30);
    frame.add(cbCourse);

    JLabel lblStudent = new JLabel("Student");
    lblStudent.setBounds(40, 150, 120, 25);
    frame.add(lblStudent);

    JComboBox<String> cbStudent = new JComboBox<>();
    cbStudent.setBounds(180, 150, 300, 30);
    frame.add(cbStudent);

    JLabel lblSemester = new JLabel("Semester");
    lblSemester.setBounds(40, 210, 120, 25);
    frame.add(lblSemester);
    JTextField tfSemester = new JTextField();
    tfSemester.setEditable(false);
    tfSemester.setBounds(180, 210, 300, 30);
    frame.add(tfSemester);
    
    JLabel lblMid = new JLabel("Mid Marks (0-20):");
    lblMid.setBounds(40, 270, 150, 25);
    frame.add(lblMid);

    JTextField tfMid = new JTextField("0");
    tfMid.setBounds(200, 270, 280, 30);
    frame.add(tfMid);

    JLabel lblAssign = new JLabel("Assignments (0-30):");
    lblAssign.setBounds(40, 320, 150, 25);
    frame.add(lblAssign);

    JTextField tfAssign = new JTextField("0");
    tfAssign.setBounds(200, 320, 280, 30);
    frame.add(tfAssign);

    JLabel lblFinal = new JLabel("Final Marks (0-50):");
    lblFinal.setBounds(40, 370, 150, 25);
    frame.add(lblFinal);

    JTextField tfFinal = new JTextField("0");
    tfFinal.setBounds(200, 370, 280, 30);
    frame.add(tfFinal);

    JLabel lblTotal = new JLabel("Total (Calculated):");
    lblTotal.setBounds(40, 420, 150, 25);
    frame.add(lblTotal);

    JTextField tfTotal = new JTextField("0.0");
    tfTotal.setBounds(200, 420, 280, 30);
    tfTotal.setEditable(false); // Read-only total calculation!
    frame.add(tfTotal);

    JButton submit = new JButton("Submit");
    submit.setBounds(200, 480, 120, 35);
    frame.add(submit);

    // ── AUTOMATIC LIVE TOTAL CALCULATION ──
    java.awt.event.KeyAdapter autoCalculateTotal = new java.awt.event.KeyAdapter() {
        @Override
        public void keyReleased(java.awt.event.KeyEvent e) {
            try {
                double mid = tfMid.getText().trim().isEmpty() ? 0 : Double.parseDouble(tfMid.getText().trim());
                double assign = tfAssign.getText().trim().isEmpty() ? 0 : Double.parseDouble(tfAssign.getText().trim());
                double fin = tfFinal.getText().trim().isEmpty() ? 0 : Double.parseDouble(tfFinal.getText().trim());
                tfTotal.setText(String.valueOf(mid + assign + fin));
            } catch (NumberFormatException ignored) {}
        }
    };
    tfMid.addKeyListener(autoCalculateTotal);
    tfAssign.addKeyListener(autoCalculateTotal);
    tfFinal.addKeyListener(autoCalculateTotal);

    // LOAD COURSES
    try {
        Conn conn = new Conn();
        PreparedStatement ps = conn.c.prepareStatement(
                "SELECT tc.courseId, c.courseName, tc.semester " +
                "FROM teacher_course tc " +
                "JOIN course c ON tc.courseId=c.courseId " +
                "WHERE tc.empId=?"
        );
        ps.setString(1, loggedInTeacherId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            cbCourse.addItem(rs.getString("courseId") + " - " + rs.getString("courseName") + " [" + rs.getString("semester") + "]");
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    // LOAD STUDENTS
    cbCourse.addActionListener(e -> {
        if (cbCourse.getSelectedItem() == null) return;
        cbStudent.removeAllItems();
        try {
            String selected = cbCourse.getSelectedItem().toString();
            String courseId = selected.split(" - ")[0];
            String semester = selected.substring(selected.indexOf("[") + 1, selected.indexOf("]"));
            tfSemester.setText(semester);

            Conn conn = new Conn();
            PreparedStatement ps = conn.c.prepareStatement(
                "SELECT s.rollno, s.name FROM student_course sc " +
                "JOIN student s ON sc.rollno=s.rollno " +
                "WHERE sc.courseId=? AND sc.semester=?"
            );
            ps.setString(1, courseId);
            ps.setString(2, semester);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbStudent.addItem(rs.getString("rollno") + " - " + rs.getString("name"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    // SAVE MARKS
    submit.addActionListener(e -> {
        try {
            if (cbStudent.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(frame, "No student selected!");
                return;
            }
            String courseSelected = cbCourse.getSelectedItem().toString();
            String studentSelected = cbStudent.getSelectedItem().toString();
            String courseId = courseSelected.split(" - ")[0];
            String rollno = studentSelected.split(" - ")[0];
            String semester = tfSemester.getText();

            double mid = tfMid.getText().trim().isEmpty() ? 0 : Double.parseDouble(tfMid.getText().trim());
            double assign = tfAssign.getText().trim().isEmpty() ? 0 : Double.parseDouble(tfAssign.getText().trim());
            double fin = tfFinal.getText().trim().isEmpty() ? 0 : Double.parseDouble(tfFinal.getText().trim());

            double totalObtained = mid + assign + fin;
            double percentage = (totalObtained / 100.0) * 100;
            double gpa = 0.0;
            
            if (percentage >= 80) gpa = 4.0;
            else if (percentage >= 70) gpa = 3.0;
            else if (percentage >= 60) gpa = 2.0;
            else if (percentage >= 50) gpa = 1.0;

            String status = (totalObtained >= 50) ? "PASS" : "FAIL";

            Conn conn = new Conn();
            
            // Fixed Query: Included 'gpa' column accurately in insertion list mapping indices explicitly
            String query = "INSERT INTO marks (rollno, courseId, semester, midMarks, assignmentMarks, finalMarks, totalObtained, gpa, status) " +
                           "VALUES (?,?,?,?,?,?,?,?,?) " +
                           "ON DUPLICATE KEY UPDATE midMarks=?, assignmentMarks=?, finalMarks=?, totalObtained=?, gpa=?, status=?";
                           
            PreparedStatement ps = conn.c.prepareStatement(query);
            // Insert Bindings
            ps.setString(1, rollno); 
            ps.setString(2, courseId); 
            ps.setString(3, semester);
            ps.setDouble(4, mid); 
            ps.setDouble(5, assign); 
            ps.setDouble(6, fin); 
            ps.setDouble(7, totalObtained);
            ps.setDouble(8, gpa);
            ps.setString(9, status);
            
            // Update Bindings (9 through 14 corrected sequence mapping)
            ps.setDouble(10, mid);
            ps.setDouble(11, assign);
            ps.setDouble(12, fin);
            ps.setDouble(13, totalObtained);
            ps.setDouble(14, gpa);
            ps.setString(15, status);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(frame, "Marks Saved Successfully! Total: " + totalObtained + "/100");
            
            tfMid.setText("0"); 
            tfFinal.setText("0");
            tfAssign.setText("0");
            tfTotal.setText("0.0");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    });

    frame.setVisible(true);
}
}