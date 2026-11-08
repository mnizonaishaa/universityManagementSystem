package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import net.proteanit.sql.DbUtils;

public class FeeStatus {

    public void viewFeeStatus() {
        JFrame frame = new JFrame("Fee Status Dashboard");
        frame.setSize(850, 550);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Color.WHITE);

        // --- TOP PANEL (Heading + Controls Layout) ---
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.setBackground(Color.WHITE);

        JLabel heading = new JLabel("Student Fee Status", SwingConstants.CENTER);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.setFont(new Font("Tahoma", Font.BOLD, 26));
        heading.setForeground(new Color(0, 51, 102));
        heading.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));
        northPanel.add(heading);

        // Control Toolbar Panel
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        controls.setBackground(Color.WHITE);

        JLabel lblroll = new JLabel("Select Roll Number:");
        lblroll.setFont(new Font("Tahoma", Font.BOLD, 12));
        controls.add(lblroll);

        Choice crollno = new Choice();
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT rollno FROM student ORDER BY rollno");
            while (rs.next()) {
                crollno.add(rs.getString("rollno"));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        controls.add(crollno);

        JButton search = new JButton("Search");
        styleBtn(search, Color.BLACK);
        controls.add(search);

        JButton showAll = new JButton("Show All");
        styleBtn(showAll, new Color(0, 51, 102));
        controls.add(showAll);

        JButton cancel = new JButton("Close");
        styleBtn(cancel, new Color(120, 0, 0));
        controls.add(cancel);

        northPanel.add(controls);
        frame.add(northPanel, BorderLayout.NORTH);

        // --- CENTER PANEL 
        JTable table = new JTable();
        table.setRowHeight(28);
        table.setFont(new Font("Tahoma", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
        
        JScrollPane jsp = new JScrollPane(table);
        jsp.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        frame.add(jsp, BorderLayout.CENTER);

        // --- SOUTH PANEL 
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.setBackground(Color.WHITE);
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 25, 20, 25));

        JLabel lblSummary = new JLabel("Fee History");
        lblSummary.setFont(new Font("Tahoma", Font.PLAIN, 13));
        southPanel.add(lblSummary);

        JLabel lblWarning = new JLabel(" ");
        lblWarning.setFont(new Font("Tahoma", Font.BOLD, 13));
        lblWarning.setForeground(Color.RED);
        southPanel.add(lblWarning);

        frame.add(southPanel, BorderLayout.SOUTH);

        // ── LOAD ALL METHOD ──
        Runnable loadAll = () -> {
            try {
                Conn c = new Conn();
                String query = "SELECT payId AS 'Receipt ID', rollno AS 'Roll Number', " +
                               "semester AS 'Semester Term', amount AS 'Paid Fees (Rs.)', " +
                               "paidOn AS 'Payment Date', status AS 'Status Label' FROM fee_payment";
                ResultSet rs = c.s.executeQuery(query);
                table.setModel(DbUtils.resultSetToTableModel(rs));

                lblSummary.setText("Showing all student fee records.");
                lblWarning.setText(" ");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        // ── EVENT ACTIONS BINDING ──
        search.addActionListener(ae -> {
            String roll = crollno.getSelectedItem();
            if (roll == null) return;

            try {
                Conn c = new Conn();
                
                // Fetch dynamic filtered student ledger
                String query1 = "SELECT payId AS 'Receipt ID', rollno AS 'Roll Number', " +
                                "semester AS 'Semester Term', amount AS 'Paid Fees (Rs.)', " +
                                "paidOn AS 'Payment Date', status AS 'Status Label' " +
                                "FROM fee_payment WHERE rollno = ? ORDER BY semester";
                PreparedStatement ps1 = c.c.prepareStatement(query1);
                ps1.setString(1, roll);
                ResultSet rs1 = ps1.executeQuery();
                table.setModel(DbUtils.resultSetToTableModel(rs1));

                // Fetch total counts and paid calculations
                String query2 = "SELECT COUNT(*) AS cnt, COALESCE(SUM(amount), 0) AS totalPaid " +
                                "FROM fee_payment WHERE rollno = ?";
                PreparedStatement ps2 = c.c.prepareStatement(query2);
                ps2.setString(1, roll);
                ResultSet rs2 = ps2.executeQuery();

                if (rs2.next()) {
                    int count = rs2.getInt("cnt");
                    double totalPaid = rs2.getDouble("totalPaid");

                    if (count == 0) {
                        lblSummary.setText("No fee records found for Roll No: " + roll);
                        lblWarning.setText("⚠ Warning: This student has not paid any fees yet!");
                    } else {
                        lblSummary.setText("Total Semesters Paid: " + count + "  |  Total Amount Paid: Rs. " + totalPaid);
                        lblWarning.setText(" ");
                    }
                }
                rs1.close();
                ps1.close();
                rs2.close();
                ps2.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        showAll.addActionListener(ae -> loadAll.run());
        cancel.addActionListener(ae -> frame.dispose());

        loadAll.run(); // Initial load when frame opens
        frame.setVisible(true);
    }

    private void styleBtn(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Tahoma", Font.BOLD, 12));
    }
}