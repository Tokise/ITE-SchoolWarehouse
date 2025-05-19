/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package modules;

import Package1.DBConnection;
import Package1.User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.awt.Frame;
import javax.swing.SwingUtilities;
import java.math.BigDecimal;
import javax.swing.JCheckBox; // Import JCheckBox
import java.awt.event.ItemEvent; // Import ItemEvent
import java.awt.event.MouseAdapter; // Import MouseAdapter for button column click handling
import java.awt.event.MouseEvent; // Import MouseEvent
import javax.swing.table.TableCellRenderer; // Import TableCellRenderer
import javax.swing.table.TableCellEditor; // Import TableCellEditor
import java.awt.Component; // Import Component
import java.util.EventObject; // Import EventObject
import javax.swing.AbstractCellEditor; // Import AbstractCellEditor
import java.awt.event.ActionEvent; // Import ActionEvent
import java.awt.event.ActionListener; // Import ActionListener

// Imports for printing
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JTextArea; // Needed for printing content


/**
 * JPanel for managing Purchase Orders.
 * Includes sections for items needing reorder, pending POs, and completed POs.
 */
public class PurchaseOrder extends javax.swing.JPanel implements PurchaseOrderDetailsDialog.PurchaseOrderActionListener { // Implement the listener interface

    private Connection conn = null;
    private User currentUser;

    private JTabbedPane tabbedPane;
    private JPanel itemsNeedingReorderPanel;
    private JPanel pendingOrdersPanel;
    private JPanel completedOrdersPanel;

    private JTable itemsNeedingReorderTable;
    private DefaultTableModel itemsNeedingReorderTableModel;
    private JButton createPoButton;
    private JCheckBox selectAllItemsCheckbox; // Global checkbox for items needing reorder

    private JTable pendingOrdersTable;
    private DefaultTableModel pendingOrdersTableModel;
    private JButton viewPendingPoButton;


    private JTable completedOrdersTable;
    private DefaultTableModel completedOrdersTableModel;
    private JButton viewCompletedPoButton;

    /**
     * Creates new form PurchaseOrder
     */
    public PurchaseOrder() {
        initComponents(); // NetBeans generated - should remain untouched
        connectToDatabase();
        setupPurchaseOrderPanel(); // Custom initialization method

    }

    /**
     * Sets the current user for the Purchase Order panel.
     * This user will be recorded as the creator of new POs.
     * @param user The current logged-in user.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (this.currentUser != null) {
            System.out.println("PurchaseOrder: User object set. UserID: " + this.currentUser.getUserId() + ", Username: " + this.currentUser.getUsername());
             int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                loadItemsNeedingReorder();
            } else if (selectedIndex == 1) {
                loadOrders("Pending");
            } else if (selectedIndex == 2) {
                loadOrders("Completed");
            }
        } else {
            System.out.println("PurchaseOrder: Current user is null.");
             itemsNeedingReorderTableModel.setRowCount(0);
             pendingOrdersTableModel.setRowCount(0);
             completedOrdersTableModel.setRowCount(0);
        }
    }

    /**
     * Establishes a connection to the database.
     * @return true if connection is successful, false otherwise.
     */
    private boolean connectToDatabase() {
        try {
            conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connected successfully in PurchaseOrder.");
                return true;
            } else {
                System.err.println("Failed to establish database connection in PurchaseOrder.");
                return false;
            }
        } catch (SQLException ex) {
            System.err.println("Database connection error in PurchaseOrder: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Sets up the UI components for the Purchase Order panel.
     * This is a custom initialization method.
     */
    private void setupPurchaseOrderPanel() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new Color(30, 30, 30));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Verdana", Font.BOLD, 14));
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setBackground(new Color(40, 40, 40));
        tabbedPane.setOpaque(true);

        itemsNeedingReorderPanel = createItemsNeedingReorderPanel();
        pendingOrdersPanel = createOrdersPanel("Pending Orders");
        completedOrdersPanel = createOrdersPanel("Completed Orders");

        tabbedPane.addTab("Items Needing Reorder", itemsNeedingReorderPanel);
        tabbedPane.addTab("Pending Orders", pendingOrdersPanel);
        tabbedPane.addTab("Completed Orders", completedOrdersPanel);

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                loadItemsNeedingReorder();
            } else if (selectedIndex == 1) {
                loadOrders("Pending");
            } else if (selectedIndex == 2) {
                loadOrders("Completed");
            }
        });

        this.add(tabbedPane, BorderLayout.CENTER);
    }


    /**
     * Creates the panel for displaying items needing reorder.
     * Includes the global select-all checkbox and the table.
     * @return The JPanel for items needing reorder.
     */
    private JPanel createItemsNeedingReorderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Added "Unit Price" column to fetch price for calculating total amount during PO creation
        String[] columns = {"Select", "Item ID", "Item Name", "Category", "Current Qty", "Reorder Level", "Suggested Order Qty", "Unit Price"};
        itemsNeedingReorderTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                 if (columnIndex == 7) { // Unit Price column
                    return BigDecimal.class;
                }
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Only the "Select" column is editable
            }
        };

        itemsNeedingReorderTable = new JTable(itemsNeedingReorderTableModel);
        itemsNeedingReorderTable.setForeground(Color.WHITE);
        itemsNeedingReorderTable.setBackground(new Color(40, 40, 40));
        itemsNeedingReorderTable.setGridColor(new Color(60, 60, 60));
        itemsNeedingReorderTable.setSelectionBackground(new Color(51, 153, 255));
        itemsNeedingReorderTable.setSelectionForeground(Color.WHITE);
        itemsNeedingReorderTable.setFont(new Font("Verdana", Font.PLAIN, 12));
        itemsNeedingReorderTable.setRowHeight(25);
        itemsNeedingReorderTable.setAutoCreateRowSorter(true);
        itemsNeedingReorderTable.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 12));
        itemsNeedingReorderTable.getTableHeader().setBackground(new Color(50, 50, 50));
        itemsNeedingReorderTable.getTableHeader().setForeground(Color.WHITE);
        itemsNeedingReorderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        itemsNeedingReorderTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        itemsNeedingReorderTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        itemsNeedingReorderTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        itemsNeedingReorderTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        itemsNeedingReorderTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        itemsNeedingReorderTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        itemsNeedingReorderTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        itemsNeedingReorderTable.getColumnModel().getColumn(7).setPreferredWidth(80); // Set width for Unit Price


        JScrollPane scrollPane = new JScrollPane(itemsNeedingReorderTable);
        scrollPane.getViewport().setBackground(new Color(40, 40, 40));

        // Panel for the global checkbox and table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);

        // Add global select-all checkbox
        JPanel selectAllPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectAllPanel.setOpaque(false);
        selectAllItemsCheckbox = new JCheckBox("Select All");
        selectAllItemsCheckbox.setFont(new Font("Verdana", Font.BOLD, 12));
        selectAllItemsCheckbox.setForeground(Color.WHITE);
        selectAllItemsCheckbox.setOpaque(false);
        selectAllItemsCheckbox.addItemListener(e -> {
            boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            for (int i = 0; i < itemsNeedingReorderTableModel.getRowCount(); i++) {
                itemsNeedingReorderTableModel.setValueAt(selected, i, 0);
            }
        });
        selectAllPanel.add(selectAllItemsCheckbox);

        tablePanel.add(selectAllPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);


        panel.add(tablePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        createPoButton = new JButton("Create Purchase Order");
        styleButton(createPoButton, new Color(46, 204, 113));
        createPoButton.addActionListener(e -> createPurchaseOrder());
        buttonPanel.add(createPoButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates a generic panel for displaying lists of purchase orders.
     * Includes a "Print PO" button column.
     * @param title The title for the panel's border.
     * @return The JPanel for displaying orders.
     */
    private JPanel createOrdersPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns;
        if (title.equals("Pending Orders")) {
            // Added "Print PO" column header
            columns = new String[]{"PO ID", "PO Number", "PO Date", "Status", "Total Amount", "Print PO"};
        } else { // Completed Orders
            // Added "Print PO" column header
            columns = new String[]{"PO ID", "PO Number", "PO Date", "Status", "Total Amount", "Print PO"};
        }

        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
             @Override
            public boolean isCellEditable(int row, int column) {
                 // Only the "Print PO" column is editable (to trigger button click)
                 return column == getColumnIndex("Print PO", this);
            }

             @Override
            public Class<?> getColumnClass(int columnIndex) {
                 if (columnIndex == getColumnIndex("Total Amount", this)) {
                    return BigDecimal.class;
                }
                 if (columnIndex == getColumnIndex("Print PO", this)) {
                     return JButton.class; // Indicate this column contains buttons
                 }
                return super.getColumnClass(columnIndex);
            }
        };
        JTable ordersTable = new JTable(tableModel);
        ordersTable.setForeground(Color.WHITE);
        ordersTable.setBackground(new Color(40, 40, 40));
        ordersTable.setGridColor(new Color(60, 60, 60));
        ordersTable.setSelectionBackground(new Color(51, 153, 255));
        ordersTable.setSelectionForeground(Color.WHITE);
        ordersTable.setFont(new Font("Verdana", Font.PLAIN, 12));
        ordersTable.setRowHeight(25);
        ordersTable.setAutoCreateRowSorter(true);
        ordersTable.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 12));
        ordersTable.getTableHeader().setBackground(new Color(50, 50, 50));
        ordersTable.getTableHeader().setForeground(Color.WHITE);
        ordersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(ordersTable);
        scrollPane.getViewport().setBackground(new Color(40, 40, 40));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        if (title.equals("Pending Orders")) {
            pendingOrdersTable = ordersTable;
            pendingOrdersTableModel = tableModel;

            viewPendingPoButton = new JButton("View Details");
            styleButton(viewPendingPoButton, new Color(52, 152, 219));
            viewPendingPoButton.addActionListener(e -> viewPurchaseOrderDetails(pendingOrdersTable));
            buttonPanel.add(viewPendingPoButton);

             // Setup the Print PO button column for Pending Orders
            setupPrintPoButtonColumn(pendingOrdersTable, pendingOrdersTableModel);


        } else if (title.equals("Completed Orders")) {
            completedOrdersTable = ordersTable;
            completedOrdersTableModel = tableModel;

            viewCompletedPoButton = new JButton("View Details");
            styleButton(viewCompletedPoButton, new Color(52, 152, 219));
            viewCompletedPoButton.addActionListener(e -> viewPurchaseOrderDetails(completedOrdersTable));
            buttonPanel.add(viewCompletedPoButton);

            // Setup the Print PO button column for Completed Orders
            setupPrintPoButtonColumn(completedOrdersTable, completedOrdersTableModel);
        }

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
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
     * Sets up the "Print PO" button column for a given table.
     * @param table The JTable to add the button column to.
     * @param model The DefaultTableModel for the table.
     */
    private void setupPrintPoButtonColumn(JTable table, DefaultTableModel model) {
        // Find the index of the "Print PO" column
        int printColumnIndex = getColumnIndex("Print PO", model);
        if (printColumnIndex == -1) {
            System.err.println("Error: 'Print PO' column not found in table model.");
            return; // Should not happen if columns are defined correctly
        }

        // Set up the button renderer and editor
        table.getColumnModel().getColumn(printColumnIndex).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(printColumnIndex).setCellEditor(new ButtonEditor(new JCheckBox()));

        // Add a MouseListener to handle button clicks
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                // Check if the click is within the bounds of the button column
                if (column == printColumnIndex && row >= 0) {
                    // Manually trigger the editor to activate the button's action
                    table.getCellEditor(row, column).stopCellEditing();
                }
            }
        });
    }

     /**
     * Custom TableCellRenderer for rendering a JButton in a JTable cell.
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setForeground(Color.WHITE);
            setBackground(new Color(52, 152, 219)); // Blue color for Print button
            setFont(new Font("Verdana", Font.BOLD, 12));
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getBackground().darker(), 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
            ));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Print"); // Set button text
            return this;
        }
    }

    /**
     * Custom TableCellEditor for handling clicks on a JButton in a JTable cell.
     */
    class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JButton button;
        private int poId; // To store the PO ID of the row being edited

        public ButtonEditor(JCheckBox checkBox) {
            button = new JButton();
            button.setOpaque(true);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(52, 152, 219)); // Blue color for Print button
            button.setFont(new Font("Verdana", Font.BOLD, 12));
             button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(button.getBackground().darker(), 1),
                BorderFactory.createEmptyBorder(3, 8, 3, 8)
            ));
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            // Get the PO ID from the first column of the current row
            poId = (Integer) table.getModel().getValueAt(row, getColumnIndex("PO ID", (DefaultTableModel) table.getModel()));
            button.setText("Print");
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Print"; // Return button text or any indicator
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
             // Make the cell editable only on mouse click
            if (anEvent instanceof MouseEvent) {
                return ((MouseEvent) anEvent).getClickCount() >= 1;
            }
            return true;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // This method is called when the button is clicked
            fireEditingStopped(); // Stop editing immediately after click
            // Call the print action for the stored PO ID
            printPurchaseOrder(poId);
        }
    }


    /**
     * Loads items that are below their reorder level (excluding machinery)
     * into the itemsNeedingReorderTable.
     * This method no longer executes the AutoGenerateDraftPOForLowStock stored procedure.
     * It queries for low stock items that are NOT already on any PO that is
     * not yet 'Received' or 'Cancelled'.
     */
    private void loadItemsNeedingReorder() {
        itemsNeedingReorderTableModel.setRowCount(0);
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (currentUser == null) {
             System.err.println("Cannot load items needing reorder: Current user is not set.");
             return;
        }

        // Removed the call to AutoGenerateDraftPOForLowStock stored procedure

        // Updated SQL to exclude items that are on any PO with a status other than 'Received' or 'Cancelled'
        String sql = "SELECT i.ItemID, i.ItemName, c.CategoryName, i.Quantity, i.ReorderLevel, i.UnitPrice " +
                     "FROM Items i " +
                     "LEFT JOIN Categories c ON i.CategoryID = c.CategoryID " +
                     "WHERE i.Quantity <= i.ReorderLevel " + // Filter only by low stock
                     "AND i.IsMachinery = FALSE " +
                     "AND NOT EXISTS ( " +
                     "    SELECT 1 " +
                     "    FROM PurchaseOrderItems poi " +
                     "    JOIN PurchaseOrders po ON poi.POID = po.POID " +
                     "    WHERE poi.ItemID = i.ItemID AND po.Status NOT IN ('Received', 'Cancelled') " +
                     ") " +
                     "ORDER BY i.ItemName";


        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int itemId = rs.getInt("ItemID");
                    String itemName = rs.getString("ItemName");
                    String categoryName = rs.getString("CategoryName");
                    int currentQty = rs.getInt("Quantity");
                    int reorderLevel = rs.getInt("ReorderLevel");
                    BigDecimal unitPrice = rs.getBigDecimal("UnitPrice");
                    // int quantityOnActivePO = rs.getInt("QuantityOnActivePO"); // No longer needed for filtering

                    // Calculate suggested quantity: ReorderLevel - CurrentQty
                    int suggestedQty = reorderLevel - currentQty;

                    // **MODIFICATION:** Set suggestedQty to 5 if current quantity equals reorder level
                    if (currentQty == reorderLevel) {
                        suggestedQty = 5;
                    } else {
                         // Ensure suggestedQty is not negative if below reorder level
                         suggestedQty = Math.max(0, suggestedQty);
                    }


                    // Add all items returned by the SQL query to the table
                    itemsNeedingReorderTableModel.addRow(new Object[]{
                        false, // Default to not selected
                        itemId,
                        itemName,
                        categoryName,
                        currentQty,
                        reorderLevel,
                        suggestedQty, // Use the calculated suggestedQty
                        unitPrice // Add Unit Price
                    });

                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading items needing reorder: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading items needing reorder: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
             // Reset the select all checkbox after loading data
            if (selectAllItemsCheckbox != null) {
                selectAllItemsCheckbox.setSelected(false);
            }
        }
    }

     /**
     * Loads purchase orders based on their status ("Pending" or "Completed").
     * @param statusFilter "Pending" or "Completed".
     */
    private void loadOrders(String statusFilter) {
        DefaultTableModel modelToUse = null;
        String statusClause = "";
        String selectColumns;


        if ("Pending".equals(statusFilter)) {
            modelToUse = pendingOrdersTableModel;
            // Include 'Draft' status in Pending Orders as per user request
            statusClause = "WHERE Status IN ('Draft', 'Pending Approval', 'Approved', 'Ordered', 'Partially Received', 'Pending Reorder')";
            // Added "Print PO" to selectColumns (will be a placeholder)
            selectColumns = "POID, PONumber, PODate, Status, TotalAmount";
        } else if ("Completed".equals(statusFilter)) {
            modelToUse = completedOrdersTableModel;
            statusClause = "WHERE Status IN ('Received', 'Cancelled')";
            // Added "Print PO" to selectColumns (will be a placeholder)
            selectColumns = "POID, PONumber, PODate, Status, TotalAmount";
        } else {
            return;
        }

        if (modelToUse == null) return;

        modelToUse.setRowCount(0);
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Note: The SQL query doesn't fetch a value for "Print PO", as it's a button column.
        // We'll add a placeholder value when populating the model.
        String sql = "SELECT " + selectColumns + " FROM PurchaseOrders " +
                     statusClause + " ORDER BY PODate DESC";

        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                     if ("Pending".equals(statusFilter) || "Completed".equals(statusFilter)) {
                          String status = rs.getString("Status");
                          BigDecimal totalAmount = rs.getBigDecimal("TotalAmount");
                          int poId = rs.getInt("POID"); // Get POID to use for print action

                         modelToUse.addRow(new Object[]{
                            poId, // PO ID
                            rs.getString("PONumber"),
                            rs.getDate("PODate"),
                            status,
                            totalAmount != null ? totalAmount : BigDecimal.ZERO, // Display 0.00 if TotalAmount is null
                            "Print" // Placeholder for the button column
                         });
                     }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading " + statusFilter + " orders: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading " + statusFilter + " orders: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Creates a new purchase order from the selected items in the
     * itemsNeedingReorderTable.
     * Calculates the total amount based on selected items and updates the PO.
     */
    private void createPurchaseOrder() {
        if (currentUser == null) {
             JOptionPane.showMessageDialog(this, "User not logged in. Cannot create PO.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
         if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Integer> selectedRows = new ArrayList<>();
        for (int i = 0; i < itemsNeedingReorderTableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) itemsNeedingReorderTableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                selectedRows.add(i);
            }
        }

        if (selectedRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No items selected to create a purchase order.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lists to hold item details for the new PO
        List<Integer> itemIds = new ArrayList<>();
        List<String> itemDescriptions = new ArrayList<>();
        List<Integer> quantitiesOrdered = new ArrayList<>();
        List<BigDecimal> itemUnitPrices = new ArrayList<>(); // To calculate total amount

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Flag to check if any selected item has a quantity to order > 0
        boolean hasItemsToOrder = false;

        for (int rowIndex : selectedRows) {
             try {
                int itemId = (Integer) itemsNeedingReorderTableModel.getValueAt(rowIndex, 1);
                String itemName = (String) itemsNeedingReorderTableModel.getValueAt(rowIndex, 2);
                // Get the suggested quantity as displayed in the table
                int suggestedQty = (Integer) itemsNeedingReorderTableModel.getValueAt(rowIndex, 6);
                BigDecimal unitPrice = (BigDecimal) itemsNeedingReorderTableModel.getValueAt(rowIndex, 7); // Get Unit Price

                // Use the suggested quantity from the table as the quantity to order
                int quantityToOrder = suggestedQty;

                // Only add item to the lists if quantityTo order is greater than 0
                if (quantityToOrder > 0) {
                    itemIds.add(itemId);
                    itemDescriptions.add(itemName);
                    quantitiesOrdered.add(quantityToOrder);
                    itemUnitPrices.add(unitPrice);

                    // Calculate subtotal for this item and add to total amount
                    BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantityToOrder));
                    totalAmount = totalAmount.add(subtotal);

                    hasItemsToOrder = true; // Mark that at least one item has quantity > 0
                }


            } catch (ClassCastException e) {
                 System.err.println("Error casting value from selected table row " + rowIndex + ": " + e.getMessage());
                 JOptionPane.showMessageDialog(this, "Error reading selected item data.", "Data Error", JOptionPane.ERROR_MESSAGE);
                 return;
            } catch (NullPointerException e) {
                 System.err.println("Null value encountered for item in row " + rowIndex + ": " + e.getMessage());
                 JOptionPane.showMessageDialog(this, "Missing data for a selected item.", "Data Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }
        }

         // If no selected items have a quantity to order > 0, show a message and stop
         if (!hasItemsToOrder) {
             JOptionPane.showMessageDialog(this, "Selected items have a suggested order quantity of 0. No purchase order will be created.", "No Items to Order", JOptionPane.WARNING_MESSAGE);
             // Optionally, you could clear selections and refresh here
             if (selectAllItemsCheckbox != null) {
                selectAllItemsCheckbox.setSelected(false);
            }
             loadItemsNeedingReorder(); // Refresh the list
             return;
         }


        try {
            conn.setAutoCommit(false); // Start transaction

            // Insert the new Purchase Order
            String insertPoSql = "INSERT INTO PurchaseOrders (PONumber, PODate, Status, CreatedBy, TotalAmount) VALUES (?, ?, ?, ?, ?)";
            int poId = -1;
            try (PreparedStatement pstmtPo = conn.prepareStatement(insertPoSql, Statement.RETURN_GENERATED_KEYS)) {

                String poNumber = generatePONumber();
                LocalDate currentDate = LocalDate.now();

                pstmtPo.setString(1, poNumber);
                pstmtPo.setDate(2, java.sql.Date.valueOf(currentDate));
                pstmtPo.setString(3, "Draft"); // Create as Draft initially
                pstmtPo.setInt(4, currentUser.getUserId());
                pstmtPo.setBigDecimal(5, totalAmount); // Set the calculated total amount

                int affectedRows = pstmtPo.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating purchase order failed, no rows affected.");
                }

                try (ResultSet generatedKeys = pstmtPo.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        poId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating purchase order failed, no ID obtained.");
                    }
                }
            }

            // Insert the selected items into PurchaseOrderItems
            String insertPoItemSql = "INSERT INTO PurchaseOrderItems (POID, ItemID, Description, QuantityOrdered) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmtPoItem = conn.prepareStatement(insertPoItemSql)) {
                for (int i = 0; i < itemIds.size(); i++) {
                     // Only add items to PO items if the quantity to order is > 0
                     if (quantitiesOrdered.get(i) > 0) { // This check is redundant now but good practice
                         pstmtPoItem.setInt(1, poId);
                         pstmtPoItem.setInt(2, itemIds.get(i));
                         pstmtPoItem.setString(3, itemDescriptions.get(i));
                         pstmtPoItem.setInt(4, quantitiesOrdered.get(i));
                         pstmtPoItem.addBatch();
                     }
                }
                pstmtPoItem.executeBatch();
            }

            conn.commit(); // Commit the transaction

            JOptionPane.showMessageDialog(this, "Purchase Order " + generatePONumber() + " created successfully as Draft.", "Success", JOptionPane.INFORMATION_MESSAGE); // Use generated number

            // **MODIFICATION:** Remove selected items from the "Items Needing Reorder" table model
            // Iterate backwards to avoid index issues when removing rows
            // Get the actual row indices in the view before removing
            int[] selectedViewRows = itemsNeedingReorderTable.getSelectedRows();
            // Convert view rows to model rows and sort in descending order
            List<Integer> selectedModelRows = new ArrayList<>();
            for(int viewRow : selectedViewRows) {
                selectedModelRows.add(itemsNeedingReorderTable.convertRowIndexToModel(viewRow));
            }
            selectedModelRows.sort((a, b) -> b.compareTo(a)); // Sort descending

            // Remove rows from the model
            for (int modelRowIndex : selectedModelRows) {
                 itemsNeedingReorderTableModel.removeRow(modelRowIndex);
            }


            // Reset the select all checkbox after creating PO
            if (selectAllItemsCheckbox != null) {
                selectAllItemsCheckbox.setSelected(false);
            }


            // Refresh the Pending Orders tab
            tabbedPane.setSelectedIndex(1);
            loadOrders("Pending");

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback transaction on error
                }
            } catch (SQLException ex) {
                System.err.println("Error during transaction rollback: " + ex.getMessage());
            }
            System.err.println("Database error during PO creation: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating Purchase Order: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
             System.err.println("An unexpected error occurred during PO creation: " + e.getMessage());
             e.printStackTrace();
             JOptionPane.showMessageDialog(this, "An unexpected error occurred during Purchase Order creation.", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
             try {
                if (conn != null) {
                    conn.setAutoCommit(true); // Restore auto-commit
                }
            } catch (SQLException ex) {
                System.err.println("Error restoring auto-commit: " + ex.getMessage());
            }
        }
    }

    /**
     * Generates a unique Purchase Order number.
     * Example format: PO-Manual-YYYYMMDD-Sequence
     * @return A unique PO number string.
     */
    private String generatePONumber() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = today.format(formatter);

        // Using a simple timestamp sequence. For a production system,
        // a more robust sequence generator (e.e., from the database) is recommended.
        long sequence = System.currentTimeMillis() % 100000; // Increased sequence range

        return "PO-" + datePart + "-" + sequence; // Simplified format
    }


    /**
     * Opens a dialog or panel to view the details of a selected purchase order.
     * This method will now open a PurchaseOrderDetailsDialog.
     * @param table The JTable containing the purchase orders (pending or completed).
     */
    private void viewPurchaseOrderDetails(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        // Get PO ID from the first column (index 0)
        int poId = (Integer) table.getModel().getValueAt(modelRow, 0);


        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);

        PurchaseOrderDetailsDialog detailsDialog = new PurchaseOrderDetailsDialog(parentFrame, true, conn, currentUser, poId);
        detailsDialog.setPurchaseOrderActionListener(this); // Set the listener
        detailsDialog.setVisible(true);

        // The refresh is now handled by the purchaseOrderChanged() method
        // loadOrders("Pending");
        // loadOrders("Completed");
        // loadItemsNeorder();
    }

    /**
     * Generates the printable content for a Purchase Order.
     * Fetches details from the database and formats them into a string.
     * @param poId The ID of the purchase order to generate content for.
     * @return A formatted string containing the purchase order details.
     */
    private String generatePurchaseOrderContent(int poId) {
        StringBuilder poContent = new StringBuilder();
        poContent.append("========================================\n");
        poContent.append("         AssetWise Academia Warehouse\n");
        poContent.append("            Purchase Order\n");
        poContent.append("========================================\n");

        String poDetailsSql = "SELECT PONumber, PODate, Status, TotalAmount, CreatedBy FROM PurchaseOrders WHERE POID = ?";
        String poItemsSql = "SELECT poi.ItemID, poi.Description, poi.QuantityOrdered, i.UnitPrice, (poi.QuantityOrdered * i.UnitPrice) AS Subtotal " +
                            "FROM PurchaseOrderItems poi JOIN Items i ON poi.ItemID = i.ItemID WHERE poi.POID = ?";

        try (PreparedStatement pstmtPo = conn.prepareStatement(poDetailsSql)) {
            pstmtPo.setInt(1, poId);
            try (ResultSet rsPo = pstmtPo.executeQuery()) {
                if (rsPo.next()) {
                    poContent.append(String.format("%-15s: %s\n", "PO Number", rsPo.getString("PONumber")));
                    poContent.append(String.format("%-15s: %s\n", "PO Date", rsPo.getDate("PODate")));
                    poContent.append(String.format("%-15s: %s\n", "Status", rsPo.getString("Status")));
                    poContent.append(String.format("%-15s: %s\n", "Created By", rsPo.getInt("CreatedBy"))); // Assuming CreatedBy is UserID
                    BigDecimal totalAmount = rsPo.getBigDecimal("TotalAmount");
                    poContent.append(String.format("%-15s: %s\n", "Total Amount", totalAmount != null ? totalAmount.toPlainString() : "0.00"));
                    poContent.append("----------------------------------------\n");
                } else {
                    poContent.append("Purchase Order details not found for ID: ").append(poId).append("\n");
                    return poContent.toString();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating PO content (details): " + e.getMessage());
            poContent.append("Error loading PO details.\n");
            return poContent.toString();
        }

        poContent.append(String.format("%-10s %-25s %-10s %-10s %-10s\n", "Item ID", "Description", "Qty", "Unit Price", "Subtotal"));
        poContent.append("------------------------------------------------------------------------\n");

        try (PreparedStatement pstmtItems = conn.prepareStatement(poItemsSql)) {
            pstmtItems.setInt(1, poId);
            try (ResultSet rsItems = pstmtItems.executeQuery()) {
                while (rsItems.next()) {
                    int itemId = rsItems.getInt("ItemID");
                    String description = rsItems.getString("Description");
                    int quantityOrdered = rsItems.getInt("QuantityOrdered");
                    BigDecimal unitPrice = rsItems.getBigDecimal("UnitPrice");
                    BigDecimal subtotal = rsItems.getBigDecimal("Subtotal");

                    poContent.append(String.format("%-10d %-25s %-10d %-10.2f %-10.2f\n",
                                                   itemId,
                                                   description,
                                                   quantityOrdered,
                                                   unitPrice != null ? unitPrice : BigDecimal.ZERO,
                                                   subtotal != null ? subtotal : BigDecimal.ZERO));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating PO content (items): " + e.getMessage());
            poContent.append("Error loading PO items.\n");
        }

        poContent.append("========================================\n");

        return poContent.toString();
    }


    /**
     * Implements the printing functionality for a purchase order.
     * Uses the content generated by generatePurchaseOrderContent.
     * @param poId The ID of the purchase order to print.
     */
    private void printPurchaseOrder(int poId) {
        String poContent = generatePurchaseOrderContent(poId);

        // Use a JTextArea to hold the content for printing
        JTextArea printableArea = new JTextArea(poContent);
        printableArea.setFont(new Font("Courier New", Font.PLAIN, 12)); // Use a monospaced font

        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                // Print the content of the JTextArea
                printableArea.printAll(g2d);

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
        }
    }


    // Implement the PurchaseOrderActionListener interface method
    @Override
    public void purchaseOrderChanged() {
        System.out.println("Purchase order status changed. Refreshing tables.");
        loadOrders("Pending");
        loadOrders("Completed");
        loadItemsNeedingReorder(); // Also refresh items needing reorder as inventory might change upon receiving
    }


     /**
     * Helper method to get column index by name from a specific table model.
     * @param columnName The name of the column.
     * @param model The DefaultTableModel to search within.
     * @return The index of the column, or -1 if not found.
     */
    private int getColumnIndex(String columnName, DefaultTableModel model) {
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (model.getColumnName(i).equals(columnName)) {
                return i;
            }
        }
        return -1; // Should not happen if column exists
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
