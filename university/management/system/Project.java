package university.management.system;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Project — main window. Accepts role + loggedInId from Login.
 * Background image resizes on full screen via ComponentListener.
 * Role-based menus: ADMIN full, FACULTY own courses/students, STUDENT own data.
 */
public class Project extends JFrame implements ActionListener {

    private final String role;
    private final String loggedInId;

    // Feature class instances — all receive loggedInId for session filtering
    private Department     dept        = new Department();
    private Course         course      = new Course();
    private Teacher        teacher;
    private Student        student;
    private AssignmentMgr  assignment;
    private AttendanceMgr  attendance;
    private Examination examination;
    private SemesterMgr    semMgr      = new SemesterMgr();
    private About          about       = new About();
    private CGPACalculator cgpa;
    private Fee            fee         = new Fee();
    private FeeStatus      feeStatus;
    private Reports reports = new Reports();

    // Background image
    private Image  rawBgImage = null;
    private JLabel bgLabel    = null;

    public Project(String role, String loggedInId) {
        this.role        = role;
        this.loggedInId  = loggedInId;

        // Pass session to classes that need it
        this.teacher    = new Teacher(loggedInId);
        this.student    = new Student(loggedInId);
        this.assignment = new AssignmentMgr(loggedInId);
        this.attendance = new AttendanceMgr(loggedInId);
        this.examination = new Examination(loggedInId);
        this.cgpa       = new CGPACalculator();
        this.feeStatus  = new FeeStatus();

        setTitle("University Management System  [" + role + " — " + loggedInId + "]");
        setSize(1280, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // Background image — resizes with window
        try {
            ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("icons/third.jpg"));
            if (icon.getIconWidth() > 0) {
                rawBgImage = icon.getImage();
                Image scaled = rawBgImage.getScaledInstance(1280, 720, Image.SCALE_DEFAULT);
                bgLabel = new JLabel(new ImageIcon(scaled));
                bgLabel.setBounds(0, 0, 1280, 720);
                getLayeredPane().add(bgLabel, Integer.valueOf(Integer.MIN_VALUE));
                ((JPanel) getContentPane()).setOpaque(false);
            }
        } catch (Exception ignored) {}

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (rawBgImage != null && bgLabel != null) {
                    int w = getWidth(), h = getHeight();
                    bgLabel.setIcon(new ImageIcon(rawBgImage.getScaledInstance(w, h, Image.SCALE_DEFAULT)));
                    bgLabel.setBounds(0, 0, w, h);
                }
            }
        });

      JMenuBar mb = new JMenuBar();
      mb.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));
        if      (role.equals("ADMIN"))   buildAdminMenu(mb);
        else if (role.equals("FACULTY")) buildFacultyMenu(mb);
        else                             buildStudentMenu(mb);
        setJMenuBar(mb);
        setVisible(true);
    }

    // ── ADMIN menu ────────────────────────────────────────────
    private void buildAdminMenu(JMenuBar mb) {

    // =========================
    // USER MANAGEMENT
    // =========================
    JMenu users = menu("User Management");

    addItem(users, "New Faculty");
    addItem(users, "New Student");

    addItem(users, "View Faculty Details");
    addItem(users, "View Student Details");

    addItem(users, "Update Faculty");
    addItem(users, "Update Student");

    addItem(users, "Delete Faculty");
    addItem(users, "Delete Student");

    mb.add(users);


    // =========================
    // ACADEMICS
    // =========================
    JMenu academics = menu("Academics");

    JMenu dept = menu("Department");
    addItem(dept, "Add Department");
    addItem(dept, "View Departments");
    addItem(dept, "Update Department");
    addItem(dept, "Delete Department");
    academics.add(dept);

    JMenu course = menu("Course Management");
    addItem(course, "Add Course");
    addItem(course, "View Courses");
    addItem(course, "Assign Teacher to Course");
    addItem(course, "View Teacher-Course Assignments");
    addItem(course, "Enroll Student in Course");
    addItem(course, "View Enrolled Students");
    academics.add(course);

    mb.add(academics);


    // =========================
    // ACADEMIC MONITORING
    // =========================
    JMenu monitor = menu("Monitoring");

    JMenu att = menu("Attendance");
    addItem(att, "View All Attendance");
    monitor.add(att);

    JMenu assign = menu("Assignments");
    addItem(assign, "View All Assignments");
    addItem(assign, "View All Submissions");
    monitor.add(assign);

    JMenu marks = menu("Results");
    addItem(marks, "View Student Results");
    monitor.add(marks);

    mb.add(monitor);


    // =========================
    // LEAVE MANAGEMENT
    // =========================
    JMenu leave = menu("Leave Management");

    addItem(leave, "View Student Leaves");
    addItem(leave, "View Faculty Leaves");
    addItem(leave, "Approve/Reject Leave");

    mb.add(leave);
    
    // =========================
    // REPORTS
    // =========================
    
    JMenu reportsMenu = menu("Reports");

    addItem(reportsMenu, "Student Enrollment Report");
    addItem(reportsMenu, "Faculty Workload Report");
    addItem(reportsMenu, "Course Statistics");
    addItem(reportsMenu, "Low Attendance Warning");

    mb.add(reportsMenu);
    
    // =========================
    // Semester MANAGEMENT
    // =========================
    JMenu semester = menu("Semester Management");
    addItem(semester, "Process Semester Promotion");
    addItem(semester, "View Failed Students");
    addItem(semester, "View Held Students");
    mb.add(semester);

    // =========================
    // FINANCE
    // =========================
    JMenu finance = menu("Finance");

    JMenu fee = menu("Fee System");
    addItem(fee, "Fee Structure");
    addItem(fee, "View Fee Status");
    finance.add(fee);

    mb.add(finance);


    // =========================
    // SECURITY
    // =========================
    JMenu security = menu("Security");

    addItem(security, "Reset Student Password");
    addItem(security, "Reset Faculty Password");

    mb.add(security);


    // =========================
    // TOOLS
    // =========================
    JMenu tools = menu("Tools");

    addItem(tools, "Note Pad");
    addItem(tools, "Calculator");
    addItem(tools, "About");

    mb.add(tools);


    // =========================
    // EXIT
    // =========================
    JMenu exit = menu("Exit");
    addItem(exit, "Exit");

    mb.add(exit);
}

    // ── FACULTY menu ──────────────────────────────────────────
   private void buildFacultyMenu(JMenuBar mb) {

    // =========================
    // TEACHER COURSES
    // =========================
    JMenu mCourse = menu("My Courses");
    addItem(mCourse, "View My Courses");
    addItem(mCourse, "View My Students");
    mb.add(mCourse);
    
    // =========================
    // ASSIGNMENT
    // =========================
    JMenu mAssign = menu("Assignments");
    addItem(mAssign, "Post Assignment");
    addItem(mAssign, "View My Assignments");
    addItem(mAssign, "View Submissions");
    addItem(mAssign, "Update Assignment");
    addItem(mAssign, "Delete Assignment");
    mb.add(mAssign);
    
    // =========================
    // ATTENDANCE
    // =========================
    JMenu mAtt = menu("Attendance");
    addItem(mAtt, "Mark Attendance");
    addItem(mAtt, "View Attendance Summary");
    addItem(mAtt, "Update Attendance");
    mb.add(mAtt);
    // =========================
    //MARKS
    // =========================
    JMenu mMarks = menu("Marks");
    addItem(mMarks, "Enter Marks");
    addItem(mMarks, "View Student Results");
    mb.add(mMarks);
    // =========================
    // LEAVE
    // =========================
    JMenu mLeave = menu("Leave");
    addItem(mLeave, "Apply Leave");
    addItem(mLeave, "My Leaves");
    mb.add(mLeave);
    // =========================
    // EXIT
    // =========================
    JMenu exit = menu("Exit");
    addItem(exit, "Exit");
    mb.add(exit);
}

    // ── STUDENT menu ──────────────────────────────────────────
    private void buildStudentMenu(JMenuBar mb) {

    JMenu mInfo = menu("My Profile");
    addItem(mInfo, "View Profile");
    addItem(mInfo, "View My Courses");
    mb.add(mInfo);

    JMenu mAssign = menu("Assignments");
    addItem(mAssign, "View My Assignments");
    addItem(mAssign, "Submit Assignment");
    mb.add(mAssign);

    JMenu mResults = menu("Results");
    addItem(mResults, "View Result Card");
    addItem(mResults, "View CGPA");
    mb.add(mResults);

    JMenu mAtt = menu("Attendance");
    addItem(mAtt, "View My Attendance");
    mb.add(mAtt);

    JMenu mFee = menu("Fee");
    addItem(mFee, "Pay Fee");
    addItem(mFee, "View Fee Status");
    mb.add(mFee);

    JMenu mLeave = menu("Leave");
    addItem(mLeave, "Apply Leave");
    addItem(mLeave, "My Leaves");
    mb.add(mLeave);

    JMenu exit = menu("Exit");
    addItem(exit, "Exit");
    mb.add(exit);
}
    // ── ACTION DISPATCHER ─────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent ae) {
        switch (ae.getActionCommand()) {

            // ── New Information ───────────────────────────────
            case "New Faculty":                   teacher.addTeacher();                      break;
            case "New Student":                   student.addStudent();                      break;

            // ── Department ───────────────────────────────────
            case "Add Department":                dept.addDepartment();                      break;
            case "View Departments":              dept.viewDepartments();                    break;
            case "Update Department":             dept.updateDepartment();                   break;
            case "Delete Department":             dept.deleteDepartment();                   break;

            // ── Course ───────────────────────────────────────
            case "Add Course":                    course.addCourse();                        break;
            case "View Courses":                  course.viewCourses();                      break;
            case "Assign Teacher to Course":      course.assignTeacherToCourse();            break;
            case "View Teacher-Course Assignments": course.viewAssignments();                break;
            case "Enroll Student in Course":      course.enrollStudent();                    break;
            case "View Enrolled Students":        course.viewEnrolledStudents();             break;
            // ── View ─────────────────────────────────────────
            case "View Faculty Details":          teacher.viewTeachers();                    break;
            case "View Student Details":          student.viewStudents();                    break;
            case "Search Student by Name":        student.searchByName();                    break;
            case "Search Faculty by Name":        teacher.searchByName();                    break;

            // ── Update ───────────────────────────────────────
            case "Update Faculty":                teacher.updateTeacher();                   break;
            case "Update Student":                student.updateStudent();                   break;

            // ── Delete ───────────────────────────────────────
            case "Delete Student":                student.deleteStudent();                   break;
            case "Delete Faculty":                teacher.deleteTeacher();                   break;

            // ── Leave (Admin) ─────────────────────────────────
            case "View Student Leaves":           student.viewStudentLeaves();               break;
            case "View Faculty Leaves":           teacher.viewTeacherLeaves();               break;
            case "Approve/Reject Leave":          approveLeave();                            break;

            // ── Leave (Faculty) ───────────────────────────────
            case "Apply Leave":
                if (role.equals("FACULTY")) teacher.applyLeave();
                else                        student.applyLeave();
                break;
            case "My Leaves":
                if (role.equals("FACULTY")) teacher.myLeaves();
                else                        student.myLeaves();
                break;

            // ── Attendance ────────────────────────────────────
            case "Mark Attendance":               attendance.markAttendance();               break;
            case "View Attendance Summary":       attendance.viewSummary();                  break;
            case "View All Attendance":           attendance.viewAttendance();               break;
            case "View My Attendance":            attendance.viewMyAttendance();             break;
            case "Update Attendance":             attendance.updateAttendance();             break;
            
            // ── Assignments ───────────────────────────────────
            case "Post Assignment":               assignment.createAssignment();             break;
            case "View My Assignments":
                if (role.equals("STUDENT")) assignment.viewStudentAssignments();
                else                        assignment.viewFacultyAssignments();
                break;
            case "View Submissions":
                if (role.equals("FACULTY")) {
                    assignment.gradeSubmissions();
                } else {
                    assignment.viewAllSubmissions(); 
                }  break;
            case "Submit Assignment":             assignment.submitAssignment();             break;
            case "View All Assignments":          assignment.viewAllAssignments();           break;
            case "View All Submissions":          assignment.viewAllSubmissions();break;
            case "Update Assignment":             assignment.updateAssignment();             break;
            case "Delete Assignment":             assignment.deleteAssignment();             break;
            case "Delete Submission":             assignment.deleteSubmission();             break;
            
            // ── Marks / Examination ───────────────────────────
            case "Enter Marks":                   examination.enterMarks();                  break;
            case "View Results":                  examination.examinationDetails();          break;
            case "View Student Results":          examination.examinationDetails();          break;
            case "View Result Card":              examination.showMarks(loggedInId);         break;
            case "CGPA Calculator":               cgpa.showCGPA();                           break;
            case "View CGPA":                     student.viewCGPA();                        break;

            // ── Semester ─────────────────────────────────────
            case "Process Semester Promotion":    semMgr.processPromotion();                 break;
            case "View Failed Students":          semMgr.viewFailedStudents();               break;
            case "View Held Students":            semMgr.viewHeldStudents();                 break;

            // ── Reports ─────────────────────────────────────
            case "Student Enrollment Report":     reports.generateStudentReport();           break;
            case "Faculty Workload Report":       reports.generateTeacherReport();           break;
            case "Course Statistics":             reports.generateCourseReport();            break;
            case "Low Attendance Warning":        reports.generateAttendanceReport();        break;
          
            // ── Fee ───────────────────────────────────────────
            case "Fee Structure":                 fee.feeStructure();                        break;
            case "Pay Fee":
                if (role.equals("STUDENT")) student.payFee();
                else                        fee.studentFeeForm();
                break;
            case "View Fee Status":
                if (role.equals("STUDENT")) student.viewFeeStatus();
                else                        feeStatus.viewFeeStatus();
                break;

            // ── My data (Student) ─────────────────────────────
            case "View Profile":                  student.viewProfile();                     break;
            case "View My Courses":
                if (role.equals("STUDENT")) student.viewEnrolledCourses();
                else                        teacher.viewMyCourses();
                break;

            // ── Faculty own data ──────────────────────────────
            case "View My Students":              teacher.viewMyStudents();                  break;

            // ── Password resets ───────────────────────────────
            case "Reset Student Password":        student.resetPassword();                   break;
            case "Reset Faculty Password":        teacher.resetPassword();                   break;

            // ── Utilities ─────────────────────────────────────
            case "About":                         about.showAbout();                         break;
            case "Note Pad":
                try { Runtime.getRuntime().exec("notepad.exe"); } 
                catch (Exception e) { e.printStackTrace(); }
                break;
            case "Calculator":
                try { Runtime.getRuntime().exec("calc.exe"); }  
                catch (Exception e) { e.printStackTrace(); }
                break;
            case "Exit":
                int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to exit?",
                    "Confirm Exit", JOptionPane.YES_NO_OPTION);
                if (c == JOptionPane.YES_OPTION) System.exit(0);
                break;
        }
    }

    private void approveLeave() {
        
        String[] opts = {"Student Leave","Faculty Leave"};
        
        String type = (String) JOptionPane.showInputDialog(this, "Which leave type?",
            "Approve/Reject", JOptionPane.QUESTION_MESSAGE, null, opts, opts[0]);
        
        if (type == null) return;
        String table = type.equals("Student Leave") ? "studentleave" : "teacherleave";
        
        String idCol  = type.equals("Student Leave") ? "rollno" : "empId";
        
        String leaveId = JOptionPane.showInputDialog("Enter Leave ID to process:");
        
        if (leaveId == null || leaveId.trim().isEmpty()) return;
        String[] actions = {"APPROVED","REJECTED"};
        String action = (String) JOptionPane.showInputDialog(this, "Action?",
            "Approve/Reject", JOptionPane.QUESTION_MESSAGE, null, actions, actions[0]);
        
        if (action == null) return;
        try {
            Conn cn = new Conn();
            cn.s.executeUpdate("UPDATE " + table + " SET status='" + action + "' WHERE leaveId=" + leaveId);
            JOptionPane.showMessageDialog(this, "Leave " + action + " successfully.");
        } 
        catch (Exception e) { e.printStackTrace(); }
    }

   private JMenu menu(String label) {

    JMenu m = new JMenu(label);

    m.setForeground(Color.black);

    m.setFont(new Font("SansSerif", Font.BOLD, 12));

    return m;
}
    private void addItem(JMenu menu, String label) {
        JMenuItem item = new JMenuItem(label);
        item.setBackground(Color.white);
        item.setFont(new Font("Tahoma", Font.PLAIN, 13));
        item.addActionListener(this);
        menu.add(item);
    }

    public static void main(String[] args) { new Login(); }
}