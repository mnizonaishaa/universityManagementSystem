package university.management.system;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;

/**
 * Login — Step 1: numbered list role selection
 *          Step 2: username + password
 * Image on right (icons/second.jpg). No italic. Max 3 attempts.
 * Passes role + loggedInId to Project.
 */
public class Login extends JFrame implements ActionListener {

    JButton btn1Admin, btn2Faculty, btn3Student;
    JButton btnLogin, btnBack, btnCancel;
    JTextField     tfUsername;
    JPasswordField tfPassword;
    JPanel panelStep1, panelStep2;
    JLabel lblSelectedRole, lblError;
    String selectedRole = "";
    int attempts = 0;

    public Login() {
        setTitle("University Management System - Login");
        setSize(500, 265);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Color.white);
        setLayout(null);

        // Title
        JLabel appTitle = new JLabel("University Management System");
        appTitle.setBounds(20, 15, 310, 28);
        appTitle.setFont(new Font("Tahoma", Font.BOLD, 16));
        add(appTitle);

        JLabel appSub = new JLabel("Bahria University Karachi Campus");
        appSub.setBounds(20, 44, 310, 22);
        appSub.setFont(new Font("Tahoma", Font.BOLD, 13));
        appSub.setForeground(new Color(0, 70, 140));
        add(appSub);

        // Right image
        try {
            ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icons/second.jpg"));
            Image i2 = i1.getImage().getScaledInstance(160, 160, Image.SCALE_DEFAULT);
            JLabel image = new JLabel(new ImageIcon(i2));
            image.setBounds(325, 10, 160, 160);
            add(image);
        } catch (Exception ignored) {}

        // STEP 1: numbered list
        panelStep1 = new JPanel(null);
        panelStep1.setBounds(0, 75, 310, 180);
        panelStep1.setBackground(Color.white);

        JLabel lblChoose = new JLabel("Select your role:");
        lblChoose.setBounds(20, 5, 260, 24);
        lblChoose.setFont(new Font("Tahoma", Font.BOLD, 14));
        panelStep1.add(lblChoose);

        btn1Admin   = listButton("1. Admin");
        btn2Faculty = listButton("2. Faculty");
        btn3Student = listButton("3. Student");
        btn1Admin.setBounds(20, 35, 250, 32);
        btn2Faculty.setBounds(20, 75, 250, 32);
        btn3Student.setBounds(20, 115, 250, 32);
        panelStep1.add(btn1Admin);
        panelStep1.add(btn2Faculty);
        panelStep1.add(btn3Student);
        add(panelStep1);

        // STEP 2: username + password
        panelStep2 = new JPanel(null);
        panelStep2.setBounds(0, 75, 310, 180);
        panelStep2.setBackground(Color.white);
        panelStep2.setVisible(false);

        lblSelectedRole = new JLabel("");
        lblSelectedRole.setBounds(20, 2, 290, 24);
        lblSelectedRole.setFont(new Font("Tahoma", Font.BOLD, 13));
        panelStep2.add(lblSelectedRole);

        JLabel lblUser = new JLabel("Username:");
        lblUser.setBounds(20, 32, 90, 24);
        lblUser.setFont(new Font("Tahoma", Font.BOLD, 13));
        panelStep2.add(lblUser);

        tfUsername = new JTextField();
        tfUsername.setBounds(115, 32, 175, 26);
        tfUsername.setFont(new Font("Tahoma", Font.PLAIN, 13));
        panelStep2.add(tfUsername);

        JLabel lblPass = new JLabel("Password:");
        lblPass.setBounds(20, 66, 90, 24);
        lblPass.setFont(new Font("Tahoma", Font.BOLD, 13));
        panelStep2.add(lblPass);

        tfPassword = new JPasswordField();
        tfPassword.setBounds(115, 66, 175, 26);
        tfPassword.setFont(new Font("Tahoma", Font.PLAIN, 13));
        tfPassword.addActionListener(this);
        panelStep2.add(tfPassword);

        lblError = new JLabel(" ");
        lblError.setBounds(20, 97, 290, 18);
        lblError.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblError.setForeground(Color.red);
        panelStep2.add(lblError);

        btnLogin = new JButton("Login");
        btnLogin.setBounds(15, 120, 85, 28);
        btnLogin.setBackground(Color.black); btnLogin.setForeground(Color.white);
        btnLogin.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnLogin.addActionListener(this);
        panelStep2.add(btnLogin);

        btnBack = new JButton("Back");
        btnBack.setBounds(110, 120, 85, 28);
        btnBack.setBackground(new Color(80,80,80)); btnBack.setForeground(Color.white);
        btnBack.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnBack.addActionListener(this);
        panelStep2.add(btnBack);

        btnCancel = new JButton("Cancel");
        btnCancel.setBounds(205, 120, 85, 28);
        btnCancel.setBackground(new Color(140,0,0)); btnCancel.setForeground(Color.white);
        btnCancel.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnCancel.addActionListener(this);
        panelStep2.add(btnCancel);

        add(panelStep2);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object src = ae.getSource();
        if (src == btn1Admin)   { selectRole("ADMIN");   return; }
        if (src == btn2Faculty) { selectRole("FACULTY"); return; }
        if (src == btn3Student) { selectRole("STUDENT"); return; }
        if (src == btnBack)     { goBack();              return; }
        if (src == btnCancel)   { System.exit(0);        return; }
        if (src == btnLogin || src == tfPassword) { attemptLogin(); }
    }

    private void selectRole(String role) {
        selectedRole = role;
        String display = role.equals("ADMIN") ? "Admin" : role.equals("FACULTY") ? "Faculty" : "Student";
        lblSelectedRole.setText("Login as: " + display);
        lblError.setText(" ");
        tfUsername.setText(""); tfPassword.setText("");
        panelStep1.setVisible(false);
        panelStep2.setVisible(true);
        tfUsername.requestFocus();
    }

    private void goBack() {
        selectedRole = ""; attempts = 0;
        tfUsername.setText(""); tfPassword.setText("");
        lblError.setText(" ");
        panelStep2.setVisible(false);
        panelStep1.setVisible(true);
    }

    private void attemptLogin() {
        String username = tfUsername.getText().trim();
        String password = new String(tfPassword.getPassword()).trim();
        if (username.isEmpty() || password.isEmpty()) {
            lblError.setText("Enter both username and password.");
            return;
        }
        try {
            Conn c = new Conn();
            PreparedStatement ps = c.c.prepareStatement(
                "SELECT role FROM login WHERE username=? AND password=? AND role=?");
            ps.setString(1, username); ps.setString(2, password); ps.setString(3, selectedRole);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                ps.close(); c.c.close();
                setVisible(false);
                new Project(role, username);
            } else {
                attempts++;
                int rem = 3 - attempts;
                tfPassword.setText(""); tfPassword.requestFocus();
                if (rem <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "Too many failed attempts. Application will close.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
                    lblError.setText("Invalid credentials. " + rem + " attempt(s) left.");
                }
                ps.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error. Make sure MySQL is running.", "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton listButton(String label) {
        JButton btn = new JButton(label);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBackground(Color.white); btn.setForeground(Color.black);
        btn.setFont(new Font("Tahoma", Font.PLAIN, 14));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(this);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(220,230,255));
                btn.setFont(new Font("Tahoma", Font.BOLD, 14));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Color.white);
                btn.setFont(new Font("Tahoma", Font.PLAIN, 14));
            }
        });
        return btn;
    }

    public static void main(String[] args) { new Login(); }
}