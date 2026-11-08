package university.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Random;
import net.proteanit.sql.DbUtils;
import com.toedter.calendar.JDateChooser;

/**
 * Student — all student screens.
 * loggedInId = rollno when role is STUDENT. Every query filters by it.
 * Admin passes "ADMIN" as loggedInId — admin sees all.
 */
public class Student {
    private final String loggedInId;

    public Student(String loggedInId) { this.loggedInId = loggedInId; }

    // ── ADD STUDENT ───────────────────────────────────────────
    public void addStudent() {
        JFrame f = new JFrame("Add New Student");
        f.setSize(730, 600); f.setLocationRelativeTo(null);
        f.setLayout(null);
        f.getContentPane().setBackground(Color.white);

        JLabel h = new JLabel("ADD NEW STUDENT", SwingConstants.CENTER);
        h.setBounds(0,15,730,40);
        h.setFont(new Font("Tahoma",Font.BOLD,24));
        h.setForeground(new Color(0,51,102)); 
        f.add(h);

        String rollno = "1533" + (Math.abs(new Random().nextLong()%9000L)+1000L);

        JLabel lr = lbl("Roll No (Auto):"); 
        lr.setBounds(30,65,160,25);
        f.add(lr);
        JLabel valRoll = new JLabel(rollno);
        valRoll.setBounds(200,65,220,25);
        valRoll.setFont(new Font("Tahoma",Font.BOLD,13));
        valRoll.setForeground(new Color(0,120,0)); f.add(valRoll);

        int lx1=30,fx1=195,lx2=395,fx2=555,fw=155,fh=27,lh=27;

        JTextField tfName=tf(),tfFname=tf(),tfAddr=tf(),tfPhone=tf(),tfEmail=tf(),tfX=tf(),tfXii=tf(),tfCnic=tf();
        JDateChooser dcDob=new JDateChooser();

        addRow(f,"Name:",         lx1,108,lh,tfName,  fx1,108,fw,fh);
        addRow(f,"Father's Name:",lx2,108,lh,tfFname, fx2,108,fw,fh);
        addRow(f,"Date of Birth:",lx1,148,lh,dcDob,   fx1,148,fw,fh);
        addRow(f,"Address:",      lx2,148,lh,tfAddr,  fx2,148,fw,fh);
        addRow(f,"Phone:",        lx1,188,lh,tfPhone, fx1,188,fw,fh);
        addRow(f,"Email:",        lx2,188,lh,tfEmail, fx2,188,fw,fh);
        addRow(f,"Matric %:",     lx1,228,lh,tfX,     fx1,228,fw,fh);
        addRow(f,"Inter %:",      lx2,228,lh,tfXii,   fx2,228,fw,fh);
        addRow(f,"CNIC:",         lx1,268,lh,tfCnic,  fx1,268,fw,fh);

        JLabel hintC=new JLabel("(00000-0000000-0)");
        hintC.setBounds(fx1+fw+5,268,170,fh);
        hintC.setFont(new Font("Tahoma",Font.PLAIN,10));
        hintC.setForeground(Color.GRAY);
        f.add(hintC);

        JLabel lProg=lbl("Program:"); lProg.setBounds(lx1,308,160,lh);
        f.add(lProg);
        String[] progs={"B.Tech","BBA","BCA","BSAI","MSc","MBA","MCA","MCom","MA","BA"};
        JComboBox<String> cbProg=new JComboBox<>(progs); 
        cbProg.setBounds(fx1,308,fw,fh);
        cbProg.setBackground(Color.white); 
        f.add(cbProg);

        JLabel lDept=lbl("Department:");
        lDept.setBounds(lx2,308,160,lh); f.add(lDept);
        JComboBox<String> cbDept=new JComboBox<>(); 
        cbDept.setBounds(fx2,308,fw,fh);
        cbDept.setBackground(Color.white); f.add(cbDept);
        try {
            ResultSet rs=new Conn().s.executeQuery("SELECT deptId,deptName FROM department ORDER BY deptName");
            while(rs.next()) cbDept.addItem(rs.getString("deptId")+" — "+rs.getString("deptName"));
        } 
        catch(Exception ex){ex.printStackTrace();}

        JLabel lSem=lbl("Semester:"); lSem.setBounds(lx1,348,160,lh); f.add(lSem);
        String[] sems={"1st Semester","2nd Semester","3rd Semester","4th Semester",
                        "5th Semester","6th Semester","7th Semester","8th Semester"};
        JComboBox<String> cbSem=new JComboBox<>(sems);
        cbSem.setBounds(fx1,348,fw,fh);
        cbSem.setBackground(Color.white); f.add(cbSem);

        JLabel lPwd=lbl("Default Password:"); lPwd.setBounds(lx2,348,160,lh); f.add(lPwd);
        JTextField tfPwd=tf(); tfPwd.setBounds(fx2,348,fw,fh); tfPwd.setText("student123"); f.add(tfPwd);

        JButton sub=subBtn(f,"Submit",225,445);
        JButton can=canBtn(f,"Cancel",370,445);
        f.setVisible(true);

        sub.addActionListener(e -> {
            String name=tfName.getText().trim(), fname=tfFname.getText().trim();
            String dob=((JTextField)dcDob.getDateEditor().getUiComponent()).getText().trim();
            String addr=tfAddr.getText().trim(), phone=tfPhone.getText().trim();
            String email=tfEmail.getText().trim(), cnic=tfCnic.getText().trim();
            String x=tfX.getText().trim(), xii=tfXii.getText().trim();
            String prog=(String)cbProg.getSelectedItem();
            String deptId=cbDept.getSelectedItem().toString().split(" — ")[0];
            String sem=(String)cbSem.getSelectedItem();
            String pwd=tfPwd.getText().trim();

            if(name.isEmpty()||fname.isEmpty()||phone.isEmpty()||email.isEmpty()||cnic.isEmpty()){
                err("All fields are required."); return;}
            if(!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")){err("Invalid email format."); return;}
            if(!cnic.matches("\\d{5}-\\d{7}-\\d")){err("Invalid CNIC. Use: 00000-0000000-0"); return;}
            if(!phone.matches("\\d+")){err("Phone must be digits only."); return;}

            try {
                Conn c=new Conn();
               PreparedStatement ps = c.c.prepareStatement(
             "INSERT INTO student (rollno,name,fname,dob,address,phone,email,cnic,class_x,class_xii,program,deptId,semester)"
                     + "  VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"
              );
                ps.setString(1, rollno);
                ps.setString(2, name);
                ps.setString(3, fname);
                ps.setString(4, dob);
                ps.setString(5, addr);
                ps.setString(6, phone);
                ps.setString(7, email);
                ps.setString(8, cnic);
                ps.setString(9, x);
                ps.setString(10, xii);
                ps.setString(11, prog);
                ps.setString(12, deptId);
                ps.setString(13, sem);
                ps.executeUpdate(); 
                ps.close();

                PreparedStatement pl=c.c.prepareStatement("INSERT INTO login VALUES(?,?,'STUDENT')");
                pl.setString(1,rollno); pl.setString(2,pwd);
                pl.executeUpdate(); pl.close();

                ok("Student added!\nRoll No: "+rollno+"\nPassword: "+pwd); f.dispose();
            } catch(Exception ex){err("DB Error: "+ex.getMessage());}
        });
        can.addActionListener(e->f.dispose());
    }

    // ── VIEW ALL STUDENTS (ADMIN) ─────────────────────────────
    public void viewStudents() {
    JFrame f = new JFrame("Student Records");
    f.setSize(1100, 600); // Increased height for better view
    f.setLocationRelativeTo(null);
    f.setLayout(new BorderLayout());

    // --- TOP SECTION (Heading + Search Bar) ---
    JPanel northPanel = new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
    northPanel.setBackground(Color.white);

    // 1. Heading
    JLabel h = new JLabel("STUDENT RECORDS", SwingConstants.CENTER);
    h.setAlignmentX(Component.CENTER_ALIGNMENT);
    h.setFont(new Font("Tahoma", Font.BOLD, 22));
    h.setForeground(new Color(0, 51, 102));
    h.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
    northPanel.add(h);

    // 2. Search Bar Panel
    JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
    searchBar.setBackground(Color.white);

    JLabel lRoll = new JLabel("Roll No:");
    lRoll.setFont(new Font("Tahoma", Font.BOLD, 12));
    searchBar.add(lRoll);

    Choice cRoll = new Choice();
    // Load roll numbers
    try {
        ResultSet rs = new Conn().s.executeQuery("SELECT rollno FROM student ORDER BY rollno");
        while(rs.next()) cRoll.add(rs.getString("rollno"));
    } catch(Exception ex){ex.printStackTrace();}
    searchBar.add(cRoll);

    JButton btnSearch = new JButton("Search");
    btnSearch.setBackground(Color.black);
    btnSearch.setForeground(Color.white);
    searchBar.add(btnSearch);

    JButton btnAll = new JButton("Show All");
    btnAll.setBackground(new Color(0, 100, 0));
    btnAll.setForeground(Color.white);
    searchBar.add(btnAll);

    northPanel.add(searchBar);
    f.add(northPanel, BorderLayout.NORTH); // Add everything to the TOP

    // --- CENTER SECTION (The Table) ---
    JTable table = new JTable();
    styleTable(table); // Using your existing helper
    JScrollPane jsp = new JScrollPane(table);
    f.add(jsp, BorderLayout.CENTER); // Table now takes up all the main space

    // --- LOGIC ---
    Runnable loadAll = () -> {
        try {
            ResultSet rs = new Conn().s.executeQuery(
                "SELECT s.rollno, s.name, s.fname, s.program, d.deptName, s.semester, s.phone, s.email " +
                "FROM student s JOIN department d ON s.deptId=d.deptId ORDER BY s.name");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception ex){ex.printStackTrace();}
    };

    btnSearch.addActionListener(e -> {
        try {
            ResultSet rs = new Conn().s.executeQuery(
                "SELECT s.rollno, s.name, s.fname, s.program, d.deptName, s.semester, s.phone, s.email " +
                "FROM student s JOIN department d ON s.deptId=d.deptId WHERE s.rollno='" + cRoll.getSelectedItem() + "'");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception ex){ex.printStackTrace();}
    });

    btnAll.addActionListener(e -> loadAll.run());

    loadAll.run();
    f.setVisible(true);
}

    // ── SEARCH BY NAME ────────────────────────────────────────
    public void searchByName() {
        String name=JOptionPane.showInputDialog("Enter student name to search:");
        if(name==null||name.trim().isEmpty()) return;
        JFrame f=new JFrame("Student Search — "+name);
        f.setSize(1000,400); f.setLocationRelativeTo(null);
        JTable table=new JTable();
        try {
            ResultSet rs=new Conn().s.executeQuery(
                "SELECT s.rollno,s.name,s.fname,s.program,d.deptName,s.semester " +
                "FROM student s JOIN department d ON s.deptId=d.deptId WHERE s.name LIKE '%"+name+"%'");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception ex){ex.printStackTrace();}
        styleTable(table);
        f.add(new JScrollPane(table)); f.setVisible(true);
    }

    // ── UPDATE STUDENT ────────────────────────────────────────
    public void updateStudent() {
        JFrame f=new JFrame("Update Student");
        f.setSize(720,440); f.setLocationRelativeTo(null);
        f.setLayout(null); f.getContentPane().setBackground(Color.white);

        JLabel h=new JLabel("UPDATE STUDENT",SwingConstants.CENTER);
        h.setBounds(0,15,720,40); h.setFont(new Font("Tahoma",Font.BOLD,22));
        h.setForeground(new Color(0,51,102)); f.add(h);

        JLabel ls=lbl("Select Roll No:"); ls.setBounds(30,65,150,25); f.add(ls);
        Choice cRoll=new Choice(); cRoll.setBounds(190,65,200,25); f.add(cRoll);
        try {
            ResultSet rs=new Conn().s.executeQuery("SELECT rollno FROM student ORDER BY rollno");
            while(rs.next()) cRoll.add(rs.getString("rollno"));
        } catch(Exception ex){ex.printStackTrace();}

        JLabel lName=new JLabel(),lFname=new JLabel(),lRoll=new JLabel(),lDob=new JLabel(),lCnic=new JLabel();
        JTextField tfAddr=tf(),tfPhone=tf(),tfEmail=tf(),tfProg=tf(),tfDept=tf();

        int lx1=30,fx1=185,lx2=390,fx2=545,fw=155,fh=27,lh=27,lw=145;
        addROLabel(f,"Name:",     lx1,108,lw,lh,lName, fx1,108,fw,fh);
        addROLabel(f,"Father:",   lx2,108,lw,lh,lFname,fx2,108,fw,fh);
        addROLabel(f,"Roll No:",  lx1,148,lw,lh,lRoll, fx1,148,fw,fh);
        addROLabel(f,"DOB:",      lx2,148,lw,lh,lDob,  fx2,148,fw,fh);
        addROLabel(f,"CNIC:",     lx1,188,lw,lh,lCnic, fx1,188,fw,fh);
        addRow(f,"Address:", lx1,228,lh,tfAddr, fx1,228,fw,fh);
        addRow(f,"Phone:",   lx2,228,lh,tfPhone,fx2,228,fw,fh);
        addRow(f,"Email:",   lx1,268,lh,tfEmail,fx1,268,fw,fh);
        addRow(f,"Program:", lx2,268,lh,tfProg, fx2,268,fw,fh);
        addRow(f,"Dept ID:", lx1,308,lh,tfDept, fx1,308,fw,fh);

        JButton sub=subBtn(f,"Update",215,368);
        JButton can=canBtn(f,"Cancel",360,368);

        Runnable load=()->{
            try {
                ResultSet rs=new Conn().s.executeQuery("SELECT * FROM student WHERE rollno='"+cRoll.getSelectedItem()+"'");
                if(rs.next()){
                    lName.setText(rs.getString("name")); lFname.setText(rs.getString("fname"));
                    lRoll.setText(rs.getString("rollno")); lDob.setText(rs.getString("dob"));
                    lCnic.setText(rs.getString("cnic")); tfAddr.setText(rs.getString("address"));
                    tfPhone.setText(rs.getString("phone")); tfEmail.setText(rs.getString("email"));
                    tfProg.setText(rs.getString("program")); tfDept.setText(rs.getString("deptId"));
                }
            } catch(Exception ex){ex.printStackTrace();}
        };
        load.run(); cRoll.addItemListener(ie->load.run()); f.setVisible(true);

        sub.addActionListener(e->{
            String rollno=lRoll.getText();
            if(!tfEmail.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")){err("Invalid email."); return;}
            try {
                PreparedStatement ps=new Conn().c.prepareStatement(
                    "UPDATE student SET address=?,phone=?,email=?,program=?,deptId=? WHERE rollno=?");
                ps.setString(1,tfAddr.getText().trim()); ps.setString(2,tfPhone.getText().trim());
                ps.setString(3,tfEmail.getText().trim()); ps.setString(4,tfProg.getText().trim());
                ps.setString(5,tfDept.getText().trim()); ps.setString(6,rollno);
                ps.executeUpdate(); ps.close(); ok("Student updated."); f.dispose();
            } catch(Exception ex){err(ex.getMessage());}
        });
        can.addActionListener(e->f.dispose());
    }

    // ── DELETE STUDENT ────────────────────────────────────────
    public void deleteStudent() {
        JFrame f=new JFrame("Delete Student");
        f.setSize(750,460); f.setLocationRelativeTo(null);
        f.setLayout(null); f.getContentPane().setBackground(Color.white);

        JLabel h=new JLabel("DELETE STUDENT",SwingConstants.CENTER);
        h.setBounds(0,15,750,40); h.setFont(new Font("Tahoma",Font.BOLD,22));
        h.setForeground(new Color(0,51,102)); f.add(h);

        JLabel ls=lbl("Select Roll No:"); ls.setBounds(30,65,150,25); f.add(ls);
        Choice cRoll=new Choice(); cRoll.setBounds(190,65,200,25); f.add(cRoll);
        try {
            ResultSet rs=new Conn().s.executeQuery("SELECT rollno FROM student ORDER BY rollno");
            while(rs.next()) cRoll.add(rs.getString("rollno"));
        } catch(Exception ex){ex.printStackTrace();}

        JTable table=new JTable(); styleTable(table);
        JScrollPane jsp=new JScrollPane(table); jsp.setBounds(0,100,750,270); f.add(jsp);

        JButton prev=new JButton("Preview"); prev.setBounds(410,62,100,26);
        prev.setBackground(Color.black); prev.setForeground(Color.white);
        prev.setFont(new Font("Tahoma",Font.BOLD,12)); f.add(prev);

        JButton del=new JButton("Delete"); del.setBounds(200,390,130,32);
        del.setBackground(new Color(180,0,0)); del.setForeground(Color.white);
        del.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(del);
        JButton can=canBtn(f,"Cancel",345,390);

        Runnable loadPrev=()->{
            try {
                ResultSet rs=new Conn().s.executeQuery("SELECT * FROM student WHERE rollno='"+cRoll.getSelectedItem()+"'");
                table.setModel(DbUtils.resultSetToTableModel(rs));
            } catch(Exception ex){ex.printStackTrace();}
        };
        loadPrev.run(); cRoll.addItemListener(ie->loadPrev.run());
        prev.addActionListener(e->loadPrev.run()); f.setVisible(true);

        del.addActionListener(e->{
            String rollno=cRoll.getSelectedItem(); if(rollno==null) return;
            int c2=JOptionPane.showConfirmDialog(f,"Delete student "+rollno+"?\nMarks, attendance, leaves and enrollment records will also be removed.","Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
            if(c2!=JOptionPane.YES_OPTION) return;
            try {
                Conn c=new Conn();
                c.s.executeUpdate("DELETE FROM marks        WHERE rollno='"+rollno+"'");
                c.s.executeUpdate("DELETE FROM attendance   WHERE rollno='"+rollno+"'");
                c.s.executeUpdate("DELETE FROM studentleave WHERE rollno='"+rollno+"'");
                c.s.executeUpdate("DELETE FROM student_course WHERE rollno='"+rollno+"'");
                c.s.executeUpdate("DELETE FROM submission   WHERE rollno='"+rollno+"'");
                c.s.executeUpdate("DELETE FROM fee_payment  WHERE rollno='"+rollno+"'");
                c.s.executeUpdate("DELETE FROM semester_status WHERE rollno='"+rollno+"'");
                c.s.executeUpdate("DELETE FROM student      WHERE rollno='"+rollno+"'");
                c.s.executeUpdate("DELETE FROM login        WHERE username='"+rollno+"'");
                ok("Student deleted. Department NOT affected."); f.dispose();
            } catch(Exception ex){err(ex.getMessage());}
        });
        can.addActionListener(e->f.dispose());
    }

    // ── VIEW MY PROFILE (STUDENT SELF) ────────────────────────
    public void viewProfile() {
        JFrame f=new JFrame("My Profile — "+loggedInId);
        f.setSize(620,540); f.setLocationRelativeTo(null);
        f.setLayout(null); f.getContentPane().setBackground(Color.white);

        JLabel h=new JLabel("MY PROFILE",SwingConstants.CENTER);
        h.setBounds(0,15,620,35); h.setFont(new Font("Tahoma",Font.BOLD,22));
        h.setForeground(new Color(0,51,102)); f.add(h);
        JSeparator sep=new JSeparator(); sep.setBounds(30,55,560,2); f.add(sep);

        try {
            Conn c=new Conn();
            ResultSet rs=c.s.executeQuery(
                "SELECT s.*,d.deptName FROM student s JOIN department d ON s.deptId=d.deptId WHERE s.rollno='"+loggedInId+"'");
            if(rs.next()){
                String[][] data={
                    {"Roll Number :",rs.getString("rollno")},
                    {"Name :",rs.getString("name")},
                    {"Father's Name :",rs.getString("fname")},
                    {"Date of Birth :",rs.getString("dob")},
                    {"Address :",rs.getString("address")},
                    {"Phone :",rs.getString("phone")},
                    {"Email :",rs.getString("email")},
                    {"CNIC :",rs.getString("cnic")},
                    {"Matric % :",rs.getString("class_x")},
                    {"Inter % :",rs.getString("class_xii")},
                    {"Program :",rs.getString("program")},
                    {"Department :",rs.getString("deptName")},
                    {"Current Semester :",rs.getString("semester")}
                };
                int y=70;
                for(String[] row:data){
                    JLabel l1=new JLabel(row[0]); l1.setBounds(40,y,160,26);
                    l1.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(l1);
                    JSeparator s2=new JSeparator(JSeparator.VERTICAL); s2.setBounds(205,y,2,24); f.add(s2);
                    JLabel l2=new JLabel(row[1]!=null?row[1]:"-"); l2.setBounds(215,y,380,26);
                    l2.setFont(new Font("Tahoma",Font.PLAIN,13)); f.add(l2);
                    y+=32;
                }
            }
        } catch(Exception ex){ex.printStackTrace();}

        JButton close=new JButton("Close"); close.setBounds(240,490,130,30);
        close.setBackground(Color.black); close.setForeground(Color.white);
        close.setFont(new Font("Tahoma",Font.BOLD,12));
        close.addActionListener(e->f.dispose()); f.add(close);
        f.setVisible(true);
    }

    // ── VIEW ENROLLED COURSES (STUDENT) ───────────────────────
    public void viewEnrolledCourses() {
        JFrame f=new JFrame("My Enrolled Courses — "+loggedInId);
        f.setSize(900,420); f.setLocationRelativeTo(null);
        JTable table=new JTable();
        try {
            ResultSet rs=new Conn().s.executeQuery(
                "SELECT c.courseId, c.courseName, d.deptName, c.creditHours " +
                "FROM student_course sc JOIN course c ON sc.courseId=c.courseId " +
                "JOIN department d ON c.deptId=d.deptId WHERE sc.rollno='"+loggedInId+"'");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception ex){ex.printStackTrace();}
        styleTable(table);
        f.add(new JScrollPane(table)); f.setVisible(true);
    }

    // ── VIEW CGPA (STUDENT) ───────────────────────────────────
    public void viewCGPA() { 
    JFrame f = new JFrame("CGPA — " + loggedInId);
    f.setSize(600, 500); 
    f.setLocationRelativeTo(null);
    f.setLayout(null); 
    f.getContentPane().setBackground(Color.white);

    JLabel h = new JLabel("CGPA REPORT", SwingConstants.CENTER);
    h.setBounds(0, 20, 600, 35); 
    h.setFont(new Font("Tahoma", Font.BOLD, 22));
    h.setForeground(new Color(0, 51, 102)); 
    f.add(h);
    
    JSeparator sep = new JSeparator(); 
    sep.setBounds(30, 58, 540, 2); 
    f.add(sep);

    // Headers
    JLabel hC = new JLabel("Course Name"); hC.setBounds(30, 65, 250, 24); 
    hC.setFont(new Font("Tahoma", Font.BOLD, 13)); f.add(hC);
    
    JLabel hO = new JLabel("Total/100"); hO.setBounds(300, 65, 80, 24); 
    hO.setFont(new Font("Tahoma", Font.BOLD, 13)); f.add(hO);
    
    JLabel hG = new JLabel("GPA"); hG.setBounds(400, 65, 120, 24); 
    hG.setFont(new Font("Tahoma", Font.BOLD, 13)); f.add(hG);
    
    JSeparator sep2 = new JSeparator(); 
    sep2.setBounds(30, 90, 540, 2); 
    f.add(sep2);

    int y = 95; 
    double totalGPA = 0; 
    int cnt = 0;

    try {
        Conn conn = new Conn();
        
        String query = "SELECT co.courseName, m.totalObtained, m.gpa " +
                       "FROM marks m JOIN course co ON m.courseId=co.courseId " +
                       "WHERE m.rollno='" + loggedInId + "'";
        
        ResultSet rs = conn.s.executeQuery(query);
        
        while (rs.next()) {
            double totalScore = rs.getDouble("totalObtained"); 
            double gpaValue = rs.getDouble("gpa");

            JLabel lC = new JLabel(rs.getString("courseName")); 
            lC.setBounds(30, y, 260, 24);
            lC.setFont(new Font("Tahoma", Font.PLAIN, 13)); f.add(lC);
            
            JLabel lO = new JLabel(String.format("%.0f", totalScore)); 
            lO.setBounds(300, y, 80, 24);
            lO.setFont(new Font("Tahoma", Font.PLAIN, 13)); f.add(lO);
            
            JLabel lG = new JLabel(String.format("%.2f", gpaValue)); 
            lG.setBounds(400, y, 120, 24);
            lG.setFont(new Font("Tahoma", Font.PLAIN, 13)); f.add(lG);
            
            JSeparator ln = new JSeparator(); 
            ln.setBounds(30, y + 24, 540, 1); 
            f.add(ln);
            
            totalGPA += gpaValue; 
            cnt++; 
            y += 30;
        }
    } catch (Exception ex) { ex.printStackTrace(); }

    if (cnt > 0) {
        double cgpa = totalGPA / cnt;
        JLabel lCGPA = new JLabel(String.format("Final CGPA : %.2f / 4.0", cgpa));
        lCGPA.setBounds(30, y + 20, 500, 30); 
        lCGPA.setFont(new Font("Tahoma", Font.BOLD, 18));
        lCGPA.setForeground(new Color(0, 102, 0)); 
        f.add(lCGPA); 
        y += 60;
    } else {
        JLabel noData = new JLabel("No result data found for this session."); 
        noData.setBounds(30, y, 400, 26);
        noData.setFont(new Font("Tahoma", Font.ITALIC, 13)); 
        f.add(noData); 
        y += 40;
    }

    JButton close = new JButton("Close"); 
    close.setBounds(235, y + 10, 130, 30);
    close.setBackground(Color.black); 
    close.setForeground(Color.white);
    close.addActionListener(e -> f.dispose()); 
    f.add(close);

    f.setSize(600, y + 100); 
    f.setVisible(true);
}
    // ── VIEW FEE STATUS (STUDENT) ─────────────────────────────
    public void viewFeeStatus() {
        JFrame f=new JFrame("Fee Status — "+loggedInId);
        f.setSize(600,500); f.setLocationRelativeTo(null);
        f.setLayout(null); f.getContentPane().setBackground(Color.white);

        JLabel h=new JLabel("FEE STATUS",SwingConstants.CENTER);
        h.setBounds(0,20,600,35); h.setFont(new Font("Tahoma",Font.BOLD,22));
        h.setForeground(new Color(0,51,102)); f.add(h);
        JSeparator sep=new JSeparator(); sep.setBounds(30,58,540,2); f.add(sep);

        int y=65;
        try {
            Conn c=new Conn();
            ResultSet rsS=c.s.executeQuery("SELECT name,program,semester FROM student WHERE rollno='"+loggedInId+"'");
            if(rsS.next()){
                String prog=rsS.getString("program");
                addInfoLine(f,"Student :",rsS.getString("name"),y); y+=30;
                addInfoLine(f,"Program :",prog,y); y+=30;
                addInfoLine(f,"Semester :",rsS.getString("semester"),y); y+=30;

                ResultSet rsFee=c.s.executeQuery("SELECT perSemester FROM fee WHERE program='"+prog+"'");
                double perSem=rsFee.next()?rsFee.getDouble("perSemester"):0;
                addInfoLine(f,"Fee/Semester :","Rs. "+String.format("%.0f",perSem),y); y+=30;

                JSeparator s2=new JSeparator(); s2.setBounds(30,y,540,2); f.add(s2); y+=10;

                JLabel hd1=new JLabel("Semester"); hd1.setBounds(30,y,100,24); hd1.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(hd1);
                JLabel hd2=new JLabel("Amount Due"); hd2.setBounds(150,y,120,24); hd2.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(hd2);
                JLabel hd3=new JLabel("Amount Paid"); hd3.setBounds(290,y,120,24); hd3.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(hd3);
                JLabel hd4=new JLabel("Status"); hd4.setBounds(430,y,100,24); hd4.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(hd4);
                y+=28;
                JSeparator s3=new JSeparator(); s3.setBounds(30,y,540,1); f.add(s3); y+=5;

                ResultSet rsPay=c.s.executeQuery(
                    "SELECT semester,SUM(amount) as paid FROM fee_payment WHERE rollno='"+loggedInId+"' GROUP BY semester");
                java.util.HashMap<String,Double> paidMap=new java.util.HashMap<>();
                while(rsPay.next()) paidMap.put(rsPay.getString("semester"),rsPay.getDouble("paid"));

                String[] sems={"1st Semester","2nd Semester","3rd Semester","4th Semester",
                                "5th Semester","6th Semester","7th Semester","8th Semester"};
                for(String sem:sems){
                    double paid=paidMap.getOrDefault(sem,0.0);
                    String status=paid>=perSem?"✔ PAID":(paid>0?"PARTIAL":"✖ PENDING");
                    Color sc=paid>=perSem?new Color(0,140,0):(paid>0?new Color(180,100,0):Color.RED);
                    JLabel s1=new JLabel(sem); s1.setBounds(30,y,120,24); s1.setFont(new Font("Tahoma",Font.PLAIN,13)); f.add(s1);
                    JLabel s2b=new JLabel("Rs. "+String.format("%.0f",perSem)); s2b.setBounds(150,y,120,24); s2b.setFont(new Font("Tahoma",Font.PLAIN,13)); f.add(s2b);
                    JLabel s3b=new JLabel("Rs. "+String.format("%.0f",paid)); s3b.setBounds(290,y,120,24); s3b.setFont(new Font("Tahoma",Font.PLAIN,13)); f.add(s3b);
                    JLabel s4=new JLabel(status); s4.setBounds(430,y,120,24); s4.setFont(new Font("Tahoma",Font.BOLD,13)); s4.setForeground(sc); f.add(s4);
                    y+=26;
                }
            }
        } catch(Exception ex){ex.printStackTrace();}

        JButton close=new JButton("Close"); close.setBounds(235,y+10,130,30);
        close.setBackground(Color.black); close.setForeground(Color.white);
        close.setFont(new Font("Tahoma",Font.BOLD,12));
        close.addActionListener(e->f.dispose()); f.add(close);
        f.setSize(600,y+70); f.setVisible(true);
    }

    // ── PAY FEE (STUDENT) ─────────────────────────────────────
    public void payFee() {
        JFrame f=new JFrame("Pay Fee — "+loggedInId);
        f.setSize(480,330); f.setLocationRelativeTo(null);
        f.setLayout(null); f.getContentPane().setBackground(Color.white);

        JLabel h=new JLabel("PAY FEE",SwingConstants.CENTER);
        h.setBounds(0,20,480,35); h.setFont(new Font("Tahoma",Font.BOLD,20));
        h.setForeground(new Color(0,51,102)); f.add(h);

        JLabel ls=lbl("Select Semester:"); ls.setBounds(30,80,150,25); f.add(ls);
        String[] sems={"1st Semester","2nd Semester","3rd Semester","4th Semester",
                        "5th Semester","6th Semester","7th Semester","8th Semester"};
        JComboBox<String> cbSem=new JComboBox<>(sems); cbSem.setBounds(195,80,230,28);
        cbSem.setBackground(Color.white); f.add(cbSem);

        JLabel lFee=lbl("Fee Amount:"); lFee.setBounds(30,125,150,25); f.add(lFee);
        JLabel valFee=new JLabel("Rs. 0"); valFee.setBounds(195,125,230,25);
        valFee.setFont(new Font("Tahoma",Font.BOLD,14)); valFee.setForeground(new Color(0,51,102)); f.add(valFee);

        // Auto-load fee amount
        Runnable loadFee=()->{
            try {
                ResultSet rs=new Conn().s.executeQuery("SELECT program FROM student WHERE rollno='"+loggedInId+"'");
                if(rs.next()){
                    ResultSet rf=new Conn().s.executeQuery("SELECT perSemester FROM fee WHERE program='"+rs.getString("program")+"'");
                    if(rf.next()) valFee.setText("Rs. "+String.format("%.0f",rf.getDouble("perSemester")));
                }
            } catch(Exception ex){ex.printStackTrace();}
        };
        loadFee.run();

        JButton sub=subBtn(f,"Pay Now",145,230);
        JButton can=canBtn(f,"Cancel",295,230);
        f.setVisible(true);

        sub.addActionListener(e->{
            String sem=(String)cbSem.getSelectedItem();
            try {
                double amt=Double.parseDouble(valFee.getText().replace("Rs. ","").trim());
                Conn c=new Conn();
                ResultSet rs=c.s.executeQuery("SELECT payId FROM fee_payment WHERE rollno='"+loggedInId+"' AND semester='"+sem+"'");
                if(rs.next()){err("Fee for "+sem+" already paid!"); return;}
                PreparedStatement ps=c.c.prepareStatement(
                    "INSERT INTO fee_payment (rollno,semester,amount,paidOn,status) VALUES(?,?,?,CURDATE(),'PAID')");
                ps.setString(1,loggedInId); ps.setString(2,sem); ps.setDouble(3,amt);
                ps.executeUpdate(); ps.close();
                ok("Fee paid successfully for "+sem+"!"); f.dispose();
            } catch(Exception ex){err(ex.getMessage());}
        });
        can.addActionListener(e->f.dispose());
    }

    // ── APPLY LEAVE (STUDENT) ─────────────────────────────────
    public void applyLeave() {
        JFrame f=new JFrame("Apply Leave — Student");
        f.setSize(460,370); f.setLocationRelativeTo(null);
        f.setLayout(null); f.getContentPane().setBackground(Color.white);

        JLabel h=new JLabel("APPLY LEAVE — STUDENT",SwingConstants.CENTER);
        h.setBounds(0,20,460,35); h.setFont(new Font("Tahoma",Font.BOLD,20));
        h.setForeground(new Color(0,51,102)); f.add(h);

        JLabel lr=lbl("Roll No:"); lr.setBounds(40,75,130,25); f.add(lr);
        JLabel valRoll=new JLabel(loggedInId); valRoll.setBounds(185,75,200,25);
        valRoll.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(valRoll);

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
            try {
                PreparedStatement ps=new Conn().c.prepareStatement(
                    "INSERT INTO studentleave (rollno,date,duration,reason,status) VALUES(?,?,?,?,'PENDING')");
                ps.setString(1,loggedInId); ps.setString(2,date);
                ps.setString(3,cDur.getSelectedItem()); ps.setString(4,tfReas.getText().trim());
                ps.executeUpdate(); ps.close();
                ok("Leave applied successfully."); f.dispose();
            } catch(Exception ex){err(ex.getMessage());}
        });
        can.addActionListener(e->f.dispose());
    }

    // ── VIEW MY LEAVES (STUDENT) ──────────────────────────────
    public void myLeaves() {
        JFrame f=new JFrame("My Leaves — "+loggedInId);
        f.setSize(900,400); f.setLocationRelativeTo(null);
        JTable table=new JTable();
        try {
            ResultSet rs=new Conn().s.executeQuery(
                "SELECT leaveId,date,duration,reason,status FROM studentleave WHERE rollno='"+loggedInId+"' ORDER BY leaveId DESC");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception ex){ex.printStackTrace();}
        styleTable(table);
        f.add(new JScrollPane(table)); f.setVisible(true);
    }

    // ── VIEW ALL STUDENT LEAVES (ADMIN) ───────────────────────
    public void viewStudentLeaves() {
        JFrame f=new JFrame("Student Leave Records");
        f.setSize(950,450); f.setLocationRelativeTo(null);
        JTable table=new JTable();
        try {
            ResultSet rs=new Conn().s.executeQuery(
                "SELECT sl.leaveId, sl.rollno, s.name, sl.date, sl.duration, sl.reason, sl.status " +
                "FROM studentleave sl JOIN student s ON sl.rollno=s.rollno ORDER BY sl.leaveId DESC");
            table.setModel(DbUtils.resultSetToTableModel(rs));
        } catch(Exception ex){ex.printStackTrace();}
        styleTable(table);
        f.add(new JScrollPane(table)); f.setVisible(true);
    }

    // ── RESET STUDENT PASSWORD (ADMIN) ───────────────────────
    public void resetPassword() {
        String rollno=JOptionPane.showInputDialog("Enter Roll No to reset password:");
        if(rollno==null||rollno.trim().isEmpty()) return;
        
        String newPwd=JOptionPane.showInputDialog("Enter new password for "+rollno+":");
        if(newPwd==null||newPwd.trim().isEmpty()) return;
      
        try {
            PreparedStatement ps=new Conn().c.prepareStatement(
                "UPDATE login SET password=? WHERE username=? AND role='STUDENT'");
            ps.setString(1,newPwd); ps.setString(2,rollno);
            int rows=ps.executeUpdate(); ps.close();
            if(rows>0) ok("Password reset successfully.");
            else err("Roll No not found in login system.");
        } 
        catch(Exception ex){err(ex.getMessage());}
    }

    // ── Helpers ───────────────────────────────────────────────
    private void addRow(JFrame f,String txt,int lx,int ly,int lh,java.awt.Component comp,int cx,int cy,int cw,int ch){
        JLabel l=new JLabel(txt); l.setBounds(lx,ly,165,lh); l.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(l);
        comp.setBounds(cx,cy,cw,ch); f.add(comp);
    }
    private void addROLabel(JFrame f,String txt,int lx,int ly,int lw,int lh,JLabel disp,int dx,int dy,int dw,int dh){
        JLabel l=new JLabel(txt); l.setBounds(lx,ly,lw,lh); l.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(l);
        disp.setBounds(dx,dy,dw,dh); disp.setFont(new Font("Tahoma",Font.PLAIN,13)); f.add(disp);
    }
    private void addInfoLine(JFrame f,String label,String value,int y){
        JLabel l1=new JLabel(label); l1.setBounds(30,y,160,24); l1.setFont(new Font("Tahoma",Font.BOLD,13)); f.add(l1);
        JLabel l2=new JLabel(value!=null?value:"-"); l2.setBounds(200,y,380,24); l2.setFont(new Font("Tahoma",Font.PLAIN,13)); f.add(l2);
    }
    private JLabel lbl(String t)
    { JLabel l=new JLabel(t); l.setFont(new Font("Tahoma",Font.BOLD,13)); return l; }
   
    private JTextField tf()
    { JTextField t=new JTextField(); t.setFont(new Font("Tahoma",Font.PLAIN,13)); return t; }
   
    private JButton subBtn(JFrame f,String t,int x,int y){
        JButton b=new JButton(t); b.setBounds(x,y,130,32); b.setBackground(Color.black);
        b.setForeground(Color.white); b.setFont(new Font("Tahoma",Font.BOLD,12)); f.add(b); return b;
    }
    private JButton canBtn(JFrame f,String t,int x,int y){
        JButton b=new JButton(t); b.setBounds(x,y,130,32); b.setBackground(new Color(100,100,100));
        b.setForeground(Color.white); b.setFont(new Font("Tahoma",Font.BOLD,12)); f.add(b); return b;
    }
    private void styleTable(JTable t){
        t.setFont(new Font("Tahoma",Font.PLAIN,13)); t.setRowHeight(22); t.getTableHeader().setFont(new Font("Tahoma",Font.BOLD,13)); }
   
    private String truncate(String s,int n)
    { if(s==null) return ""; return s.length()<=n?s:s.substring(0,n-2)+".."; }
   
    private String grade(double p)
    { if(p>=85) return "A+"; if(p>=80) return "A"; if(p>=75) return "B+"; if(p>=70) return "B"; if(p>=65) return "C+"; if(p>=60) return "C"; if(p>=50) return "D"; return "F"; }
    
    private double toGP(double p)
    { if(p>=90) return 4.0; if(p>=85) return 3.7; if(p>=80) return 3.3; if(p>=75) return 3.0; if(p>=70) return 2.7; if(p>=65) return 2.3; if(p>=60) return 2.0; if(p>=55) return 1.7; if(p>=50) return 1.0; return 0.0; }
    
    private String gradeLetter(double g)
    { if(g>=3.7) return "A+"; if(g>=3.3) return "A"; if(g>=3.0) return "A-"; if(g>=2.7) return "B+"; if(g>=2.3) return "B"; if(g>=2.0) return "B-"; if(g>=1.0) return "C"; return "F"; }
    
    private void ok(String m)
    {JOptionPane.showMessageDialog(null,m);}
    
    private void err(String m)
    {JOptionPane.showMessageDialog(null,m,"Error",JOptionPane.ERROR_MESSAGE);}
}