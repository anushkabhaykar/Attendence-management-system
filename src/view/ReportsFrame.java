package view;

import controller.AttendanceController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import model.AttendanceReport;
import util.UIUtils;
import util.ValidationUtil;

/**
 * Analytical Attendance Reports and CSV Export Screen.
 * Provides date-range filtering, department aggregation, and student percentage summaries.
 * Location: src/view/ReportsFrame.java
 */
public class ReportsFrame extends JFrame {

    private final AttendanceController controller;

    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JComboBox<String> cbDept;
    private JComboBox<String> cbYear;

    private JLabel lblTotalStudents;
    private JLabel lblAvgPercentage;
    private JLabel lblEligibleCount;
    private JLabel lblDefaulterCount;

    private DefaultTableModel tableModel;
    private JTable reportTable;
    private List<AttendanceReport> currentReports;

    public ReportsFrame() {
        setTitle("Attendance Reports & Analytics - CampusFace");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BG_LIGHT);

        controller = new AttendanceController();

        setLayout(new BorderLayout(15, 15));

        // 1. Top Filter Controls
        add(createTopFilterPanel(), BorderLayout.NORTH);

        // 2. Center: Report Data Table
        add(createTablePanel(), BorderLayout.CENTER);

        // 3. Bottom: Summary Cards & Export Actions
        add(createBottomPanel(), BorderLayout.SOUTH);

        // Initial report query (Last 30 days)
        generateReport();
    }

    private JPanel createTopFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER));

        LocalDate now = LocalDate.now();
        LocalDate lastMonth = now.minusDays(30);

        JLabel lblFrom = new JLabel("From:");
        lblFrom.setFont(UIUtils.FONT_BOLD);
        txtStartDate = new JTextField(lastMonth.toString(), 9);

        JLabel lblTo = new JLabel("To:");
        lblTo.setFont(UIUtils.FONT_BOLD);
        txtEndDate = new JTextField(now.toString(), 9);

        JLabel lblDept = new JLabel("Department:");
        lblDept.setFont(UIUtils.FONT_BOLD);
        String[] depts = {"All Departments", "Computer Science", "Information Tech", "Electronics", "Mechanical", "Civil"};
        cbDept = new JComboBox<>(depts);

        JLabel lblYr = new JLabel("Year:");
        lblYr.setFont(UIUtils.FONT_BOLD);
        String[] years = {"All Years", "1", "2", "3", "4"};
        cbYear = new JComboBox<>(years);

        JButton btnGenerate = UIUtils.createAccentButton("📊 Generate Report");
        btnGenerate.addActionListener(e -> generateReport());

        JButton btnLast7Days = UIUtils.createSecondaryButton("Last 7 Days");
        btnLast7Days.addActionListener(e -> {
            txtStartDate.setText(LocalDate.now().minusDays(7).toString());
            txtEndDate.setText(LocalDate.now().toString());
            generateReport();
        });

        JButton btnThisMonth = UIUtils.createSecondaryButton("This Month");
        btnThisMonth.addActionListener(e -> {
            txtStartDate.setText(LocalDate.now().withDayOfMonth(1).toString());
            txtEndDate.setText(LocalDate.now().toString());
            generateReport();
        });

        panel.add(lblFrom);
        panel.add(txtStartDate);
        panel.add(lblTo);
        panel.add(txtEndDate);
        panel.add(lblDept);
        panel.add(cbDept);
        panel.add(lblYr);
        panel.add(cbYear);
        panel.add(btnGenerate);
        panel.add(btnLast7Days);
        panel.add(btnThisMonth);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        String[] cols = {"Roll No", "Student Name", "Department", "Year", "Div", "Total Sessions", "Present", "Absent", "Attendance %"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        reportTable = new JTable(tableModel);
        UIUtils.styleTable(reportTable);

        // Highlight percentage column: Green if >= 75%, Red if < 75%
        reportTable.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setFont(UIUtils.FONT_BOLD);
                String valStr = value != null ? value.toString().replace("%", "") : "0";
                try {
                    double pct = Double.parseDouble(valStr);
                    if (pct >= 75.0) {
                        l.setForeground(UIUtils.SUCCESS);
                        l.setText(String.format("%.1f%%  ✔", pct));
                    } else {
                        l.setForeground(UIUtils.DANGER);
                        l.setText(String.format("%.1f%%  ⚠ Short", pct));
                    }
                } catch (Exception e) {
                    l.setForeground(UIUtils.TEXT_DARK);
                }
                return l;
            }
        });

        JScrollPane scroll = new JScrollPane(reportTable);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.BORDER));
        panel.setPreferredSize(new Dimension(0, 75));

        // Left Summary Indicators
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 18));
        summaryPanel.setOpaque(false);

        lblTotalStudents = new JLabel("Students: 0");
        lblTotalStudents.setFont(UIUtils.FONT_BOLD);

        lblAvgPercentage = new JLabel("Avg Turnout: 0%");
        lblAvgPercentage.setFont(UIUtils.FONT_BOLD);
        lblAvgPercentage.setForeground(UIUtils.ACCENT);

        lblEligibleCount = new JLabel("≥ 75% (Eligible): 0");
        lblEligibleCount.setFont(UIUtils.FONT_BOLD);
        lblEligibleCount.setForeground(UIUtils.SUCCESS);

        lblDefaulterCount = new JLabel("< 75% (Shortage): 0");
        lblDefaulterCount.setFont(UIUtils.FONT_BOLD);
        lblDefaulterCount.setForeground(UIUtils.DANGER);

        summaryPanel.add(lblTotalStudents);
        summaryPanel.add(lblAvgPercentage);
        summaryPanel.add(lblEligibleCount);
        summaryPanel.add(lblDefaulterCount);

        panel.add(summaryPanel, BorderLayout.WEST);

        // Right CSV Export
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 16));
        exportPanel.setOpaque(false);

        JButton btnExportCSV = UIUtils.createPrimaryButton("📥 Export to CSV");
        btnExportCSV.addActionListener(e -> controller.exportReportToCSV(this, currentReports));

        exportPanel.add(btnExportCSV);
        panel.add(exportPanel, BorderLayout.EAST);

        return panel;
    }

    public void populateReportTable(List<AttendanceReport> list) {
        this.currentReports = list;
        tableModel.setRowCount(0);

        int totalStuds = list != null ? list.size() : 0;
        double sumPct = 0;
        int eligible = 0;
        int defaulter = 0;

        if (list != null) {
            for (AttendanceReport r : list) {
                tableModel.addRow(new Object[]{
                        r.getRollNo(),
                        r.getStudentName(),
                        r.getDepartment(),
                        r.getYear(),
                        r.getDivision(),
                        r.getTotalClasses(),
                        r.getPresentCount(),
                        r.getAbsentCount(),
                        r.getPercentage()
                });

                sumPct += r.getPercentage();
                if (r.getPercentage() >= 75.0) {
                    eligible++;
                } else {
                    defaulter++;
                }
            }
        }

        double avg = totalStuds > 0 ? (sumPct / totalStuds) : 0.0;

        lblTotalStudents.setText("Evaluated Students: " + totalStuds);
        lblAvgPercentage.setText(String.format("Average Turnout: %.1f%%", avg));
        lblEligibleCount.setText("≥ 75% (Exam Eligible): " + eligible);
        lblDefaulterCount.setText("< 75% (Short Attendance): " + defaulter);
    }

    private void generateReport() {
        Date start = ValidationUtil.parseSqlDate(txtStartDate.getText());
        Date end = ValidationUtil.parseSqlDate(txtEndDate.getText());
        String dept = (String) cbDept.getSelectedItem();
        String yrStr = (String) cbYear.getSelectedItem();
        Integer yr = null;
        if (!"All Years".equalsIgnoreCase(yrStr)) {
            try {
                yr = Integer.parseInt(yrStr);
            } catch (NumberFormatException ignored) {}
        }

        controller.loadReportData(this, start, end, dept, yr);
    }
}
