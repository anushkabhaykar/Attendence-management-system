package view;

import controller.AttendanceController;
import dao.StudentDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import model.Attendance;
import model.Student;
import util.UIUtils;
import util.ValidationUtil;

/**
 * Professional Attendance Records Management Screen.
 * Allows viewing all regular attendance, date-wise daily attendance navigation,
 * class-wise regular attendance taking, search, and status correction.
 * Location: src/view/AttendanceFrame.java
 */
public class AttendanceFrame extends JFrame {

    private final AttendanceController controller;

    private JTextField txtDate;
    private JLabel lblDateInfo;
    private JComboBox<String> cbDept;
    private JComboBox<String> cbStatus;
    private JTextField txtSearch;

    private DefaultTableModel tableModel;
    private JTable attendanceTable;
    private JLabel lblRecordCount;
    private List<Attendance> currentList;

    public AttendanceFrame() {
        setTitle("Attendance Records Management - CampusFace");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1140, 720);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BG_LIGHT);

        controller = new AttendanceController();

        setLayout(new BorderLayout(15, 15));

        // 1. Top Filter & Date Navigation Bar
        add(createFilterPanel(), BorderLayout.NORTH);

        // 2. Center: Records Table with count badge
        add(createTablePanel(), BorderLayout.CENTER);

        // 3. Bottom: Manual Override and Quick Actions
        add(createBottomActionPanel(), BorderLayout.SOUTH);

        // Load today's records by default
        refreshRecords();
    }

    private JPanel createFilterPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER));

        // Main filter row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterRow.setOpaque(false);

        JLabel lblDate = new JLabel("Date (YYYY-MM-DD):");
        lblDate.setFont(UIUtils.FONT_BOLD);
        txtDate = new JTextField(LocalDate.now().toString(), 9);
        txtDate.setPreferredSize(new Dimension(105, 32));

        JButton btnPrevDay = UIUtils.createSecondaryButton("◀");
        btnPrevDay.setPreferredSize(new Dimension(38, 32));
        btnPrevDay.setToolTipText("Previous Day");
        btnPrevDay.addActionListener(e -> stepDate(-1));

        JButton btnToday = UIUtils.createSecondaryButton("Today");
        btnToday.setPreferredSize(new Dimension(70, 32));
        btnToday.addActionListener(e -> {
            txtDate.setText(LocalDate.now().toString());
            updateDateInfoLabel();
            refreshRecords();
        });

        JButton btnNextDay = UIUtils.createSecondaryButton("▶");
        btnNextDay.setPreferredSize(new Dimension(38, 32));
        btnNextDay.setToolTipText("Next Day");
        btnNextDay.addActionListener(e -> stepDate(1));

        JButton btnAllDates = UIUtils.createPrimaryButton("📅 All Dates (Regular)");
        btnAllDates.setPreferredSize(new Dimension(165, 32));
        btnAllDates.setToolTipText("Show complete regular attendance history across all dates");
        btnAllDates.addActionListener(e -> {
            txtDate.setText("");
            updateDateInfoLabel();
            refreshRecords();
        });

        JLabel lblDept = new JLabel("Dept:");
        lblDept.setFont(UIUtils.FONT_BOLD);
        String[] depts = {"All Departments", "Computer Science", "Information Tech", "Electronics", "Mechanical", "Civil"};
        cbDept = new JComboBox<>(depts);
        cbDept.setPreferredSize(new Dimension(145, 32));

        JLabel lblStat = new JLabel("Status:");
        lblStat.setFont(UIUtils.FONT_BOLD);
        String[] stats = {"All Status", "PRESENT", "ABSENT", "LATE"};
        cbStatus = new JComboBox<>(stats);
        cbStatus.setPreferredSize(new Dimension(105, 32));

        JLabel lblKw = new JLabel("Search:");
        lblKw.setFont(UIUtils.FONT_BOLD);
        txtSearch = new JTextField(10);
        txtSearch.setPreferredSize(new Dimension(110, 32));

        JButton btnFilter = UIUtils.createAccentButton("🔍 Filter");
        btnFilter.setPreferredSize(new Dimension(85, 32));
        btnFilter.addActionListener(e -> {
            updateDateInfoLabel();
            refreshRecords();
        });

        filterRow.add(lblDate);
        filterRow.add(txtDate);
        filterRow.add(btnPrevDay);
        filterRow.add(btnToday);
        filterRow.add(btnNextDay);
        filterRow.add(btnAllDates);
        filterRow.add(lblDept);
        filterRow.add(cbDept);
        filterRow.add(lblStat);
        filterRow.add(cbStatus);
        filterRow.add(lblKw);
        filterRow.add(txtSearch);
        filterRow.add(btnFilter);

        // Sub-row: Day indicator & notice
        JPanel subRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 4));
        subRow.setOpaque(false);
        lblDateInfo = new JLabel();
        lblDateInfo.setFont(UIUtils.FONT_SMALL);
        lblDateInfo.setForeground(UIUtils.PRIMARY);
        updateDateInfoLabel();
        subRow.add(lblDateInfo);

        container.add(filterRow, BorderLayout.CENTER);
        container.add(subRow, BorderLayout.SOUTH);

        return container;
    }

    private void stepDate(int deltaDays) {
        String currentText = txtDate.getText().trim();
        LocalDate ld;
        if (currentText.isEmpty()) {
            ld = LocalDate.now();
        } else {
            Date d = ValidationUtil.parseSqlDate(currentText);
            ld = (d != null) ? d.toLocalDate() : LocalDate.now();
        }
        ld = ld.plusDays(deltaDays);
        txtDate.setText(ld.toString());
        updateDateInfoLabel();
        refreshRecords();
    }

    private void updateDateInfoLabel() {
        String currentText = txtDate.getText().trim();
        if (currentText.isEmpty()) {
            lblDateInfo.setText("● Showing ALL regular attendance records across all dates. Click 'Today' or enter date to filter.");
            lblDateInfo.setForeground(UIUtils.ACCENT);
        } else {
            Date d = ValidationUtil.parseSqlDate(currentText);
            if (d != null) {
                LocalDate ld = d.toLocalDate();
                String dayName = ld.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
                boolean isToday = ld.equals(LocalDate.now());
                lblDateInfo.setText("● Viewing Daily Attendance for: " + dayName + (isToday ? " (TODAY)" : ""));
                lblDateInfo.setForeground(isToday ? UIUtils.SUCCESS : UIUtils.TEXT_MUTED);
            } else {
                lblDateInfo.setText("● Date filter format: YYYY-MM-DD (e.g. " + LocalDate.now() + ")");
                lblDateInfo.setForeground(UIUtils.DANGER);
            }
        }
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        // Header with record count indicator
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel lblTitle = new JLabel("Attendance Records");
        lblTitle.setFont(UIUtils.FONT_HEADER);
        lblTitle.setForeground(UIUtils.TEXT_DARK);

        lblRecordCount = new JLabel("Total Records: 0");
        lblRecordCount.setFont(UIUtils.FONT_BOLD);
        lblRecordCount.setForeground(UIUtils.TEXT_MUTED);

        header.add(lblTitle, BorderLayout.WEST);
        header.add(lblRecordCount, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "Date", "Time", "Roll No", "Student Name", "Department", "Status", "Method", "Session", "Marked By"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        attendanceTable = new JTable(tableModel);
        UIUtils.styleTable(attendanceTable);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Hide ID
        attendanceTable.getColumnModel().getColumn(0).setMinWidth(0);
        attendanceTable.getColumnModel().getColumn(0).setMaxWidth(0);
        attendanceTable.getColumnModel().getColumn(0).setWidth(0);

        // Custom status badge renderer on column 6 (Status)
        attendanceTable.getColumnModel().getColumn(6).setCellRenderer(new UIUtils.StatusBadgeRenderer());

        JScrollPane scroll = new JScrollPane(attendanceTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.BORDER));

        JButton btnTakeClass = UIUtils.createPrimaryButton("📋 Take Class Attendance");
        JButton btnMarkStudent = UIUtils.createSecondaryButton("➕ Mark Single Student");
        JButton btnMarkPresent = UIUtils.createSuccessButton("✔ Set Selected PRESENT");
        JButton btnMarkAbsent = UIUtils.createDangerButton("✖ Set Selected ABSENT");
        JButton btnRefresh = UIUtils.createSecondaryButton("🔄 Refresh");

        btnTakeClass.addActionListener(e -> showTakeClassAttendanceDialog());
        btnMarkStudent.addActionListener(e -> showMarkStudentAttendanceDialog());
        btnMarkPresent.addActionListener(e -> updateSelectedRecordStatus("PRESENT"));
        btnMarkAbsent.addActionListener(e -> updateSelectedRecordStatus("ABSENT"));
        btnRefresh.addActionListener(e -> refreshRecords());

        panel.add(new JLabel("Regular Attendance Actions: "));
        panel.add(btnTakeClass);
        panel.add(btnMarkStudent);
        panel.add(btnMarkPresent);
        panel.add(btnMarkAbsent);
        panel.add(btnRefresh);

        return panel;
    }

    /**
     * Dialog allowing teachers to take regular attendance for an entire class / division at once.
     */
    private void showTakeClassAttendanceDialog() {
        try {
            StudentDAO sDAO = new StudentDAO();
            List<Student> allStudents = sDAO.getAllStudents();
            if (allStudents.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students registered in the database.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JDialog dialog = new JDialog(this, "Take Regular Class Attendance", true);
            dialog.setSize(620, 560);
            dialog.setLocationRelativeTo(this);
            dialog.setLayout(new BorderLayout(10, 10));

            // Top class parameters
            JPanel top = new JPanel(new GridLayout(2, 4, 8, 8));
            top.setBackground(Color.WHITE);
            top.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)));

            String defaultDate = !txtDate.getText().trim().isEmpty() ? txtDate.getText().trim() : LocalDate.now().toString();
            JTextField dlgDate = new JTextField(defaultDate);
            JComboBox<String> dlgSession = new JComboBox<>(new String[]{"Morning Session", "Afternoon Session", "Lecture 1 - CS301", "Lecture 2 - CS302", "Lab Session"});
            JComboBox<String> dlgDept = new JComboBox<>(new String[]{"All Departments", "Computer Science", "Information Tech", "Electronics", "Mechanical", "Civil"});
            JComboBox<String> dlgYear = new JComboBox<>(new String[]{"All Years", "1", "2", "3", "4"});

            top.add(new JLabel("Attendance Date:"));
            top.add(dlgDate);
            top.add(new JLabel("Academic Session:"));
            top.add(dlgSession);
            top.add(new JLabel("Department:"));
            top.add(dlgDept);
            top.add(new JLabel("Year:"));
            top.add(dlgYear);

            dialog.add(top, BorderLayout.NORTH);

            // Table of students with Present checkbox
            String[] cols = {"Select", "Student ID", "Roll No", "Student Name", "Department"};
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                @Override
                public Class<?> getColumnClass(int col) {
                    return (col == 0) ? Boolean.class : super.getColumnClass(col);
                }
                @Override
                public boolean isCellEditable(int r, int c) {
                    return c == 0;
                }
            };

            for (Student s : allStudents) {
                model.addRow(new Object[]{Boolean.TRUE, s.getId(), s.getRollNo(), s.getName(), s.getDepartment()});
            }

            JTable table = new JTable(model);
            UIUtils.styleTable(table);
            table.getColumnModel().getColumn(0).setPreferredWidth(50);
            table.getColumnModel().getColumn(1).setMinWidth(0);
            table.getColumnModel().getColumn(1).setMaxWidth(0);

            dialog.add(new JScrollPane(table), BorderLayout.CENTER);

            // Filter students when dept/year change
            java.awt.event.ActionListener filterStudents = ev -> {
                String d = (String) dlgDept.getSelectedItem();
                String y = (String) dlgYear.getSelectedItem();
                model.setRowCount(0);
                for (Student s : allStudents) {
                    boolean deptOk = "All Departments".equals(d) || d.equalsIgnoreCase(s.getDepartment());
                    boolean yearOk = "All Years".equals(y) || String.valueOf(s.getYear()).equals(y);
                    if (deptOk && yearOk) {
                        model.addRow(new Object[]{Boolean.TRUE, s.getId(), s.getRollNo(), s.getName(), s.getDepartment()});
                    }
                }
            };
            dlgDept.addActionListener(filterStudents);
            dlgYear.addActionListener(filterStudents);

            // Bottom action panel
            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            bottom.setBackground(Color.WHITE);
            bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.BORDER));

            JButton btnAllPresent = UIUtils.createSecondaryButton("Select All (Present)");
            btnAllPresent.addActionListener(ev -> {
                for (int i = 0; i < model.getRowCount(); i++) model.setValueAt(Boolean.TRUE, i, 0);
            });

            JButton btnAllAbsent = UIUtils.createSecondaryButton("Deselect All (Absent)");
            btnAllAbsent.addActionListener(ev -> {
                for (int i = 0; i < model.getRowCount(); i++) model.setValueAt(Boolean.FALSE, i, 0);
            });

            JButton btnSaveClass = UIUtils.createSuccessButton("💾 Record Class Attendance");
            btnSaveClass.addActionListener(ev -> {
                Date attDate = ValidationUtil.parseSqlDate(dlgDate.getText());
                if (attDate == null) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid Date (YYYY-MM-DD).", "Invalid Date", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String session = (String) dlgSession.getSelectedItem();
                int recorded = 0;
                for (int i = 0; i < model.getRowCount(); i++) {
                    boolean isPresent = Boolean.TRUE.equals(model.getValueAt(i, 0));
                    int sId = (Integer) model.getValueAt(i, 1);
                    String status = isPresent ? "PRESENT" : "ABSENT";
                    try {
                        controller.markManualAttendance(this, sId, attDate, status, session);
                        recorded++;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                JOptionPane.showMessageDialog(dialog, "Successfully recorded attendance for " + recorded + " students.", "Attendance Saved", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                refreshRecords();
            });

            bottom.add(btnAllPresent);
            bottom.add(btnAllAbsent);
            bottom.add(btnSaveClass);
            dialog.add(bottom, BorderLayout.SOUTH);

            dialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMarkStudentAttendanceDialog() {
        try {
            StudentDAO studentDAO = new StudentDAO();
            List<Student> students = studentDAO.getAllStudents();
            if (students.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No students registered in the database.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
            JComboBox<String> cbStudents = new JComboBox<>();
            for (Student s : students) {
                cbStudents.addItem(s.getRollNo() + " - " + s.getName() + " (" + s.getDepartment() + ")");
            }

            String defaultDate = !txtDate.getText().trim().isEmpty() ? txtDate.getText().trim() : LocalDate.now().toString();
            JTextField dlgDate = new JTextField(defaultDate);

            String[] statuses = {"PRESENT", "ABSENT", "LATE"};
            JComboBox<String> cbStat = new JComboBox<>(statuses);

            String[] sessions = {"Morning Session", "Afternoon Session", "Lecture 1 - CS301", "Lecture 2 - CS302", "Lab Session"};
            JComboBox<String> cbSess = new JComboBox<>(sessions);

            panel.add(new JLabel("Select Student:"));
            panel.add(cbStudents);
            panel.add(new JLabel("Attendance Date (YYYY-MM-DD):"));
            panel.add(dlgDate);
            panel.add(new JLabel("Attendance Status:"));
            panel.add(cbStat);
            panel.add(new JLabel("Academic Session:"));
            panel.add(cbSess);

            int result = JOptionPane.showConfirmDialog(this, panel, "Mark Student Attendance", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                int selIdx = cbStudents.getSelectedIndex();
                if (selIdx >= 0 && selIdx < students.size()) {
                    Student targetStudent = students.get(selIdx);
                    Date customDate = ValidationUtil.parseSqlDate(dlgDate.getText());
                    if (customDate == null) {
                        JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.", "Date Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String selStatus = (String) cbStat.getSelectedItem();
                    String selSession = (String) cbSess.getSelectedItem();
                    controller.markManualAttendance(this, targetStudent.getId(), customDate, selStatus, selSession);
                    refreshRecords();
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSelectedRecordStatus(String newStatus) {
        int row = attendanceTable.getSelectedRow();
        if (row < 0 || currentList == null || row >= currentList.size()) {
            JOptionPane.showMessageDialog(this, "Please select an attendance record from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Attendance att = currentList.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Change status for " + att.getStudentName() + " (" + att.getStudentRollNo() + ") to " + newStatus + "?",
                "Confirm Status Change", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            controller.markManualAttendance(this, att.getStudentId(), att.getAttendanceDate(), newStatus, att.getSessionId());
            refreshRecords();
        }
    }

    public void populateTable(List<Attendance> list) {
        this.currentList = list;
        tableModel.setRowCount(0);
        int count = (list != null) ? list.size() : 0;
        lblRecordCount.setText("Total Records: " + count);

        if (list != null) {
            for (Attendance a : list) {
                tableModel.addRow(new Object[]{
                        a.getId(),
                        a.getAttendanceDate() != null ? a.getAttendanceDate().toString() : "-",
                        a.getAttendanceTime() != null ? a.getAttendanceTime().toString() : "-",
                        a.getStudentRollNo(),
                        a.getStudentName(),
                        a.getDepartment(),
                        a.getStatus(),
                        a.getMethod(),
                        a.getSessionId(),
                        a.getMarkedBy()
                });
            }
        }
    }

    public void refreshRecords() {
        Date d = ValidationUtil.parseSqlDate(txtDate.getText());
        String dept = (String) cbDept.getSelectedItem();
        String stat = (String) cbStatus.getSelectedItem();
        String kw = txtSearch.getText();
        controller.loadAttendanceRecords(this, d, dept, stat, kw);
    }
}
