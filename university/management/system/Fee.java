package university.management.system;

import java.awt.*;
import javax.swing.*;
import java.sql.*;
import net.proteanit.sql.DbUtils;

public class Fee {

    // =========================================================
    //   1. FEE STRUCTURE (ADMIN VIEW)
    // =========================================================
    public void feeStructure() {
        JFrame frame = new JFrame("Fee Structure");
        frame.setSize(1000, 500);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        JPanel northPanel = new JPanel();
        northPanel.setBackground(Color.WHITE);
        JLabel heading = new JLabel("Fee Structure Overview", SwingConstants.CENTER);
        heading.setFont(new Font("Tahoma", Font.BOLD, 24));
        heading.setForeground(new Color(0, 51, 102));
        heading.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        northPanel.add(heading);
        frame.add(northPanel, BorderLayout.NORTH);

        JTable table = new JTable();
        table.setRowHeight(25);
        table.setFont(new Font("Tahoma", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));

        try {
            Conn c = new Conn();
            // Aliasing column fields to display clean titles
            ResultSet rs = c.s.executeQuery("SELECT program AS 'Academic Program', perSemester AS 'Fee Per Semester (Rs.)' FROM fee");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JScrollPane jsp = new JScrollPane(table);
        jsp.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        frame.add(jsp, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    // =========================================================
    //   2. STUDENT FEE PAYMENT FORM
    // =========================================================
    public void studentFeeForm() {
        JFrame frame = new JFrame("Student Fee Form");
        frame.setSize(450, 550);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);

        JLabel lblrollnumber = new JLabel("Roll Number:");
        lblrollnumber.setBounds(40, 40, 150, 25);
        lblrollnumber.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblrollnumber);

        Choice crollno = new Choice();
        crollno.setBounds(200, 40, 180, 25);
        frame.add(crollno);

        // Load active student registration keys
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT rollno FROM student ORDER BY rollno");
            while (rs.next()) crollno.add(rs.getString("rollno"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel lblname = new JLabel("Name: -");
        lblname.setBounds(40, 90, 350, 25);
        lblname.setFont(new Font("Tahoma", Font.PLAIN, 13));
        frame.add(lblname);

        JLabel labelfname = new JLabel("Father: -");
        labelfname.setBounds(40, 130, 350, 25);
        labelfname.setFont(new Font("Tahoma", Font.PLAIN, 13));
        frame.add(labelfname);

        JLabel lblcourse = new JLabel("Registered Program:");
        lblcourse.setBounds(40, 180, 150, 25);
        lblcourse.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblcourse);

        JTextField tfCourse = new JTextField();
        tfCourse.setBounds(200, 180, 180, 28);
        tfCourse.setEditable(false); // Populated automatically from DB
        frame.add(tfCourse);

        JLabel lblbranch = new JLabel("Department/Branch:");
        lblbranch.setBounds(40, 220, 150, 25);
        lblbranch.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblbranch);

        JTextField tfBranch = new JTextField();
        tfBranch.setBounds(200, 220, 180, 28);
        tfBranch.setEditable(false); // Populated automatically from DB
        frame.add(tfBranch);

        JLabel lblsemester = new JLabel("Billing Semester:");
        lblsemester.setBounds(40, 260, 150, 25);
        lblsemester.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lblsemester);

        String[] semesterArr = {"1st Semester","2nd Semester","3rd Semester","4th Semester",
                                "5th Semester","6th Semester","7th Semester","8th Semester"};
        JComboBox<String> cbsemester = new JComboBox<>(semesterArr);
        cbsemester.setBounds(200, 260, 180, 28);
        cbsemester.setBackground(Color.WHITE);
        frame.add(cbsemester);

        JLabel lbltotal = new JLabel("Total Payable:");
        lbltotal.setBounds(40, 310, 150, 25);
        lbltotal.setFont(new Font("Tahoma", Font.BOLD, 14));
        frame.add(lbltotal);

        JLabel labeltotal = new JLabel("0.0");
        labeltotal.setBounds(200, 310, 150, 25);
        labeltotal.setFont(new Font("Tahoma", Font.BOLD, 16));
        labeltotal.setForeground(new Color(120, 0, 0));
        frame.add(labeltotal);

        JButton update = new JButton("Get Fee");
        JButton pay = new JButton("Pay Fee");
        JButton back = new JButton("Cancel");

        update.setBounds(40, 400, 100, 32);
        update.setBackground(Color.BLACK);
        update.setForeground(Color.WHITE);
        
        pay.setBounds(165, 400, 100, 32);
        pay.setBackground(new Color(0, 102, 51));
        pay.setForeground(Color.WHITE);
        
        back.setBounds(290, 400, 100, 32);
        back.setBackground(Color.DARK_GRAY);
        back.setForeground(Color.WHITE);

        frame.add(update);
        frame.add(pay);
        frame.add(back);

        // ── AUTOMATIC PROFILE RETRIEVAL LOGIC ──
        Runnable loadStudentDetails = () -> {
            if (crollno.getSelectedItem() == null) return;
            try {
                Conn c = new Conn();
                String query = "SELECT s.name, s.fname, s.program, d.deptName " +
                               "FROM student s " +
                               "JOIN department d ON s.deptId = d.deptId " +
                               "WHERE s.rollno = ?";
                PreparedStatement ps = c.c.prepareStatement(query);
                ps.setString(1, crollno.getSelectedItem());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    lblname.setText("Student Name:  " + rs.getString("name"));
                    labelfname.setText("Father's Name: " + rs.getString("fname"));
                    tfCourse.setText(rs.getString("program"));
                    tfBranch.setText(rs.getString("deptName"));
                    labeltotal.setText("0.0"); // Reset total until they fetch calculation
                }
                rs.close();
                ps.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // Load immediately on open and on dropdown change
        loadStudentDetails.run();
        crollno.addItemListener(e -> loadStudentDetails.run());

        // ── LIVE BILLING LOOKUP ──
        update.addActionListener(e -> {
            if (tfCourse.getText().isEmpty()) return;
            try {
                Conn c = new Conn();
                String query = "SELECT perSemester FROM fee WHERE program = ?";
                PreparedStatement ps = c.c.prepareStatement(query);
                ps.setString(1, tfCourse.getText().trim());

                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    labeltotal.setText(rs.getString("perSemester"));
                } else {
                    labeltotal.setText("0.0");
                    JOptionPane.showMessageDialog(frame, "No fee structure configured for program: " + tfCourse.getText());
                }
                rs.close();
                ps.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // ── INJECT TRANSACTION INTO HISTORY ──
        pay.addActionListener(e -> {
            if (labeltotal.getText().equals("0.0") || labeltotal.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Please click 'Get Fee' to update calculations first!");
                return;
            }

            try {
                Conn c = new Conn();
                // FIXED TABLE TARGET: Changed collegefee to fee_payment matching schema
                String query = "INSERT INTO fee_payment (rollno, semester, amount, paidOn, status) VALUES (?, ?, ?, CURDATE(), 'PAID')";
                PreparedStatement ps = c.c.prepareStatement(query);

                ps.setString(1, crollno.getSelectedItem());
                ps.setString(2, (String) cbsemester.getSelectedItem());
                ps.setDouble(3, Double.parseDouble(labeltotal.getText()));

                ps.executeUpdate();
                ps.close();

                JOptionPane.showMessageDialog(frame, "Transaction completed! Fee paid successfully.");
                frame.dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Transaction Failed: " + ex.getMessage());
            }
        });

        back.addActionListener(e -> frame.dispose());
        frame.setVisible(true);
    }
}