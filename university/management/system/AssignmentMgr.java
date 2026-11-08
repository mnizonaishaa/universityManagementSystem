package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import net.proteanit.sql.DbUtils;

public class AssignmentMgr {

    private final String loggedInId;

    public AssignmentMgr(String loggedInId) {
        this.loggedInId = loggedInId;
    }

    // =========================================================
    // CREATE ASSIGNMENT (FACULTY)
    // BUG FIX: Conn was being opened and ResultSet closed before
    // the combo box was used. Now uses a dedicated try-with-resources
    // block. Also added error dialog if no courses found.
    // =========================================================
    public void createAssignment() {
        JFrame frame = new JFrame("Create Assignment");
        frame.setSize(650, 550);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);

        JLabel heading = new JLabel("CREATE ASSIGNMENT");
        heading.setBounds(200, 20, 300, 35);
        heading.setFont(new Font("Tahoma", Font.BOLD, 22));
        frame.add(heading);

        JLabel lblCourse = new JLabel("Course:");
        lblCourse.setBounds(50, 80, 120, 30);
        lblCourse.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblCourse);

        JComboBox<String> cbCourse = new JComboBox<>();
        cbCourse.setBounds(180, 80, 350, 30);
        frame.add(cbCourse);

        try {
            Connection c = new Conn().c;
            PreparedStatement ps = c.prepareStatement(
                "SELECT courseId FROM teacher_course WHERE empId = ?"
            );
            ps.setString(1, loggedInId);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                cbCourse.addItem(rs.getString("courseId"));
                count++;
            }
            rs.close();
            ps.close();
            c.close();

            if (count == 0) {
                JOptionPane.showMessageDialog(frame,
                    "No courses assigned to you yet.\nAsk admin to assign you a course first.",
                    "No Courses Found", JOptionPane.WARNING_MESSAGE);
                frame.dispose();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database Error loading courses: " + e.getMessage());
            frame.dispose();
            return;
        }

        JLabel lblTitle = new JLabel("Title:");
        lblTitle.setBounds(50, 130, 120, 30);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblTitle);

        JTextField tfTitle = new JTextField();
        tfTitle.setBounds(180, 130, 350, 30);
        frame.add(tfTitle);

        JLabel lblDesc = new JLabel("Description:");
        lblDesc.setBounds(50, 180, 120, 30);
        lblDesc.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblDesc);

        JTextArea taDesc = new JTextArea();
        taDesc.setLineWrap(true);
        taDesc.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(taDesc);
        sp.setBounds(180, 180, 350, 120);
        frame.add(sp);

        JLabel lblDue = new JLabel("Due Date:");
        lblDue.setBounds(50, 320, 120, 30);
        lblDue.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblDue);

        JTextField tfDue = new JTextField("2026-05-20");
        tfDue.setBounds(180, 320, 200, 30);
        frame.add(tfDue);

        JLabel lblMarks = new JLabel("Total Marks:");
        lblMarks.setBounds(50, 370, 120, 30);
        lblMarks.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblMarks);

        JTextField tfMarks = new JTextField();
        tfMarks.setBounds(180, 370, 120, 30);
        frame.add(tfMarks);

        JButton submit = new JButton("POST ASSIGNMENT");
        submit.setBounds(220, 440, 220, 40);
        submit.setBackground(new Color(0, 102, 51));
        submit.setForeground(Color.WHITE);
        submit.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(submit);

        submit.addActionListener(ae -> {
            String course = (String) cbCourse.getSelectedItem();
            String title  = tfTitle.getText().trim();
            String desc   = taDesc.getText().trim();
            String due    = tfDue.getText().trim();
            String marks  = tfMarks.getText().trim();

            if (course == null || title.isEmpty() || due.isEmpty() || marks.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please fill all required fields.");
                return;
            }

            try {
                int totalMarks = Integer.parseInt(marks);
                Connection c = new Conn().c;
                PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO assignment(courseId, empId, title, description, dueDate, totalMarks, postedOn) " +
                    "VALUES (?, ?, ?, ?, ?, ?, CURDATE())"
                );
                ps.setString(1, course);
                ps.setString(2, loggedInId);
                ps.setString(3, title);
                ps.setString(4, desc);
                ps.setString(5, due);
                ps.setInt(6, totalMarks);
                ps.executeUpdate();
                ps.close();
                c.close();
                JOptionPane.showMessageDialog(frame, "Assignment Posted Successfully!");
                frame.dispose();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(frame, "Total Marks must be a number.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage());
            }
        });

        frame.setVisible(true);
    }

    // =========================================================
    // VIEW ASSIGNMENTS (FACULTY)
    // =========================================================
    public void viewFacultyAssignments() {
        JFrame frame = new JFrame("My Assignments");
        frame.setSize(950, 600);
        frame.setLocationRelativeTo(null);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        frame.add(new JScrollPane(area));

        try {
            Connection c = new Conn().c;
            PreparedStatement ps = c.prepareStatement(
                "SELECT * FROM assignment WHERE empId = ? ORDER BY postedOn DESC"
            );
            ps.setString(1, loggedInId);
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder();
            sb.append("\n==================== MY ASSIGNMENTS ====================\n\n");
            boolean found = false;
            while (rs.next()) {
                found = true;
                sb.append("Assignment ID : ").append(rs.getInt("assignId")).append("\n");
                sb.append("Course ID     : ").append(rs.getString("courseId")).append("\n");
                sb.append("Title         : ").append(rs.getString("title")).append("\n");
                sb.append("Description   : ").append(rs.getString("description")).append("\n");
                sb.append("Due Date      : ").append(rs.getString("dueDate")).append("\n");
                sb.append("Total Marks   : ").append(rs.getInt("totalMarks")).append("\n");
                sb.append("Posted On     : ").append(rs.getString("postedOn")).append("\n");
                sb.append("------------------------------------------------------------\n\n");
            }
            if (!found) sb.append("No assignments posted yet.\n");
            area.setText(sb.toString());
            rs.close();
            ps.close();
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage());
        }
        frame.setVisible(true);
    }

    // =========================================================
    // VIEW STUDENT ASSIGNMENTS
    // =========================================================
    public void viewStudentAssignments() {
        JFrame frame = new JFrame("My Assignments");
        frame.setSize(950, 620);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(0, 0));

        // ── Top filter panel: course dropdown ─────────────────
        // Students pick which enrolled course to view assignments for.
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JLabel lbl = new JLabel("Filter by Course:");
        lbl.setFont(new Font("Tahoma", Font.BOLD, 13));
        filterPanel.add(lbl);

        JComboBox<String> cbCourse = new JComboBox<>();
        cbCourse.setPreferredSize(new Dimension(380, 28));
        cbCourse.setBackground(Color.WHITE);
        filterPanel.add(cbCourse);

        frame.add(filterPanel, BorderLayout.NORTH);

        // ── Assignment display area ────────────────────────────
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        frame.add(new JScrollPane(area), BorderLayout.CENTER);

        // ── Submit button at bottom ────────────────────────────
        JButton submitBtn = new JButton("SUBMIT ASSIGNMENT");
        submitBtn.setBackground(Color.BLACK);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        frame.add(submitBtn, BorderLayout.SOUTH);

        // ── Load the student's enrolled courses into combo box ─
        // Each entry shows: "CS101 — Programming Fundamentals"
        try {
            Connection c = new Conn().c;
            PreparedStatement ps = c.prepareStatement(
                "SELECT sc.courseId, co.courseName " +
                "FROM student_course sc " +
                "JOIN course co ON sc.courseId = co.courseId " +
                "WHERE sc.rollno = ? " +
                "ORDER BY sc.courseId"
            );
            ps.setString(1, loggedInId);
            ResultSet rs = ps.executeQuery();
            cbCourse.addItem("--- All Courses ---");
            int count = 0;
            while (rs.next()) {
                cbCourse.addItem(
                    rs.getString("courseId") + " — " + rs.getString("courseName")
                );
                count++;
            }
            rs.close(); ps.close(); c.close();

            if (count == 0) {
                area.setText("\n  You are not enrolled in any courses yet.\n" +
                             "  Ask the admin to enroll you in a course first.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage());
        }

        // ── Runnable: load assignments for selected course ─────
        Runnable loadAssignments = () -> {
            String selectedCourse = (String) cbCourse.getSelectedItem();
            boolean allCourses = (cbCourse.getSelectedIndex() <= 0);

            try {
                Connection c = new Conn().c;
                PreparedStatement ps;

                if (allCourses) {
                    // Show assignments from ALL enrolled courses
                    ps = c.prepareStatement(
                        "SELECT a.assignId, a.courseId, a.title, a.description, " +
                        "       a.dueDate, a.totalMarks " +
                        "FROM assignment a " +
                        "JOIN student_course sc ON a.courseId = sc.courseId " +
                        "WHERE sc.rollno = ? " +
                        "ORDER BY a.dueDate"
                    );
                    ps.setString(1, loggedInId);
                } else {
                    // Filter by the selected course only
                    String courseId = selectedCourse.split(" — ")[0].trim();
                    ps = c.prepareStatement(
                        "SELECT a.assignId, a.courseId, a.title, a.description, " +
                        "       a.dueDate, a.totalMarks " +
                        "FROM assignment a " +
                        "JOIN student_course sc ON a.courseId = sc.courseId " +
                        "WHERE sc.rollno = ? AND a.courseId = ? " +
                        "ORDER BY a.dueDate"
                    );
                    ps.setString(1, loggedInId);
                    ps.setString(2, courseId);
                }

                ResultSet rs = ps.executeQuery();
                StringBuilder sb = new StringBuilder();
                sb.append("\n==================== ASSIGNMENTS ====================\n\n");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    // Highlight overdue assignments so students notice them
                    String dueStr  = rs.getString("dueDate");
                    String overdue = "";
                    try {
                        java.time.LocalDate due = java.time.LocalDate.parse(dueStr);
                        if (java.time.LocalDate.now().isAfter(due)) overdue = "  *** OVERDUE ***";
                    } catch (Exception ignored) {}

                    sb.append("Assignment ID : ").append(rs.getInt("assignId")).append("\n");
                    sb.append("Course ID     : ").append(rs.getString("courseId")).append("\n");
                    sb.append("Title         : ").append(rs.getString("title")).append("\n");
                    sb.append("Description   : ").append(rs.getString("description")).append("\n");
                    sb.append("Due Date      : ").append(dueStr).append(overdue).append("\n");
                    sb.append("Total Marks   : ").append(rs.getInt("totalMarks")).append("\n");
                    sb.append("------------------------------------------------------------\n\n");
                }
                if (!found) {
                    sb.append("  No assignments found for the selected course.\n");
                }
                area.setText(sb.toString());
                area.setCaretPosition(0); // Scroll to top
                rs.close(); ps.close(); c.close();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage());
            }
        };

        // Register listener AFTER combo is populated, then load immediately
        cbCourse.addActionListener(ae -> loadAssignments.run());
        loadAssignments.run();

        submitBtn.addActionListener(ae -> submitAssignment());
        frame.setVisible(true);
    }

    // =========================================================
    // SUBMIT ASSIGNMENT
    // =========================================================
    public void submitAssignment() {
        String assignId = JOptionPane.showInputDialog(null, "Enter Assignment ID");
        if (assignId == null || assignId.trim().isEmpty()) return;

        try {
            Connection c = new Conn().c;

            // Check due date
            PreparedStatement dps = c.prepareStatement(
                "SELECT dueDate FROM assignment WHERE assignId=?"
            );
            dps.setInt(1, Integer.parseInt(assignId.trim()));
            ResultSet drs = dps.executeQuery();

            if (drs.next()) {
                java.time.LocalDate due   = java.time.LocalDate.parse(drs.getString("dueDate"));
                java.time.LocalDate today = java.time.LocalDate.now();
                if (today.isAfter(due)) {
                    JOptionPane.showMessageDialog(null, "Submission Closed! Due date has passed.");
                    drs.close(); dps.close(); c.close();
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(null, "Assignment ID not found.");
                drs.close(); dps.close(); c.close();
                return;
            }
            drs.close(); dps.close();

            // Check duplicate submission
            PreparedStatement cps = c.prepareStatement(
                "SELECT subId FROM submission WHERE assignId=? AND rollno=?"
            );
            cps.setInt(1, Integer.parseInt(assignId.trim()));
            cps.setString(2, loggedInId);
            ResultSet crs = cps.executeQuery();
            if (crs.next()) {
                JOptionPane.showMessageDialog(null, "You have already submitted this assignment!");
                crs.close(); cps.close(); c.close();
                return;
            }
            crs.close(); cps.close();

            // Insert submission
            PreparedStatement ps = c.prepareStatement(
                "INSERT INTO submission(assignId, rollno, submittedOn) VALUES (?, ?, CURDATE())"
            );
            ps.setInt(1, Integer.parseInt(assignId.trim()));
            ps.setString(2, loggedInId);
            int inserted = ps.executeUpdate();
            ps.close(); c.close();

            if (inserted > 0) {
                JOptionPane.showMessageDialog(null, "Assignment Submitted Successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Submission Failed!");
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Assignment ID must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
    }

    // =========================================================
    // DELETE SUBMISSION (STUDENT)
    // =========================================================
    public void deleteSubmission() {
        String submissionId = JOptionPane.showInputDialog(null, "Enter Submission ID");
        if (submissionId == null || submissionId.trim().isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(null,
            "Are you sure you want to delete this submission?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection c = new Conn().c;
            PreparedStatement dps = c.prepareStatement(
                "SELECT a.dueDate FROM submission s " +
                "JOIN assignment a ON s.assignId = a.assignId WHERE s.subId=?"
            );
            dps.setInt(1, Integer.parseInt(submissionId.trim()));
            ResultSet rs = dps.executeQuery();

            if (rs.next()) {
                java.time.LocalDate due   = java.time.LocalDate.parse(rs.getString("dueDate"));
                java.time.LocalDate today = java.time.LocalDate.now();
                if (today.isAfter(due)) {
                    JOptionPane.showMessageDialog(null, "Cannot delete submission after due date!");
                    rs.close(); dps.close(); c.close();
                    return;
                }
            }
            rs.close(); dps.close();

            PreparedStatement ps = c.prepareStatement(
                "DELETE FROM submission WHERE subId=? AND rollno=?"
            );
            ps.setInt(1, Integer.parseInt(submissionId.trim()));
            ps.setString(2, loggedInId);
            int deleted = ps.executeUpdate();
            ps.close(); c.close();

            if (deleted > 0) {
                JOptionPane.showMessageDialog(null, "Submission Deleted Successfully!");
            } else {
                JOptionPane.showMessageDialog(null, "Submission Not Found or Not Yours!");
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Submission ID must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // UPDATE ASSIGNMENT (FACULTY)
    // =========================================================
    public void updateAssignment() {
        JFrame frame = new JFrame("Update Assignment");
        frame.setSize(700, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);

        JLabel heading = new JLabel("UPDATE ASSIGNMENT");
        heading.setBounds(200, 20, 320, 35);
        heading.setFont(new Font("Tahoma", Font.BOLD, 24));
        frame.add(heading);

        JLabel lblAssign = new JLabel("Assignment ID:");
        lblAssign.setBounds(50, 80, 140, 30);
        lblAssign.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblAssign);

        JComboBox<String> cbAssign = new JComboBox<>();
        cbAssign.setBounds(200, 80, 250, 30);
        frame.add(cbAssign);

        // FIX: Load combo box with its own dedicated connection
        try {
            Connection c = new Conn().c;
            PreparedStatement ps = c.prepareStatement(
                "SELECT assignId FROM assignment WHERE empId=? ORDER BY assignId DESC"
            );
            ps.setString(1, loggedInId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) cbAssign.addItem(rs.getString("assignId"));
            rs.close(); ps.close(); c.close();

            if (cbAssign.getItemCount() == 0) {
                JOptionPane.showMessageDialog(frame, "No Assignments Found!");
                frame.dispose();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error loading assignments: " + e.getMessage());
            frame.dispose();
            return;
        }

        JLabel lblTitle = new JLabel("Title:");
        lblTitle.setBounds(50, 140, 120, 30);
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblTitle);

        JTextField tfTitle = new JTextField();
        tfTitle.setBounds(200, 140, 350, 30);
        frame.add(tfTitle);

        JLabel lblDesc = new JLabel("Description:");
        lblDesc.setBounds(50, 200, 120, 30);
        lblDesc.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblDesc);

        JTextArea taDesc = new JTextArea();
        JScrollPane sp = new JScrollPane(taDesc);
        sp.setBounds(200, 200, 350, 120);
        frame.add(sp);

        JLabel lblDue = new JLabel("Due Date:");
        lblDue.setBounds(50, 350, 120, 30);
        lblDue.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblDue);

        JTextField tfDue = new JTextField();
        tfDue.setBounds(200, 350, 200, 30);
        frame.add(tfDue);

        JLabel lblMarks = new JLabel("Total Marks:");
        lblMarks.setBounds(50, 410, 120, 30);
        lblMarks.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblMarks);

        JTextField tfMarks = new JTextField();
        tfMarks.setBounds(200, 410, 120, 30);
        frame.add(tfMarks);

        // Lambda to load selected assignment data into form fields
        Runnable loadData = () -> {
            String selected = (String) cbAssign.getSelectedItem();
            if (selected == null) return;
            try {
                Connection c = new Conn().c;
                PreparedStatement ps = c.prepareStatement(
                    "SELECT * FROM assignment WHERE assignId=?"
                );
                ps.setInt(1, Integer.parseInt(selected));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    tfTitle.setText(rs.getString("title"));
                    taDesc.setText(rs.getString("description"));
                    tfDue.setText(rs.getString("dueDate"));
                    tfMarks.setText(String.valueOf(rs.getInt("totalMarks")));
                }
                rs.close(); ps.close(); c.close();
            } catch (Exception e) { e.printStackTrace(); }
        };

        loadData.run();
        cbAssign.addActionListener(ae -> loadData.run());

        JButton updateBtn = new JButton("UPDATE");
        updateBtn.setBounds(220, 500, 150, 40);
        updateBtn.setBackground(new Color(0, 102, 204));
        updateBtn.setForeground(Color.WHITE);
        updateBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(updateBtn);

        updateBtn.addActionListener(ae -> {
            try {
                Connection c = new Conn().c;
                PreparedStatement ps = c.prepareStatement(
                    "UPDATE assignment SET title=?, description=?, dueDate=?, totalMarks=? WHERE assignId=?"
                );
                ps.setString(1, tfTitle.getText().trim());
                ps.setString(2, taDesc.getText().trim());
                ps.setString(3, tfDue.getText().trim());
                ps.setInt(4, Integer.parseInt(tfMarks.getText().trim()));
                ps.setInt(5, Integer.parseInt((String) cbAssign.getSelectedItem()));
                ps.executeUpdate();
                ps.close(); c.close();
                JOptionPane.showMessageDialog(frame, "Assignment Updated Successfully!");
                frame.dispose();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error updating: " + e.getMessage());
            }
        });

        frame.setVisible(true);
    }

    // =========================================================
    // DELETE ASSIGNMENT (FACULTY)
    // =========================================================
    public void deleteAssignment() {
        JFrame frame = new JFrame("Delete Assignment");
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);

        JLabel heading = new JLabel("DELETE ASSIGNMENT");
        heading.setBounds(120, 20, 300, 35);
        heading.setFont(new Font("Tahoma", Font.BOLD, 24));
        frame.add(heading);

        JLabel lblAssign = new JLabel("Select Assignment:");
        lblAssign.setBounds(50, 80, 150, 30);
        lblAssign.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblAssign);

        JComboBox<String> cbAssign = new JComboBox<>();
        cbAssign.setBounds(200, 80, 200, 30);
        frame.add(cbAssign);

        // FIX: Own dedicated connection for loading
        try {
            Connection c = new Conn().c;
            PreparedStatement ps = c.prepareStatement(
                "SELECT assignId, title FROM assignment WHERE empId=? ORDER BY assignId DESC"
            );
            ps.setString(1, loggedInId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cbAssign.addItem(rs.getString("assignId") + " - " + rs.getString("title"));
            }
            rs.close(); ps.close(); c.close();

            if (cbAssign.getItemCount() == 0) {
                JOptionPane.showMessageDialog(frame, "No Assignments Found!");
                frame.dispose();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            frame.dispose();
            return;
        }

        JButton deleteBtn = new JButton("DELETE");
        deleteBtn.setBounds(180, 180, 150, 40);
        deleteBtn.setBackground(Color.RED);
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(deleteBtn);

        deleteBtn.addActionListener(ae -> {
            int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete this assignment?\nAll student submissions will also be deleted.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try {
                // Extract the numeric ID from "5 - Assignment Title"
                String selected = (String) cbAssign.getSelectedItem();
                int assignId = Integer.parseInt(selected.split(" - ")[0].trim());

                Connection c = new Conn().c;

                // Delete child submissions first to avoid FK violation
                PreparedStatement psDel = c.prepareStatement(
                    "DELETE FROM submission WHERE assignId=?"
                );
                psDel.setInt(1, assignId);
                psDel.executeUpdate();
                psDel.close();

                PreparedStatement ps = c.prepareStatement(
                    "DELETE FROM assignment WHERE assignId=? AND empId=?"
                );
                ps.setInt(1, assignId);
                ps.setString(2, loggedInId);
                int deleted = ps.executeUpdate();
                ps.close(); c.close();

                if (deleted > 0) {
                    JOptionPane.showMessageDialog(frame, "Deleted Successfully!");
                    cbAssign.removeItem(selected);
                    if (cbAssign.getItemCount() == 0) frame.dispose();
                } else {
                    JOptionPane.showMessageDialog(frame, "Delete failed. Assignment may not belong to you.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            }
        });

        frame.setVisible(true);
    }

    // =========================================================
    // GRADE SUBMISSIONS (FACULTY VIEW)
    // =========================================================
    public void gradeSubmissions() {
        JFrame frame = new JFrame("Grade Submissions");
        frame.setSize(1000, 650);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        frame.add(new JScrollPane(area), BorderLayout.CENTER);

        JButton gradeBtn = new JButton("GRADE A SUBMISSION");
        gradeBtn.setBackground(new Color(0, 102, 51));
        gradeBtn.setForeground(Color.WHITE);
        gradeBtn.setFont(new Font("Tahoma", Font.BOLD, 13));
        frame.add(gradeBtn, BorderLayout.SOUTH);

        Runnable loadSubmissions = () -> {
            try {
                Connection c = new Conn().c;
                PreparedStatement ps = c.prepareStatement(
                    "SELECT s.subId, s.assignId, s.rollno, s.marksObtained, " +
                    "       s.submittedOn, s.status, s.feedback, a.title, a.totalMarks " +
                    "FROM submission s " +
                    "JOIN assignment a ON s.assignId = a.assignId " +
                    "WHERE a.empId = ? " +
                    "ORDER BY s.submittedOn DESC"
                );
                ps.setString(1, loggedInId);
                ResultSet rs = ps.executeQuery();

                StringBuilder sb = new StringBuilder();
                sb.append("\n==================== STUDENT SUBMISSIONS ====================\n\n");

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    int    marks      = rs.getInt("marksObtained");
                    int    total      = rs.getInt("totalMarks");
                    String status     = rs.getString("status");
                    String feedback   = rs.getString("feedback");
                    String marksLabel = (marks == 0 && "SUBMITTED".equals(status))
                                        ? "Not Graded Yet"
                                        : marks + " / " + total;

                    sb.append("Submission ID : ").append(rs.getInt("subId")).append("\n");
                    sb.append("Assignment    : ").append(rs.getString("title"))
                      .append("  [ID: ").append(rs.getInt("assignId")).append("]\n");
                    sb.append("Student Roll  : ").append(rs.getString("rollno")).append("\n");
                    sb.append("Marks Awarded : ").append(marksLabel).append("\n");
                    sb.append("Status        : ").append(status).append("\n");
                    sb.append("Feedback      : ").append(
                        (feedback != null && !feedback.isEmpty()) ? feedback : "(none)"
                    ).append("\n");
                    sb.append("Submitted On  : ").append(rs.getString("submittedOn")).append("\n");
                    sb.append("------------------------------------------------------------\n\n");
                }

                if (!found) {
                    sb.append("No student submissions found for your assignments yet.\n");
                }
                area.setText(sb.toString());
                rs.close(); ps.close(); c.close();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Database Error: " + e.getMessage());
            }
        };

        loadSubmissions.run();

        gradeBtn.addActionListener(ae -> {
            String subIdStr = JOptionPane.showInputDialog(frame, "Enter Submission ID to grade:");
            if (subIdStr == null || subIdStr.trim().isEmpty()) return;

            String marksStr = JOptionPane.showInputDialog(frame, "Enter marks to award:");
            if (marksStr == null || marksStr.trim().isEmpty()) return;

            String feedback = JOptionPane.showInputDialog(frame, "Enter feedback (optional):");

            try {
                int subId = Integer.parseInt(subIdStr.trim());
                int marks = Integer.parseInt(marksStr.trim());

                Connection c = new Conn().c;
                PreparedStatement ps = c.prepareStatement(
                    "UPDATE submission SET marksObtained=?, feedback=?, status='GRADED' " +
                    "WHERE subId=? AND assignId IN " +
                    "(SELECT assignId FROM assignment WHERE empId=?)"
                );
                ps.setInt(1, marks);
                ps.setString(2, feedback != null ? feedback.trim() : "");
                ps.setInt(3, subId);
                ps.setString(4, loggedInId);
                int updated = ps.executeUpdate();
                ps.close(); c.close();

                if (updated > 0) {
                    JOptionPane.showMessageDialog(frame, "Marks saved successfully!");
                    loadSubmissions.run(); // Refresh the view
                } else {
                    JOptionPane.showMessageDialog(frame,
                        "Update failed. Submission ID may not belong to your assignments.");
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(frame, "Submission ID and Marks must be numbers.");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            }
        });

        frame.setVisible(true);
    }

    // =========================================================
    // VIEW ALL ASSIGNMENTS (ADMIN — dropdown filter)
    // =========================================================
    public void viewAllAssignments() {
        JFrame frame = new JFrame("Global Assignments Portal (Admin)");
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.add(new JLabel("Select Course Filter:"));

        JComboBox<String> cbCourseFilter = new JComboBox<>();
        cbCourseFilter.setPreferredSize(new Dimension(300, 28));
        filterPanel.add(cbCourseFilter);
        frame.add(filterPanel, BorderLayout.NORTH);

        JTable table = new JTable();
        table.setRowHeight(25);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        
        try {
            Connection c = new Conn().c;
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT DISTINCT courseId FROM assignment ORDER BY courseId"
            );
            cbCourseFilter.addItem("--- Show All Courses ---");
            while (rs.next()) cbCourseFilter.addItem(rs.getString("courseId"));
            rs.close(); stmt.close(); c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Runnable loadTable = () -> {
            try {
                Connection c = new Conn().c;
                PreparedStatement ps;
                String baseQuery =
                    "SELECT a.assignId AS 'Assign ID', a.courseId AS 'Course ID', " +
                    "       a.title AS 'Title', a.dueDate AS 'Due Date', " +
                    "       a.totalMarks AS 'Total Marks', t.name AS 'Instructor' " +
                    "FROM assignment a JOIN teacher t ON a.empId = t.empId ";

                if (cbCourseFilter.getSelectedIndex() <= 0) {
                    ps = c.prepareStatement(baseQuery + "ORDER BY a.assignId DESC");
                } else {
                    ps = c.prepareStatement(baseQuery + "WHERE a.courseId = ? ORDER BY a.assignId DESC");
                    ps.setString(1, (String) cbCourseFilter.getSelectedItem());
                }
                ResultSet rs = ps.executeQuery();
                table.setModel(DbUtils.resultSetToTableModel(rs));
                rs.close(); ps.close(); c.close();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            }
        };

        cbCourseFilter.addActionListener(ae -> loadTable.run());
        loadTable.run(); // Load "Show All" data immediately on open

        frame.setVisible(true);
    }

    // =========================================================
    // VIEW ALL SUBMISSIONS (ADMIN — dropdown filter)
    // =========================================================
    public void viewAllSubmissions() {
        JFrame frame = new JFrame("Global Student Submissions Portal (Admin)");
        frame.setSize(1000, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.add(new JLabel("Select Assignment Filter:"));

        JComboBox<String> cbAssignFilter = new JComboBox<>();
        cbAssignFilter.setPreferredSize(new Dimension(400, 28));
        filterPanel.add(cbAssignFilter);
        frame.add(filterPanel, BorderLayout.NORTH);

        JTable table = new JTable();
        table.setRowHeight(25);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        try {
            Connection c = new Conn().c;
            Statement stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT assignId, title, courseId FROM assignment ORDER BY assignId DESC"
            );
            cbAssignFilter.addItem("--- Show All Submissions ---");
            while (rs.next()) {
                cbAssignFilter.addItem(
                    rs.getString("assignId") + " - " +
                    rs.getString("title") + " [" +
                    rs.getString("courseId") + "]"
                );
            }
            rs.close(); stmt.close(); c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Runnable loadTable = () -> {
            try {
                Connection c = new Conn().c;
                PreparedStatement ps;
                String baseQuery =
                    "SELECT s.subId AS 'Submission ID', s.assignId AS 'Assign ID', " +
                    "       a.title AS 'Assignment Title', s.rollno AS 'Roll Number', " +
                    "       s.marksObtained AS 'Marks', s.status AS 'Status', " +
                    "       s.submittedOn AS 'Date' " +
                    "FROM submission s JOIN assignment a ON s.assignId = a.assignId ";

                if (cbAssignFilter.getSelectedIndex() <= 0) {
                    ps = c.prepareStatement(baseQuery + "ORDER BY s.subId DESC");
                } else {
                    String selected = (String) cbAssignFilter.getSelectedItem();
                    int assignId = Integer.parseInt(selected.split(" - ")[0].trim());
                    ps = c.prepareStatement(baseQuery + "WHERE s.assignId = ? ORDER BY s.subId DESC");
                    ps.setInt(1, assignId);
                }
                ResultSet rs = ps.executeQuery();
                table.setModel(DbUtils.resultSetToTableModel(rs));
                rs.close(); ps.close(); c.close();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage());
            }
        };

        cbAssignFilter.addActionListener(ae -> loadTable.run());
        loadTable.run();

        frame.setVisible(true);
    }
}