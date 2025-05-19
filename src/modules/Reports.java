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

// Import necessary JasperReports libraries (you need to add these to your project)
// import net.sf.jasperreports.engine.*;
// import net.sf.jasperreports.engine.export.JRXlsExporter;
// import net.sf.jasperreports.export.SimpleExporterInput;
// import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
// import net.sf.jasperreports.export.SimpleXlsReportConfiguration;

/**
 * JPanel for generating various reports.
 */
public class Reports extends javax.swing.JPanel {

    private Connection conn = null;

    private JComboBox<String> reportTypeComboBox;
    private JComboBox<String> reportFormatComboBox;
    private JButton generateReportButton;

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

        reportFormatComboBox = new JComboBox<>(new String[]{
            "PDF",
            "Excel"
        });
        reportFormatComboBox.setFont(new Font("Verdana", Font.PLAIN, 14));
        controlPanel.add(reportFormatComboBox);

        generateReportButton = new JButton("Generate Report");
        styleButton(generateReportButton, new Color(52, 152, 219));
        generateReportButton.addActionListener(e -> generateReport());
        controlPanel.add(generateReportButton);

        this.add(controlPanel, BorderLayout.NORTH);

        // You might add a JScrollPane here to display a preview or status messages
        // For now, we'll just use dialogs for output.
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
     * Triggers the report generation process based on user selection.
     */
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

        // TODO: Implement JasperReports logic here.
        // This involves:
        // 1. Loading the correct .jrxml or .jasper file based on reportType.
        // 2. Setting up report parameters (if any, e.g., date ranges, filters).
        // 3. Filling the report with data from the database connection.
        // 4. Exporting the filled report to the selected format (PDF or Excel).
        // 5. Handling file saving/opening for the user.

        try {
            // Example placeholder for JasperReports call:
            // String reportPath = "reports/" + getReportFileName(reportType); // Method to get JRXML/Jasper path
            // JasperReport jasperReport = JasperCompileManager.compileReport(reportPath); // Or load compiled .jasper
            // Map<String, Object> parameters = new HashMap<>();
            // // Add parameters if needed, e.g., parameters.put("startDate", startDate);
            // JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);

            // if ("PDF".equals(reportFormat)) {
            //     // Export to PDF
            //     String outputPath = reportType.replace(" ", "_") + ".pdf";
            //     JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);
            //     JOptionPane.showMessageDialog(this, reportType + " generated successfully as PDF:\n" + outputPath, "Success", JOptionPane.INFORMATION_MESSAGE);
            //     // TODO: Add logic to open the file
            // } else if ("Excel".equals(reportFormat)) {
            //     // Export to Excel
            //     String outputPath = reportType.replace(" ", "_") + ".xls"; // Use .xlsx for newer Excel
            //     JRXlsExporter exporter = new JRXlsExporter();
            //     exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            //     exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputPath));
            //     SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
            //     configuration.setOnePagePerSheet(false); // Set to true if each page should be a new sheet
            //     exporter.setConfiguration(configuration);
            //     exporter.exportReport();
            //     JOptionPane.showMessageDialog(this, reportType + " generated successfully as Excel:\n" + outputPath, "Success", JOptionPane.INFORMATION_MESSAGE);
            //     // TODO: Add logic to open the file
            // }

             JOptionPane.showMessageDialog(this, "Report generation logic needs to be implemented using JasperReports.", "Info", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating report: " + e.getMessage(), "Report Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Helper method to get the report file name based on report type.
     * You would map report types to your .jrxml or .jasper files here.
     * @param reportType The selected report type string.
     * @return The file path to the report template.
     */
    private String getReportFileName(String reportType) {
        // This is a placeholder. You need to create these files in JasperReports Studio.
        switch (reportType) {
            case "Inventory Report": return "reports/InventoryReport.jrxml"; // Or .jasper
            case "Transaction History": return "reports/TransactionHistoryReport.jrxml";
            case "Purchase Order Summary": return "reports/PurchaseOrderSummaryReport.jrxml";
            case "User List": return "reports/UserListReport.jrxml";
            case "Items Below Reorder Level": return "reports/LowStockItemsReport.jrxml";
            default: return null;
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
