package view;

import controller.StudentController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.Student;
import util.UIUtils;
import util.ValidationUtil;

/**
 * Student Management Screen for student registration, details modification,
 * deletion, and launching face enrollment.
 * Location: src/view/StudentFrame.java
 */
public class StudentFrame extends JFrame {

    private final StudentController controller;

    private JTextField txtSearch;
    private JComboBox<String> cbFilterDept;
    private JComboBox<String> cbFilterYear;

    private DefaultTableModel tableModel;
    private JTable studentTable;
    private List<Student> currentStudentList;

    // Form Fields
    private int selectedStudentId = -1;
    private JTextField txtRollNo;
    private JTextField txtName;
    private JComboBox<String> cbGender;
    private JTextField txtDob;
    private JComboBox<String> cbDepartment;
    private JTextField txtCourse;
    private JComboBox<Integer> cbYear;
    private JTextField txtDivision;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JLabel lblFaceStatus;

    public StudentFrame() {
        setTitle("Student Management - CampusFace");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BG_LIGHT);

        controller = new StudentController(this);

        setLayout(new BorderLayout(15, 15));

        // 1. Top Search and Filter Bar
        add(createTopFilterPanel(), BorderLayout.NORTH);

        // 2. Center: JTable of Students
        add(createTablePanel(), BorderLayout.CENTER);

        // 3. Right: Add / Edit Student Form Panel
        add(createFormPanel(), BorderLayout.EAST);

        // Load students
        controller.loadStudents();
    }

    private JPanel createTopFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER));

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(UIUtils.FONT_BOLD);
        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(150, 32));

        JLabel lblDept = new JLabel("Department:");
        lblDept.setFont(UIUtils.FONT_BOLD);
        String[] depts = {"All Departments", "Computer Science", "Information Tech", "Electronics", "Mechanical", "Civil"};
        cbFilterDept = new JComboBox<>(depts);
        cbFilterDept.setPreferredSize(new Dimension(160, 32));

        JLabel lblYr = new JLabel("Year:");
        lblYr.setFont(UIUtils.FONT_BOLD);
        String[] years = {"All Years", "1", "2", "3", "4"};
        cbFilterYear = new JComboBox<>(years);
        cbFilterYear.setPreferredSize(new Dimension(100, 32));

        JButton btnSearch = UIUtils.createAccentButton("🔍 Search");
        btnSearch.addActionListener(e -> executeSearch());

        JButton btnReset = UIUtils.createSecondaryButton("Reset");
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbFilterDept.setSelectedIndex(0);
            cbFilterYear.setSelectedIndex(0);
            controller.loadStudents();
        });

        panel.add(lblSearch);
        panel.add(txtSearch);
        panel.add(lblDept);
        panel.add(cbFilterDept);
        panel.add(lblYr);
        panel.add(cbFilterYear);
        panel.add(btnSearch);
        panel.add(btnReset);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 0));

        String[] cols = {"ID", "Roll No", "Student Name", "Department", "Year", "Div", "Face Enrolled"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        UIUtils.styleTable(studentTable);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Hide ID column
        studentTable.getColumnModel().getColumn(0).setMinWidth(0);
        studentTable.getColumnModel().getColumn(0).setMaxWidth(0);
        studentTable.getColumnModel().getColumn(0).setWidth(0);

        studentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = studentTable.getSelectedRow();
                if (row >= 0 && currentStudentList != null && row < currentStudentList.size()) {
                    populateForm(currentStudentList.get(row));
                }
            }
        });

        JScrollPane scroll = new JScrollPane(studentTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFormPanel() {
        JPanel card = UIUtils.createCardPanel();
        card.setPreferredSize(new Dimension(380, 0));
        card.setLayout(new BorderLayout(0, 10));

        JLabel lblFormTitle = new JLabel("Student Information");
        lblFormTitle.setFont(UIUtils.FONT_HEADER);
        lblFormTitle.setForeground(UIUtils.TEXT_DARK);
        card.add(lblFormTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setOpaque(false);

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        int row = 0;

        // Roll No
        txtRollNo = new JTextField();
        addFormField(fieldsPanel, g, "Roll Number *", txtRollNo, row++);

        // Name
        txtName = new JTextField();
        addFormField(fieldsPanel, g, "Full Name *", txtName, row++);

        // Gender
        cbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        addFormField(fieldsPanel, g, "Gender", cbGender, row++);

        // DOB
        txtDob = new JTextField("2004-01-01");
        addFormField(fieldsPanel, g, "Date of Birth (YYYY-MM-DD) *", txtDob, row++);

        // Department
        cbDepartment = new JComboBox<>(new String[]{"Computer Science", "Information Tech", "Electronics", "Mechanical", "Civil"});
        addFormField(fieldsPanel, g, "Department", cbDepartment, row++);

        // Course
        txtCourse = new JTextField("B.Tech");
        addFormField(fieldsPanel, g, "Course", txtCourse, row++);

        // Year
        cbYear = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        cbYear.setSelectedItem(3);
        addFormField(fieldsPanel, g, "Year", cbYear, row++);

        // Division
        txtDivision = new JTextField("A");
        addFormField(fieldsPanel, g, "Division", txtDivision, row++);

        // Email
        txtEmail = new JTextField();
        addFormField(fieldsPanel, g, "Email *", txtEmail, row++);

        // Phone
        txtPhone = new JTextField();
        addFormField(fieldsPanel, g, "Phone *", txtPhone, row++);

        // Face Status Label
        lblFaceStatus = new JLabel("Face Biometrics: Not Enrolled");
        lblFaceStatus.setFont(UIUtils.FONT_BOLD);
        lblFaceStatus.setForeground(UIUtils.DANGER);
        g.gridx = 0;
        g.gridy = row++;
        g.gridwidth = 2;
        fieldsPanel.add(lblFaceStatus, g);

        card.add(fieldsPanel, BorderLayout.CENTER);

        // Buttons Action Panel
        JPanel btnPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        btnPanel.setOpaque(false);

        JButton btnAdd = UIUtils.createPrimaryButton("Add Student");
        JButton btnUpdate = UIUtils.createAccentButton("Update");
        JButton btnDelete = UIUtils.createDangerButton("Delete");
        JButton btnClear = UIUtils.createSecondaryButton("Clear");
        JButton btnCaptureFace = UIUtils.createSuccessButton("📷 Capture Face");

        btnAdd.addActionListener(e -> controller.addStudent(
                txtRollNo.getText(), txtName.getText(),
                (String) cbGender.getSelectedItem(), txtDob.getText(),
                (String) cbDepartment.getSelectedItem(), txtCourse.getText(),
                (Integer) cbYear.getSelectedItem(), txtDivision.getText(),
                txtEmail.getText(), txtPhone.getText()
        ));

        btnUpdate.addActionListener(e -> controller.updateStudent(
                selectedStudentId, txtRollNo.getText(), txtName.getText(),
                (String) cbGender.getSelectedItem(), txtDob.getText(),
                (String) cbDepartment.getSelectedItem(), txtCourse.getText(),
                (Integer) cbYear.getSelectedItem(), txtDivision.getText(),
                txtEmail.getText(), txtPhone.getText()
        ));

        btnDelete.addActionListener(e -> controller.deleteStudent(selectedStudentId));
        btnClear.addActionListener(e -> clearForm());
        btnCaptureFace.addActionListener(e -> controller.openFaceEnrollment(selectedStudentId));

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        btnPanel.add(btnCaptureFace);

        card.add(btnPanel, BorderLayout.SOUTH);

        return card;
    }

    private void addFormField(JPanel p, GridBagConstraints g, String label, javax.swing.JComponent cmp, int row) {
        g.gridwidth = 1;
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_REGULAR);
        p.add(lbl, g);

        g.gridx = 1;
        g.weightx = 0.65;
        cmp.setPreferredSize(new Dimension(180, 28));
        p.add(cmp, g);
    }

    public void populateTable(List<Student> students) {
        this.currentStudentList = students;
        tableModel.setRowCount(0);
        for (Student s : students) {
            String faceStr = s.hasFaceEnrolled() ? "✔ Enrolled" : "✖ Not Enrolled";
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getRollNo(),
                    s.getName(),
                    s.getDepartment(),
                    s.getYear(),
                    s.getDivision(),
                    faceStr
            });
        }
    }

    private void populateForm(Student s) {
        selectedStudentId = s.getId();
        txtRollNo.setText(s.getRollNo());
        txtName.setText(s.getName());
        cbGender.setSelectedItem(s.getGender());
        txtDob.setText(ValidationUtil.formatSqlDate(s.getDob()));
        cbDepartment.setSelectedItem(s.getDepartment());
        txtCourse.setText(s.getCourse());
        cbYear.setSelectedItem(s.getYear());
        txtDivision.setText(s.getDivision());
        txtEmail.setText(s.getEmail());
        txtPhone.setText(s.getPhone());

        if (s.hasFaceEnrolled()) {
            lblFaceStatus.setText("Face Biometrics: ✔ Enrolled (" + s.getFaceReference() + ")");
            lblFaceStatus.setForeground(UIUtils.SUCCESS);
        } else {
            lblFaceStatus.setText("Face Biometrics: ✖ Not Enrolled");
            lblFaceStatus.setForeground(UIUtils.DANGER);
        }
    }

    public void clearForm() {
        selectedStudentId = -1;
        txtRollNo.setText("");
        txtName.setText("");
        cbGender.setSelectedIndex(0);
        txtDob.setText("2004-01-01");
        cbDepartment.setSelectedIndex(0);
        txtCourse.setText("B.Tech");
        cbYear.setSelectedItem(3);
        txtDivision.setText("A");
        txtEmail.setText("");
        txtPhone.setText("");
        lblFaceStatus.setText("Face Biometrics: Not Enrolled");
        lblFaceStatus.setForeground(UIUtils.DANGER);
        studentTable.clearSelection();
    }

    private void executeSearch() {
        String kw = txtSearch.getText();
        String dept = (String) cbFilterDept.getSelectedItem();
        String yrStr = (String) cbFilterYear.getSelectedItem();
        Integer yr = null;
        if (!"All Years".equalsIgnoreCase(yrStr)) {
            try {
                yr = Integer.parseInt(yrStr);
            } catch (NumberFormatException ignored) {}
        }
        controller.searchStudents(kw, dept, yr, null);
    }
}
