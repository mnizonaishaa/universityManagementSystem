package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class SemesterMgr {

    // =========================================================
    // 1. PROCESS SEMESTER PROMOTION ENGINE
    // =========================================================
    public void processPromotion() {
        int confirm = JOptionPane.showConfirmDialog(
                null,
                "Process semester promotion for all ACTIVE students?\n" +
                        "Requirements:\n" +
                        "1. Pass all subjects (Status must be PASS)\n" +
                        "2. Overall Attendance per course ≥ 75%\n\nContinue?",
                "Semester Promotion Engine",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Conn conn = new Conn();
            // Fetch students who are currently active
            PreparedStatement ps = conn.c.prepareStatement("SELECT rollno, name, semester FROM student");
            ResultSet rs = ps.executeQuery();

            StringBuilder report = new StringBuilder();
            report.append("\n════════════════════════════════════════════════════════════════\n");
            report.append(String.format("%43s\n", "SEMESTER PROMOTION REPORT"));
            report.append("════════════════════════════════════════════════════════════════\n\n");

            int promoted = 0, held = 0;

            // Prepared statements outside the loop for high performance and stability
            PreparedStatement psMarks = conn.c.prepareStatement(
                    "SELECT status FROM marks WHERE rollno=? AND semester=?"
            );
            PreparedStatement psAtt = conn.c.prepareStatement(
                    "SELECT (SUM(CASE WHEN status = 'Present' THEN 1 ELSE 0 END) / COUNT(*) * 100) as perc " +
                    "FROM attendance WHERE rollno=? AND semester=?"
            );
            PreparedStatement psUpdateStudent = conn.c.prepareStatement(
                    "UPDATE student SET semester=? WHERE rollno=?"
            );
            PreparedStatement psInsertStatus = conn.c.prepareStatement(
                    "INSERT INTO semester_status (rollno, semester, status, remarks, updatedOn) VALUES (?, ?, ?, ?, CURDATE())"
            );

            while (rs.next()) {
                String rollno = rs.getString("rollno");
                String currentSem = rs.getString("semester");
                boolean canPromote = true;
                StringBuilder reasons = new StringBuilder();

                // Check 1: Academic Grades Tracking
                psMarks.setString(1, rollno);
                psMarks.setString(2, currentSem);
                ResultSet rsMarks = psMarks.executeQuery();
                
                boolean hasMarks = false;
                while (rsMarks.next()) {
                    hasMarks = true;
                    if (!"PASS".equalsIgnoreCase(rsMarks.getString("status"))) {
                        canPromote = false;
                        reasons.append("Failed course(s). ");
                    }
                }
                if (!hasMarks) {
                    canPromote = false;
                    reasons.append("Marks not finalized. ");
                }
                rsMarks.close();

                // Check 2: Attendance Tracking
                psAtt.setString(1, rollno);
                psAtt.setString(2, currentSem);
                ResultSet rsAtt = psAtt.executeQuery();

             if (rsAtt.next()) {
            // Safely check if the database returned a NULL value for the percentage calculation
             Object percentageObj = rsAtt.getObject("perc");
    
            if (percentageObj != null) {
        double per = ((Number) percentageObj).doubleValue();
        if (per < 75.0) {
            canPromote = false;
            reasons.append("Low Attendance (").append(String.format("%.1f", per)).append("%). ");
        }
           } else {
        // This handles students who exist but have ZERO days marked in the attendance table
        canPromote = false;
        reasons.append("No attendance records found. ");
            }
         } else {
         canPromote = false;
         reasons.append("No attendance records found. ");
   }
            rsAtt.close();
                // Check 3: Promotion Operations Logic Execution
                if (canPromote) {
                    int semNum = getSemNumber(currentSem);
                    if (semNum < 8) {
                        String nextSem = getNextSemester(semNum);
                        
                        // Safely Update Student Profile
                        psUpdateStudent.setString(1, nextSem);
                        psUpdateStudent.setString(2, rollno);
                        psUpdateStudent.executeUpdate();
                        
                        // Log Promotion Clearances
                        psInsertStatus.setString(1, rollno);
                        psInsertStatus.setString(2, currentSem);
                        psInsertStatus.setString(3, "PROMOTED");
                        psInsertStatus.setString(4, "Clearance checks passed.");
                        psInsertStatus.executeUpdate();
                        
                        promoted++;
                        report.append(String.format("✓ %-12s Passed → %s\n", rollno, nextSem));
                    } else {
                        report.append(String.format("🎓 %-12s Graduate status confirmed (Completed 8th Sem)\n", rollno));
                    }
                } else {
                    // Log Hold Status
                    psInsertStatus.setString(1, rollno);
                    psInsertStatus.setString(2, currentSem);
                    psInsertStatus.setString(3, "HELD");
                    psInsertStatus.setString(4, reasons.toString().trim());
                    psInsertStatus.executeUpdate();

                    held++;
                    report.append(String.format("✖ %-12s Held on %s | Reasons: %s\n", rollno, currentSem, reasons.toString().trim()));
                }
            }

            report.append("\n" + "═".repeat(64) + "\n");
            report.append("PROMOTION SUMMARY: Promoted: ").append(promoted).append(" | Held/Retained: ").append(held).append("\n");

            displayReport(report.toString());
            
            // Close core statement parameters cleanly
            psMarks.close();
            psAtt.close();
            psUpdateStudent.close();
            psInsertStatus.close();
            rs.close();
            ps.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Promotion Error: " + ex.getMessage());
        }
    }

    // =========================================================
    // 2. VIEW FAILED STUDENTS DASHBOARD
    // =========================================================
    public void viewFailedStudents() {
        JFrame frame = new JFrame("Academic Deficit — Failed Students Log");
        frame.setSize(950, 550);
        frame.setLocationRelativeTo(null);

        JTextArea ta = new JTextArea();
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setEditable(false);
        frame.add(new JScrollPane(ta));

        try {
            Conn conn = new Conn();
            String query = "SELECT m.rollno, s.name, m.courseId, m.semester, m.totalObtained " +
                           "FROM marks m JOIN student s ON m.rollno = s.rollno " +
                           "WHERE m.status = 'FAIL' ORDER BY m.rollno ASC";
            
            PreparedStatement ps = conn.c.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder();
            sb.append("\n=========================================================================\n");
            sb.append(String.format("%45s\n", "FAILED COURSE REPORT LOG"));
            sb.append("=========================================================================\n\n");
            sb.append(String.format("%-12s %-20s %-12s %-15s %s\n", "Roll No", "Student Name", "Course ID", "Semester", "Total Marks"));
            sb.append("-".repeat(73) + "\n");

            boolean recordsFound = false;
            while (rs.next()) {
                recordsFound = true;
         
                sb.append(String.format("%-12s %-20s %-12s %-15s %.1f/100\n",
                        rs.getString("rollno"),
                        rs.getString("name"),
                        rs.getString("courseId"),
                        rs.getString("semester"),
                        rs.getDouble("totalObtained")
                ));
            }

            if (!recordsFound) {
                sb.append("\nNice! No failed student records found in the database system.\n");
            }

            ta.setText(sb.toString());
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame.setVisible(true);
    }

    // =========================================================
    // 3. VIEW HELD STUDENTS LOG PANEL
    // =========================================================
    public void viewHeldStudents() {
        JFrame frame = new JFrame("Academic Review Log — Held Students");
        frame.setSize(900, 550);
        frame.setLocationRelativeTo(null);

        JTextArea ta = new JTextArea();
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setEditable(false);
        frame.add(new JScrollPane(ta));

        try {
            Conn conn = new Conn();
            String query = "SELECT ss.rollno, s.name, ss.semester, ss.remarks, ss.updatedOn " +
                           "FROM semester_status ss JOIN student s ON ss.rollno = s.rollno " +
                           "WHERE ss.status = 'HELD' ORDER BY ss.updated_on DESC";
            
            PreparedStatement ps = conn.c.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder();
            sb.append("\n=========================================================================\n");
            sb.append(String.format("%45s\n", "SEMESTER HELD RETENTION LOG"));
            sb.append("=========================================================================\n\n");
            sb.append(String.format("%-12s %-20s %-15s %-15s %s\n", "Roll No", "Student Name", "Held Semester", "Log Date", "Reasons/Remarks"));
            sb.append("-".repeat(85) + "\n");

            boolean recordsFound = false;
            while (rs.next()) {
                recordsFound = true;
                sb.append(String.format("%-12s %-20s %-15s %-15s %s\n",
                        rs.getString("rollno"),
                        rs.getString("name"),
                        rs.getString("semester"),
                        rs.getString("updatedOn"),
                        rs.getString("remarks")
                ));
            }

            if (!recordsFound) {
                sb.append("\nAwesome! Clear track record. No students are currently held back.\n");
            }

            ta.setText(sb.toString());
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame.setVisible(true);
    }

    // --- Core Helper Subroutines ---

    private int getSemNumber(String sem) {
        if (sem.contains("1st")) return 1;
        if (sem.contains("2nd")) return 2;
        if (sem.contains("3rd")) return 3;
        if (sem.contains("4th")) return 4;
        if (sem.contains("5th")) return 5;
        if (sem.contains("6th")) return 6;
        if (sem.contains("7th")) return 7;
        if (sem.contains("8th")) return 8;
        return 1;
    }

    private String getNextSemester(int current) {
        String[] sems = {"", "1st Semester", "2nd Semester", "3rd Semester", "4th Semester", 
                         "5th Semester", "6th Semester", "7th Semester", "8th Semester"};
        return sems[current + 1];
    }

    private void displayReport(String text) {
        JTextArea ta = new JTextArea(text);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        ta.setEditable(false);
        JFrame f = new JFrame("Promotion Run Results Engine Output");
        f.setSize(750, 600);
        f.setLocationRelativeTo(null);
        f.add(new JScrollPane(ta));
        f.setVisible(true);
    }
}