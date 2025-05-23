package modules;

import Package1.DBConnection;
import Package1.User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Date;
import java.awt.GridLayout;
import javax.swing.table.DefaultTableCellRenderer;


public class PurchaseOrderDetailsDialog extends JDialog {

    private Connection conn;
    private User currentUser;
    private int poId;

    private JLabel lblPoNumber;
    private JLabel lblPoDate;
    private JLabel lblStatus;
    private JLabel lblTotalAmount;
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;
    private JButton btnApprove;
    private JButton btnCancel;
    private JButton btnReceive;


    private PurchaseOrderActionListener actionListener;

    public interface PurchaseOrderActionListener {
        void purchaseOrderChanged();
    }

    public void setPurchaseOrderActionListener(PurchaseOrderActionListener listener) {
        this.actionListener = listener;
    }


    public PurchaseOrderDetailsDialog(Frame parent, boolean modal, Connection conn, User currentUser, int poId) {
        super(parent, modal);
        this.conn = conn;
        this.currentUser = currentUser;
        this.poId = poId;
        setTitle("Purchase Order Details");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setupDialogComponents();

        loadPurchaseOrderDetails();
    }

    private void setupDialogComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(new Color(30, 30, 30));

        JPanel headerPanel = new JPanel(new GridLayout(4, 1, 0, 5));
        headerPanel.setOpaque(false);
        lblPoNumber = createStyledLabel("PO Number: ");
        lblPoDate = createStyledLabel("PO Date: ");
        lblStatus = createStyledLabel("Status: ");
        lblTotalAmount = createStyledLabel("Total Amount: ");

        headerPanel.add(lblPoNumber);
        headerPanel.add(lblPoDate);
        headerPanel.add(lblStatus);
        headerPanel.add(lblTotalAmount);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        String[] itemColumns = {"Item ID", "Description", "Quantity Ordered", "Unit Price", "Subtotal"};
        itemsTableModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3 || columnIndex == 4) {
                    return BigDecimal.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        itemsTable = new JTable(itemsTableModel);
        styleTable(itemsTable);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < itemsTable.getColumnCount(); i++) {
            itemsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }


        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.getViewport().setBackground(new Color(40, 40, 40));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        btnApprove = createStyledButton("Approve", new Color(52, 152, 219));
        btnCancel = createStyledButton("Cancel PO", new Color(231, 76, 60));
        btnReceive = createStyledButton("Receive Items", new Color(46, 204, 113));

        btnApprove.addActionListener(e -> updatePurchaseOrderStatus("Approved", null));
        btnCancel.addActionListener(e -> updatePurchaseOrderStatus("Cancelled", null));
        btnReceive.addActionListener(e -> handleReceiveItems());


        buttonPanel.add(btnApprove);
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnReceive);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setLocationRelativeTo(getParent());
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Verdana", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        return label;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Verdana", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
        return button;
    }

     private void styleTable(JTable table) {
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


    private void loadPurchaseOrderDetails() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }

        String poSql = "SELECT PONumber, PODate, Status, TotalAmount FROM PurchaseOrders WHERE POID = ?";
        String itemsSql = "SELECT poi.ItemID, poi.Description, poi.QuantityOrdered, i.UnitPrice, (poi.QuantityOrdered * i.UnitPrice) AS Subtotal " +
                          "FROM PurchaseOrderItems poi JOIN Items i ON poi.ItemID = i.ItemID WHERE poi.POID = ?";


        try (PreparedStatement pstmtPo = conn.prepareStatement(poSql)) {
            pstmtPo.setInt(1, poId);
            try (ResultSet rsPo = pstmtPo.executeQuery()) {
                if (rsPo.next()) {
                    String currentStatus = rsPo.getString("Status");

                    lblPoNumber.setText("PO Number: " + rsPo.getString("PONumber"));
                    lblPoDate.setText("PO Date: " + rsPo.getDate("PODate"));
                    lblStatus.setText("Status: " + currentStatus);
                    BigDecimal totalAmount = rsPo.getBigDecimal("TotalAmount");
                    lblTotalAmount.setText("Total Amount: " + (totalAmount != null ? totalAmount.toPlainString() : "0.00"));

                    boolean isAdmin = currentUser != null && "Admin".equals(currentUser.getRole());
                    boolean isCustodian = currentUser != null && "Custodian".equals(currentUser.getRole());

                    btnApprove.setVisible(isAdmin && ("Draft".equals(currentStatus) || "Pending Approval".equals(currentStatus)));

                    btnCancel.setVisible(!("Cancelled".equals(currentStatus) || "Received".equals(currentStatus)));

                    btnReceive.setVisible((isAdmin || isCustodian) && ("Approved".equals(currentStatus) || "Ordered".equals(currentStatus) || "Partially Received".equals(currentStatus)));


                } else {
                    JOptionPane.showMessageDialog(this, "Purchase Order not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    dispose();
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading purchase order details: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading purchase order details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
             dispose();
             return;
        }

        itemsTableModel.setRowCount(0);
        try (PreparedStatement pstmtItems = conn.prepareStatement(itemsSql)) {
            pstmtItems.setInt(1, poId);
            try (ResultSet rsItems = pstmtItems.executeQuery()) {
                while (rsItems.next()) {
                    BigDecimal unitPrice = rsItems.getBigDecimal("UnitPrice");
                    BigDecimal subtotal = rsItems.getBigDecimal("Subtotal");

                    itemsTableModel.addRow(new Object[]{
                        rsItems.getInt("ItemID"),
                        rsItems.getString("Description"),
                        rsItems.getInt("QuantityOrdered"),
                        unitPrice,
                        subtotal
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading purchase order items: " + e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Unknown column 'i.UnitPrice'")) {
                 JOptionPane.showMessageDialog(this, "Database Error: Could not find 'UnitPrice' column in the Items table.\nPlease ensure your database schema is up to date.", "Database Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error loading purchase order items: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updatePurchaseOrderStatus(String newStatus, String expectedCurrentStatus) {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("Approved".equals(newStatus) && (currentUser == null || !"Admin".equals(currentUser.getRole()))) {
             JOptionPane.showMessageDialog(this, "You do not have permission to approve purchase orders.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
             loadPurchaseOrderDetails();
             return;
        }
         if ("Cancelled".equals(newStatus) && (currentUser == null || (!"Admin".equals(currentUser.getRole()) && !"Custodian".equals(currentUser.getRole())))) {
             JOptionPane.showMessageDialog(this, "You do not have permission to cancel purchase orders.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
             loadPurchaseOrderDetails();
             return;
         }


        String actualCurrentStatus = "";
        String fetchStatusSql = "SELECT Status FROM PurchaseOrders WHERE POID = ?";
        try (PreparedStatement pstmtFetch = conn.prepareStatement(fetchStatusSql)) {
            pstmtFetch.setInt(1, poId);
            try (ResultSet rs = pstmtFetch.executeQuery()) {
                if (rs.next()) {
                    actualCurrentStatus = rs.getString("Status");
                } else {
                    JOptionPane.showMessageDialog(this, "Purchase Order not found. Cannot update status.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching current PO status: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error verifying PO status: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (expectedCurrentStatus != null && !actualCurrentStatus.equals(expectedCurrentStatus)) {
            JOptionPane.showMessageDialog(this,
                "Action cannot be performed. The PO status is '" + actualCurrentStatus + "', but expected '" + expectedCurrentStatus + "'. Please refresh.",
                "Status Mismatch", JOptionPane.WARNING_MESSAGE);
            loadPurchaseOrderDetails();
            return;
        }

        if ("Approved".equals(newStatus) && !("Draft".equals(actualCurrentStatus) || "Pending Approval".equals(actualCurrentStatus))) {
             JOptionPane.showMessageDialog(this,
                "Cannot approve a PO with status '" + actualCurrentStatus + "'. It must be 'Draft' or 'Pending Approval'.",
                "Invalid Action", JOptionPane.WARNING_MESSAGE);
            loadPurchaseOrderDetails();
            return;
        }

        if ("Cancelled".equals(newStatus) && ("Received".equals(actualCurrentStatus) || "Cancelled".equals(actualCurrentStatus))) {
             JOptionPane.showMessageDialog(this,
                "Cannot cancel a PO that is already '" + actualCurrentStatus + "'.",
                "Invalid Action", JOptionPane.WARNING_MESSAGE);
            loadPurchaseOrderDetails();
            return;
        }

        if ("Received".equals(newStatus) && !("Approved".equals(actualCurrentStatus) || "Ordered".equals(actualCurrentStatus) || "Partially Received".equals(actualCurrentStatus))) {
             JOptionPane.showMessageDialog(this,
                "Cannot mark PO as Received with status '" + actualCurrentStatus + "'. It must be 'Approved', 'Ordered', or 'Partially Received'.",
                "Invalid Action", JOptionPane.WARNING_MESSAGE);
            loadPurchaseOrderDetails();
            return;
        }


        String sql = "UPDATE PurchaseOrders SET Status = ? WHERE POID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, poId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Purchase Order status updated to " + newStatus, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPurchaseOrderDetails();

                if (actionListener != null) {
                    actionListener.purchaseOrderChanged();
                }
                dispose();

            } else {
                JOptionPane.showMessageDialog(this, "Failed to update purchase order status. PO might have been modified or deleted.", "Update Error", JOptionPane.ERROR_MESSAGE);
                loadPurchaseOrderDetails();
            }
        } catch (SQLException e) {
             System.err.println("Error updating purchase order status: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error updating purchase order status: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleReceiveItems() {
         if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

         if (currentUser == null || (!"Admin".equals(currentUser.getRole()) && !"Custodian".equals(currentUser.getRole()))) {
             JOptionPane.showMessageDialog(this, "You do not have permission to receive purchase orders.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
             loadPurchaseOrderDetails();
             return;
         }

        String currentStatusFromDB = "";
        String fetchStatusSql = "SELECT Status FROM PurchaseOrders WHERE POID = ?";
        try (PreparedStatement pstmtFetch = conn.prepareStatement(fetchStatusSql)) {
            pstmtFetch.setInt(1, poId);
            try (ResultSet rs = pstmtFetch.executeQuery()) {
                if (rs.next()) {
                    currentStatusFromDB = rs.getString("Status");
                } else {
                    JOptionPane.showMessageDialog(this, "Purchase Order not found. Cannot receive items.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching current PO status for receiving: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error verifying PO status for receiving: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


         if ("Approved".equals(currentStatusFromDB) || "Ordered".equals(currentStatusFromDB) || "Partially Received".equals(currentStatusFromDB)) {
             int confirm = JOptionPane.showConfirmDialog(this, "Mark this PO as fully received and update inventory?", "Confirm Receive", JOptionPane.YES_NO_OPTION);
             if (confirm == JOptionPane.YES_OPTION) {
                 try {
                     conn.setAutoCommit(false);

                     String updateInventorySql = "UPDATE Items SET Quantity = Quantity + ? WHERE ItemID = ?";
                     try (PreparedStatement pstmtUpdateInventory = conn.prepareStatement(updateInventorySql)) {
                         for (int i = 0; i < itemsTableModel.getRowCount(); i++) {
                             int itemId = (Integer) itemsTableModel.getValueAt(i, 0);
                             int quantityOrdered = (Integer) itemsTableModel.getValueAt(i, 2);

                             pstmtUpdateInventory.setInt(1, quantityOrdered);
                             pstmtUpdateInventory.setInt(2, itemId);
                             pstmtUpdateInventory.addBatch();
                         }
                         pstmtUpdateInventory.executeBatch();
                         System.out.println("Inventory updated for PO ID: " + poId);
                     }

                     String updatePoStatusSql = "UPDATE PurchaseOrders SET Status = ? WHERE POID = ?";
                     try (PreparedStatement pstmtUpdatePo = conn.prepareStatement(updatePoStatusSql)) {
                         pstmtUpdatePo.setString(1, "Received");
                         pstmtUpdatePo.setInt(2, poId);
                         int affectedRows = pstmtUpdatePo.executeUpdate();

                         if (affectedRows == 0) {
                             throw new SQLException("Failed to update purchase order status to Received. PO might have been modified or deleted.");
                         }
                         System.out.println("Purchase Order status updated to Received for PO ID: " + poId);
                     }

                     conn.commit();
                     JOptionPane.showMessageDialog(this, "Purchase Order marked as Received and inventory updated.", "Success", JOptionPane.INFORMATION_MESSAGE);

                     loadPurchaseOrderDetails();
                     if (actionListener != null) {
                         actionListener.purchaseOrderChanged();
                     }
                     dispose();

                 } catch (SQLException e) {
                     try {
                         if (conn != null) {
                             conn.rollback();
                             System.err.println("Transaction rolled back due to error during receiving items.");
                         }
                     } catch (SQLException ex) {
                         System.err.println("Error during transaction rollback: " + ex.getMessage());
                     }
                     System.err.println("Database error during receiving items: " + e.getMessage());
                     JOptionPane.showMessageDialog(this, "Error receiving items and updating inventory: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                     loadPurchaseOrderDetails();

                 } finally {
                     try {
                         if (conn != null) {
                             conn.setAutoCommit(true);
                         }
                     } catch (SQLException ex) {
                         System.err.println("Error restoring auto-commit: " + ex.getMessage());
                     }
                 }
             }
         } else {
             JOptionPane.showMessageDialog(this, "This Purchase Order is not in a status that allows receiving items (Current: " + currentStatusFromDB + "). It must be 'Approved', 'Ordered', or 'Partially Received'. Please refresh.", "Invalid Status", JOptionPane.WARNING_MESSAGE);
             loadPurchaseOrderDetails();
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
