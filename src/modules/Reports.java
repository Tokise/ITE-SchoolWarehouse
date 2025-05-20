/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package modules;

import Package1.DBConnection; // Assuming DBConnection is in Package1

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Vector;
import java.math.BigDecimal; // Import BigDecimal
import javax.swing.ListSelectionModel; // Import ListSelectionModel
import javax.swing.SwingConstants; // Import SwingConstants


// Imports for JFreeChart
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;

// Imports for standard Java Printing
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.geom.Rectangle2D; // Requires AWT


/**
 * JPanel for generating various reports.
 */
public class Reports extends javax.swing.JPanel {

    private Connection conn = null;

    private JComboBox<String> reportTypeComboBox;
    private JComboBox<String> reportFormatComboBox;
    private JButton generateReportButton;
    private JPanel reportContentPanel; // Panel to display report content (table and chart)
    private JScrollPane reportScrollPane; // Scroll pane for the report content


    /**
     * Creates new form Reports
     */
    public Reports() {
        initComponents();
        connectToDatabase(); // Establish database connection
        setupReportsPanel(); // Setup the UI components
    }

    /**
     * Establishes a connection to the database.
     * @return true if connection is successful, false otherwise.
     */
    private boolean connectToDatabase() {
        try {
            conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connected successfully in Reports");
                return true;
            } else {
                System.err.println("Failed to establish database connection in Reports.");
                return false;
            }
        } catch (SQLException ex) {
            System.err.println("Database connection error in Reports: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Sets up the UI components for the Reports panel.
     */
    private void setupReportsPanel() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new Color(30, 30, 30));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.setOpaque(false);

        JLabel typeLabel = new JLabel("Report Type:");
        typeLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        typeLabel.setForeground(Color.WHITE);
        controlPanel.add(typeLabel);

        reportTypeComboBox = new JComboBox<>(new String[]{
            "Inventory Report",
            "Transaction History",
            "Purchase Order Summary",
            "User List",
            "Items Below Reorder Level"
            // Add other report types as needed
        });
        reportTypeComboBox.setFont(new Font("Verdana", Font.PLAIN, 14));
        controlPanel.add(reportTypeComboBox);

        JLabel formatLabel = new JLabel("Format:");
        formatLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        formatLabel.setForeground(Color.WHITE);
        controlPanel.add(formatLabel);

        // Removed Excel format as we are not using JasperReports for export
        reportFormatComboBox = new JComboBox<>(new String[]{
            "Print (to Printer/Preview)", // Renamed for clarity
            // "Excel"
        });
        reportFormatComboBox.setFont(new Font("Verdana", Font.PLAIN, 14));
        controlPanel.add(reportFormatComboBox);

        generateReportButton = new JButton("Generate Report");
        styleButton(generateReportButton, new Color(52, 152, 219));
        generateReportButton.addActionListener(e -> generateReport());
        controlPanel.add(generateReportButton);

        this.add(controlPanel, BorderLayout.NORTH);

        // Panel to hold the report content (table and chart)
        reportContentPanel = new JPanel(new BorderLayout());
        reportContentPanel.setOpaque(false);
        reportScrollPane = new JScrollPane(reportContentPanel);
        reportScrollPane.getViewport().setBackground(new Color(40, 40, 40)); // Match theme
        reportScrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60))); // Add border
        this.add(reportScrollPane, BorderLayout.CENTER);

        // Initial empty state for the report content panel
        reportContentPanel.add(createMessagePanel("Select a report type and click 'Generate Report'."));

    }

     /**
     * Styles a JButton with custom background, foreground, and font.
     * @param button The button to style.
     * @param bgColor The background color.
     */
    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Verdana", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }

    /**
     * Creates a simple panel with a message label.
     * @param message The message to display.
     * @return A JPanel containing the message.
     */
    private JPanel createMessagePanel(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Verdana", Font.PLAIN, 16));
        panel.add(messageLabel, BorderLayout.CENTER);
        return panel;
    }


    /**
     * Triggers the report generation process based on user selection.
     */
    private void generateReport() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String reportType = (String) reportTypeComboBox.getSelectedItem();
        String reportFormat = (String) reportFormatComboBox.getSelectedItem(); // Now "Print (to Printer/Preview)"

        if (reportType == null || reportFormat == null) {
            JOptionPane.showMessageDialog(this, "Please select report type and format.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("Generating report: " + reportType + " in format: " + reportFormat);

        // Clear previous report content
        reportContentPanel.removeAll();

        // Fetch data and generate content based on report type
        JPanel reportPanel = null; // Panel to hold the report data and chart

        switch (reportType) {
            case "Inventory Report":
                reportPanel = generateInventoryReport();
                break;
            case "Transaction History":
                reportPanel = generateTransactionHistoryReport();
                break;
            case "Purchase Order Summary":
                reportPanel = generatePurchaseOrderSummaryReport();
                break;
            case "User List":
                reportPanel = generateUserListReport();
                break;
            case "Items Below Reorder Level":
                reportPanel = generateLowStockItemsReport();
                break;
            default:
                reportPanel = createMessagePanel("Unknown report type selected.");
                break;
        }

        if (reportPanel != null) {
            reportContentPanel.add(reportPanel, BorderLayout.CENTER);
            reportContentPanel.revalidate();
            reportContentPanel.repaint();

            // If format is "Print (to Printer/Preview)", attempt to print the reportContentPanel
            if ("Print (to Printer/Preview)".equals(reportFormat)) {
                printPanelContent(reportContentPanel, reportType); // Call the updated print method
            }
        }
    }

    /**
     * Generates the Inventory Report panel with data table and a category distribution chart.
     * @return A JPanel containing the inventory report.
     */
    private JPanel generateInventoryReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // --- Data Table ---
        String[] columns = {"Item ID", "Item Name", "Category", "Quantity", "Reorder Level", "Unit Price", "Is Machinery"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3 || columnIndex == 4) return Integer.class; // Quantity, Reorder Level
                if (columnIndex == 5) return BigDecimal.class; // Unit Price
                if (columnIndex == 6) return Boolean.class; // Is Machinery
                return String.class;
            }
        };
        JTable table = new JTable(model);
        styleReportTable(table); // Apply styling
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.getViewport().setBackground(new Color(40, 40, 40));

        String sql = "SELECT i.ItemID, i.ItemName, c.CategoryName, i.Quantity, i.ReorderLevel, i.UnitPrice, i.IsMachinery " +
                     "FROM Items i LEFT JOIN Categories c ON i.CategoryID = c.CategoryID WHERE i.IsArchived = FALSE";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("ItemID"),
                    rs.getString("ItemName"),
                    rs.getString("CategoryName"),
                    rs.getInt("Quantity"),
                    rs.getInt("ReorderLevel"),
                    rs.getBigDecimal("UnitPrice"),
                    rs.getBoolean("IsMachinery")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error fetching Inventory Report data: " + e.getMessage());
            e.printStackTrace();
            model.addRow(new Object[]{"Error", "Error fetching data", "", "", "", "", false});
        }

        panel.add(tableScrollPane, BorderLayout.CENTER);

        // --- Chart (Inventory by Category Pie Chart) ---
        DefaultPieDataset categoryDataset = new DefaultPieDataset();
        String chartSql = "SELECT c.CategoryName, COUNT(i.ItemID) AS ItemCount FROM Categories c LEFT JOIN Items i ON c.CategoryID = c.CategoryID WHERE i.IsArchived = FALSE OR i.ItemID IS NULL GROUP BY c.CategoryName HAVING COUNT(i.ItemID) > 0";
         try (PreparedStatement pstmt = conn.prepareStatement(chartSql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                categoryDataset.setValue(rs.getString("CategoryName"), rs.getInt("ItemCount"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching Inventory Category Chart data: " + e.getMessage());
            e.printStackTrace();
             categoryDataset.setValue("Error", 1);
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Inventory by Category",
                categoryDataset,
                true, // legend
                true, // tooltips
                false // urls
        );
        customizeChart(pieChart);
        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(400, 300)); // Adjust size as needed
        chartPanel.setOpaque(false);

        panel.add(chartPanel, BorderLayout.SOUTH); // Add chart below the table

        return panel;
    }

     /**
     * Generates the Transaction History Report panel with a data table and a movement chart.
     * @return A JPanel containing the transaction history report.
     */
    private JPanel generateTransactionHistoryReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // --- Data Table ---
        String[] columns = {"Transaction ID", "Date", "Item Name", "Type", "Quantity", "User", "Details"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Integer.class; // Quantity
                return String.class;
            }
        };
        JTable table = new JTable(model);
        styleReportTable(table); // Apply styling
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.getViewport().setBackground(new Color(40, 40, 40));

        String sql = "SELECT t.TransactionID, t.TransactionDate, i.ItemName, t.TransactionType, t.Quantity, u.FullName AS UserName, t.Details " +
                     "FROM Transactions t LEFT JOIN Items i ON t.ItemID = i.ItemID LEFT JOIN Users u ON t.UserID = u.UserID " +
                     "ORDER BY t.TransactionDate DESC"; // Order by date
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("TransactionID"),
                    rs.getTimestamp("TransactionDate"), // Use Timestamp for date and time
                    rs.getString("ItemName"),
                    rs.getString("TransactionType"),
                    rs.getInt("Quantity"),
                    rs.getString("UserName"),
                    rs.getString("Details")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error fetching Transaction History Report data: " + e.getMessage());
            e.printStackTrace();
            model.addRow(new Object[]{"Error", "Error fetching data", "", "", "", "", ""});
        }

        panel.add(tableScrollPane, BorderLayout.CENTER);

        // --- Chart (Inventory Movement Bar Chart) ---
        DefaultCategoryDataset movementDataset = new DefaultCategoryDataset();
        String chartSql = "SELECT " +
                     "    DATE_FORMAT(TransactionDate, '%Y-%m') AS Month, " +
                     "    TransactionType, " +
                     "    SUM(Quantity) AS TotalQuantity " +
                     "FROM Transactions " +
                     "WHERE TransactionDate >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " + // Last 6 months
                     "GROUP BY DATE_FORMAT(TransactionDate, '%Y-%m'), TransactionType " +
                     "ORDER BY Month";
         try (PreparedStatement pstmt = conn.prepareStatement(chartSql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                movementDataset.addValue(rs.getDouble("TotalQuantity"), rs.getString("TransactionType"), rs.getString("Month"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching Transaction History Chart data: " + e.getMessage());
            e.printStackTrace();
             movementDataset.addValue(0, "Received", "Error");
             movementDataset.addValue(0, "Issued", "Error");
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Inventory Movement (Last 6 Months)",
                "Month",
                "Quantity",
                movementDataset,
                PlotOrientation.VERTICAL,
                true, // legend
                true, // tooltips
                false // urls
        );
        customizeChart(barChart);
        CategoryPlot plot = barChart.getCategoryPlot();
        // Customize bar colors if needed
        ((BarRenderer) plot.getRenderer()).setSeriesPaint(0, new Color(46, 204, 113)); // Received (example color)
        ((BarRenderer) plot.getRenderer()).setSeriesPaint(1, new Color(231, 76, 60)); // Issued (example color)

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 300)); // Adjust size as needed
        chartPanel.setOpaque(false);

        panel.add(chartPanel, BorderLayout.SOUTH); // Add chart below the table

        return panel;
    }

     /**
     * Generates the Purchase Order Summary Report panel with a data table and a status distribution chart.
     * @return A JPanel containing the purchase order summary report.
     */
    private JPanel generatePurchaseOrderSummaryReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // --- Data Table ---
        String[] columns = {"PO ID", "PO Number", "PO Date", "Status", "Total Amount", "Created By"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 5) return Integer.class; // PO ID, Created By (assuming UserID)
                if (columnIndex == 4) return BigDecimal.class; // Total Amount
                return String.class;
            }
        };
        JTable table = new JTable(model);
        styleReportTable(table); // Apply styling
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.getViewport().setBackground(new Color(40, 40, 40));

        String sql = "SELECT po.POID, po.PONumber, po.PODate, po.Status, po.TotalAmount, po.CreatedBy, u.FullName AS CreatedByName " +
                     "FROM PurchaseOrders po LEFT JOIN Users u ON po.CreatedBy = u.UserID " +
                     "ORDER BY po.PODate DESC"; // Order by date
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("POID"),
                    rs.getString("PONumber"),
                    rs.getDate("PODate"),
                    rs.getString("Status"),
                    rs.getBigDecimal("TotalAmount"),
                    rs.getString("CreatedByName") // Display user's full name
                });
            }
        } catch (SQLException e) {
            System.err.println("Error fetching Purchase Order Summary Report data: " + e.getMessage());
            e.printStackTrace();
            model.addRow(new Object[]{"Error", "Error fetching data", "", "", BigDecimal.ZERO, ""});
        }

        panel.add(tableScrollPane, BorderLayout.CENTER);

        // --- Chart (Purchase Order Status Distribution Pie Chart) ---
        DefaultPieDataset statusDataset = new DefaultPieDataset();
        String chartSql = "SELECT Status, COUNT(*) AS StatusCount FROM PurchaseOrders GROUP BY Status HAVING COUNT(*) > 0";
         try (PreparedStatement pstmt = conn.prepareStatement(chartSql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                statusDataset.setValue(rs.getString("Status"), rs.getInt("StatusCount"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching Purchase Order Status Chart data: " + e.getMessage());
            e.printStackTrace();
             statusDataset.setValue("Error", 1);
        }

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Purchase Order Status Distribution",
                statusDataset,
                true, // legend
                true, // tooltips
                false // urls
        );
        customizeChart(pieChart);
        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(400, 300)); // Adjust size as needed
        chartPanel.setOpaque(false);

        panel.add(chartPanel, BorderLayout.SOUTH); // Add chart below the table

        return panel;
    }

     /**
     * Generates the User List Report panel with a data table. (No chart typically needed for a simple list)
     * @return A JPanel containing the user list report.
     */
    private JPanel generateUserListReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // --- Data Table ---
        String[] columns = {"User ID", "Username", "Full Name", "Role", "Is Active"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // User ID
                if (columnIndex == 4) return Boolean.class; // Is Active
                return String.class;
            }
        };
        JTable table = new JTable(model);
        styleReportTable(table); // Apply styling
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.getViewport().setBackground(new Color(40, 40, 40));

        String sql = "SELECT UserID, Username, FullName, Role, IsActive FROM Users ORDER BY FullName";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("FullName"),
                    rs.getString("Role"),
                    rs.getBoolean("IsActive")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error fetching User List Report data: " + e.getMessage());
            e.printStackTrace();
            model.addRow(new Object[]{"Error", "Error fetching data", "", "", false});
        }

        panel.add(tableScrollPane, BorderLayout.CENTER);

        // No chart typically needed for a simple user list

        return panel;
    }

     /**
     * Generates the Items Below Reorder Level Report panel with a data table. (No chart typically needed)
     * @return A JPanel containing the low stock items report.
     */
    private JPanel generateLowStockItemsReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        // --- Data Table ---
        String[] columns = {"Item ID", "Item Name", "Category", "Current Quantity", "Reorder Level", "Is Machinery"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 3 || columnIndex == 4) return Integer.class; // Item ID, Quantity, Reorder Level
                if (columnIndex == 5) return Boolean.class; // Is Machinery
                return String.class;
            }
        };
        JTable table = new JTable(model);
        styleReportTable(table); // Apply styling
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.getViewport().setBackground(new Color(40, 40, 40));


        // SQL to select items below reorder level (excluding archived)
        String sql = "SELECT i.ItemID, i.ItemName, c.CategoryName, i.Quantity, i.ReorderLevel, i.IsMachinery " +
                     "FROM Items i LEFT JOIN Categories c ON i.CategoryID = c.CategoryID " +
                     "WHERE i.Quantity <= i.ReorderLevel AND i.IsArchived = FALSE " +
                     "ORDER BY i.ItemName";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("ItemID"),
                    rs.getString("ItemName"),
                    rs.getString("CategoryName"),
                    rs.getInt("Quantity"),
                    rs.getInt("ReorderLevel"),
                    rs.getBoolean("IsMachinery")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error fetching Items Below Reorder Level Report data: " + e.getMessage());
            e.printStackTrace();
            model.addRow(new Object[]{"Error", "Error fetching data", "", "", "", false});
        }

        panel.add(tableScrollPane, BorderLayout.CENTER);

        // No chart typically needed for a simple list

        return panel;
    }


    /**
     * Styles a JTable for reports to match the theme.
     * @param table The table to style.
     */
    private void styleReportTable(JTable table) {
        table.setForeground(Color.WHITE);
        table.setBackground(new Color(40, 40, 40));
        table.setGridColor(new Color(60, 60, 60));
        table.setSelectionBackground(new Color(51, 153, 255));
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("Verdana", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setAutoCreateRowSorter(true); // Enable sorting
        table.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(50, 50, 50));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Single selection by default
    }


     /**
     * Customizes JFreeChart appearance to match the dark theme.
     * @param chart The chart to customize.
     */
    private void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(30, 30, 30));
        chart.getTitle().setPaint(Color.WHITE);
        if (chart.getLegend() != null) {
             chart.getLegend().setBackgroundPaint(new Color(30, 30, 30));
             chart.getLegend().setItemPaint(Color.WHITE);
        }

        // Customize plot
        if (chart.getPlot() != null) {
            chart.getPlot().setBackgroundPaint(new Color(30, 30, 30));
            chart.getPlot().setOutlinePaint(new Color(30, 30, 30));

            // Customize axis colors for CategoryPlot (used in Bar Charts)
            if (chart.getPlot() instanceof CategoryPlot) {
                CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();
                if (categoryPlot.getDomainAxis() != null) {
                    categoryPlot.getDomainAxis().setLabelPaint(Color.WHITE);
                    categoryPlot.getDomainAxis().setTickLabelPaint(Color.WHITE);
                }
                 if (categoryPlot.getRangeAxis() != null) {
                    categoryPlot.getRangeAxis().setLabelPaint(Color.WHITE);
                    categoryPlot.getRangeAxis().setTickLabelPaint(Color.WHITE);
                 }
                 categoryPlot.setRangeGridlinePaint(new Color(60, 60, 60)); // Darker grid lines
            }
             // Customize section colors for PiePlot (used in Pie Charts)
            if (chart.getPlot() instanceof PiePlot) {
                PiePlot piePlot = (PiePlot) chart.getPlot();
                piePlot.setLabelPaint(Color.WHITE); // Label color
                piePlot.setOutlinePaint(null); // Remove outline
                // You might want to set custom section colors here if the default ones clash with the theme
                // Example: piePlot.setSectionPaint("Category1", new Color(100, 149, 237)); // Cornflower Blue
            }
        }
    }


     /**
     * Prints the content of a JPanel using the standard Java printing API.
     * This typically sends the output to the default printer or print preview dialog.
     * Note: This method does NOT generate a PDF file directly.
     * @param panel The JPanel to print.
     * @param reportTitle The title of the report (used in messages).
     */
    private void printPanelContent(JPanel panel, String reportTitle) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setJobName(reportTitle); // Set job name

        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                // Scale the content to fit the page if necessary
                double scaleX = pageFormat.getImageableWidth() / panel.getWidth();
                double scaleY = pageFormat.getImageableHeight() / panel.getHeight();
                double scale = Math.min(scaleX, scaleY);
                g2d.scale(scale, scale);

                // Print the content of the JPanel
                // This will print all components within the panel, including the table and chart
                panel.printAll(g2d);

                return Printable.PAGE_EXISTS;
            }
        });

        boolean doPrint = printerJob.printDialog();
        if (doPrint) {
            try {
                printerJob.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Error during printing: " + e.getMessage(), "Printing Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        } else {
             System.out.println("Print dialog cancelled by user.");
        }
    }



    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
