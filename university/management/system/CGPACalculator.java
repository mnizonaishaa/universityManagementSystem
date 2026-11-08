package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

/**
 * CGPACalculator — Calculates semester GPA + overall CGPA with grade.
 * Works with marks table: rollno, Semester, Marks1–Marks5
 */

public class CGPACalculator {

    // ---------------------------
    // Grade conversion (GPA scale)
    // ---------------------------
    private double toGP(String markStr) {
        try {
            double m = Double.parseDouble(markStr);

            if (m >= 90) return 4.0;
            if (m >= 80) return 3.3;
            if (m >= 70) return 2.7;
            if (m >= 60) return 2.0;
            if (m >= 50) return 1.0;
            return 0.0;

        } catch (Exception e) {
            return 0.0;
        }
    }

    // ---------------------------
    // Grade letter
    // ---------------------------
    private String getGrade(double cgpa) {
        if (cgpa >= 3.7) return "A+";
        if (cgpa >= 3.3) return "A";
        if (cgpa >= 3.0) return "A-";
        if (cgpa >= 2.7) return "B+";
        if (cgpa >= 2.3) return "B";
        if (cgpa >= 2.0) return "B-";
        if (cgpa >= 1.0) return "C";
        return "F";
    }

    // ---------------------------
    // MAIN UI
    // ---------------------------
    public void showCGPA() {

        JFrame frame = new JFrame("CGPA Calculator");
        frame.setSize(650, 550);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);

        JLabel heading = new JLabel("CGPA CALCULATOR", SwingConstants.CENTER);
        heading.setBounds(0, 20, 650, 40);
        heading.setFont(new Font("Tahoma", Font.BOLD, 24));
        frame.add(heading);

        JLabel lblRoll = new JLabel("Select Roll No:");
        lblRoll.setBounds(50, 90, 150, 25);
        lblRoll.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblRoll);

        Choice cRoll = new Choice();
        cRoll.setBounds(200, 90, 200, 25);
        loadRollNumbers(cRoll);
        frame.add(cRoll);

        JButton btnCalc = new JButton("CALCULATE");
        btnCalc.setBounds(420, 85, 150, 30);
        btnCalc.setBackground(Color.BLACK);
        btnCalc.setForeground(Color.WHITE);
        frame.add(btnCalc);

        JTextArea area = new JTextArea();
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBounds(50, 140, 520, 260);
        frame.add(scroll);

        JLabel lblCGPA = new JLabel("", SwingConstants.CENTER);
        lblCGPA.setBounds(50, 420, 520, 30);
        lblCGPA.setFont(new Font("Tahoma", Font.BOLD, 18));
        lblCGPA.setForeground(new Color(0, 70, 140));
        frame.add(lblCGPA);

        JLabel lblGrade = new JLabel("", SwingConstants.CENTER);
        lblGrade.setBounds(50, 450, 520, 25);
        lblGrade.setFont(new Font("Tahoma", Font.BOLD, 16));
        frame.add(lblGrade);

        JButton close = new JButton("CLOSE");
        close.setBounds(260, 480, 120, 30);
        close.setBackground(Color.BLACK);
        close.setForeground(Color.WHITE);
        frame.add(close);

        frame.setVisible(true);

        // ---------------------------
        // CALCULATION LOGIC
        // ---------------------------
        btnCalc.addActionListener(e -> {

            String roll = cRoll.getSelectedItem();

            double totalGPA = 0;
            int semCount = 0;

            StringBuilder sb = new StringBuilder();

            sb.append("\nSEMESTER GPA BREAKDOWN\n");
            sb.append("────────────────────────────────────────────\n");
            sb.append(String.format("%-12s %-25s %-10s\n",
                    "Semester", "GPs (5 subjects)", "GPA"));
            sb.append("────────────────────────────────────────────\n");

            try {

                Conn conn = new Conn();

                String query =
                        "SELECT * FROM marks WHERE rollno='" + roll + "'";

                ResultSet rs = conn.s.executeQuery(query);

                while (rs.next()) {

                    String sem = rs.getString("Semester");

                    double[] gp = new double[5];

                    for (int i = 0; i < 5; i++) {
                        gp[i] = toGP(rs.getString("Marks" + (i + 1)));
                    }

                    double semGPA =
                            (gp[0] + gp[1] + gp[2] + gp[3] + gp[4]) / 5.0;

                    sb.append(String.format(
                            "%-12s %.1f %.1f %.1f %.1f %.1f   %.2f\n",
                            sem,
                            gp[0], gp[1], gp[2], gp[3], gp[4],
                            semGPA));

                    totalGPA += semGPA;
                    semCount++;
                }

                if (semCount == 0) {

                    area.setText("No marks found for roll: " + roll);
                    lblCGPA.setText("CGPA: N/A");
                    lblGrade.setText("");

                } else {

                    double cgpa = totalGPA / semCount;

                    String grade = getGrade(cgpa);

                    sb.append("\n────────────────────────────────────────────\n");
                    sb.append("Total Semesters: " + semCount + "\n");

                    area.setText(sb.toString());

                    lblCGPA.setText(
                            String.format("CGPA: %.2f (%d semester%s)",
                                    cgpa,
                                    semCount,
                                    semCount > 1 ? "s" : ""));

                    lblGrade.setText("Grade: " + grade);

                    lblGrade.setForeground(
                            cgpa >= 2.0
                                    ? new Color(0, 120, 0)
                                    : Color.RED);
                }

            } catch (Exception ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(frame,
                        "Database Error: " + ex.getMessage());
            }
        });

        close.addActionListener(e -> frame.dispose());
    }

    // ---------------------------
    // LOAD ROLL NUMBERS
    // ---------------------------
    private void loadRollNumbers(Choice c) {

        try {

            Conn conn = new Conn();

            ResultSet rs =
                    conn.s.executeQuery(
                            "SELECT rollno FROM student");

            while (rs.next()) {
                c.add(rs.getString("rollno"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}