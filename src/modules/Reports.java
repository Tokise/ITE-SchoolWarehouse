package modules;

import Package1.DBConnection;

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
import java.math.BigDecimal;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.geom.Rectangle2D;


public class Reports extends javax.swing.JPanel {

    private Connection conn = null;

    private JComboBox<String> reportTypeComboBox;
    private JComboBox<String> reportFormatComboBox;
    private JButton generateReportButton;
    private JPanel reportContentPanel;
    private JScrollPane reportScrollPane;


    public Reports() {
        initComponents();
        connectToDatabase();
        setupReportsPanel();
    }

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
        });
        reportTypeComboBox.setFont(new Font("Verdana", Font.PLAIN, 14));
        controlPanel.add(reportTypeComboBox);

        JLabel formatLabel = new JLabel("Format:");
        formatLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        formatLabel.setForeground(Color.WHITE);
        controlPanel.add(formatLabel);

        reportFormatComboBox = new JComboBox<>(new String[]{
            "Print (to Printer/Preview)",
        });
        reportFormatComboBox.setFont(new Font("Verdana", Font.PLAIN, 14));
        controlPanel.add(reportFormatComboBox);

        generateReportButton = new JButton("Generate Report");
        styleButton(generateReportButton, new Color(52, 152, 219));
        generateReportButton.addActionListener(e -> generateReport());
        controlPanel.add(generateReportButton);

        this.add(controlPanel, BorderLayout.NORTH);

        reportContentPanel = new JPanel(new BorderLayout());
        reportContentPanel.setOpaque(false);
        reportScrollPane = new JScrollPane(reportContentPanel);
        reportScrollPane.getViewport().setBackground(new Color(40, 40, 40));
        reportScrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        this.add(reportScrollPane, BorderLayout.CENTER);

        reportContentPanel.add(createMessagePanel("Select a report type and click 'Generate Report'."));

    }

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

    private JPanel createMessagePanel(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Verdana", Font.PLAIN, 16));
        panel.add(messageLabel, BorderLayout.CENTER);
        return panel;
    }


    private void generateReport() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String reportType = (String) reportTypeComboBox.getSelectedItem();
        String reportFormat = (String) reportFormatComboBox.getSelectedItem();

        if (reportType == null || reportFormat == null) {
            JOptionPane.showMessageDialog(this, "Please select report type and format.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("Generating report: " + reportType + " in format: " + reportFormat);

        reportContentPanel.removeAll();

        JPanel reportPanel = null;

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

            if ("Print (to Printer/Preview)".equals(reportFormat)) {
                printPanelContent(reportContentPanel, reportType);
            }
        }
    }

    private JPanel generateInventoryReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        String[] columns = {"Item ID", "Item Name", "Category", "Quantity", "Reorder Level", "Unit Price", "Is Machinery"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3 || columnIndex == 4) return Integer.class;
                if (columnIndex == 5) return BigDecimal.class;
                if (columnIndex == 6) return Boolean.class;
                return String.class;
            }
        };
        JTable table = new JTable(model);
        styleReportTable(table);
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
                true,
                true,
                false
        );
        customizeChart(pieChart);
        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(400, 300));
        chartPanel.setOpaque(false);

        panel.add(chartPanel, BorderLayout.SOUTH);

        return panel;
    }

     private JPanel generateTransactionHistoryReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        String[] columns = {"Transaction ID", "Date", "Item Name", "Type", "Quantity", "User", "Details"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return Integer.class;
                return String.class;
            }
        };
        JTable table = new JTable(model);
        styleReportTable(table);
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.getViewport().setBackground(new Color(40, 40, 40));

        String sql = "SELECT t.TransactionID, t.TransactionDate, i.ItemName, t.TransactionType, t.Quantity, u.FullName AS UserName, t.Details " +
                     "FROM Transactions t LEFT JOIN Items i ON t.ItemID = i.ItemID LEFT JOIN Users u ON t.UserID = u.UserID " +
                     "ORDER BY t.TransactionDate DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("TransactionID"),
                    rs.getTimestamp("TransactionDate"),
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

        DefaultCategoryDataset movementDataset = new DefaultCategoryDataset();
        String chartSql = "SELECT " +
                     "    DATE_FORMAT(TransactionDate, '%Y-%m') AS Month, " +
                     "    TransactionType, " +
                     "    SUM(Quantity) AS TotalQuantity " +
                     "FROM Transactions " +
                     "WHERE TransactionDate >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
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
                true,
                true,
                false
        );
        customizeChart(barChart);
        CategoryPlot plot = barChart.getCategoryPlot();
        ((BarRenderer) plot.getRenderer()).setSeriesPaint(0, new Color(46, 204, 113));
        ((BarRenderer) plot.getRenderer()).setSeriesPaint(1, new Color(231, 76, 60));

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 300));
        chartPanel.setOpaque(false);

        panel.add(chartPanel, BorderLayout.SOUTH);

        return panel;
    }

     private JPanel generatePurchaseOrderSummaryReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        String[] columns = {"PO ID", "PO Number", "PO Date", "Status", "Total Amount", "Created By"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 5) return Integer.class;
                if (columnIndex == 4) return BigDecimal.class;
                return String.class;
            }
        };
        JTable table = new JTable(model);
        styleReportTable(table);
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.getViewport().setBackground(new Color(40, 40, 40));

        String sql = "SELECT po.POID, po.PONumber, po.PODate, po.Status, po.TotalAmount, po.CreatedBy, u.FullName AS CreatedByName " +
                     "FROM PurchaseOrders po LEFT JOIN Users u ON po.CreatedBy = u.UserID " +
                     "ORDER BY po.PODate DESC";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("POID"),
                    rs.getString("PONumber"),
                    rs.getDate("PODate"),
                    rs.getString("Status"),
                    rs.getBigDecimal("TotalAmount"),
                    rs.getString("CreatedByName")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error fetching Purchase Order Summary Report data: " + e.getMessage());
            e.printStackTrace();
            model.addRow(new Object[]{"Error", "Error fetching data", "", "", BigDecimal.ZERO, ""});
        }

        panel.add(tableScrollPane, BorderLayout.CENTER);

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
                true,
                true,
                false
        );
        customizeChart(pieChart);
        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(400, 300));
        chartPanel.setOpaque(false);

        panel.add(chartPanel, BorderLayout.SOUTH);

        return panel;
    }

     private JPanel generateUserListReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        String[] columns = {"User ID", "Username", "Full Name", "Role", "Is Active"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                if (columnIndex == 4) return Boolean.class;
                return String.class;
            }
        };
        JTable table = new JTable(model);
        styleReportTable(table);
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

        return panel;
    }

     private JPanel generateLowStockItemsReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        String[] columns = {"Item ID", "Item Name", "Category", "Current Quantity", "Reorder Level", "Is Machinery"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
             @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 3 || columnIndex == 4) return Integer.class;
                if (columnIndex == 5) return Boolean.class;
                return String.class;
            }
        };
        JTable table = new JTable(model);
        styleReportTable(table);
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.getViewport().setBackground(new Color(40, 40, 40));


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

        return panel;
    }


    private void styleReportTable(JTable table) {
        table.setForeground(Color.WHITE);
        table.setBackground(new Color(40, 40, 40));
        table.setGridColor(new Color(60, 60, 60));
        table.setSelectionBackground(new Color(51, 153, 255));
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("Verdana", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(50, 50, 50));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }


     private void customizeChart(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(30, 30, 30));
        chart.getTitle().setPaint(Color.WHITE);
        if (chart.getLegend() != null) {
             chart.getLegend().setBackgroundPaint(new Color(30, 30, 30));
             chart.getLegend().setItemPaint(Color.WHITE);
        }

        if (chart.getPlot() != null) {
            chart.getPlot().setBackgroundPaint(new Color(30, 30, 30));
            chart.getPlot().setOutlinePaint(new Color(30, 30, 30));

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
                 categoryPlot.setRangeGridlinePaint(new Color(60, 60, 60));
            }
            if (chart.getPlot() instanceof PiePlot) {
                PiePlot piePlot = (PiePlot) chart.getPlot();
                piePlot.setLabelPaint(Color.WHITE);
                piePlot.setOutlinePaint(null);
            }
        }
    }


     private void printPanelContent(JPanel panel, String reportTitle) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setJobName(reportTitle);

        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                double scaleX = pageFormat.getImageableWidth() / panel.getWidth();
                double scaleY = pageFormat.getImageableHeight() / panel.getHeight();
                double scale = Math.min(scaleX, scaleY);
                g2d.scale(scale, scale);

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
