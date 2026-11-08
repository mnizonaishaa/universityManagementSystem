package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Random;
import net.proteanit.sql.DbUtils;
import com.toedter.calendar.JDateChooser;

/**
 * Teacher — all faculty screens.
 * loggedInId = empId when role is FACULTY. Used to filter own data.
 */
public class Teacher {
    private final String loggedInId;

    public Teacher(String loggedInId) { this.loggedInId = loggedInId; }

    // ── ADD TEACHER ───────────────────────────────────────────
    public void addTeacher() {
        JFrame f = new JFrame("Add New Faculty");
        f.setSize(720, 490); f.setLocationRelativeTo(null);
        f.setLayout(null); f.getContentPane().setBackground(Color.white);

        JLabel h = new JLabel("ADD NEW FACULTY", SwingConstants.CENTER);
        h.setBounds(0,15,720,40); h.setFont(new Font("Tahoma",Font.BOLD,24));
        h.setForeground(new Color(0,51,102)); f.add(h);

        String empId = "EMP" + String.format("%04d", new Random().nextInt(9000)+1000);
        JLabel lEmp = lbl("Employee ID (Auto):"); lEmp.setBounds(30,65,170,25); f.add(lEmp);
        JLabel valEmp = new JLabel(empId); valEmp.setBounds(210,65,180,25);
        valEmp.setFont(new Font("Tahoma",Font.BOLD,13)); valEmp.setForeground(new Color(0,120,0)); f.add(valEmp);

        int lx1=30,fx1=185,lx2=390,fx2=545,fw=155,fh=27,lh=27;

        JTextField tfName=tf(); JTextField tfFname=tf();
        JDateChooser dcDob=new JDateChooser();
        JTextField tfAddr=tf(); JTextField tfPhone=tf(); JTextField tfEmail=tf(); JTextField tfCnic=tf();

        addRow(f,"Name:",         lx1,108,lh,tfName,   fx1,108,fw,fh);
        addRow(f,"Father's Name:",lx2,108,lh,tfFname,  fx2,108,fw,fh);
        addRow(f,"Date of Birth:",lx1,148,lh,dcDob,    fx1,148,fw,fh);
        addRow(f,"Address:",      lx2,148,lh,tfAddr,   fx2,148,fw,fh);
        addRow(f,"Phone:",        lx1,188,lh,tfPhone,  fx1,188,fw,fh);
        addRow(f,"Email:",        lx2,188,lh,tfEmail,  fx2,188,fw,fh);
        addRow(f,"CNIC:",         lx1,228,lh,tfCnic,   fx1,228,fw,fh);

        JLabel lEdu=lbl("Qualification:"); lEdu.setBounds(lx1,268,170,lh); f.add(lEdu);
        String[] quals={"MSc","MBA","PhD","MCA","MA","B.Tech","BCA","MCom","BSc"};
        JComboBox<String> cbEdu=new JComboBox<>(quals); cbEdu.setBounds(fx1,268,fw,fh);
        cbEdu.setBackground(Color.white); f.add(cbEdu);

        JLabel lDept=lbl("Department:"); lDept.setBounds(lx2,268,170,lh); f.add(lDept);
        JComboBox<String> cbDept=new JComboBox<>(); cbDept.setBounds(fx2,268,fw,fh);
        cbDept.setBackground(Color.white); f.add(cbDept);
        try {
            ResultSet rs=new Conn().s.executeQuery("SELECT deptId,deptName FROM department ORDER BY deptName");
            while(rs.next()) cbDept.addItem(rs.getString("deptId")+" — "+rs.getString("deptName"));
        } catch(Exception ex){ex.printStackTrace();}

        JLabel hintCnic=new JLabel("(00000-0000000-0)");
        hintCnic.setBounds(fx1+fw+5,228,180,fh); hintCnic.setFont(new Font("Tahoma",Font.PLAIN,10));
        hintCnic.setForeground(Color.GRAY); f.add(hintCnic);

        JLabel lPwd=lbl("Default Password:"); lPwd.setBounds(lx1,310,170,lh); f.add(lPwd);
        JTextField tfPwd=tf(); tfPwd.setBounds(fx1,310,fw,fh); tfPwd.setText("teacher123"); f.add(tfPwd);

        JButton sub=subBtn(f,"Submit",220,380);
        JButton can=canBtn(f,"Cancel",365,380);
        f.setVisible(true);

        sub.addActionListener(e -> {
            String name=tfName.getText().trim(), fname=tfFname.getText().trim();
            String dob=((JTextField)dcDob.getDateEditor().getUiComponent()).getText().trim();
            String addr=tfAddr.getText().trim(), phone=tfPhone.getText().trim();
            String email=tfEmail.getText().trim(), cnic=tfCnic.getText().trim();
            String edu=(String)cbEdu.getSelectedItem();
            String deptId=cbDept.getSelectedItem().toString().split(" — ")[0];
            String pwd=tfPwd.getText().trim();

            if(name.isEmpty()||fname.isEmpty()||phone.isEmpty()||email.isEmpty()||cnic.isEmpty()){
                err("All fields are required."); return; }
            if(!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")){ err("Invalid email."); return; }
            if(!cnic.matches("\\d{5}-\\d{7}-\\d")){ err("Invalid CNIC format."); return; }
            if(!phone.matches("\\d+")){ err("Phone must be digits only."); return; }

            try {
                Conn c=new Conn();
                PreparedStatement ps=c.c.prepareStatement(
                    "INSERT INTO teacher VALUES (?,?,?,?,?,?,?,?,?,?)");
                ps.setString(1,empId); ps.setString(2,name); ps.setString(3,fname);
                ps.setString(4,dob);  ps.setString(5,addr); ps.setString(6,phone);
                ps.setString(7,email);ps.setString(8,cnic); ps.setString(9,edu);
                ps.setString(10,deptId); ps.executeUpdate(); ps.close();

                PreparedStatement pl=c.c.prepareStatement(
                    "INSERT INTO login VALUES (?,?,'FACULTY')");
                pl.setString(1,empId); pl.setString(2,pwd);
                pl.executeUpdate(); pl.close();

                ok("Faculty added!\nEmp ID: "+empId+"\nPassword: "+pwd); f.dispose();
            } catch(Exception ex){err("DB Error: "+ex.getMessage());}
        });
        can.addActionListener(e->f.dispose());
    }

    // ── VIEW TEACHERS ─────────────────────────────────────────
   public void viewTeachers() {
    JFrame f = new JFrame("Faculty Records");
    f.setSize(1100, 600);
    f.setLocationRelativeTo(null);
    f.setLayout(new BorderLayout());

    // --- TOP SECTION (Heading + Search Bar) ---
    JPanel northPanel = new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
    northPanel.setBackground(Color.white);

    // 1. Heading
    JLabel h = new JLabel("FACULTY RECORDS", SwingConstants.CENTER);
    h.setAlignmentX(Component.CENTER_ALIGNMENT);
    h.setFont(new Font("Tahoma", Font.BOLD, 22));
    h.setForeground(new Color(0, 51, 102));
    h.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    northPanel.add(h);

    // 2. Search Bar Panel
    JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
    searchBar.setBackground(Color.white);

    JLabel lEmp = new JLabel("Employee ID:");
    lEmp.setFont(new Font("Tahoma", Font.BOLD, 12));
    searchBar.add(lEmp);

    Choice cEmp = new Choice();
    // Load Employee IDs
    try {
        ResultSet rs = new Conn().s.executeQuery("SELECT empId FROM teacher ORDER BY empId");
        while(rs.next()) cEmp.add(rs.getString("empId"));
    } catch(Exception ex){ex.printStackTrace();}
    searchBar.add(cEmp);

    JButton btnSearch = new JButton("Search");
    btnSearch.setBackground(Color.black);
    btnSearch.setForeground(Color.white);
    searchBar.add(btnSearch);

    JButton btnAll = new JButton("Show All");
    btnAll.setBackground(new Color(0, 100, 0));
    btnAll.setForeground(Color.white);
    searchBar.add(btnAll);

    northPanel.add(searchBar);
    f.add(northPanel, BorderLayout.NORTH);

    // --- CENTER SECTION (The Table) ---
    JTable table = new JTable();
    styleTable(table); // Uses your existing helper
    JScrollPane jsp = new JScrollPane(table);
    f.add(jsp, BorderLayout.CENTER);

    // --- LOGIC ---
    Runnable loadAll = () -> {
        try {
            ResultSet rs = new Conn().s.executeQuery(
                "SELECT t.empId, t.name, t.fname, t.phone, t.email, t.education, d.deptName " +
                "FROM teacher t JOIN department d ON t.deptId=d.deptId ORDER BY t.name");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception ex){ex.printStackTrace();}
    };

    btnSearch.addActionListener(e -> {
        try {
            ResultSet rs = new Conn().s.executeQuery(
                "SELECT t.empId, t.name, t.fname, t.phone, t.email, t.education, d.deptName " +
                "FROM teacher t JOIN department d ON t.deptId=d.deptId WHERE t.empId='" + cEmp.getSelectedItem() + "'");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception ex){ex.printStackTrace();}
    });

    btnAll.addActionListener(e -> loadAll.run());

    loadAll.run(); // Initial load
    f.setVisible(true);
}

    // ── SEARCH BY NAME ────────────────────────────────────────
    public void searchByName() {
        String name=JOptionPane.showInputDialog("Enter Faculty Name to search:");
        if(name==null||name.trim().isEmpty()) return;
        JFrame f=new JFrame("Faculty Search - "+name);
        f.setSize(1050,400); f.setLocationRelativeTo(null);
        JTable table=new JTable();
        try {
            ResultSet rs=new Conn().s.executeQuery(
                "SELECT t.empId,t.name,t.fname,t.phone,t.email,d.deptName FROM teacher t " +
                "JOIN department d ON t.deptId=d.deptId WHERE t.name LIKE '%"+name+"%'");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception ex){ex.printStackTrace();}
        styleTable(table);
        f.add(new JScrollPane(table)); f.setVisible(true);
    }

    // ── UPDATE TEACHER ────────────────────────────────────────
    public void updateTeacher() {
        JFrame f=new JFrame("Update Faculty");
        f.setSize(720,430); f.setLocationRelativeTo(null);
        f.setLayout(null); f.getContentPane().setBackground(Color.white);

        JLabel h=new JLabel("UPDATE FACULTY",SwingConstants.CENTER);
        h.setBounds(0,15,720,40); h.setFont(new Font("Tahoma",Font.BOLD,22));
        h.setForeground(new Color(0,51,102)); f.add(h);

        JLabel ls=lbl("Select Emp ID:"); ls.setBounds(30,65,150,25); f.add(ls);
        Choice cEmp=new Choice(); cEmp.setBounds(190,65,200,25); f.add(cEmp);
        try {
            ResultSet rs=new Conn().s.executeQuery("SELECT empId FROM teacher ORDER BY empId");
            while(rs.next()) cEmp.add(rs.getString("empId"));
        } catch(Exception ex){ex.printStackTrace();}

        JLabel lName=new JLabel(); JLabel lFname=new JLabel();
        JLabel lEmpId=new JLabel(); JLabel lDob=new JLabel(); JLabel lCnic=new JLabel();
        JTextField tfAddr=tf(),tfPhone=tf(),tfEmail=tf(),tfEdu=tf(),tfDept=tf();

        int lx1=30,fx1=185,lx2=390,fx2=545,fw=155,fh=27,lh=27,lw=145;
        addROLabel(f,"Name:",lx1,108,lw,lh,lName,fx1,108,fw,fh);
        addROLabel(f,"Father Name:",lx2,108,lw,lh,lFname,fx2,108,fw,fh);
        addROLabel(f,"Emp ID:",lx1,148,lw,lh,lEmpId,fx1,148,fw,fh);
        addROLabel(f,"DOB:",lx2,148,lw,lh,lDob,fx2,148,fw,fh);
        addROLabel(f,"CNIC:",lx1,188,lw,lh,lCnic,fx1,188,fw,fh);
        addRow(f,"Address:",lx1,228,lh,tfAddr,fx1,228,fw,fh);
        addRow(f,"Phone:",  lx2,228,lh,tfPhone,fx2,228,fw,fh);
        addRow(f,"Email:",  lx1,268,lh,tfEmail,fx1,268,fw,fh);
        addRow(f,"Education:",lx2,268,lh,tfEdu,fx2,268,fw,fh);
        addRow(f,"Department ID:",lx1,308,lh,tfDept,fx1,308,fw,fh);

        JButton sub=subBtn(f,"Update",215,365);
        JButton can=canBtn(f,"Cancel",360,365);

        Runnable load=()->{
            try {
                ResultSet rs=new Conn().s.executeQuery(
                    "SELECT * FROM teacher WHERE empId='"+cEmp.getSelectedItem()+"'");
                if(rs.next()){
                    lName.setText(rs.getString("name")); lFname.setText(rs.getString("fname"));
                    lEmpId.setText(rs.getString("empId")); lDob.setText(rs.getString("dob"));
                    lCnic.setText(rs.getString("cnic")); tfAddr.setText(rs.getString("address"));
                    tfPhone.setText(rs.getString("phone")); tfEmail.setText(rs.getString("email"));
                    tfEdu.setText(rs.getString("education")); tfDept.setText(rs.getString("deptId"));
                }
            } catch(Exception ex){ex.printStackTrace();}
        };
        load.run();
        cEmp.addItemListener(ie->load.run());
        f.setVisible(true);

        sub.addActionListener(e->{
            String empId=lEmpId.getText();
            String addr=tfAddr.getText().trim(), phone=tfPhone.getText().trim();
            String email=tfEmail.getText().trim(),edu=tfEdu.getText().trim(),deptId=tfDept.getText().trim();
            if(addr.isEmpty()||phone.isEmpty()||email.isEmpty()){err("Address, Phone, Email required."); return;}
            if(!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")){err("Invalid email."); return;}
            try {
                PreparedStatement ps=new Conn().c.prepareStatement(
                    "UPDATE teacher SET address=?,phone=?,email=?,education=?,deptId=? WHERE empId=?");
                ps.setString(1,addr); ps.setString(2,phone); ps.setString(3,email);
                ps.setString(4,edu);  ps.setString(5,deptId); ps.setString(6,empId);
                ps.executeUpdate(); ps.close();
                ok("Faculty updated successfully."); f.dispose();
            } catch(Exception ex){err(ex.getMessage());}
        });
        can.addActionListener(e->f.dispose());
    }

    // ── DELETE TEACHER ────────────────────────────────────────
    public void deleteTeacher() {
        JFrame f=new JFrame("Delete Faculty");
        f.setSize(750,460); f.setLocationRelativeTo(null);
        f.setLayout(null); f.getContentPane().setBackground(Color.white);

        JLabel h=new JLabel("DELETE FACULTY",SwingConstants.CENTER);
        h.setBounds(0,15,750,40); h.setFont(new Font("Tahoma",Font.BOLD,22));
        h.setForeground(new Color(0,51,102)); f.add(h);

        JLabel ls=lbl("Select Emp ID:"); ls.setBounds(30,65,140,25); f.add(ls);
        Choice cEmp=new Choice(); cEmp.setBounds(180,65,200,25); f.add(cEmp);
        try {
            ResultSet rs=new Conn().s.executeQuery("SELECT empId FROM teacher ORDER BY empId");
            while(rs.next()) cEmp.add(rs.getString("empId"));
        } catch(Exception ex){ex.printStackTrace();}

        JTable table=new JTable(); styleTable(table);
        JScrollPane jsp=new JScrollPane(table); jsp.setBounds(0,100,750,280); f.add(jsp);

        JButton prev=new JButton("Preview"); prev.setBounds(420,62,100,26);
        prev.setBackground(Color.black); prev.setForeground(Color.white);
        prev.setFont(new Font("Tahoma",Font.BOLD,12)); f.add(prev);

        JButton del=new JButton("Delete"); del.setBounds(190,390,130,32);
        del.setBackground(new Color(180,0,0)); del.setForeground(Color.white);
        del.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(del);

        JButton can=canBtn(f,"Cancel",340,390);

        Runnable loadPrev=()->{
            try {
                ResultSet rs=new Conn().s.executeQuery(
                    "SELECT * FROM teacher WHERE empId='"+cEmp.getSelectedItem()+"'");
                table.setModel(DbUtils.resultSetToTableModel(rs));
            } catch(Exception ex){ex.printStackTrace();}
        };
        loadPrev.run();
        cEmp.addItemListener(ie->loadPrev.run());
        prev.addActionListener(e->loadPrev.run());
        f.setVisible(true);

        del.addActionListener(e->{
            String empId=cEmp.getSelectedItem();
            if(empId==null) return;
            int c2=JOptionPane.showConfirmDialog(f,"Delete faculty "+empId+"? Their leave records will also be removed.","Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(c2!=JOptionPane.YES_OPTION) return;
            try {
                Conn c=new Conn();
                c.s.executeUpdate("DELETE FROM teacherleave WHERE empId='"+empId+"'");
                c.s.executeUpdate("DELETE FROM teacher_course WHERE empId='"+empId+"'");
                c.s.executeUpdate("DELETE FROM teacher WHERE empId='"+empId+"'");
                c.s.executeUpdate("DELETE FROM login WHERE username='"+empId+"'");
                ok("Faculty deleted. Department NOT affected."); f.dispose();
            } catch(Exception ex){err(ex.getMessage());}
        });
        can.addActionListener(e->f.dispose());
    }

    // ── VIEW MY COURSES (FACULTY) ─────────────────────────────
    public void viewMyCourses() {
        JFrame f=new JFrame("My Courses");
        f.setSize(900,400); f.setLocationRelativeTo(null);
        JTable table=new JTable();
        try {
            ResultSet rs=new Conn().s.executeQuery(
                "SELECT tc.courseId, c.courseName, d.deptName, c.creditHours " +
                "FROM teacher_course tc JOIN course c ON tc.courseId=c.courseId " +
                "JOIN department d ON c.deptId=d.deptId WHERE tc.empId='"+loggedInId+"'");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception ex){ex.printStackTrace();}
        styleTable(table);
        f.add(new JScrollPane(table)); f.setVisible(true);
    }

    // ── VIEW MY STUDENTS (only students in my courses) ─────────
  // ── VIEW MY STUDENTS ─────────────────────────────────
// ── VIEW MY STUDENTS ─────────────────────────────────
public void viewMyStudents() {

    JFrame frame = new JFrame("My Students");

    frame.setSize(950, 550);
    frame.setLocationRelativeTo(null);
    frame.setLayout(null);

    JLabel heading = new JLabel(
            "MY STUDENTS",
            SwingConstants.CENTER
    );

    heading.setBounds(0, 10, 950, 35);

    heading.setFont(
            new Font("Tahoma", Font.BOLD, 24)
    );

    heading.setForeground(
            new Color(0,51,102)
    );

    frame.add(heading);

    // ============================================
    // COURSE DROPDOWN
    // ============================================

    JLabel lblCourse =
            new JLabel("Select Course:");

    lblCourse.setBounds(40, 70, 120, 25);

    lblCourse.setFont(
            new Font("Tahoma", Font.BOLD, 14)
    );

    frame.add(lblCourse);

    JComboBox<String> cbCourse =
            new JComboBox<>();

    cbCourse.setBounds(170, 70, 350, 28);

    cbCourse.setBackground(Color.WHITE);

    frame.add(cbCourse);

    // ============================================
    // LOAD COURSES
    // ============================================

    try {

        Conn conn = new Conn();

        ResultSet rs =
                conn.s.executeQuery(

            "SELECT tc.courseId, " +
            "c.courseName, " +
            "tc.semester " +

            "FROM teacher_course tc " +

            "JOIN course c " +
            "ON tc.courseId = c.courseId " +

            "WHERE tc.empId = '" + loggedInId + "'"
        );

        while (rs.next()) {

            cbCourse.addItem(

                rs.getString("courseId")
                + " — "
                + rs.getString("courseName")
                + " — "
                + rs.getString("semester")
            );
        }

    } catch (Exception ex) {

        ex.printStackTrace();
    }

    // ============================================
    // LOAD BUTTON
    // ============================================

    JButton loadBtn =
            new JButton("LOAD STUDENTS");

    loadBtn.setBounds(560, 70, 180, 30);

    loadBtn.setBackground(Color.BLACK);

    loadBtn.setForeground(Color.WHITE);

    loadBtn.setFont(
            new Font("Tahoma", Font.BOLD, 12)
    );

    frame.add(loadBtn);

    // ============================================
    // TABLE
    // ============================================

    JTable table = new JTable();

    JScrollPane jsp =
            new JScrollPane(table);

    jsp.setBounds(20, 130, 900, 340);

    frame.add(jsp);

    // ============================================
    // LOAD STUDENTS
    // ============================================

    loadBtn.addActionListener(e -> {

        try {

            String selected =
                    cbCourse.getSelectedItem()
                            .toString();

            String courseId =
                    selected.split(" — ")[0];

            String semester =
                    selected.split(" — ")[2];

            Conn conn = new Conn();

           PreparedStatement ps = conn.c.prepareStatement(
    "SELECT DISTINCT s.rollno, s.name, s.fname, s.program " +
    "FROM student s " +
    "JOIN student_course sc ON s.rollno = sc.rollno " +
    "WHERE sc.courseId = ?"
);

    ps.setString(1, courseId);
     ResultSet rs = ps.executeQuery();
            table.setModel(
                    DbUtils.resultSetToTableModel(rs)
            );

        } catch (Exception ex) {

            ex.printStackTrace();
        }
    });

    frame.setVisible(true);
}

    // ── APPLY LEAVE (FACULTY) ─────────────────────────────────
    public void applyLeave() {
        JFrame f=new JFrame("Apply Leave — Faculty");
        f.setSize(460,370); f.setLocationRelativeTo(null);
        f.setLayout(null); f.getContentPane().setBackground(Color.white);

        JLabel h=new JLabel("APPLY LEAVE — FACULTY",SwingConstants.CENTER);
        h.setBounds(0,20,460,35); h.setFont(new Font("Tahoma",Font.BOLD,20));
        h.setForeground(new Color(0,51,102)); f.add(h);

        JLabel le=lbl("Emp ID:"); le.setBounds(40,75,130,25); f.add(le);
        JLabel valEmp=new JLabel(loggedInId); valEmp.setBounds(185,75,200,25);
        valEmp.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(valEmp);

        JLabel ld=lbl("Leave Date:"); ld.setBounds(40,115,130,25); f.add(ld);
        JDateChooser dc=new JDateChooser(); dc.setBounds(185,115,200,25); f.add(dc);

        JLabel ldur=lbl("Duration:"); ldur.setBounds(40,158,130,25); f.add(ldur);
        Choice cDur=new Choice(); cDur.add("Full Day"); cDur.add("Half Day");
        cDur.setBounds(185,158,200,25); f.add(cDur);

        JLabel lreas=lbl("Reason:"); lreas.setBounds(40,200,130,25); f.add(lreas);
        JTextField tfReas=tf(); tfReas.setBounds(185,200,230,26); f.add(tfReas);

        JButton sub=subBtn(f,"Submit",110,270);
        JButton can=canBtn(f,"Cancel",255,270);
        f.setVisible(true);

        sub.addActionListener(e->{
            String date=((JTextField)dc.getDateEditor().getUiComponent()).getText().trim();
            if(date.isEmpty()){err("Please select a date."); return;}
            try{
                PreparedStatement ps=new Conn().c.prepareStatement(
                    "INSERT INTO teacherleave (empId,date,duration,reason,status) VALUES(?,?,?,?,'PENDING')");
                ps.setString(1,loggedInId); ps.setString(2,date);
                ps.setString(3,cDur.getSelectedItem()); ps.setString(4,tfReas.getText().trim());
                ps.executeUpdate(); ps.close();
                ok("Leave applied successfully."); f.dispose();
            } catch(Exception ex){err(ex.getMessage());}
        });
        can.addActionListener(e->f.dispose());
    }

    // ── VIEW MY LEAVES (FACULTY) ──────────────────────────────
    public void myLeaves() {
        JFrame f=new JFrame("My Leave Records — "+loggedInId);
        f.setSize(900,400); f.setLocationRelativeTo(null);
        JTable table=new JTable();
        try {
            ResultSet rs=new Conn().s.executeQuery(
                "SELECT leaveId,date,duration,reason,status FROM teacherleave WHERE empId='"+loggedInId+"' ORDER BY leaveId DESC");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception ex){ex.printStackTrace();}
        styleTable(table);
        f.add(new JScrollPane(table)); f.setVisible(true);
    }

    // ── VIEW ALL TEACHER LEAVES (ADMIN) ──────────────────────
    public void viewTeacherLeaves() {
        JFrame f=new JFrame("Faculty Leave Records");
        f.setSize(950,450); f.setLocationRelativeTo(null);
        JTable table=new JTable();
        try {
            ResultSet rs=new Conn().s.executeQuery(
                "SELECT tl.leaveId, tl.empId, t.name, tl.date, tl.duration, tl.reason, tl.status " +
                "FROM teacherleave tl JOIN teacher t ON tl.empId=t.empId ORDER BY tl.leaveId DESC");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception ex){ex.printStackTrace();}
        styleTable(table);
        f.add(new JScrollPane(table)); f.setVisible(true);
    }

    // ── RESET PASSWORD ────────────────────────────────────────
    public void resetPassword() {
        String empId=JOptionPane.showInputDialog("Enter Emp ID to reset password:");
        if(empId==null||empId.trim().isEmpty()) return;
        String newPwd=JOptionPane.showInputDialog("Enter new password for "+empId+":");
        if(newPwd==null||newPwd.trim().isEmpty()) return;
        try{
            PreparedStatement ps=new Conn().c.prepareStatement(
                "UPDATE login SET password=? WHERE username=? AND role='FACULTY'");
            ps.setString(1,newPwd); ps.setString(2,empId);
            int rows=ps.executeUpdate(); ps.close();
            if(rows>0) ok("Password reset successfully.");
            else err("Faculty ID not found in login system.");
        }catch(Exception ex){err(ex.getMessage());}
    }

    // ── Helpers ───────────────────────────────────────────────
    private void addRow(JFrame f,String txt,int lx,int ly,int lh,java.awt.Component comp,int cx,int cy,int cw,int ch){
        JLabel l=new JLabel(txt); l.setBounds(lx,ly,160,lh);
        l.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(l);
        comp.setBounds(cx,cy,cw,ch); f.add(comp);
    }
    private void addROLabel(JFrame f,String txt,int lx,int ly,int lw,int lh,JLabel disp,int dx,int dy,int dw,int dh){
        JLabel l=new JLabel(txt); l.setBounds(lx,ly,lw,lh);
        l.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(l);
        disp.setBounds(dx,dy,dw,dh); disp.setFont(new Font("Tahoma",Font.PLAIN,13)); f.add(disp);
    }
    private JLabel lbl(String t){ JLabel l=new JLabel(t); l.setFont(new Font("Tahoma",Font.BOLD,13)); return l; }
    private JTextField tf(){ JTextField t=new JTextField(); t.setFont(new Font("Tahoma",Font.PLAIN,13)); return t; }
    private JButton subBtn(JFrame f,String t,int x,int y){
        JButton b=new JButton(t); b.setBounds(x,y,130,32);
        b.setBackground(Color.black); b.setForeground(Color.white);
        b.setFont(new Font("Tahoma",Font.BOLD,12)); f.add(b); return b;
    }
    private JButton canBtn(JFrame f,String t,int x,int y){
        JButton b=new JButton(t); b.setBounds(x,y,130,32);
        b.setBackground(new Color(100,100,100)); b.setForeground(Color.white);
        b.setFont(new Font("Tahoma",Font.BOLD,12)); f.add(b); return b;
    }
    private void styleTable(JTable t){
        t.setFont(new Font("Tahoma",Font.PLAIN,13)); t.setRowHeight(22);
        t.getTableHeader().setFont(new Font("Tahoma",Font.BOLD,13));
    }
    private void ok(String m){JOptionPane.showMessageDialog(null,m);}
    private void err(String m){JOptionPane.showMessageDialog(null,m,"Error",JOptionPane.ERROR_MESSAGE);}
}