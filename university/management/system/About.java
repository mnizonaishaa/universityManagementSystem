package university.management.system;

import javax.swing.*;
import java.awt.*;

public class About {

    public void showAbout() {

        JFrame frame = new JFrame("About");
        frame.setSize(450, 360);
        frame.setLocationRelativeTo(null);
        frame.setLayout(null);
        frame.setResizable(false);

        frame.getContentPane().setBackground(Color.white);

        // =====================================================
        // TITLE
        // =====================================================

        JLabel title = new JLabel(
            "UNIVERSITY MANAGEMENT SYSTEM",
            SwingConstants.CENTER);

        title.setBounds(0, 20, 450, 35);
        title.setFont(new Font("Tahoma", Font.BOLD, 18));
        title.setForeground(new Color(0, 51, 102));

        frame.add(title);

        // =====================================================
        // TOP LINE
        // =====================================================

        JSeparator sep1 = new JSeparator();
        sep1.setBounds(25, 65, 390, 2);
        frame.add(sep1);

        // =====================================================
        // DEVELOPERS
        // =====================================================

        JLabel developed = new JLabel("DEVELOPED BY:");
        developed.setBounds(35, 78, 200, 30);
        developed.setFont(new Font("Tahoma", Font.BOLD, 15));

        frame.add(developed);

        JLabel dev1 = new JLabel("1. Mishal Shehzadi");
        dev1.setBounds(55, 112, 300, 25);
        dev1.setFont(new Font("Tahoma", Font.PLAIN, 14));

        frame.add(dev1);

        JLabel dev2 = new JLabel("2. Zonaisha Rashid");
        dev2.setBounds(55, 140, 300, 25);
        dev2.setFont(new Font("Tahoma", Font.PLAIN, 14));

        frame.add(dev2);

        // =====================================================
        // SECOND LINE
        // =====================================================

        JSeparator sep2 = new JSeparator();
        sep2.setBounds(25, 178, 390, 2);

        frame.add(sep2);

        // =====================================================
        // FACULTY
        // =====================================================

        JLabel faculty = new JLabel("FACULTY SUPERVISOR:");
        faculty.setBounds(35, 190, 250, 30);
        faculty.setFont(new Font("Tahoma", Font.BOLD, 15));

        frame.add(faculty);

        JLabel facultyName = new JLabel("→ Miss Salas Akbar");
        facultyName.setBounds(55, 225, 300, 25);
        facultyName.setFont(new Font("Tahoma", Font.PLAIN, 14));

        frame.add(facultyName);

        // =====================================================
        // THIRD LINE
        // =====================================================

        JSeparator sep3 = new JSeparator();
        sep3.setBounds(25, 262, 390, 2);

        frame.add(sep3);

        // =====================================================
        // UNIVERSITY NAME
        // =====================================================

        JLabel uni = new JLabel(
            "Bahria University Karachi Campus",
            SwingConstants.CENTER);

        uni.setBounds(0, 273, 450, 25);

        uni.setFont(new Font("Tahoma", Font.ITALIC, 13));

        uni.setForeground(new Color(0, 70, 140));

        frame.add(uni);

        // =====================================================
        // CLOSE BUTTON
        // =====================================================

        JButton close = new JButton("Close");

        close.setBounds(160, 305, 120, 32);

        close.setBackground(Color.black);
        close.setForeground(Color.white);

        close.setFont(new Font("Tahoma", Font.BOLD, 13));

        close.setFocusPainted(false);

        frame.add(close);

        close.addActionListener(ae -> frame.setVisible(false));

        // =====================================================
        // SHOW FRAME
        // =====================================================

        frame.setVisible(true);
    }
}