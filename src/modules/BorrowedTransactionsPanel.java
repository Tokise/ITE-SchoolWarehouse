package modules;

import Package1.DBConnection;
import Package1.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BorrowedTransactionsPanel extends JPanel {

    private JTable borrowedTransactionsTable;
    private DefaultTableModel borrowedTableModel;
    private JTable returnedTransactionsTable;
    private DefaultTableModel returnedTableModel;
    private Connection conn;
    private JTextField searchField;
    private JButton searchButton;
    private JButton returnItemButton;
    private JButton viewDetailsButton;
    private User currentUser;
    private boolean isDbConnected = false;

    public static final SimpleDateFormat DATE_FORMAT_TIMESTAMP = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat DATE_FORMAT_DATE_ONLY = new SimpleDateFormat("yyyy-MM-dd");

    public BorrowedTransactionsPanel() {
        try {
            this.conn = DBConnection.getConnection();
            if (this.conn != null && !this.conn.isClosed()) {
                this.isDbConnected = true;
            } else {
                JOptionPane.showMessageDialog(this, "Failed to establish database connection.", "DB Connection Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(BorrowedTransactionsPanel.class.getName()).log(Level.SEVERE, "Failed to establish database connection for BorrowedTransactionsPanel.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database connection error: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(BorrowedTransactionsPanel.class.getName()).log(Level.SEVERE, "SQL Exception during DB connection for BorrowedTransactionsPanel.", ex);
        }

        if (isDbConnected) {
            initializeUI();
            loadBorrowedTransactions("");
            loadReturnedTransactions("");
        } else {
            setLayout(new BorderLayout());
            JLabel errorLabel = new JLabel("<html><center>Database connection failed.<br>Cannot display transaction panels.</center></html>", SwingConstants.CENTER);
            errorLabel.setForeground(Color.RED);
            errorLabel.setFont(new Font("Verdana", Font.BOLD, 16));
            add(errorLabel, BorderLayout.CENTER);
        }
    }
    
    public BorrowedTransactionsPanel(User currentUser) {
        this();
        this.currentUser = currentUser;
    }


    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new BorderLayout(10,10));
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Item Transactions Overview", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 20));
        titleLabel.setForeground(new Color(200, 200, 200));
        topPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel searchFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchFilterPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
        searchLabel.setForeground(new Color(200, 200, 200));
        searchFilterPanel.add(searchLabel);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Verdana", Font.PLAIN, 12));
        searchField.setBackground(new Color(60, 60, 60));
        searchField.setForeground(new Color(220, 220, 220));
        searchField.setCaretColor(new Color(220, 220, 220));
        searchFilterPanel.add(searchField);

        searchButton = new JButton("Search");
        styleButton(searchButton, new Color(70,130,180));
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDbConnected) {
                    loadBorrowedTransactions(searchField.getText().trim());
                    loadReturnedTransactions(searchField.getText().trim());
                } else {
                    JOptionPane.showMessageDialog(BorrowedTransactionsPanel.this, "Database not connected.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        searchFilterPanel.add(searchButton);
        
        topPanel.add(searchFilterPanel, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(30, 30, 30));
        tabbedPane.setForeground(new Color(220, 220, 220));
        tabbedPane.setFont(new Font("Verdana", Font.BOLD, 14));

        JPanel borrowedItemsPanel = new JPanel(new BorderLayout());
        borrowedItemsPanel.setBackground(new Color(30, 30, 30));
        String[] borrowedColumnNames = {"Transaction ID", "Item ID", "Item Name", "Quantity", "Borrower Name", "Department", "Grade Level", "Section", "School Year", "Purpose", "Transaction Date", "Expected Return", "Status"};
        borrowedTableModel = new DefaultTableModel(borrowedColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        borrowedTransactionsTable = new JTable(borrowedTableModel);
        styleTable(borrowedTransactionsTable);
        borrowedTransactionsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        borrowedTransactionsTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        borrowedTransactionsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        JScrollPane borrowedScrollPane = new JScrollPane(borrowedTransactionsTable);
        borrowedScrollPane.getViewport().setBackground(new Color(55, 55, 55));
        borrowedItemsPanel.add(borrowedScrollPane, BorderLayout.CENTER);

        JPanel borrowedButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        borrowedButtonsPanel.setOpaque(false);
        viewDetailsButton = new JButton("View Transaction Details");
        styleButton(viewDetailsButton, new Color(52, 152, 219));
        viewDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDbConnected) {
                    viewTransactionDetails(borrowedTransactionsTable);
                } else {
                    JOptionPane.showMessageDialog(BorrowedTransactionsPanel.this, "Database not connected.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        borrowedButtonsPanel.add(viewDetailsButton);

        returnItemButton = new JButton("Return Selected Item");
        styleButton(returnItemButton, new Color(60, 179, 113));
        returnItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDbConnected) {
                    processReturnItem();
                } else {
                    JOptionPane.showMessageDialog(BorrowedTransactionsPanel.this, "Database not connected.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        borrowedButtonsPanel.add(returnItemButton);
        borrowedItemsPanel.add(borrowedButtonsPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Borrowed Items", borrowedItemsPanel);

        JPanel returnedItemsPanel = new JPanel(new BorderLayout());
        returnedItemsPanel.setBackground(new Color(30, 30, 30));
        String[] returnedColumnNames = {"Transaction ID", "Item ID", "Item Name", "Quantity", "Borrower Name", "Department", "Grade Level", "Section", "School Year", "Purpose", "Transaction Date", "Actual Return Date"};
        returnedTableModel = new DefaultTableModel(returnedColumnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        returnedTransactionsTable = new JTable(returnedTableModel);
        styleTable(returnedTransactionsTable);
        returnedTransactionsTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        returnedTransactionsTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        returnedTransactionsTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        JScrollPane returnedScrollPane = new JScrollPane(returnedTransactionsTable);
        returnedScrollPane.getViewport().setBackground(new Color(55, 55, 55));
        returnedItemsPanel.add(returnedScrollPane, BorderLayout.CENTER);

        JPanel returnedButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        returnedButtonsPanel.setOpaque(false);
        JButton viewReturnedDetailsButton = new JButton("View Transaction Details");
        styleButton(viewReturnedDetailsButton, new Color(52, 152, 219));
        viewReturnedDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDbConnected) {
                    viewTransactionDetails(returnedTransactionsTable);
                } else {
                    JOptionPane.showMessageDialog(BorrowedTransactionsPanel.this, "Database not connected.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        returnedButtonsPanel.add(viewReturnedDetailsButton);
        returnedItemsPanel.add(returnedButtonsPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("Returned Items", returnedItemsPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Verdana", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }
    
    private void styleTable(JTable table) {
        table.setForeground(new Color(220, 220, 220));
        table.setBackground(new Color(30, 30, 30));
        table.setGridColor(new Color(30, 30, 30));
        table.setSelectionBackground(new Color(30, 30, 30));
        table.setSelectionForeground(new Color(255, 255, 255));
        table.setFont(new Font("Verdana", Font.PLAIN, 11));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(30, 30, 30));
        table.getTableHeader().setForeground(new Color(255, 255, 255));
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private void loadBorrowedTransactions(String searchTerm) {
        borrowedTableModel.setRowCount(0);
        if (conn == null || !isDbConnected) {
            JOptionPane.showMessageDialog(this, "Database connection is not available.", "DB Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "SELECT t.TransactionID, t.ItemID, i.ItemName, t.Quantity, t.IssuedToPersonName, " +
                     "t.IssuedToDepartment, t.IssuedToGradeLevel, t.IssuedToSection, t.SchoolYear, t.Purpose, " +
                     "t.TransactionDate, t.ExpectedReturnDate, t.ActualReturnDate " +
                     "FROM Transactions t " +
                     "JOIN Items i ON t.ItemID = i.ItemID " +
                     "WHERE t.TransactionType = 'Issued' AND t.ActualReturnDate IS NULL";

        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql += " AND (LOWER(i.ItemName) LIKE ? OR LOWER(t.IssuedToPersonName) LIKE ? OR LOWER(t.Purpose) LIKE ?)";
        }
        sql += " ORDER BY t.TransactionDate DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = new Object[13];
                row[0] = rs.getInt("TransactionID");
                row[1] = rs.getInt("ItemID");
                row[2] = rs.getString("ItemName");
                row[3] = rs.getInt("Quantity");
                row[4] = rs.getString("IssuedToPersonName");
                row[5] = rs.getString("IssuedToDepartment");
                row[6] = rs.getString("IssuedToGradeLevel");
                row[7] = rs.getString("IssuedToSection");
                row[8] = rs.getString("SchoolYear");
                row[9] = rs.getString("Purpose");
                
                Timestamp transactionDateTimestamp = rs.getTimestamp("TransactionDate");
                row[10] = transactionDateTimestamp != null ? DATE_FORMAT_TIMESTAMP.format(new Date(transactionDateTimestamp.getTime())) : "N/A";
                
                Date expectedReturnSqlDate = rs.getDate("ExpectedReturnDate");
                row[11] = expectedReturnSqlDate != null ? DATE_FORMAT_DATE_ONLY.format(expectedReturnSqlDate) : "N/A (Consumable)";
                
                Date actualReturnSqlDate = rs.getDate("ActualReturnDate");
                String status;
                if (actualReturnSqlDate != null) {
                    status = "Returned";
                } else {
                    if (expectedReturnSqlDate != null) {
                        Calendar todayCal = Calendar.getInstance();
                        todayCal.set(Calendar.HOUR_OF_DAY, 0);
                        todayCal.set(Calendar.MINUTE, 0);
                        todayCal.set(Calendar.SECOND, 0);
                        todayCal.set(Calendar.MILLISECOND, 0);
                        Date today = todayCal.getTime();

                        if (expectedReturnSqlDate.before(today)) {
                            status = "Overdue";
                        } else {
                            status = "Borrowed";
                        }
                    } else {
                        status = "Borrowed (Consumable)";
                    }
                }
                row[12] = status;
                borrowedTableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading borrowed transactions: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(BorrowedTransactionsPanel.class.getName()).log(Level.SEVERE, "Error loading borrowed transactions", e);
        }
    }

    private void loadReturnedTransactions(String searchTerm) {
        returnedTableModel.setRowCount(0);
        if (conn == null || !isDbConnected) {
            JOptionPane.showMessageDialog(this, "Database connection is not available.", "DB Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "SELECT t.TransactionID, t.ItemID, i.ItemName, t.Quantity, t.IssuedToPersonName, " +
                     "t.IssuedToDepartment, t.IssuedToGradeLevel, t.IssuedToSection, t.SchoolYear, t.Purpose, " +
                     "t.TransactionDate, t.ActualReturnDate " +
                     "FROM Transactions t " +
                     "JOIN Items i ON t.ItemID = i.ItemID " +
                     "WHERE t.TransactionType = 'Issued' AND t.ActualReturnDate IS NOT NULL";

        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql += " AND (LOWER(i.ItemName) LIKE ? OR LOWER(t.IssuedToPersonName) LIKE ? OR LOWER(t.Purpose) LIKE ?)";
        }
        sql += " ORDER BY t.ActualReturnDate DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchPattern = "%" + searchTerm.toLowerCase() + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Object[] row = new Object[12];
                row[0] = rs.getInt("TransactionID");
                row[1] = rs.getInt("ItemID");
                row[2] = rs.getString("ItemName");
                row[3] = rs.getInt("Quantity");
                row[4] = rs.getString("IssuedToPersonName");
                row[5] = rs.getString("IssuedToDepartment");
                row[6] = rs.getString("IssuedToGradeLevel");
                row[7] = rs.getString("IssuedToSection");
                row[8] = rs.getString("SchoolYear");
                row[9] = rs.getString("Purpose");
                
                Timestamp transactionDateTimestamp = rs.getTimestamp("TransactionDate");
                row[10] = transactionDateTimestamp != null ? DATE_FORMAT_TIMESTAMP.format(new Date(transactionDateTimestamp.getTime())) : "N/A";
                
                Timestamp actualReturnDateTimestamp = rs.getTimestamp("ActualReturnDate");
                row[11] = actualReturnDateTimestamp != null ? DATE_FORMAT_TIMESTAMP.format(new Date(actualReturnDateTimestamp.getTime())) : "N/A";
                
                returnedTableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading returned transactions: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(BorrowedTransactionsPanel.class.getName()).log(Level.SEVERE, "Error loading returned transactions", e);
        }
    }


    private void processReturnItem() {
        int selectedRow = borrowedTransactionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to mark as returned.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = borrowedTransactionsTable.convertRowIndexToModel(selectedRow);
        int transactionId = (Integer) borrowedTableModel.getValueAt(modelRow, 0);
        int itemId = (Integer) borrowedTableModel.getValueAt(modelRow, 1);
        int quantityReturned = (Integer) borrowedTableModel.getValueAt(modelRow, 3);
        String currentStatus = (String) borrowedTableModel.getValueAt(modelRow, 12);

        if ("Returned".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This item has already been marked as returned.", "Already Returned", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to mark this item as returned?\nTransaction ID: " + transactionId + "\nItem: " + borrowedTableModel.getValueAt(modelRow, 2) + "\nQuantity: " + quantityReturned,
            "Confirm Return", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Connection transactionConn = null;
            try {
                transactionConn = DBConnection.getConnection();
                if (transactionConn == null || transactionConn.isClosed()) {
                    throw new SQLException("Database connection is not available for transaction.");
                }
                transactionConn.setAutoCommit(false);

                String updateTransactionSql = "UPDATE Transactions SET ActualReturnDate = NOW() WHERE TransactionID = ?";
                try (PreparedStatement pstmtTransaction = transactionConn.prepareStatement(updateTransactionSql)) {
                    pstmtTransaction.setInt(1, transactionId);
                    int affectedRowsTransaction = pstmtTransaction.executeUpdate();
                    if (affectedRowsTransaction == 0) {
                        throw new SQLException("Failed to update transaction record. No rows affected.");
                    }
                }

                String updateItemSql = "UPDATE Items SET Quantity = Quantity + ? WHERE ItemID = ?";
                try (PreparedStatement pstmtItem = transactionConn.prepareStatement(updateItemSql)) {
                    pstmtItem.setInt(1, quantityReturned);
                    pstmtItem.setInt(2, itemId);
                    int affectedRowsItem = pstmtItem.executeUpdate();
                     if (affectedRowsItem == 0) {
                        throw new SQLException("Failed to update item quantity. ItemID not found or no change needed.");
                    }
                }
                
                transactionConn.commit();
                JOptionPane.showMessageDialog(this, "Item marked as returned successfully.", "Return Success", JOptionPane.INFORMATION_MESSAGE);
                loadBorrowedTransactions(searchField.getText().trim());
                loadReturnedTransactions(searchField.getText().trim());

            } catch (SQLException e) {
                if (transactionConn != null) {
                    try {
                        transactionConn.rollback();
                    } catch (SQLException ex) {
                        Logger.getLogger(BorrowedTransactionsPanel.class.getName()).log(Level.SEVERE, "Error during rollback", ex);
                    }
                }
                JOptionPane.showMessageDialog(this, "Error processing return: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
                Logger.getLogger(BorrowedTransactionsPanel.class.getName()).log(Level.SEVERE, "Error processing return item", e);
            } finally {
                if (transactionConn != null) {
                    try {
                        transactionConn.setAutoCommit(true);
                    } catch (SQLException ex) {
                        Logger.getLogger(BorrowedTransactionsPanel.class.getName()).log(Level.SEVERE, "Error resetting auto-commit", ex);
                    }
                }
            }
        }
    }

    private void viewTransactionDetails(JTable sourceTable) {
        int selectedRow = sourceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to view its details.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = sourceTable.convertRowIndexToModel(selectedRow);
        int transactionId = (Integer) sourceTable.getModel().getValueAt(modelRow, 0);

        TransactionDetailsDialog dialog = new TransactionDetailsDialog((Frame) SwingUtilities.getWindowAncestor(this), conn, transactionId);
        dialog.setVisible(true);
    }
    
    private void logActivity(String activityType, String details) {
        if (conn == null || !isDbConnected || currentUser == null || currentUser.getUserId() <= 0) {
            System.err.println("Cannot log activity: DB connection null or invalid UserID");
            return;
        }
        String sql = "INSERT INTO RecentActivities (ActivityType, UserID, UserName, Details, ActivityDate) VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, activityType);
            pstmt.setInt(2, currentUser.getUserId());
            pstmt.setString(3, currentUser.getUsername());
            pstmt.setString(4, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging activity: " + e.getMessage());
            e.printStackTrace();
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

class TransactionDetailsDialog extends JDialog {
    private Connection conn;
    private int transactionId;

    public TransactionDetailsDialog(Frame owner, Connection connection, int transactionId) {
        super(owner, "Transaction Details (ID: " + transactionId + ")", true);
        this.conn = connection;
        this.transactionId = transactionId;
        initializeDialogUI();
        loadTransactionDetails();
    }

    private void initializeDialogUI() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 30));
        setMinimumSize(new Dimension(600, 500));
        setLocationRelativeTo(getOwner());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setOpaque(false);
        JLabel headerLabel = new JLabel("Details for Transaction ID: " + transactionId);
        headerLabel.setFont(new Font("Verdana", Font.BOLD, 18));
        headerLabel.setForeground(new Color(200, 200, 200));
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setOpaque(false);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)));
        add(scrollPane, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setOpaque(false);
        JButton closeButton = new JButton("Close");
        styleButton(closeButton, new Color(192, 57, 43));
        closeButton.addActionListener(e -> dispose());
        footerPanel.add(closeButton);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JLabel createDetailLabel(String label, String value) {
        JLabel detailLabel = new JLabel("<html><b style='color:#E0E0E0;'>" + label + ":</b> <span style='color:#B0B0B0;'>" + value + "</span></html>");
        detailLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
        detailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        detailLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        return detailLabel;
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Verdana", Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }

    private void loadTransactionDetails() {
        JPanel detailsPanel = (JPanel) ((JScrollPane) getContentPane().getComponent(1)).getViewport().getView();
        detailsPanel.removeAll();

        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection is not available.", "DB Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "SELECT t.TransactionID, t.ItemID, i.ItemName, i.Description AS ItemDescription, " +
                     "t.Quantity, t.IssuedToPersonName, t.IssuedToDepartment, t.IssuedToGradeLevel, " +
                     "t.IssuedToSection, t.SchoolYear, t.Purpose, t.TransactionDate, t.ExpectedReturnDate, t.ActualReturnDate " +
                     "FROM Transactions t " +
                     "JOIN Items i ON t.ItemID = i.ItemID " +
                     "WHERE t.TransactionID = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, transactionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                detailsPanel.add(createDetailLabel("Transaction ID", String.valueOf(rs.getInt("TransactionID"))));
                detailsPanel.add(createDetailLabel("Item ID", String.valueOf(rs.getInt("ItemID"))));
                detailsPanel.add(createDetailLabel("Item Name", rs.getString("ItemName")));
                detailsPanel.add(createDetailLabel("Item Description", rs.getString("ItemDescription")));
                detailsPanel.add(createDetailLabel("Quantity", String.valueOf(rs.getInt("Quantity"))));
                detailsPanel.add(createDetailLabel("Borrower Name", rs.getString("IssuedToPersonName")));
                detailsPanel.add(createDetailLabel("Department", rs.getString("IssuedToDepartment")));
                detailsPanel.add(createDetailLabel("Grade Level", rs.getString("IssuedToGradeLevel")));
                detailsPanel.add(createDetailLabel("Section", rs.getString("IssuedToSection")));
                detailsPanel.add(createDetailLabel("School Year", rs.getString("SchoolYear")));
                detailsPanel.add(createDetailLabel("Purpose", rs.getString("Purpose")));
                
                Timestamp transactionDateTimestamp = rs.getTimestamp("TransactionDate");
                detailsPanel.add(createDetailLabel("Transaction Date", transactionDateTimestamp != null ? BorrowedTransactionsPanel.DATE_FORMAT_TIMESTAMP.format(new Date(transactionDateTimestamp.getTime())) : "N/A"));
                
                Date expectedReturnSqlDate = rs.getDate("ExpectedReturnDate");
                detailsPanel.add(createDetailLabel("Expected Return Date", expectedReturnSqlDate != null ? BorrowedTransactionsPanel.DATE_FORMAT_DATE_ONLY.format(expectedReturnSqlDate) : "N/A (Consumable)"));

                Date actualReturnSqlDate = rs.getDate("ActualReturnDate");
                detailsPanel.add(createDetailLabel("Actual Return Date", actualReturnSqlDate != null ? BorrowedTransactionsPanel.DATE_FORMAT_DATE_ONLY.format(actualReturnSqlDate) : "Not Yet Returned"));

            } else {
                detailsPanel.add(createDetailLabel("No Details Found", "For Transaction ID: " + transactionId));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transaction details: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(TransactionDetailsDialog.class.getName()).log(Level.SEVERE, "Error loading transaction details", e);
        } finally {
            detailsPanel.revalidate();
            detailsPanel.repaint();
        }
    }
}
