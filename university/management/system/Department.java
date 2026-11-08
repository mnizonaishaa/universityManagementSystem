package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import net.proteanit.sql.DbUtils;

public class Department {

    // ── ADD ───────────────────────────────────────────────────
    public void addDepartment() {
        JFrame f = new JFrame("Add Department");
        f.setSize(480, 320); f.setLocationRelativeTo(null);
        f.setLayout(null); f.getContentPane().setBackground(Color.white);

        head(f, "ADD DEPARTMENT");
        JTextField tfId   = field(f, "Department ID:",   20, 80);
        JTextField tfName = field(f, "Department Name:", 20, 125);
        JTextField tfHod  = field(f, "Head of Dept:",    20, 170);

        JButton sub = btn(f, "Submit", 140, 235, new Color(0,100,0));
        JButton can = btn(f, "Cancel", 285, 235, Color.DARK_GRAY);
        f.setVisible(true);

        sub.addActionListener(e -> {
            if (tfId.getText().trim().isEmpty() || tfName.getText().trim().isEmpty()) {
                err("ID and Name are required."); return;
            }
            try {
                Conn c = new Conn();
                PreparedStatement ps = c.c.prepareStatement(
                    "INSERT INTO department VALUES (?,?,?)");
                ps.setString(1, tfId.getText().trim().toUpperCase());
                ps.setString(2, tfName.getText().trim());
                ps.setString(3, tfHod.getText().trim());
                ps.executeUpdate(); ps.close();
                ok("Department added successfully!"); f.dispose();
            } catch (Exception ex) { err(ex.getMessage()); }
        });
        can.addActionListener(e -> f.dispose());
    }

    // ── VIEW ──────────────────────────────────────────────────
    public void viewDepartments() {
        JFrame f = new JFrame("Departments");
        f.setSize(700, 400); f.setLocationRelativeTo(null);
        JTable table = new JTable();
        try {
            ResultSet rs = new Conn().s.executeQuery("SELECT * FROM department ORDER BY deptId");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (Exception ex) { ex.printStackTrace(); }
        styleTable(table);
        f.add(new JScrollPane(table));
        f.setVisible(true);
    }

    // ── UPDATE ────────────────────────────────────────────────
    public void updateDepartment() {
        String id = JOptionPane.showInputDialog("Enter Department ID to update:");
        if (id == null || id.trim().isEmpty()) return;
        try {
            Conn c = new Conn();
            ResultSet rs = c.s.executeQuery("SELECT * FROM department WHERE deptId='" + id.trim().toUpperCase() + "'");
            if (!rs.next()) { err("Department not found."); return; }
            String newName = JOptionPane.showInputDialog("New Department Name:", rs.getString("deptName"));
            String newHod  = JOptionPane.showInputDialog("New Head of Department:", rs.getString("hod"));
            if (newName == null) return;
            c.s.executeUpdate("UPDATE department SET deptName='" + newName + "', hod='" + newHod + "' WHERE deptId='" + id.trim().toUpperCase() + "'");
            ok("Department updated.");
        } catch (Exception ex) { err(ex.getMessage()); }
    }

    // ── DELETE ────────────────────────────────────────────────
    public void deleteDepartment() {
        String id = JOptionPane.showInputDialog("Enter Department ID to delete:");
        if (id == null || id.trim().isEmpty()) return;
        int c2 = JOptionPane.showConfirmDialog(null,
            "Delete department " + id + "?\nTeachers/students in this dept will NOT be deleted.",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (c2 != JOptionPane.YES_OPTION) return;
        try {
            new Conn().s.executeUpdate("DELETE FROM department WHERE deptId='" + id.trim().toUpperCase() + "'");
            ok("Department deleted.");
        } catch (Exception ex) { err(ex.getMessage()); }
    }

    // ── Helpers ───────────────────────────────────────────────
    private void head(JFrame f, String t) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setBounds(0, 20, 480, 35); l.setFont(new Font("Tahoma", Font.BOLD, 20));
        l.setForeground(new Color(0,51,102)); f.add(l);
    }
    private JTextField field(JFrame f, String label, int x, int y) {
        JLabel l = new JLabel(label); l.setBounds(x, y, 150, 25);
        l.setFont(new Font("Tahoma", Font.BOLD, 13)); f.add(l);
        JTextField tf = new JTextField(); tf.setBounds(175, y, 260, 28);
        tf.setFont(new Font("Tahoma", Font.PLAIN, 13)); f.add(tf); return tf;
    }
    private JButton btn(JFrame f, String t, int x, int y, Color bg) {
        JButton b = new JButton(t); b.setBounds(x, y, 120, 32);
        b.setBackground(bg); b.setForeground(Color.white);
        b.setFont(new Font("Tahoma", Font.BOLD, 12)); f.add(b); return b;
    }
    private void styleTable(JTable t) {
        t.setFont(new Font("Tahoma", Font.PLAIN, 13));
        t.setRowHeight(22);
        t.getTableHeader().setFont(new Font("Tahoma", Font.BOLD, 13));
    }
    private void ok(String msg)  { JOptionPane.showMessageDialog(null, msg); }
    private void err(String msg) { JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE); }
}