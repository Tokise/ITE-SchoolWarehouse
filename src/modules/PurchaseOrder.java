/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package modules;

import Package1.DBConnection; // Import DBConnection class
import Package1.User; // Import User class

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame; // Import Frame for dialog parent
import java.awt.Graphics; // Import Graphics for printing
import java.awt.Graphics2D; // Import Graphics2D for printing
import java.awt.Component; // Import Component for table cell rendering/editing
import java.awt.event.ActionEvent; // Import ActionEvent for button listeners
import java.awt.event.ActionListener; // Import ActionListener for button listeners
import java.awt.event.ItemEvent; // Import ItemEvent for checkbox listeners
import java.awt.event.MouseAdapter; // Import MouseAdapter for table mouse events
import java.awt.event.MouseEvent; // Import MouseEvent for table mouse events
import java.awt.print.PageFormat; // Import PageFormat for printing
import java.awt.print.Printable; // Import Printable for printing
import java.awt.print.PrinterException; // Import PrinterException for printing
import java.awt.print.PrinterJob; // Import PrinterJob for printing

import java.sql.CallableStatement; // Import CallableStatement (if needed, currently not used)
import java.sql.Connection; // Import Connection
import java.sql.SQLException; // Import SQLException
import java.sql.Statement; // Import Statement
import java.sql.ResultSet; // Import ResultSet
import java.sql.PreparedStatement; // Import PreparedStatement

import java.util.ArrayList; // Import ArrayList
import java.util.List; // Import List
import java.util.Vector; // Import Vector for table model rows
import java.util.EventObject; // Import EventObject for cell editing

import javax.swing.AbstractCellEditor; // Import AbstractCellEditor for custom table editor
import javax.swing.BorderFactory; // Import BorderFactory for borders
import javax.swing.JButton; // Import JButton
import javax.swing.JCheckBox; // Import JCheckBox
import javax.swing.JLabel; // Import JLabel
import javax.swing.JOptionPane; // Import JOptionPane for dialogs
import javax.swing.JPanel; // Import JPanel
import javax.swing.JScrollPane; // Import JScrollPane
import javax.swing.JTabbedPane; // Import JTabbedPane
import javax.swing.JTable; // Import JTable
import javax.swing.JTextArea; // Import JTextArea for printing content
import javax.swing.ListSelectionModel; // Import ListSelectionModel for table selection mode
import javax.swing.SwingUtilities; // Import SwingUtilities for getting window ancestor
import javax.swing.table.DefaultTableModel; // Import DefaultTableModel
import javax.swing.table.TableCellRenderer; // Import TableCellRenderer for custom table renderer
import javax.swing.table.TableCellEditor; // Import TableCellEditor for custom table editor
import javax.swing.SwingConstants; // Import SwingConstants for alignment
import javax.swing.UIManager; // Import UIManager for button styling
import javax.swing.JTextField; // Import JTextField for search

import java.time.LocalDate; // Import LocalDate for dates
import java.time.format.DateTimeFormatter; // Import DateTimeFormatter for date formatting
import java.math.BigDecimal; // Import BigDecimal for currency calculations
import javax.swing.event.CellEditorListener; // Import CellEditorListener

// Import the PurchaseOrderDetailsDialog class from the modules package
// This assumes PurchaseOrderDetailsDialog.java is in the same 'modules' package
import modules.PurchaseOrderDetailsDialog;


/**
 * JPanel for managing Purchase Orders.
 * Includes sections for items needing reorder, pending POs, and completed POs.
 */
public class PurchaseOrder extends javax.swing.JPanel implements PurchaseOrderDetailsDialog.PurchaseOrderActionListener { // Implement the listener interface

    private Connection conn = null;
    private User currentUser;

    private JTabbedPane tabbedPane;
    private JPanel itemsNeedingReorderPanel;
    private JPanel machineryNeedingReorderPanel; // New panel for machinery
    private JPanel pendingOrdersPanel;
    private JPanel completedOrdersPanel;

    private JTable itemsNeedingReorderTable;
    private DefaultTableModel itemsNeedingReorderTableModel;
    private JButton createPoButton;
    private JCheckBox selectAllItemsCheckbox; // Global checkbox for items needing reorder
    // Pagination controls for Items Needing Reorder
    private JButton itemsPrevButton;
    private JButton itemsNextButton;
    private JLabel itemsPageLabel;
    private int currentItemsPage = 1;
    private int totalItems = 0;
    private final int ITEMS_PER_PAGE = 10;


    private JTable machineryNeedingReorderTable; // New table for machinery
    private DefaultTableModel machineryNeedingReorderTableModel; // New model for machinery
    private JButton createMachineryPoButton; // Button for creating PO for machinery
    private JCheckBox selectAllMachineryCheckbox; // Global checkbox for machinery
    // Pagination controls for Machinery Needing Reorder
    private JButton machineryPrevButton;
    private JButton machineryNextButton;
    private JLabel machineryPageLabel;
    private int currentMachineryPage = 1;
    private int totalMachinery = 0;
    private final int MACHINERY_PER_PAGE = 10;


    private JTable pendingOrdersTable;
    private DefaultTableModel pendingOrdersTableModel;
    private JButton viewPendingPoButton;
    // New buttons for batch actions on Pending Orders
    private JButton approveSelectedButton;
    private JButton receiveSelectedButton;
    private JCheckBox selectAllPendingCheckbox; // Global checkbox for pending orders
    // Pagination controls for Pending Orders
    private JButton pendingPrevButton;
    private JButton pendingNextButton;
    private JLabel pendingPageLabel;
    private int currentPendingPage = 1;
    private int totalPendingOrders = 0;
    private final int PENDING_ORDERS_PER_PAGE = 10;
    // Search controls for Pending Orders
    private JTextField pendingSearchField;
    private JButton pendingSearchButton;
    private String currentPendingSearchText = "";


    private JTable completedOrdersTable;
    private DefaultTableModel completedOrdersTableModel;
    private JButton viewCompletedPoButton;
    // Pagination controls for Completed Orders
    private JButton completedPrevButton;
    private JButton completedNextButton;
    private JLabel completedPageLabel;
    private int currentCompletedPage = 1;
    private int totalCompletedOrders = 0;
    private final int COMPLETED_ORDERS_PER_PAGE = 10;
    // Search controls for Completed Orders
    private JTextField completedSearchField;
    private JButton completedSearchButton;
    private String currentCompletedSearchText = "";


    // Define a buffer quantity to order above the reorder level
    private static final int REORDER_BUFFER = 5;
    // Define a minimum quantity to order if below or at reorder level
    private static final int MIN_ORDER_QUANTITY = 5;
    // private String poNumber; // This field is not used and can be removed
    private String poNumber;


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
             // Load data for the currently selected tab after user is set
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                loadItemsNeedingReorder();
            } else if (selectedIndex == 1) { // Index 1 will now be Machinery
                 loadMachineryNeedingReorder();
            } else if (selectedIndex == 2) { // Index 2 will now be Pending Orders
                currentPendingSearchText = ""; // Clear search on tab change
                pendingSearchField.setText(""); // Clear search field
                loadOrders("Pending");
            } else if (selectedIndex == 3) { // Index 3 will now be Completed Orders
                currentCompletedSearchText = ""; // Clear search on tab change
                completedSearchField.setText(""); // Clear search field
                loadOrders("Completed");
            }
            // Update button visibility based on user role
            updateButtonVisibility();
        } else {
            System.out.println("PurchaseOrder: Current user is null.");
             itemsNeedingReorderTableModel.setRowCount(0);
             machineryNeedingReorderTableModel.setRowCount(0); // Clear machinery table
             pendingOrdersTableModel.setRowCount(0);
             completedOrdersTableModel.setRowCount(0);
             // Hide all action buttons if no user is logged in
             createPoButton.setVisible(false);
             createMachineryPoButton.setVisible(false);
             approveSelectedButton.setVisible(false);
             receiveSelectedButton.setVisible(false);
             viewPendingPoButton.setVisible(false);
             viewCompletedPoButton.setVisible(false);

             // Hide pagination controls if no user
             updatePaginationControlsVisibility();
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
        machineryNeedingReorderPanel = createMachineryNeedingReorderPanel(); // Create machinery panel
        pendingOrdersPanel = createOrdersPanel("Pending Orders");
        completedOrdersPanel = createOrdersPanel("Completed Orders");

        tabbedPane.addTab("Items Needing Reorder", itemsNeedingReorderPanel);
        tabbedPane.addTab("Machinery Needing Reorder", machineryNeedingReorderPanel); // Add new tab
        tabbedPane.addTab("Pending Orders", pendingOrdersPanel);
        tabbedPane.addTab("Completed Orders", completedOrdersPanel);

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                currentItemsPage = 1; // Reset page on tab change
                loadItemsNeedingReorder();
            } else if (selectedIndex == 1) {
                 currentMachineryPage = 1; // Reset page on tab change
                 loadMachineryNeedingReorder(); // Load machinery when tab is selected
            } else if (selectedIndex == 2) {
                currentPendingPage = 1; // Reset page on tab change
                currentPendingSearchText = ""; // Clear search on tab change
                pendingSearchField.setText(""); // Clear search field
                loadOrders("Pending");
            } else if (selectedIndex == 3) {
                currentCompletedPage = 1; // Reset page on tab change
                currentCompletedSearchText = ""; // Clear search on tab change
                completedSearchField.setText(""); // Clear search field
                loadOrders("Completed");
            }
            // Update button visibility when tab changes
            updateButtonVisibility();
            updatePaginationControlsVisibility();
        });

        this.add(tabbedPane, BorderLayout.CENTER);
    }


    /**
     * Creates the panel for displaying items needing reorder (excluding machinery).
     * Includes the global select-all checkbox, the table, and pagination controls.
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
        styleTable(itemsNeedingReorderTable); // Apply consistent table styling

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

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        createPoButton = new JButton("Create Purchase Order");
        styleButton(createPoButton, new Color(46, 204, 113));
        createPoButton.addActionListener(e -> createPurchaseOrder()); // This button creates PO for non-machinery
        buttonPanel.add(createPoButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // Pagination controls for Items Needing Reorder
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setOpaque(false);
        itemsPrevButton = new JButton("Previous");
        styleButton(itemsPrevButton, new Color(52, 152, 219));
        itemsPrevButton.addActionListener(e -> {
            if (currentItemsPage > 1) {
                currentItemsPage--;
                loadItemsNeedingReorder();
            }
        });
        paginationPanel.add(itemsPrevButton);

        itemsPageLabel = new JLabel("Page 1 of 1");
        itemsPageLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
        itemsPageLabel.setForeground(Color.WHITE);
        paginationPanel.add(itemsPageLabel);

        itemsNextButton = new JButton("Next");
        styleButton(itemsNextButton, new Color(52, 152, 219));
        itemsNextButton.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
            if (currentItemsPage < totalPages) {
                currentItemsPage++;
                loadItemsNeedingReorder();
            }
        });
        paginationPanel.add(itemsNextButton);
        bottomPanel.add(paginationPanel, BorderLayout.CENTER);


        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }


     /**
     * Creates the panel for displaying machinery items needing reorder.
     * Includes the global select-all checkbox, the table, and pagination controls.
     * @return The JPanel for machinery needing reorder.
     */
    private JPanel createMachineryNeedingReorderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Added "Unit Price" column
        String[] columns = {"Select", "Item ID", "Item Name", "Category", "Current Qty", "Reorder Level", "Suggested Order Qty", "Unit Price"};
        machineryNeedingReorderTableModel = new DefaultTableModel(columns, 0) {
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

        machineryNeedingReorderTable = new JTable(machineryNeedingReorderTableModel);
        styleTable(machineryNeedingReorderTable); // Apply consistent table styling

        machineryNeedingReorderTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        machineryNeedingReorderTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        machineryNeedingReorderTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        machineryNeedingReorderTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        machineryNeedingReorderTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        machineryNeedingReorderTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        machineryNeedingReorderTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        machineryNeedingReorderTable.getColumnModel().getColumn(7).setPreferredWidth(80); // Set width for Unit Price


        JScrollPane scrollPane = new JScrollPane(machineryNeedingReorderTable);
        scrollPane.getViewport().setBackground(new Color(40, 40, 40));

        // Panel for the global checkbox and table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);

        // Add global select-all checkbox for machinery
        JPanel selectAllPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectAllPanel.setOpaque(false);
        selectAllMachineryCheckbox = new JCheckBox("Select All");
        selectAllMachineryCheckbox.setFont(new Font("Verdana", Font.BOLD, 12));
        selectAllMachineryCheckbox.setForeground(Color.WHITE);
        selectAllMachineryCheckbox.setOpaque(false);
        selectAllMachineryCheckbox.addItemListener(e -> {
            boolean selected = e.getStateChange() == ItemEvent.SELECTED;
            for (int i = 0; i < machineryNeedingReorderTableModel.getRowCount(); i++) {
                machineryNeedingReorderTableModel.setValueAt(selected, i, 0);
            }
        });
        selectAllPanel.add(selectAllMachineryCheckbox);

        tablePanel.add(selectAllPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);


        panel.add(tablePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        createMachineryPoButton = new JButton("Create Purchase Order");
        styleButton(createMachineryPoButton, new Color(46, 204, 113));
        createMachineryPoButton.addActionListener(e -> createMachineryPurchaseOrder()); // This button creates PO for machinery
        buttonPanel.add(createMachineryPoButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // Pagination controls for Machinery Needing Reorder
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setOpaque(false);
        machineryPrevButton = new JButton("Previous");
        styleButton(machineryPrevButton, new Color(52, 152, 219));
        machineryPrevButton.addActionListener(e -> {
            if (currentMachineryPage > 1) {
                currentMachineryPage--;
                loadMachineryNeedingReorder();
            }
        });
        paginationPanel.add(machineryPrevButton);

        machineryPageLabel = new JLabel("Page 1 of 1");
        machineryPageLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
        machineryPageLabel.setForeground(Color.WHITE);
        paginationPanel.add(machineryPageLabel);

        machineryNextButton = new JButton("Next");
        styleButton(machineryNextButton, new Color(52, 152, 219));
        machineryNextButton.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) totalMachinery / MACHINERY_PER_PAGE);
            if (currentMachineryPage < totalPages) {
                currentMachineryPage++;
                loadMachineryNeedingReorder(); // Corrected typo here
            }
        });
        paginationPanel.add(machineryNextButton);
        bottomPanel.add(paginationPanel, BorderLayout.CENTER);


        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }


    /**
     * Creates a generic panel for displaying lists of purchase orders.
     * Includes a "Print PO" button column, select-all checkbox, batch action buttons, and pagination controls.
     * Also includes search functionality.
     * @param title The title for the panel's border.
     * @return The JPanel for displaying orders.
     */
    private JPanel createOrdersPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns;
        DefaultTableModel tableModel;
        JTable ordersTable;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JLabel pageLabel;
        JButton prevButton;
        JButton nextButton;
        int itemsPerPage;
        JTextField searchField;
        JButton searchButton;


        if (title.equals("Pending Orders")) {
            // Added "Select" column and "Print PO" column header
            columns = new String[]{"Select", "PO ID", "PO Number", "PO Date", "Status", "Total Amount", "Print PO"};
             tableModel = new DefaultTableModel(columns, 0) {
                 @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) { // Select column
                        return Boolean.class;
                    }
                     if (columnIndex == getColumnIndex("Total Amount", this)) {
                        return BigDecimal.class;
                    }
                     if (columnIndex == getColumnIndex("Print PO", this)) {
                         return JButton.class; // Indicate this column contains buttons
                     }
                    return super.getColumnClass(columnIndex);
                }

                @Override
                public boolean isCellEditable(int row, int column) {
                     // Only the "Select" and "Print PO" columns are editable
                     return column == 0 || column == getColumnIndex("Print PO", this);
                }
            };
            ordersTable = new JTable(tableModel);
            pendingOrdersTable = ordersTable;
            pendingOrdersTableModel = tableModel;
            pageLabel = pendingPageLabel = new JLabel("Page 1 of 1");
            prevButton = pendingPrevButton = new JButton("Previous");
            nextButton = pendingNextButton = new JButton("Next");
            itemsPerPage = PENDING_ORDERS_PER_PAGE;
            searchField = pendingSearchField = new JTextField(20);
            searchButton = pendingSearchButton = new JButton("Search");


            // Allow multiple selections for Pending Orders
            pendingOrdersTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            styleTable(pendingOrdersTable); // Apply consistent table styling


            // Add global select-all checkbox for Pending Orders
            JPanel selectAllPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            selectAllPanel.setOpaque(false);
            selectAllPendingCheckbox = new JCheckBox("Select All");
            selectAllPendingCheckbox.setFont(new Font("Verdana", Font.BOLD, 12));
            selectAllPendingCheckbox.setForeground(Color.WHITE);
            selectAllPendingCheckbox.setOpaque(false);
            selectAllPendingCheckbox.addItemListener(e -> {
                boolean selected = e.getStateChange() == ItemEvent.SELECTED;
                for (int i = 0; i < pendingOrdersTableModel.getRowCount(); i++) {
                    pendingOrdersTableModel.setValueAt(selected, i, 0);
                }
            });
            selectAllPanel.add(selectAllPendingCheckbox);

            // Add batch action buttons
            approveSelectedButton = new JButton("Approve Selected");
            styleButton(approveSelectedButton, new Color(52, 152, 219)); // Blue color
            approveSelectedButton.addActionListener(e -> approveSelectedPOs());
            buttonPanel.add(approveSelectedButton);

            receiveSelectedButton = new JButton("Receive Selected");
            styleButton(receiveSelectedButton, new Color(46, 204, 113)); // Green color
            receiveSelectedButton.addActionListener(e -> receiveSelectedPOs());
            buttonPanel.add(receiveSelectedButton);

            viewPendingPoButton = new JButton("View Details");
            styleButton(viewPendingPoButton, new Color(52, 152, 219));
            viewPendingPoButton.addActionListener(e -> viewPurchaseOrderDetails(pendingOrdersTable));
            buttonPanel.add(viewPendingPoButton);

            // Panel for the global checkbox and table for Pending Orders
            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setOpaque(false);
            tablePanel.add(selectAllPanel, BorderLayout.NORTH);
            JScrollPane scrollPane = new JScrollPane(pendingOrdersTable);
            scrollPane.getViewport().setBackground(new Color(40, 40, 40));
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            panel.add(tablePanel, BorderLayout.CENTER);

             // Setup the Print PO button column for Pending Orders
            setupPrintPoButtonColumn(pendingOrdersTable, pendingOrdersTableModel);


        } else { // Completed Orders
            // Added "Print PO" column header
            columns = new String[]{"PO ID", "PO Number", "PO Date", "Status", "Total Amount", "Print PO"};
             tableModel = new DefaultTableModel(columns, 0) {
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
            ordersTable = new JTable(tableModel);
            completedOrdersTable = ordersTable;
            completedOrdersTableModel = tableModel;
            pageLabel = completedPageLabel = new JLabel("Page 1 of 1");
            prevButton = completedPrevButton = new JButton("Previous");
            nextButton = completedNextButton = new JButton("Next");
            itemsPerPage = COMPLETED_ORDERS_PER_PAGE;
            searchField = completedSearchField = new JTextField(20);
            searchButton = completedSearchButton = new JButton("Search");


            // Completed Orders table does not support multiple selection or batch actions
            completedOrdersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            styleTable(completedOrdersTable); // Apply consistent table styling


            JScrollPane scrollPane = new JScrollPane(completedOrdersTable);
            scrollPane.getViewport().setBackground(new Color(40, 40, 40));
            panel.add(scrollPane, BorderLayout.CENTER);


            viewCompletedPoButton = new JButton("View Details");
            styleButton(viewCompletedPoButton, new Color(52, 152, 219));
            viewCompletedPoButton.addActionListener(e -> viewPurchaseOrderDetails(completedOrdersTable));
            buttonPanel.add(viewCompletedPoButton);

            // Setup the Print PO button column for Completed Orders
            setupPrintPoButtonColumn(completedOrdersTable, completedOrdersTableModel);
        }

        // Search Panel for Orders
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Verdana", Font.BOLD, 12));
        searchLabel.setForeground(Color.WHITE);
        searchPanel.add(searchLabel);
        searchField.setFont(new Font("Verdana", Font.PLAIN, 12));
        searchPanel.add(searchField);
        styleButton(searchButton, new Color(52, 152, 219)); // Blue color for search button
        searchButton.addActionListener(e -> {
            if (title.equals("Pending Orders")) {
                currentPendingSearchText = pendingSearchField.getText().trim();
                currentPendingPage = 1; // Reset page on search
                loadOrders("Pending");
            } else { // Completed Orders
                currentCompletedSearchText = completedSearchField.getText().trim();
                currentCompletedPage = 1; // Reset page on search
                loadOrders("Completed");
            }
        });
        searchPanel.add(searchButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        if (title.equals("Pending Orders")) {
             // Add select all panel to top panel for Pending Orders
             JPanel selectAllPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             selectAllPanel.setOpaque(false);
             selectAllPanel.add(selectAllPendingCheckbox);
             topPanel.add(selectAllPanel, BorderLayout.WEST);
        }
        topPanel.add(searchPanel, BorderLayout.CENTER); // Add search panel to the center of topPanel


        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

         // Pagination controls for Orders panels
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setOpaque(false);
        pageLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
        pageLabel.setForeground(Color.WHITE);

        styleButton(prevButton, new Color(52, 152, 219));
        prevButton.addActionListener(e -> {
            if (title.equals("Pending Orders")) {
                if (currentPendingPage > 1) {
                    currentPendingPage--;
                    loadOrders("Pending");
                }
            } else { // Completed Orders
                 if (currentCompletedPage > 1) {
                    currentCompletedPage--;
                    loadOrders("Completed");
                }
            }
        });
        paginationPanel.add(prevButton);
        paginationPanel.add(pageLabel);

        styleButton(nextButton, new Color(52, 152, 219));
        nextButton.addActionListener(e -> {
            if (title.equals("Pending Orders")) {
                int totalPages = (int) Math.ceil((double) totalPendingOrders / PENDING_ORDERS_PER_PAGE);
                if (currentPendingPage < totalPages) {
                    currentPendingPage++;
                    loadOrders("Pending");
                }
            } else { // Completed Orders
                 int totalPages = (int) Math.ceil((double) totalCompletedOrders / COMPLETED_ORDERS_PER_PAGE);
                 if (currentCompletedPage < totalPages) {
                    currentCompletedPage++;
                    loadOrders("Completed");
                }
            }
        });
        paginationPanel.add(nextButton);
        bottomPanel.add(paginationPanel, BorderLayout.CENTER);

        // Add the topPanel (containing search and select all for Pending) to the main panel
        panel.add(topPanel, BorderLayout.NORTH);
        // The JScrollPane with the table is already added to BorderLayout.CENTER
        panel.add(bottomPanel, BorderLayout.SOUTH);


        return panel;
    }

     /**
     * Applies consistent styling to a JTable to match the dark theme.
     * @param table The table to style.
     */
    private void styleTable(JTable table) {
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

        // Optional: Add alternating row colors for better readability
        // table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        //     @Override
        //     public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        //         Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        //         if (!isSelected) {
        //             c.setBackground(row % 2 == 0 ? new Color(45, 45, 45) : new Color(50, 50, 50));
        //         }
        //         return c;
        //     }
        // });
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
                    // Convert view row to model row if sorting is enabled
                    int modelRow = table.convertRowIndexToModel(row);
                    // Pass the PO ID to the editor before stopping editing
                    // Ensure the PO ID column index is correct based on the table
                    int poIdColumnIndex = (table == pendingOrdersTable) ? getColumnIndex("PO ID", pendingOrdersTableModel) : getColumnIndex("PO ID", completedOrdersTableModel);
                    if (poIdColumnIndex != -1) {
                         Object poIdValue = table.getModel().getValueAt(modelRow, poIdColumnIndex);
                         if (poIdValue instanceof Integer) {
                             ((ButtonEditor)table.getCellEditor(row, column)).setPoId((Integer) poIdValue);
                             table.getCellEditor(row, column).stopCellEditing();
                         } else {
                             System.err.println("Error: PO ID value is not an Integer at row " + modelRow);
                         }
                    } else {
                         System.err.println("Error: Could not find 'PO ID' column in the table model.");
                    }
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                // Required for the editor to work correctly in some cases
                 int column = table.columnAtPoint(e.getPoint());
                 int row = table.rowAtPoint(e.getPoint());
                 int printColumnIndex = getColumnIndex("Print PO", (DefaultTableModel) table.getModel()); // Get index dynamically
                 if (column == printColumnIndex && row >= 0) {
                      // Ensure the PO ID column index is correct based on the table
                     int poIdColumnIndex = (table == pendingOrdersTable) ? getColumnIndex("PO ID", pendingOrdersTableModel) : getColumnIndex("PO ID", completedOrdersTableModel);
                     if (poIdColumnIndex != -1) {
                         Object poIdValue = table.getModel().getValueAt(table.convertRowIndexToModel(row), poIdColumnIndex);
                          if (poIdValue instanceof Integer) {
                              ((ButtonEditor)table.getCellEditor(row, column)).setPoId((Integer) poIdValue);
                          }
                     }
                      table.getCellEditor(row, column).stopCellEditing(); // Ensure editing stops on press
                 }
            }
        });
    }

     /**
     * Loads purchase orders based on their status ("Pending" or "Completed") and populates the corresponding table.
     * Includes pagination and search logic.
     * @param statusFilter "Pending" or "Completed".
     */
    private void loadOrders(String statusFilter) {
        DefaultTableModel modelToUse = null;
        String statusClause = "";
        String selectColumns;
        JLabel pageLabel;
        int itemsPerPage;
        int currentPage;
        String currentSearchText;
        String countSql;
        String dataSql;


        if ("Pending".equals(statusFilter)) {
            modelToUse = pendingOrdersTableModel;
            statusClause = "WHERE Status IN ('Draft', 'Pending Approval', 'Approved', 'Ordered', 'Partially Received', 'Pending Reorder')";
            selectColumns = "POID, PONumber, PODate, Status, TotalAmount";
            pageLabel = pendingPageLabel;
            itemsPerPage = PENDING_ORDERS_PER_PAGE;
            currentPage = currentPendingPage;
            currentSearchText = currentPendingSearchText;

        } else if ("Completed".equals(statusFilter)) {
            modelToUse = completedOrdersTableModel;
            statusClause = "WHERE Status IN ('Received', 'Cancelled')";
            selectColumns = "POID, PONumber, PODate, Status, TotalAmount";
            pageLabel = completedPageLabel;
            itemsPerPage = COMPLETED_ORDERS_PER_PAGE;
            currentPage = currentCompletedPage;
            currentSearchText = currentCompletedSearchText;
        } else {
            return;
        }

        if (modelToUse == null) return;

        modelToUse.setRowCount(0);
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build the base SQL queries
        StringBuilder baseCountSql = new StringBuilder("SELECT COUNT(*) FROM PurchaseOrders ");
        StringBuilder baseDataSql = new StringBuilder("SELECT ").append(selectColumns).append(" FROM PurchaseOrders ");

        // Add status filter
        baseCountSql.append(statusClause);
        baseDataSql.append(statusClause);

        // Add search filter if search text is not empty
        if (currentSearchText != null && !currentSearchText.isEmpty()) {
            String searchLike = "%" + currentSearchText.toLowerCase() + "%";
            baseCountSql.append(" AND (LOWER(CAST(POID AS CHAR)) LIKE ? OR LOWER(PONumber) LIKE ? OR LOWER(PODate) LIKE ? OR LOWER(Status) LIKE ? OR LOWER(CAST(TotalAmount AS CHAR)) LIKE ?)");
            baseDataSql.append(" AND (LOWER(CAST(POID AS CHAR)) LIKE ? OR LOWER(PONumber) LIKE ? OR LOWER(PODate) LIKE ? OR LOWER(Status) LIKE ? OR LOWER(CAST(TotalAmount AS CHAR)) LIKE ?)");
        }

        // Add ordering and limit/offset for data query
        baseDataSql.append(" ORDER BY PODate DESC LIMIT ? OFFSET ?");

        countSql = baseCountSql.toString();
        dataSql = baseDataSql.toString();


        int totalItemsForFilter = 0;
         // Get total count first (with search filter applied)
         try (PreparedStatement pstmtCount = conn.prepareStatement(countSql)) {
             int paramIndex = 1;
             if (currentSearchText != null && !currentSearchText.isEmpty()) {
                 String searchLike = "%" + currentSearchText.toLowerCase() + "%";
                 pstmtCount.setString(paramIndex++, searchLike);
                 pstmtCount.setString(paramIndex++, searchLike);
                 pstmtCount.setString(paramIndex++, searchLike);
                 pstmtCount.setString(paramIndex++, searchLike);
                 pstmtCount.setString(paramIndex++, searchLike);
             }
              try (ResultSet rsCount = pstmtCount.executeQuery()) {
                 if (rsCount.next()) {
                     totalItemsForFilter = rsCount.getInt(1);
                 }
             }
         } catch (SQLException e) {
             System.err.println("Error counting " + statusFilter + " orders with search: " + e.getMessage());
             JOptionPane.showMessageDialog(this, "Error counting " + statusFilter + " orders.", "Database Error", JOptionPane.ERROR_MESSAGE);
             return;
         }

         // Update total items for the specific filter
         if ("Pending".equals(statusFilter)) {
             totalPendingOrders = totalItemsForFilter;
         } else { // Completed Orders
             totalCompletedOrders = totalItemsForFilter;
         }

        int offset = (currentPage - 1) * itemsPerPage;

        try (java.sql.PreparedStatement pstmt = conn.prepareStatement(dataSql)) {
            int paramIndex = 1;
             if (currentSearchText != null && !currentSearchText.isEmpty()) {
                 String searchLike = "%" + currentSearchText.toLowerCase() + "%";
                 pstmt.setString(paramIndex++, searchLike);
                 pstmt.setString(paramIndex++, searchLike);
                 pstmt.setString(paramIndex++, searchLike);
                 pstmt.setString(paramIndex++, searchLike);
                 pstmt.setString(paramIndex++, searchLike);
             }
            pstmt.setInt(paramIndex++, itemsPerPage);
            pstmt.setInt(paramIndex++, offset);

            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                     if ("Pending".equals(statusFilter)) {
                          String status = rs.getString("Status");
                          BigDecimal totalAmount = rs.getBigDecimal("TotalAmount");
                          int poId = rs.getInt("POID"); // Get POID to use for print action

                         modelToUse.addRow(new Object[]{
                            false, // Default to not selected for Pending Orders
                            poId, // PO ID
                            rs.getString("PONumber"),
                            rs.getDate("PODate"),
                            status,
                            totalAmount != null ? totalAmount : BigDecimal.ZERO, // Display 0.00 if TotalAmount is null
                            "Print" // Placeholder for the button column
                         });
                     } else if ("Completed".equals(statusFilter)) {
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
            System.err.println("Error loading " + statusFilter + " orders with search: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading " + statusFilter + " orders: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
             // Reset the select all checkbox after loading data for Pending Orders
            if ("Pending".equals(statusFilter) && selectAllPendingCheckbox != null) {
                selectAllPendingCheckbox.setSelected(false);
            }
            // Update pagination label and button states
            updatePaginationControls(statusFilter);
        }
    }

     /**
     * Loads items (excluding machinery) that are at or below their reorder level,
     * excluding items currently on pending or approved POs. Includes pagination.
     */
    private void loadItemsNeedingReorder() {
         if (conn == null) {
            System.err.println("Database connection not available. Cannot load items needing reorder.");
            return;
        }

        itemsNeedingReorderTableModel.setRowCount(0); // Clear existing data

        // SQL to count total items needing reorder
        String countSql = "SELECT COUNT(*) " +
                          "FROM Items i " +
                          "LEFT JOIN Categories c ON i.CategoryID = c.CategoryID " +
                          "WHERE i.Quantity <= i.ReorderLevel " +
                          "AND i.IsMachinery = FALSE " +
                          "AND NOT EXISTS ( " +
                          "    SELECT 1 " +
                          "    FROM PurchaseOrderItems poi " +
                          "    JOIN PurchaseOrders po ON poi.POID = po.POID " +
                          "    WHERE poi.ItemID = i.ItemID AND po.Status NOT IN ('Received', 'Cancelled') " +
                          ")";

        // SQL to select paginated items needing reorder
        String dataSql = "SELECT i.ItemID, i.ItemName, c.CategoryName, i.Quantity, i.ReorderLevel, i.UnitPrice " +
                         "FROM Items i " +
                         "LEFT JOIN Categories c ON i.CategoryID = c.CategoryID " +
                         "WHERE i.Quantity <= i.ReorderLevel " + // Filter only by low stock
                         "AND i.IsMachinery = FALSE " + // Exclude machinery
                         "AND NOT EXISTS ( " +
                         "    SELECT 1 " +
                         "    FROM PurchaseOrderItems poi " +
                         "    JOIN PurchaseOrders po ON poi.POID = po.POID " +
                         "    WHERE poi.ItemID = i.ItemID AND po.Status NOT IN ('Received', 'Cancelled') " +
                         ") " +
                         "ORDER BY i.ItemName LIMIT ? OFFSET ?";

        // Get total count first
        try (PreparedStatement pstmtCount = conn.prepareStatement(countSql);
             ResultSet rsCount = pstmtCount.executeQuery()) {
            if (rsCount.next()) {
                totalItems = rsCount.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting items needing reorder: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error counting items needing reorder.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int offset = (currentItemsPage - 1) * ITEMS_PER_PAGE;

        try (PreparedStatement pstmt = conn.prepareStatement(dataSql)) {
            pstmt.setInt(1, ITEMS_PER_PAGE);
            pstmt.setInt(2, offset);
            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    int currentQty = rs.getInt("Quantity");
                    int reorderLevel = rs.getInt("ReorderLevel");
                    BigDecimal unitPrice = rs.getBigDecimal("UnitPrice");

                    // Calculate suggested order quantity: Reorder Level + Buffer, minimum of MIN_ORDER_QUANTITY
                    int suggestedOrderQty = Math.max(reorderLevel - currentQty + REORDER_BUFFER, MIN_ORDER_QUANTITY);

                    Vector<Object> row = new Vector<>();
                    row.add(false); // Select checkbox
                    row.add(rs.getInt("ItemID"));
                    row.add(rs.getString("ItemName"));
                    // Use CategoryName directly from the main query result set
                    String categoryName = rs.getString("CategoryName");
                    row.add(categoryName != null ? categoryName : "N/A");
                    row.add(currentQty);
                    row.add(reorderLevel);
                    row.add(suggestedOrderQty);
                    row.add(unitPrice); // Add Unit Price
                    itemsNeedingReorderTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading items needing reorder: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading items needing reorder.", "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
             // Reset the select all checkbox after loading data
            if (selectAllItemsCheckbox != null) {
                selectAllItemsCheckbox.setSelected(false);
            }
            // Update pagination label and button states
            updatePaginationControls("ItemsNeedingReorder");
        }
    }

     /**
     * Loads machinery items that are at or below their reorder level,
     * excluding items currently on pending or approved POs. Includes pagination.
     */
    private void loadMachineryNeedingReorder() {
         if (conn == null) {
            System.err.println("Database connection not available. Cannot load machinery needing reorder.");
            return;
        }

        machineryNeedingReorderTableModel.setRowCount(0); // Clear existing data

        // SQL to count total machinery needing reorder
        String countSql = "SELECT COUNT(*) " +
                          "FROM Items i " +
                          "LEFT JOIN Categories c ON i.CategoryID = c.CategoryID " +
                          "WHERE i.Quantity <= i.ReorderLevel " +
                          "AND i.IsMachinery = TRUE " +
                          "AND i.IsArchived = FALSE " +
                          "AND NOT EXISTS ( " +
                          "    SELECT 1 " +
                          "    FROM PurchaseOrderItems poi " +
                          "    JOIN PurchaseOrders po ON poi.POID = po.POID " +
                          "    WHERE poi.ItemID = i.ItemID AND po.Status NOT IN ('Received', 'Cancelled') " +
                          ")";

        // SQL to select paginated machinery needing reorder
        String dataSql = "SELECT i.ItemID, i.ItemName, c.CategoryName, i.Quantity, i.ReorderLevel, i.UnitPrice " +
                         "FROM Items i " +
                         "LEFT JOIN Categories c ON i.CategoryID = c.CategoryID " +
                         "WHERE i.Quantity <= i.ReorderLevel " + // Filter only by low stock
                         "AND i.IsMachinery = TRUE " + // Include ONLY machinery
                         "AND i.IsArchived = FALSE " +
                         "AND NOT EXISTS ( " +
                         "    SELECT 1 " +
                         "    FROM PurchaseOrderItems poi " +
                         "    JOIN PurchaseOrders po ON poi.POID = po.POID " +
                         "    WHERE poi.ItemID = i.ItemID AND po.Status NOT IN ('Received', 'Cancelled') " +
                         ") " +
                         "ORDER BY i.ItemName LIMIT ? OFFSET ?";

        // Get total count first
        try (PreparedStatement pstmtCount = conn.prepareStatement(countSql);
             ResultSet rsCount = pstmtCount.executeQuery()) {
            if (rsCount.next()) {
                totalMachinery = rsCount.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting machinery needing reorder: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error counting machinery needing reorder.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int offset = (currentMachineryPage - 1) * MACHINERY_PER_PAGE;


        try (PreparedStatement pstmt = conn.prepareStatement(dataSql)) {
             pstmt.setInt(1, MACHINERY_PER_PAGE);
             pstmt.setInt(2, offset);
             try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    int currentQty = rs.getInt("Quantity");
                    int reorderLevel = rs.getInt("ReorderLevel");
                     BigDecimal unitPrice = rs.getBigDecimal("UnitPrice");

                    // Calculate suggested order quantity: Reorder Level + Buffer, minimum of MIN_ORDER_QUANTITY
                    int suggestedOrderQty = Math.max(reorderLevel - currentQty + REORDER_BUFFER, MIN_ORDER_QUANTITY);

                    Vector<Object> row = new Vector<>();
                    row.add(false); // Select checkbox
                    row.add(rs.getInt("ItemID"));
                    row.add(rs.getString("ItemName"));
                    // Use CategoryName directly from the main query result set
                    String categoryName = rs.getString("CategoryName");
                    row.add(categoryName != null ? categoryName : "N/A");
                    row.add(currentQty);
                    row.add(reorderLevel);
                    row.add(suggestedOrderQty);
                    row.add(unitPrice); // Add Unit Price
                    machineryNeedingReorderTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading machinery needing reorder: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading machinery needing reorder.", "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
             // Reset the select all checkbox after loading data
            if (selectAllMachineryCheckbox != null) {
                selectAllMachineryCheckbox.setSelected(false);
            }
            // Update pagination label and button states
            updatePaginationControls("MachineryNeedingReorder");
        }
    }


     /**
     * Fetches the category name for a given CategoryID.
     * @param categoryId The ID of the category.
     * @return The category name, or null if not found or error occurs.
     * NOTE: This method is no longer used by loadItemsNeedingReorder and loadMachineryNeedingReorder,
     * but is kept in case it's used elsewhere.
     */
    private String getCategoryName(int categoryId) {
        if (conn == null) {
            return null;
        }
        String sql = "SELECT CategoryName FROM Categories WHERE CategoryID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("CategoryName");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching category name for ID " + categoryId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Opens a dialog to view the details of a selected purchase order.
     * @param table The table from which the PO is selected (either pendingOrdersTable or completedOrdersTable).
     */


    /**
     * Creates a new Purchase Order from the selected items in the 'Items Needing Reorder' table.
     */
    private void createPurchaseOrder() {
        // Check if the current user is allowed to create POs (Admin or Custodian)
        if (currentUser == null || (!"Admin".equals(currentUser.getRole()) && !"Custodian".equals(currentUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "You do not have permission to create purchase orders.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        createPurchaseOrderFromTable(itemsNeedingReorderTable, itemsNeedingReorderTableModel);
    }

    /**
     * Creates a new Purchase Order from the selected items in the 'Machinery Needing Reorder' table.
     */
    private void createMachineryPurchaseOrder() {
         // Check if the current user is allowed to create POs (Admin or Custodian)
        if (currentUser == null || (!"Admin".equals(currentUser.getRole()) && !"Custodian".equals(currentUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "You do not have permission to create purchase orders.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
         createPurchaseOrderFromTable(machineryNeedingReorderTable, machineryNeedingReorderTableModel);
    }


    /**
     * Helper method to create a Purchase Order from selected items in a given table.
     * @param table The table containing the selected items (itemsNeedingReorderTable or machineryNeedingReorderTable).
     * @param model The DefaultTableModel for the table.
     */
    private void createPurchaseOrderFromTable(JTable table, DefaultTableModel model) {
        if (currentUser == null) {
            JOptionPane.showMessageDialog(this, "User not logged in. Cannot create purchase order.", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
         if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available. Cannot create purchase order.", "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Integer> selectedRows = new ArrayList<>();
        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean isSelected = (Boolean) model.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                selectedRows.add(i);
            }
        }

        if (selectedRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select items to create a purchase order.", "No Selection", JOptionPane.WARNING_MESSAGE);
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

        int selectColumnIndex = 0; // Assuming "Select" is the first column
        int itemIdColumnIndex = getColumnIndex("Item ID", model);
        int suggestedQtyColumnIndex = getColumnIndex("Suggested Order Qty", model);
        int unitPriceColumnIndex = getColumnIndex("Unit Price", model);
        int itemNameColumnIndex = getColumnIndex("Item Name", model); // To get item name for PO item description

        if (itemIdColumnIndex == -1 || suggestedQtyColumnIndex == -1 || unitPriceColumnIndex == -1 || itemNameColumnIndex == -1) {
             System.err.println("Error: Required columns not found in the table model.");
             JOptionPane.showMessageDialog(this, "Internal error: Could not find required table columns.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }


        for (int rowIndex : selectedRows) {
             try {
                int itemId = (Integer) model.getValueAt(rowIndex, itemIdColumnIndex);
                String itemName = (String) model.getValueAt(rowIndex, itemNameColumnIndex);
                // Get the suggested quantity as displayed in the table
                int suggestedQty = (Integer) model.getValueAt(rowIndex, suggestedQtyColumnIndex);
                BigDecimal unitPrice = (BigDecimal) model.getValueAt(rowIndex, unitPriceColumnIndex); // Get Unit Price

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
             if (table == itemsNeedingReorderTable && selectAllItemsCheckbox != null) {
                selectAllItemsCheckbox.setSelected(false);
            } else if (table == machineryNeedingReorderTable && selectAllMachineryCheckbox != null) {
                 selectAllMachineryCheckbox.setSelected(false);
            }
             loadItemsNeedingReorder(); // Refresh both lists
             loadMachineryNeedingReorder();
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

            JOptionPane.showMessageDialog(this, "Purchase Order " + poNumber + " created successfully as Draft.", "Success", JOptionPane.INFORMATION_MESSAGE); // Use the generated PO number


            // Remove selected items from the source table model
            // Iterate backwards to avoid index issues when removing rows
            // Get the actual row indices in the view before removing
            int[] selectedViewRows = table.getSelectedRows();
            // Convert view rows to model rows and sort in descending order
            List<Integer> selectedModelRows = new ArrayList<>();
            for(int viewRow : selectedViewRows) {
                selectedModelRows.add(table.convertRowIndexToModel(viewRow));
            }
            selectedModelRows.sort((a, b) -> b.compareTo(a)); // Sort descending

            // Remove rows from the model
            for (int modelRowIndex : selectedModelRows) {
                 model.removeRow(modelRowIndex);
            }

            // Reset the select all checkbox after creating PO
            if (table == itemsNeedingReorderTable && selectAllItemsCheckbox != null) {
                selectAllItemsCheckbox.setSelected(false);
            } else if (table == machineryNeedingReorderTable && selectAllMachineryCheckbox != null) {
                 selectAllMachineryCheckbox.setSelected(false);
            }


            // Refresh the Pending Orders tab
            tabbedPane.setSelectedIndex(2); // Pending Orders is now index 2
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
     * Added check for single selection.
     * @param table The table containing the purchase orders (pending or completed).
     */
    private void viewPurchaseOrderDetails(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ensure only one row is selected for viewing details
        if (table.getSelectedRowCount() > 1) {
             JOptionPane.showMessageDialog(this, "Please select only one purchase order to view details.", "Multiple Selection", JOptionPane.WARNING_MESSAGE);
             return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        // Get PO ID from the PO ID column (index 1 for Pending Orders, and index 0 for Completed Orders)
        int poIdColumnIndex = (table == pendingOrdersTable) ? 1 : 0;
        int poId = (Integer) table.getModel().getValueAt(modelRow, poIdColumnIndex);


        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);

        // Ensure PurchaseOrderDetailsDialog is correctly imported and has the correct constructor
        PurchaseOrderDetailsDialog detailsDialog = new PurchaseOrderDetailsDialog(parentFrame, true, conn, currentUser, poId);
        detailsDialog.setPurchaseOrderActionListener(this); // Set the listener
        detailsDialog.setLocationRelativeTo(this); // Center relative to this panel
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
        System.out.println("Attempting to print PO ID: " + poId);
        String poContent = generatePurchaseOrderContent(poId);

        if (poContent == null || poContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Could not generate content for printing.", "Printing Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Use a JTextArea to hold the content for printing
        JTextArea printableArea = new JTextArea(poContent);
        printableArea.setFont(new Font("Courier New", Font.PLAIN, 12)); // Use a monospaced font
        printableArea.setBackground(Color.WHITE); // Ensure white background for printing
        printableArea.setForeground(Color.BLACK); // Ensure black text for printing


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
                System.err.println("Printing error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
             System.out.println("Print dialog cancelled by user.");
        }
    }

    /**
     * Approves all selected pending purchase orders.
     */
    private void approveSelectedPOs() {
        // Check if the current user is allowed to approve POs (only Admin)
        if (currentUser == null || !"Admin".equals(currentUser.getRole())) {
            JOptionPane.showMessageDialog(this, "You do not have permission to approve purchase orders.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Integer> selectedPoIds = getSelectedPoIds(pendingOrdersTable, pendingOrdersTableModel);

        if (selectedPoIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending purchase orders selected for approval.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmed = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to approve " + selectedPoIds.size() + " selected purchase order(s)?",
                "Confirm Approval", JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            int approvedCount = 0;
            int failedCount = 0;
            StringBuilder failedPOs = new StringBuilder();

            for (int poId : selectedPoIds) {
                // Attempt to update status for each selected PO
                // Explicitly check status here for batch processing clarity,
                // although updatePurchaseOrderStatus also checks
                String currentStatus = getPoStatus(poId);
                if ("Draft".equals(currentStatus) || "Pending Approval".equals(currentStatus)) {
                     boolean success = updatePurchaseOrderStatus(poId, "Approved", null); // Status check is now inside updatePurchaseOrderStatus
                     if (success) {
                         approvedCount++;
                     } else {
                         failedCount++;
                         failedPOs.append(poId).append(" ");
                     }
                } else {
                     failedCount++;
                     failedPOs.append(poId).append(" (Status: ").append(currentStatus).append(") ");
                     System.err.println("Skipping approval for PO ID " + poId + ". Invalid status: " + currentStatus);
                }
            }

            if (approvedCount > 0) {
                JOptionPane.showMessageDialog(this, approvedCount + " purchase order(s) approved successfully.", "Approval Complete", JOptionPane.INFORMATION_MESSAGE);
            }
            if (failedCount > 0) {
                 JOptionPane.showMessageDialog(this, failedCount + " purchase order(s) failed to approve or were in an invalid status (IDs: " + failedPOs.toString().trim() + "). See console for details.", "Approval Failed", JOptionPane.ERROR_MESSAGE);
            }

            // Refresh the pending orders list regardless of success/failure
            loadOrders("Pending");
             // Also refresh items needing reorder as inventory might change upon receiving (though approval doesn't change inventory)
            loadItemsNeedingReorder();
            loadMachineryNeedingReorder();
        }
    }

    /**
     * Receives items for all selected pending purchase orders.
     */
    private void receiveSelectedPOs() {
         // Check if the current user is allowed to receive POs (Admin or Custodian)
        if (currentUser == null || (!"Admin".equals(currentUser.getRole()) && !"Custodian".equals(currentUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "You do not have permission to receive purchase orders.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }

         List<Integer> selectedPoIds = getSelectedPoIds(pendingOrdersTable, pendingOrdersTableModel);

        if (selectedPoIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending purchase orders selected for receiving.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmed = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to mark " + selectedPoIds.size() + " selected purchase order(s) as received and update inventory?",
                "Confirm Receiving", JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            int receivedCount = 0;
            int failedCount = 0;
            StringBuilder failedPOs = new StringBuilder();

            for (int poId : selectedPoIds) {
                // Attempt to handle receiving for each selected PO
                 // Explicitly check status here for batch processing clarity,
                // although handleReceiveItems also checks
                String currentStatus = getPoStatus(poId);
                 if ("Approved".equals(currentStatus) || "Ordered".equals(currentStatus) || "Partially Received".equals(currentStatus)) {
                     boolean success = handleReceiveItems(poId); // Status check is now inside handleReceiveItems
                     if (success) {
                         receivedCount++;
                     } else {
                         failedCount++;
                         failedPOs.append(poId).append(" ");
                     }
                 } else {
                     failedCount++;
                     failedPOs.append(poId).append(" (Status: ").append(currentStatus).append(") ");
                     System.err.println("Skipping receiving for PO ID " + poId + ". Invalid status: " + currentStatus);
                 }
            }

            if (receivedCount > 0) {
                JOptionPane.showMessageDialog(this, receivedCount + " purchase order(s) marked as received and inventory updated.", "Receiving Complete", JOptionPane.INFORMATION_MESSAGE);
            }
            if (failedCount > 0) {
                 JOptionPane.showMessageDialog(this, failedCount + " purchase order(s) failed to receive or were in an invalid status (IDs: " + failedPOs.toString().trim() + "). See console for details.", "Receiving Failed", JOptionPane.ERROR_MESSAGE);
            }

            // Refresh all relevant tables
            loadOrders("Pending");
            loadOrders("Completed");
            loadItemsNeedingReorder(); // Refresh items needing reorder as inventory changed
            loadMachineryNeedingReorder(); // Refresh machinery needing reorder
        }
    }


    /**
     * Helper method to get the list of selected PO IDs from a table with a "Select" checkbox in the first column.
     * @param table The JTable to get selections from.
     * @param model The DefaultTableModel for the table.
     * @return A list of PO IDs for the selected rows.
     */
    private List<Integer> getSelectedPoIds(JTable table, DefaultTableModel model) {
        List<Integer> selectedPoIds = new ArrayList<>();
        int selectColumnIndex = 0; // Assuming "Select" is the first column

        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean isSelected = (Boolean) model.getValueAt(i, selectColumnIndex);
            if (isSelected != null && isSelected) {
                // Assuming PO ID is in the second column (index 1) for Pending Orders
                 int poIdColumnIndex = (table == pendingOrdersTable) ? 1 : 0; // Adjust index based on table
                 try {
                     int poId = (Integer) model.getValueAt(i, poIdColumnIndex);
                     selectedPoIds.add(poId);
                 } catch (ClassCastException e) {
                     System.err.println("Error casting PO ID from table model at row " + i + ": " + e.getMessage());
                 } catch (NullPointerException e) {
                     System.err.println("Null PO ID encountered at row " + i + ": " + e.getMessage());
                 }
            }
        }
        return selectedPoIds;
    }


     /**
     * Updates the purchase order status for a single PO.
     * This is an adapted version of the method from PurchaseOrderDetailsDialog.
     * Returns true if update was successful, false otherwise.
     * @param poId The ID of the PO to update.
     * @param newStatus The new status to set.
     * @param expectedCurrentStatus The status the PO must currently have for this action to be valid (can be null if not checked).
     * @return true if the status was updated successfully, false otherwise.
     */
    private boolean updatePurchaseOrderStatus(int poId, String newStatus, String expectedCurrentStatus) {
         if (conn == null) {
            System.err.println("Database connection not available. Cannot update PO status for PO ID: " + poId);
            return false;
        }

        // Fetch the current status from the database
        String actualCurrentStatus = "";
        String fetchStatusSql = "SELECT Status FROM PurchaseOrders WHERE POID = ?";
        try (PreparedStatement pstmtFetch = conn.prepareStatement(fetchStatusSql)) {
            pstmtFetch.setInt(1, poId);
            try (ResultSet rs = pstmtFetch.executeQuery()) {
                if (rs.next()) {
                    actualCurrentStatus = rs.getString("Status");
                } else {
                    System.err.println("Purchase Order not found for ID: " + poId + ". Cannot update status.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching current PO status for ID " + poId + ": " + e.getMessage());
            return false;
        }

        // Check if the action is valid based on the actual current status
        // If expectedCurrentStatus is null, any current status is allowed for the update
        if (expectedCurrentStatus != null && !actualCurrentStatus.equals(expectedCurrentStatus)) {
            System.err.println("Action cannot be performed for PO ID " + poId + ". Status is '" + actualCurrentStatus + "', but expected '" + expectedCurrentStatus + "'.");
            return false;
        }

        // Specific checks for transitions:
        // Allow "Approved" from "Draft" or "Pending Approval"
        if ("Approved".equals(newStatus) && !("Draft".equals(actualCurrentStatus) || "Pending Approval".equals(actualCurrentStatus))) {
             System.err.println("Cannot approve PO ID " + poId + " with status '" + actualCurrentStatus + "'. It must be 'Draft' or 'Pending Approval'.");
             return false;
        }

        // For "Cancel" operation, ensure it's not already "Received" or "Cancelled"
        if ("Cancelled".equals(newStatus) && ("Received".equals(actualCurrentStatus) || "Cancelled".equals(actualCurrentStatus))) {
             System.err.println("Cannot cancel PO ID " + poId + " that is already '" + actualCurrentStatus + "'.");
             return false;
        }

         // Prevent receiving if not in an allowed status (This check is also in handleReceiveItems, but good to have here)
        if ("Received".equals(newStatus) && !("Approved".equals(actualCurrentStatus) || "Ordered".equals(actualCurrentStatus) || "Partially Received".equals(actualCurrentStatus))) {
             System.err.println("Cannot mark PO ID " + poId + " as Received with status '" + actualCurrentStatus + "'. It must be 'Approved', 'Ordered', or 'Partially Received'.");
             return false;
        }


        String sql = "UPDATE PurchaseOrders SET Status = ? WHERE POID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, poId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Purchase Order ID " + poId + " status updated to " + newStatus);
                return true;
            } else {
                System.err.println("Failed to update purchase order status for PO ID " + poId + ". PO might have been modified or deleted.");
                return false;
            }
        } catch (SQLException e) {
             System.err.println("Error updating purchase order status for PO ID " + poId + ": " + e.getMessage());
             return false;
        }
    }

    /**
     * Handles the process of receiving items for a single PO.
     * This is an adapted version of the method from PurchaseOrderDetailsDialog.
     * Returns true if receiving was successful, false otherwise.
     * @param poId The ID of the PO to receive items for.
     * @return true if items were received and status updated successfully, false otherwise.
     */
    private boolean handleReceiveItems(int poId) {
         if (conn == null) {
            System.err.println("Database connection not available. Cannot receive items for PO ID: " + poId);
            return false;
        }

        // Fetch current status directly from DB before proceeding
        String currentStatusFromDB = "";
        String fetchStatusSql = "SELECT Status FROM PurchaseOrders WHERE POID = ?";
        try (PreparedStatement pstmtFetch = conn.prepareStatement(fetchStatusSql)) {
            pstmtFetch.setInt(1, poId);
            try (ResultSet rs = pstmtFetch.executeQuery()) {
                if (rs.next()) {
                    currentStatusFromDB = rs.getString("Status");
                } else {
                    System.err.println("Purchase Order not found for ID: " + poId + ". Cannot receive items.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching current PO status for receiving PO ID " + poId + ": " + e.getMessage());
            return false;
        }


         // Strict status check for receiving
         if ("Approved".equals(currentStatusFromDB) || "Ordered".equals(currentStatusFromDB) || "Partially Received".equals(currentStatusFromDB)) {
             try {
                 conn.setAutoCommit(false); // Start Transaction for this single PO

                 // 1. Get items for this specific PO
                 List<PurchaseOrderItem> poItems = new ArrayList<>();
                 String getPoItemsSql = "SELECT ItemID, QuantityOrdered FROM PurchaseOrderItems WHERE POID = ?";
                 try(PreparedStatement pstmtGetItems = conn.prepareStatement(getPoItemsSql)) {
                     pstmtGetItems.setInt(1, poId);
                     try(ResultSet rsItems = pstmtGetItems.executeQuery()) {
                         while(rsItems.next()) {
                             poItems.add(new PurchaseOrderItem(rsItems.getInt("ItemID"), rsItems.getInt("QuantityOrdered")));
                         }
                     }
                 }


                 // 2. Update Inventory Quantities
                 String updateInventorySql = "UPDATE Items SET Quantity = Quantity + ? WHERE ItemID = ?";
                 try (PreparedStatement pstmtUpdateInventory = conn.prepareStatement(updateInventorySql)) {
                     for (PurchaseOrderItem item : poItems) {
                         pstmtUpdateInventory.setInt(1, item.getQuantityOrdered());
                         pstmtUpdateInventory.setInt(2, item.getItemId());
                         pstmtUpdateInventory.addBatch();
                     }
                     pstmtUpdateInventory.executeBatch();
                     System.out.println("Inventory updated for PO ID: " + poId);
                 }

                 // 3. Update Purchase Order Status to Received
                 String updatePoStatusSql = "UPDATE PurchaseOrders SET Status = ? WHERE POID = ?";
                 try (PreparedStatement pstmtUpdatePo = conn.prepareStatement(updatePoStatusSql)) {
                     pstmtUpdatePo.setString(1, "Received");
                     pstmtUpdatePo.setInt(2, poId);
                     int affectedRows = pstmtUpdatePo.executeUpdate();

                     if (affectedRows == 0) {
                         throw new SQLException("Failed to update purchase order status to Received for PO ID " + poId + ". PO might have been modified or deleted.");
                     }
                     System.out.println("Purchase Order status updated to Received for PO ID: " + poId);
                 }

                 conn.commit(); // If all updates successful, commit the transaction
                 return true; // Indicate success

             } catch (SQLException e) {
                 try {
                     if (conn != null) {
                         conn.rollback();
                         System.err.println("Transaction rolled back due to error during receiving items for PO ID " + poId + ".");
                     }
                 } catch (SQLException ex) {
                     System.err.println("Error during transaction rollback for PO ID " + poId + ": " + ex.getMessage());
                 }
                 System.err.println("Database error during receiving items for PO ID " + poId + ": " + e.getMessage());
                 return false; // Indicate failure

             } finally {
                 try {
                     if (conn != null) {
                         conn.setAutoCommit(true);
                     }
                 } catch (SQLException ex) {
                     System.err.println("Error restoring auto-commit: " + ex.getMessage());
                 }
             }
         } else {
             System.err.println("Cannot mark PO ID " + poId + " as Received with status '" + currentStatusFromDB + "'. It must be 'Approved', 'Ordered', or 'Partially Received'.");
             return false; // Indicate failure due to invalid status
         }
    }

    // Helper class to hold PO Item details for batch processing
    private static class PurchaseOrderItem {
        private int itemId;
        private int quantityOrdered;

        public PurchaseOrderItem(int itemId, int quantityOrdered) {
            this.itemId = itemId;
            this.quantityOrdered = quantityOrdered;
        }

        public int getItemId() {
            return itemId;
        }

        public int getQuantityOrdered() {
            return quantityOrdered;
        }
    }


    // Implement the PurchaseOrderActionListener interface method
    @Override
    public void purchaseOrderChanged() {
        System.out.println("Purchase order status changed. Refreshing tables.");
        // Reset pages to 1 and reload data for all relevant tabs
        currentPendingPage = 1;
        currentCompletedPage = 1;
        currentItemsPage = 1;
        currentMachineryPage = 1;
        loadOrders("Pending");
        loadOrders("Completed");
        loadItemsNeedingReorder(); // Also refresh items needing reorder as inventory might change upon receiving
        loadMachineryNeedingReorder(); // Refresh machinery needing reorder
    }

    /**
     * Updates the visibility of action buttons based on the current user's role.
     * Admin: Can see/use all buttons (Create, Approve, Receive, View).
     * Custodian: Can see/use Create, View buttons, but not Approve or Receive batch actions.
     * Other roles or no user: Can only see View buttons.
     */
    private void updateButtonVisibility() {
        boolean isAdmin = currentUser != null && "Admin".equals(currentUser.getRole());
        boolean isCustodian = currentUser != null && "Custodian".equals(currentUser.getRole());

        // Items Needing Reorder tab
        createPoButton.setVisible(isAdmin || isCustodian);
        createMachineryPoButton.setVisible(isAdmin || isCustodian);
        selectAllItemsCheckbox.setVisible(isAdmin || isCustodian); // Hide select all if not allowed to create
        selectAllMachineryCheckbox.setVisible(isAdmin || isCustodian); // Hide select all if not allowed to create


        // Pending Orders tab
        approveSelectedButton.setVisible(isAdmin); // Only Admin can approve
        // Modified: Receive button visible only for Admin
        receiveSelectedButton.setVisible(isAdmin);
        // Select all checkbox visible for Admin and Custodian for potential future batch actions or viewing
        selectAllPendingCheckbox.setVisible(isAdmin || isCustodian);
        viewPendingPoButton.setVisible(currentUser != null); // Anyone logged in can view

        // Completed Orders tab
        viewCompletedPoButton.setVisible(currentUser != null); // Anyone logged in can view

        // Update visibility of pagination controls based on user presence
        updatePaginationControlsVisibility();

        // Note: Print button is part of the table cell renderer/editor, its visibility
        // is handled implicitly by whether the table rows are loaded. If printing should
        // also be role-restricted, that logic would need to be added within the
        // ButtonEditor's actionPerformed or isCellEditable methods, checking currentUser role.
    }

    /**
     * Updates the state (enabled/disabled, text) of pagination controls.
     * @param tabName The name of the tab being updated ("ItemsNeedingReorder", "MachineryNeedingReorder", "Pending", "Completed").
     */
    private void updatePaginationControls(String tabName) {
        int totalItemsForTab = 0;
        int itemsPerPage = 0;
        int currentPage = 0;
        JLabel pageLabel = null;
        JButton prevButton = null;
        JButton nextButton = null;

        switch (tabName) {
            case "ItemsNeedingReorder":
                totalItemsForTab = totalItems;
                itemsPerPage = ITEMS_PER_PAGE;
                currentPage = currentItemsPage;
                pageLabel = itemsPageLabel;
                prevButton = itemsPrevButton;
                nextButton = itemsNextButton;
                break;
            case "MachineryNeedingReorder":
                totalItemsForTab = totalMachinery;
                itemsPerPage = MACHINERY_PER_PAGE;
                currentPage = currentMachineryPage;
                pageLabel = machineryPageLabel;
                prevButton = machineryPrevButton;
                nextButton = machineryNextButton;
                break;
            case "Pending":
                totalItemsForTab = totalPendingOrders;
                itemsPerPage = PENDING_ORDERS_PER_PAGE;
                currentPage = currentPendingPage;
                pageLabel = pendingPageLabel;
                prevButton = pendingPrevButton;
                nextButton = pendingNextButton;
                break;
            case "Completed":
                totalItemsForTab = totalCompletedOrders;
                itemsPerPage = COMPLETED_ORDERS_PER_PAGE;
                currentPage = currentCompletedPage;
                pageLabel = completedPageLabel;
                prevButton = completedPrevButton;
                nextButton = completedNextButton;
                break;
            default:
                return; // Should not happen
        }

        int totalPages = (int) Math.ceil((double) totalItemsForTab / itemsPerPage);
        if (totalPages == 0) totalPages = 1; // Handle case with no items

        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }

     /**
     * Updates the visibility of all pagination controls based on user presence.
     */
    private void updatePaginationControlsVisibility() {
        boolean isUserLoggedIn = currentUser != null;

        itemsPrevButton.setVisible(isUserLoggedIn);
        itemsNextButton.setVisible(isUserLoggedIn);
        itemsPageLabel.setVisible(isUserLoggedIn);

        machineryPrevButton.setVisible(isUserLoggedIn);
        machineryNextButton.setVisible(isUserLoggedIn);
        machineryPageLabel.setVisible(isUserLoggedIn);

        pendingPrevButton.setVisible(isUserLoggedIn);
        pendingNextButton.setVisible(isUserLoggedIn);
        pendingPageLabel.setVisible(isUserLoggedIn);
        pendingSearchField.setVisible(isUserLoggedIn); // Show search field if logged in
        pendingSearchButton.setVisible(isUserLoggedIn); // Show search button if logged in


        completedPrevButton.setVisible(isUserLoggedIn);
        completedNextButton.setVisible(isUserLoggedIn);
        completedPageLabel.setVisible(isUserLoggedIn);
        completedSearchField.setVisible(isUserLoggedIn); // Show search field if logged in
        completedSearchButton.setVisible(isUserLoggedIn); // Show search button if logged in
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

    /**
     * Helper method to get the current status of a PO from the database.
     * @param poId The ID of the PO.
     * @return The current status string, or null if not found or error.
     */
    private String getPoStatus(int poId) {
         if (conn == null) {
             System.err.println("Database connection not available. Cannot fetch PO status for ID: " + poId);
             return null;
         }
         String status = null;
         String sql = "SELECT Status FROM PurchaseOrders WHERE POID = ?";
         try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, poId);
             try (ResultSet rs = pstmt.executeQuery()) {
                 if (rs.next()) {
                     status = rs.getString("Status");
                 }
             }
         } catch (SQLException e) {
             System.err.println("Error fetching status for PO ID " + poId + ": " + e.getMessage());
             e.printStackTrace();
         }
         return status;
    }


    /**
     * TableCellRenderer for rendering a JButton in a table cell.
     */
    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
            // You can add default styling here if needed
             setForeground(Color.BLACK); // Default text color
             setBackground(UIManager.getColor("Button.background")); // Default button background
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    /**
     * TableCellEditor for handling button clicks in a table cell.
     */
    class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

        private JButton button;
        private String label;
        private boolean isPushed;
        private JTable table; // Keep a reference to the table
        private int poId; // To store the PO ID for the clicked row


        public ButtonEditor(JCheckBox checkBox) {
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(this);
             // Apply some basic styling to the button
             button.setForeground(Color.BLACK);
             button.setBackground(UIManager.getColor("Button.background"));
             button.setBorderPainted(true); // Ensure border is painted
             button.setFocusPainted(false); // Remove focus border
        }

        /**
         * Sets the PO ID for the row whose button is being edited.
         * This is called by the MouseListener before editing starts.
         * @param poId The Purchase Order ID.
         */
        public void setPoId(Integer poId) {
            this.poId = poId;
        }


        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table; // Store table reference
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(UIManager.getColor("Button.background"));
            }
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            // Return the button's text or some indicator that the action occurred
            // In this case, we don't need to return a specific value after the action,
            // but AbstractCellEditor requires this method.
            if (isPushed) {
                // Action happened, return the label or null
            }
            isPushed = false;
            return label; // Return the label as the cell value after editing stops
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
             super.fireEditingStopped();
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            if (isPushed) {
                // This is where the button's action happens
                // The PO ID is stored in the 'poId' field
                System.out.println("Print button clicked for PO ID: " + poId);
                // Call the print method
                printPurchaseOrder(poId);

                // Notify the editor that editing is stopped
                fireEditingStopped();
            }
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
