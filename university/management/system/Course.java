package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import net.proteanit.sql.DbUtils;

public class Course {

    // ── ADD COURSE ────────────────────────────────────────────
    public void addCourse() {
        JFrame f = new JFrame("Add Course");
        f.setSize(520, 450);
        f.setLocationRelativeTo(null);
        f.setLayout(null);
        f.getContentPane().setBackground(Color.white);

        JLabel h = new JLabel("ADD COURSE", SwingConstants.CENTER);
        h.setBounds(0, 20, 520, 35);
        h.setFont(new Font("Tahoma", Font.BOLD, 20));
        h.setForeground(new Color(0, 51, 102));
        f.add(h);

        JLabel l1 = lbl("Course ID:");
        l1.setBounds(30, 80, 140, 25);
        f.add(l1);
        JTextField tfId = tf();
        tfId.setBounds(180, 80, 290, 28);
        f.add(tfId);

        JLabel l2 = lbl("Course Name:");
        l2.setBounds(30, 120, 140, 25);
        f.add(l2);
        JTextField tfName = tf();
        tfName.setBounds(180, 120, 290, 28);
        f.add(tfName);

        JLabel l3 = lbl("Department:");
        l3.setBounds(30, 160, 140, 25);
        f.add(l3);
        JComboBox<String> cbDept = new JComboBox<>();
        cbDept.setBounds(180, 160, 290, 28);
        cbDept.setBackground(Color.white);
        f.add(cbDept);

        JLabel lSem = lbl("Semester:");
        lSem.setBounds(30, 200, 140, 25);
        f.add(lSem);
        JComboBox<String> cbSem = new JComboBox<>(new String[]{
            "1st Semester", "2nd Semester", "3rd Semester", "4th Semester",
            "5th Semester", "6th Semester", "7th Semester", "8th Semester"
        });
        cbSem.setBounds(180, 200, 290, 28);
        cbSem.setBackground(Color.white);
        f.add(cbSem);

        JLabel l4 = lbl("Credit Hours:");
        l4.setBounds(30, 240, 140, 25);
        f.add(l4);
        JComboBox<Integer> cbCr = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        cbCr.setBounds(180, 240, 80, 28);
        cbCr.setBackground(Color.white);
        f.add(cbCr);

        try {
            Connection c = new Conn().c;
            PreparedStatement ps = c.prepareStatement(
                "SELECT deptId, deptName FROM department ORDER BY deptName"
            );
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                cbDept.addItem(rs.getString("deptId") + " - " + rs.getString("deptName"));
            rs.close(); ps.close(); c.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            err("Could not load departments: " + ex.getMessage());
        }

        JButton sub = subBtn(f, "Submit", 170, 330);
        JButton can = canBtn(f, "Cancel", 315, 330);
        f.setVisible(true);

        sub.addActionListener(e -> {
            if (tfId.getText().trim().isEmpty() || tfName.getText().trim().isEmpty()) {
                err("Course ID and Name are required.");
                return;
            }
            String deptId   = cbDept.getSelectedItem().toString().split(" - ")[0];
            String semester = cbSem.getSelectedItem().toString();
            try {
                Connection c = new Conn().c;
                PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO course (courseId, courseName, deptId, semester, creditHours) " +
                    "VALUES (?, ?, ?, ?, ?)"
                );
                ps.setString(1, tfId.getText().trim().toUpperCase());
                ps.setString(2, tfName.getText().trim());
                ps.setString(3, deptId);
                ps.setString(4, semester);
                ps.setInt(5, (Integer) cbCr.getSelectedItem());
                ps.executeUpdate();
                ps.close(); c.close();
                ok("Course added successfully!");
                f.dispose();
            } catch (Exception ex) {
                err(ex.getMessage());
            }
        });

        can.addActionListener(e -> f.dispose());
    }

    // ── VIEW COURSES ──────────────────────────────────────────
    public void viewCourses() {
        JFrame f = new JFrame("Courses");
        f.setSize(900, 450);
        f.setLocationRelativeTo(null);
        JTable table = new JTable();
        try {
            Connection c = new Conn().c;
            PreparedStatement ps = c.prepareStatement(
                "SELECT c.courseId AS 'Course ID', c.courseName AS 'Course Name', " +
                "       d.deptName AS 'Department', c.semester AS 'Semester', " +
                "       c.creditHours AS 'Credit Hours' " +
                "FROM course c JOIN department d ON c.deptId = d.deptId " +
                "ORDER BY c.courseId"
            );
            ResultSet rs = ps.executeQuery();
            table.setModel(DbUtils.resultSetToTableModel(rs));
            rs.close(); ps.close(); c.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            err("Error loading courses: " + ex.getMessage());
        }
        styleTable(table);
        f.add(new JScrollPane(table));
        f.setVisible(true);
    }

    // ── ASSIGN TEACHER TO COURSE ─────────────────────────────
    public void assignTeacherToCourse() {
        JFrame f = new JFrame("Assign Teacher to Course");
        f.setSize(550, 310);
        f.setLocationRelativeTo(null);
        f.setLayout(null);
        f.getContentPane().setBackground(Color.white);

        JLabel h = new JLabel("ASSIGN TEACHER TO COURSE", SwingConstants.CENTER);
        h.setBounds(0, 20, 550, 35);
        h.setFont(new Font("Tahoma", Font.BOLD, 18));
        h.setForeground(new Color(0, 51, 102));
        f.add(h);

        JLabel l1 = lbl("Teacher:");
        l1.setBounds(30, 80, 120, 25);
        f.add(l1);
        JComboBox<String> cbT = new JComboBox<>();
        cbT.setBounds(160, 80, 350, 28);
        cbT.setBackground(Color.white);
        f.add(cbT);

        JLabel l2 = lbl("Course:");
        l2.setBounds(30, 120, 120, 25);
        f.add(l2);
        JComboBox<String> cbC = new JComboBox<>();
        cbC.setBounds(160, 120, 350, 28);
        cbC.setBackground(Color.white);
        f.add(cbC);

        JLabel l3 = lbl("Semester:");
        l3.setBounds(30, 160, 120, 25);
        f.add(l3);
        JComboBox<String> cbSem = new JComboBox<>(new String[]{
            "1st Semester", "2nd Semester", "3rd Semester", "4th Semester",
            "5th Semester", "6th Semester", "7th Semester", "8th Semester"
        });
        cbSem.setBounds(160, 160, 350, 28);
        cbSem.setBackground(Color.white);
        f.add(cbSem);

        try {
            Connection c1 = new Conn().c;
            PreparedStatement ps1 = c1.prepareStatement(
                "SELECT empId, name FROM teacher ORDER BY name"
            );
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next())
                cbT.addItem(rs1.getString("empId") + " — " + rs1.getString("name"));
            rs1.close(); ps1.close(); c1.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            err("Could not load teachers: " + ex.getMessage());
        }

        try {
            Connection c2 = new Conn().c;
            PreparedStatement ps2 = c2.prepareStatement(
                "SELECT c.courseId, c.courseName, d.deptName " +
                "FROM course c JOIN department d ON c.deptId = d.deptId " +
                "ORDER BY c.courseId"
            );
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next())
                cbC.addItem(rs2.getString("courseId") + " — " +
                            rs2.getString("courseName") + " [" +
                            rs2.getString("deptName") + "]");
            rs2.close(); ps2.close(); c2.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            err("Could not load courses: " + ex.getMessage());
        }

        JButton sub = subBtn(f, "Assign", 170, 230);
        JButton can = canBtn(f, "Cancel", 315, 230);
        f.setVisible(true);

        sub.addActionListener(e -> {
            String empId    = cbT.getSelectedItem().toString().split(" — ")[0];
            String courseId = cbC.getSelectedItem().toString().split(" — ")[0];
            String semester = cbSem.getSelectedItem().toString();
            try {
                Connection c = new Conn().c;
                PreparedStatement ps = c.prepareStatement(
                    "INSERT IGNORE INTO teacher_course VALUES (?, ?, ?)"
                );
                ps.setString(1, empId);
                ps.setString(2, courseId);
                ps.setString(3, semester);
                ps.executeUpdate();
                ps.close(); c.close();
                ok("Teacher assigned successfully!");
                f.dispose();
            } catch (Exception ex) {
                err(ex.getMessage());
            }
        });

        can.addActionListener(e -> f.dispose());
    }

    // ── VIEW TEACHER-COURSE ASSIGNMENTS ──────────────────────
    public void viewAssignments() {
        JFrame f = new JFrame("Teacher-Course Assignments");
        f.setSize(900, 450);
        f.setLocationRelativeTo(null);
        JTable table = new JTable();
        try {
            Connection c = new Conn().c;
            PreparedStatement ps = c.prepareStatement(
                "SELECT tc.empId AS 'Emp ID', t.name AS 'Teacher', " +
                "       tc.courseId AS 'Course ID', c.courseName AS 'Course Name', " +
                "       d.deptName AS 'Department', tc.semester AS 'Semester' " +
                "FROM teacher_course tc " +
                "JOIN teacher t ON tc.empId = t.empId " +
                "JOIN course c  ON tc.courseId = c.courseId " +
                "JOIN department d ON c.deptId = d.deptId " +
                "ORDER BY t.name"
            );
            ResultSet rs = ps.executeQuery();
            table.setModel(DbUtils.resultSetToTableModel(rs));
            rs.close(); ps.close(); c.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            err("Error: " + ex.getMessage());
        }
        styleTable(table);
        f.add(new JScrollPane(table));
        f.setVisible(true);
    }

    // ── ENROLL STUDENT IN COURSE ─────────────────────────────
    public void enrollStudent() {
        JFrame f = new JFrame("Enroll Student in Course");
        f.setSize(560, 310);
        f.setLocationRelativeTo(null);
        f.setLayout(null);
        f.getContentPane().setBackground(Color.white);

        JLabel h = new JLabel("ENROLL STUDENT IN COURSE", SwingConstants.CENTER);
        h.setBounds(0, 20, 560, 35);
        h.setFont(new Font("Tahoma", Font.BOLD, 18));
        h.setForeground(new Color(0, 51, 102));
        f.add(h);

        JLabel l1 = lbl("Student:");
        l1.setBounds(30, 80, 120, 25);
        f.add(l1);
        JComboBox<String> cbS = new JComboBox<>();
        cbS.setBounds(160, 80, 360, 28);
        cbS.setBackground(Color.white);
        f.add(cbS);

        JLabel l2 = lbl("Course:");
        l2.setBounds(30, 120, 120, 25);
        f.add(l2);
        JComboBox<String> cbC = new JComboBox<>();
        cbC.setBounds(160, 120, 360, 28);
        cbC.setBackground(Color.white);
        f.add(cbC);

        JLabel lSem = lbl("Semester:");
        lSem.setBounds(30, 160, 120, 25);
        f.add(lSem);
        JComboBox<String> cbSem = new JComboBox<>(new String[]{
            "1st Semester", "2nd Semester", "3rd Semester", "4th Semester",
            "5th Semester", "6th Semester", "7th Semester", "8th Semester"
        });
        cbSem.setBounds(160, 160, 360, 28);
        cbSem.setBackground(Color.white);
        f.add(cbSem);

        try {
            Connection c1 = new Conn().c;
            PreparedStatement ps1 = c1.prepareStatement(
                "SELECT rollno, name FROM student ORDER BY name"
            );
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next())
                cbS.addItem(rs1.getString("rollno") + " — " + rs1.getString("name"));
            rs1.close(); ps1.close(); c1.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            err("Could not load students: " + ex.getMessage());
        }

        try {
            Connection c2 = new Conn().c;
            PreparedStatement ps2 = c2.prepareStatement(
                "SELECT courseId, courseName FROM course ORDER BY courseId"
            );
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next())
                cbC.addItem(rs2.getString("courseId") + " — " + rs2.getString("courseName"));
            rs2.close(); ps2.close(); c2.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            err("Could not load courses: " + ex.getMessage());
        }

        JButton sub = subBtn(f, "Enroll", 170, 230);
        JButton can = canBtn(f, "Cancel", 315, 230);
        f.setVisible(true);

        sub.addActionListener(e -> {
            if (cbS.getItemCount() == 0 || cbC.getItemCount() == 0) {
                err("No students or courses available.");
                return;
            }
            String rollno   = cbS.getSelectedItem().toString().split(" — ")[0];
            String courseId = cbC.getSelectedItem().toString().split(" — ")[0];
            String semester = cbSem.getSelectedItem().toString();
            try {
                Connection c = new Conn().c;
                PreparedStatement ps = c.prepareStatement(
                    "INSERT IGNORE INTO student_course VALUES (?, ?, ?)"
                );
                ps.setString(1, rollno);
                ps.setString(2, courseId);
                ps.setString(3, semester);
                ps.executeUpdate();
                ps.close(); c.close();
                ok("Student enrolled successfully!");
                f.dispose();
            } catch (Exception ex) {
                err(ex.getMessage());
            }
        });

        can.addActionListener(e -> f.dispose());
    }

    // ── VIEW ENROLLED STUDENTS (NEW) ──────────────────────────
    // Admin selects a course from a dropdown, the table below
    // instantly refreshes to show every student enrolled in it.
    public void viewEnrolledStudents() {
        JFrame f = new JFrame("Students Enrolled in Course");
        f.setSize(950, 550);
        f.setLocationRelativeTo(null);
        f.setLayout(new BorderLayout(0, 0));

     
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 10));
        top.setBackground(Color.white);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JLabel lCourse = new JLabel("Select Course:");
        lCourse.setFont(new Font("Tahoma", Font.BOLD, 13));
        top.add(lCourse);

        JComboBox<String> cbCourse = new JComboBox<>();
        cbCourse.setPreferredSize(new Dimension(380, 28));
        cbCourse.setBackground(Color.white);
        top.add(cbCourse);

        JLabel countLbl = new JLabel("Enrolled: 0");
        countLbl.setFont(new Font("Tahoma", Font.BOLD, 13));
        countLbl.setForeground(new Color(0, 100, 0));
        top.add(countLbl);

        f.add(top, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────
        JTable table = new JTable();
        styleTable(table);
        f.add(new JScrollPane(table), BorderLayout.CENTER);

        try {
            Connection c = new Conn().c;
            PreparedStatement ps = c.prepareStatement(
                "SELECT c.courseId, c.courseName, d.deptName " +
                "FROM course c JOIN department d ON c.deptId = d.deptId " +
                "ORDER BY c.courseId"
            );
            ResultSet rs = ps.executeQuery();
            cbCourse.addItem("--- Select a Course ---");
            while (rs.next())
                cbCourse.addItem(
                    rs.getString("courseId") + " — " +
                    rs.getString("courseName") + " [" +
                    rs.getString("deptName") + "]"
                );
            rs.close(); ps.close(); c.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            err("Could not load courses: " + ex.getMessage());
            f.dispose();
            return;
        }

        Runnable loadStudents = () -> {
            if (cbCourse.getSelectedIndex() <= 0) {
                table.setModel(new javax.swing.table.DefaultTableModel());
                countLbl.setText("Enrolled: 0");
                return;
            }
           
            String selected = (String) cbCourse.getSelectedItem();
            String courseId = selected.split(" — ")[0].trim();

            try {
                Connection c = new Conn().c;
                PreparedStatement ps = c.prepareStatement(
                    "SELECT s.rollno AS 'Roll No', s.name AS 'Student Name', " +
                    "       s.program AS 'Program', s.semester AS 'Current Semester', " +
                    "       sc.semester AS 'Enrolled Semester', s.phone AS 'Phone', " +
                    "       s.email AS 'Email' " +
                    "FROM student_course sc " +
                    "JOIN student s ON sc.rollno = s.rollno " +
                    "WHERE sc.courseId = ? " +
                    "ORDER BY s.name"
                );
                ps.setString(1, courseId);
                ResultSet rs = ps.executeQuery();
                table.setModel(DbUtils.resultSetToTableModel(rs));

                int rows = table.getRowCount();
                countLbl.setText("Enrolled: " + rows + " student" + (rows == 1 ? "" : "s"));

                rs.close(); ps.close(); c.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                err("Error loading students: " + ex.getMessage());
            }
        };

        cbCourse.addActionListener(ae -> loadStudents.run());

        f.setVisible(true);
    }

    // ── Helpers ───────────────────────────────────────────────
    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Tahoma", Font.BOLD, 13));
        return l;
    }

    private JTextField tf() {
        JTextField t = new JTextField();
        t.setFont(new Font("Tahoma", Font.PLAIN, 13));
        return t;
    }

    private JButton subBtn(JFrame f, String t, int x, int y) {
        JButton b = new JButton(t);
        b.setBounds(x, y, 120, 32);
        b.setBackground(new Color(0, 100, 0));
        b.setForeground(Color.white);
        b.setFont(new Font("Tahoma", Font.BOLD, 12));
        f.add(b);
        return b;
    }

    private JButton canBtn(JFrame f, String t, int x, int y) {
        JButton b = new JButton(t);
        b.setBounds(x, y, 120, 32);
        b.setBackground(Color.DARK_GRAY);
        b.setForeground(Color.white);
        b.setFont(new Font("Tahoma", Font.BOLD, 12));
        f.add(b);
        return b;
    }

    private void styleTable(JTable t) {
        t.setFont(new Font("Tahoma", Font.PLAIN, 13));
        t.setRowHeight(22);
        t.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
    }

    private void ok(String m) {
        JOptionPane.showMessageDialog(null, m);
    }

    private void err(String m) {
        JOptionPane.showMessageDialog(null, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
}