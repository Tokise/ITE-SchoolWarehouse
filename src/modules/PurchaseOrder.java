package modules;

import Package1.DBConnection;
import Package1.User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JTextField;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import javax.swing.event.CellEditorListener;

import modules.PurchaseOrderDetailsDialog;


public class PurchaseOrder extends javax.swing.JPanel implements PurchaseOrderDetailsDialog.PurchaseOrderActionListener {

    private Connection conn = null;
    private User currentUser;

    private JTabbedPane tabbedPane;
    private JPanel itemsNeedingReorderPanel;
    private JPanel machineryNeedingReorderPanel;
    private JPanel pendingOrdersPanel;
    private JPanel completedOrdersPanel;

    private JTable itemsNeedingReorderTable;
    private DefaultTableModel itemsNeedingReorderTableModel;
    private JButton createPoButton;
    private JCheckBox selectAllItemsCheckbox;
    private JButton itemsPrevButton;
    private JButton itemsNextButton;
    private JLabel itemsPageLabel;
    private int currentItemsPage = 1;
    private int totalItems = 0;
    private final int ITEMS_PER_PAGE = 10;


    private JTable machineryNeedingReorderTable;
    private DefaultTableModel machineryNeedingReorderTableModel;
    private JButton createMachineryPoButton;
    private JCheckBox selectAllMachineryCheckbox;
    private JButton machineryPrevButton;
    private JButton machineryNextButton;
    private JLabel machineryPageLabel;
    private int currentMachineryPage = 1;
    private int totalMachinery = 0;
    private final int MACHINERY_PER_PAGE = 10;


    private JTable pendingOrdersTable;
    private DefaultTableModel pendingOrdersTableModel;
    private JButton viewPendingPoButton;
    private JButton approveSelectedButton;
    private JButton receiveSelectedButton;
    private JCheckBox selectAllPendingCheckbox;
    private JButton pendingPrevButton;
    private JButton pendingNextButton;
    private JLabel pendingPageLabel;
    private int currentPendingPage = 1;
    private int totalPendingOrders = 0;
    private final int PENDING_ORDERS_PER_PAGE = 10;
    private JTextField pendingSearchField;
    private JButton pendingSearchButton;
    private String currentPendingSearchText = "";


    private JTable completedOrdersTable;
    private DefaultTableModel completedOrdersTableModel;
    private JButton viewCompletedPoButton;
    private JButton completedPrevButton;
    private JButton completedNextButton;
    private JLabel completedPageLabel;
    private int currentCompletedPage = 1;
    private int totalCompletedOrders = 0;
    private final int COMPLETED_ORDERS_PER_PAGE = 10;
    private JTextField completedSearchField;
    private JButton completedSearchButton;
    private String currentCompletedSearchText = "";


    private static final int REORDER_BUFFER = 5;
    private static final int MIN_ORDER_QUANTITY = 5;
    private String poNumber;


    public PurchaseOrder() {
        initComponents();
        connectToDatabase();
        setupPurchaseOrderPanel();

    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (this.currentUser != null) {
            System.out.println("PurchaseOrder: User object set. UserID: " + this.currentUser.getUserId() + ", Username: " + this.currentUser.getUsername());
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                loadItemsNeedingReorder();
            } else if (selectedIndex == 1) {
                 loadMachineryNeedingReorder();
            } else if (selectedIndex == 2) {
                currentPendingSearchText = "";
                pendingSearchField.setText("");
                loadOrders("Pending");
            } else if (selectedIndex == 3) {
                currentCompletedSearchText = "";
                completedSearchField.setText("");
                loadOrders("Completed");
            }
            updateButtonVisibility();
        } else {
            System.out.println("PurchaseOrder: Current user is null.");
             itemsNeedingReorderTableModel.setRowCount(0);
             machineryNeedingReorderTableModel.setRowCount(0);
             pendingOrdersTableModel.setRowCount(0);
             completedOrdersTableModel.setRowCount(0);
             createPoButton.setVisible(false);
             createMachineryPoButton.setVisible(false);
             approveSelectedButton.setVisible(false);
             receiveSelectedButton.setVisible(false);
             viewPendingPoButton.setVisible(false);
             viewCompletedPoButton.setVisible(false);

             updatePaginationControlsVisibility();
        }
    }

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
        machineryNeedingReorderPanel = createMachineryNeedingReorderPanel();
        pendingOrdersPanel = createOrdersPanel("Pending Orders");
        completedOrdersPanel = createOrdersPanel("Completed Orders");

        tabbedPane.addTab("Items Needing Reorder", itemsNeedingReorderPanel);
        tabbedPane.addTab("Machinery Needing Reorder", machineryNeedingReorderPanel);
        tabbedPane.addTab("Pending Orders", pendingOrdersPanel);
        tabbedPane.addTab("Completed Orders", completedOrdersPanel);

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) {
                currentItemsPage = 1;
                loadItemsNeedingReorder();
            } else if (selectedIndex == 1) {
                 currentMachineryPage = 1;
                 loadMachineryNeedingReorder();
            } else if (selectedIndex == 2) {
                currentPendingPage = 1;
                currentPendingSearchText = "";
                pendingSearchField.setText("");
                loadOrders("Pending");
            } else if (selectedIndex == 3) {
                currentCompletedPage = 1;
                currentCompletedSearchText = "";
                completedSearchField.setText("");
                loadOrders("Completed");
            }
            updateButtonVisibility();
            updatePaginationControlsVisibility();
        });

        this.add(tabbedPane, BorderLayout.CENTER);
    }


    private JPanel createItemsNeedingReorderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Select", "Item ID", "Item Name", "Category", "Current Qty", "Reorder Level", "Suggested Order Qty", "Unit Price"};
        itemsNeedingReorderTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                 if (columnIndex == 7) {
                    return BigDecimal.class;
                }
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        itemsNeedingReorderTable = new JTable(itemsNeedingReorderTableModel);
        styleTable(itemsNeedingReorderTable);

        itemsNeedingReorderTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        itemsNeedingReorderTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        itemsNeedingReorderTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        itemsNeedingReorderTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        itemsNeedingReorderTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        itemsNeedingReorderTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        itemsNeedingReorderTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        itemsNeedingReorderTable.getColumnModel().getColumn(7).setPreferredWidth(80);


        JScrollPane scrollPane = new JScrollPane(itemsNeedingReorderTable);
        scrollPane.getViewport().setBackground(new Color(40, 40, 40));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);

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
        createPoButton.addActionListener(e -> createPurchaseOrder());
        buttonPanel.add(createPoButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

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


    private JPanel createMachineryNeedingReorderPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columns = {"Select", "Item ID", "Item Name", "Category", "Current Qty", "Reorder Level", "Suggested Order Qty", "Unit Price"};
        machineryNeedingReorderTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                 if (columnIndex == 7) {
                    return BigDecimal.class;
                }
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        machineryNeedingReorderTable = new JTable(machineryNeedingReorderTableModel);
        styleTable(machineryNeedingReorderTable);

        machineryNeedingReorderTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        machineryNeedingReorderTable.getColumnModel().getColumn(1).setPreferredWidth(60);
        machineryNeedingReorderTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        machineryNeedingReorderTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        machineryNeedingReorderTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        machineryNeedingReorderTable.getColumnModel().getColumn(5).setPreferredWidth(100);
        machineryNeedingReorderTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        machineryNeedingReorderTable.getColumnModel().getColumn(7).setPreferredWidth(80);


        JScrollPane scrollPane = new JScrollPane(machineryNeedingReorderTable);
        scrollPane.getViewport().setBackground(new Color(40, 40, 40));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);

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
        createMachineryPoButton.addActionListener(e -> createMachineryPurchaseOrder());
        buttonPanel.add(createMachineryPoButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

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
                loadMachineryNeedingReorder();
            }
        });
        paginationPanel.add(machineryNextButton);
        bottomPanel.add(paginationPanel, BorderLayout.CENTER);


        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }


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
            columns = new String[]{"Select", "PO ID", "PO Number", "PO Date", "Status", "Total Amount", "Print PO"};
             tableModel = new DefaultTableModel(columns, 0) {
                 @Override
                public Class<?> getColumnClass(int columnIndex) {
                    if (columnIndex == 0) {
                        return Boolean.class;
                    }
                     if (columnIndex == getColumnIndex("Total Amount", this)) {
                        return BigDecimal.class;
                    }
                     if (columnIndex == getColumnIndex("Print PO", this)) {
                         return JButton.class;
                     }
                    return super.getColumnClass(columnIndex);
                }

                @Override
                public boolean isCellEditable(int row, int column) {
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


            pendingOrdersTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            styleTable(pendingOrdersTable);


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

            approveSelectedButton = new JButton("Approve Selected");
            styleButton(approveSelectedButton, new Color(52, 152, 219));
            approveSelectedButton.addActionListener(e -> approveSelectedPOs());
            buttonPanel.add(approveSelectedButton);

            receiveSelectedButton = new JButton("Receive Selected");
            styleButton(receiveSelectedButton, new Color(46, 204, 113));
            receiveSelectedButton.addActionListener(e -> receiveSelectedPOs());
            buttonPanel.add(receiveSelectedButton);

            viewPendingPoButton = new JButton("View Details");
            styleButton(viewPendingPoButton, new Color(52, 152, 219));
            viewPendingPoButton.addActionListener(e -> viewPurchaseOrderDetails(pendingOrdersTable));
            buttonPanel.add(viewPendingPoButton);

            JPanel tablePanel = new JPanel(new BorderLayout());
            tablePanel.setOpaque(false);
            tablePanel.add(selectAllPanel, BorderLayout.NORTH);
            JScrollPane scrollPane = new JScrollPane(pendingOrdersTable);
            scrollPane.getViewport().setBackground(new Color(40, 40, 40));
            tablePanel.add(scrollPane, BorderLayout.CENTER);
            panel.add(tablePanel, BorderLayout.CENTER);

            setupPrintPoButtonColumn(pendingOrdersTable, pendingOrdersTableModel);


        } else {
            columns = new String[]{"PO ID", "PO Number", "PO Date", "Status", "Total Amount", "Print PO"};
             tableModel = new DefaultTableModel(columns, 0) {
                 @Override
                public boolean isCellEditable(int row, int column) {
                     return column == getColumnIndex("Print PO", this);
                }

                 @Override
                public Class<?> getColumnClass(int columnIndex) {
                     if (columnIndex == getColumnIndex("Total Amount", this)) {
                        return BigDecimal.class;
                    }
                     if (columnIndex == getColumnIndex("Print PO", this)) {
                         return JButton.class;
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


            completedOrdersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            styleTable(completedOrdersTable);


            JScrollPane scrollPane = new JScrollPane(completedOrdersTable);
            scrollPane.getViewport().setBackground(new Color(40, 40, 40));
            panel.add(scrollPane, BorderLayout.CENTER);


            viewCompletedPoButton = new JButton("View Details");
            styleButton(viewCompletedPoButton, new Color(52, 152, 219));
            viewCompletedPoButton.addActionListener(e -> viewPurchaseOrderDetails(completedOrdersTable));
            buttonPanel.add(viewCompletedPoButton);

            setupPrintPoButtonColumn(completedOrdersTable, completedOrdersTableModel);
        }

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Verdana", Font.BOLD, 12));
        searchLabel.setForeground(Color.WHITE);
        searchPanel.add(searchLabel);
        searchField.setFont(new Font("Verdana", Font.PLAIN, 12));
        searchPanel.add(searchField);
        styleButton(searchButton, new Color(52, 152, 219));
        searchButton.addActionListener(e -> {
            if (title.equals("Pending Orders")) {
                currentPendingSearchText = pendingSearchField.getText().trim();
                currentPendingPage = 1;
                loadOrders("Pending");
            } else {
                currentCompletedSearchText = completedSearchField.getText().trim();
                currentCompletedPage = 1;
                loadOrders("Completed");
            }
        });
        searchPanel.add(searchButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        if (title.equals("Pending Orders")) {
             JPanel selectAllPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
             selectAllPanel.setOpaque(false);
             selectAllPanel.add(selectAllPendingCheckbox);
             topPanel.add(selectAllPanel, BorderLayout.WEST);
        }
        topPanel.add(searchPanel, BorderLayout.CENTER);


        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

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
            } else {
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
            } else {
                 int totalPages = (int) Math.ceil((double) totalCompletedOrders / COMPLETED_ORDERS_PER_PAGE);
                 if (currentCompletedPage < totalPages) {
                    currentCompletedPage++;
                    loadOrders("Completed");
                }
            }
        });
        paginationPanel.add(nextButton);
        bottomPanel.add(paginationPanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);


        return panel;
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

    private void setupPrintPoButtonColumn(JTable table, DefaultTableModel model) {
        int printColumnIndex = getColumnIndex("Print PO", model);
        if (printColumnIndex == -1) {
            System.err.println("Error: 'Print PO' column not found in table model.");
            return;
        }

        table.getColumnModel().getColumn(printColumnIndex).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(printColumnIndex).setCellEditor(new ButtonEditor(new JCheckBox()));

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if (column == printColumnIndex && row >= 0) {
                    int modelRow = table.convertRowIndexToModel(row);
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
                 int column = table.columnAtPoint(e.getPoint());
                 int row = table.rowAtPoint(e.getPoint());
                 int printColumnIndex = getColumnIndex("Print PO", (DefaultTableModel) table.getModel());
                 if (column == printColumnIndex && row >= 0) {
                     int poIdColumnIndex = (table == pendingOrdersTable) ? getColumnIndex("PO ID", pendingOrdersTableModel) : getColumnIndex("PO ID", completedOrdersTableModel);
                     if (poIdColumnIndex != -1) {
                         Object poIdValue = table.getModel().getValueAt(table.convertRowIndexToModel(row), poIdColumnIndex);
                          if (poIdValue instanceof Integer) {
                              ((ButtonEditor)table.getCellEditor(row, column)).setPoId((Integer) poIdValue);
                          }
                     }
                      table.getCellEditor(row, column).stopCellEditing();
                 }
            }
        });
    }

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

        StringBuilder baseCountSql = new StringBuilder("SELECT COUNT(*) FROM PurchaseOrders ");
        StringBuilder baseDataSql = new StringBuilder("SELECT ").append(selectColumns).append(" FROM PurchaseOrders ");

        baseCountSql.append(statusClause);
        baseDataSql.append(statusClause);

        if (currentSearchText != null && !currentSearchText.isEmpty()) {
            String searchLike = "%" + currentSearchText.toLowerCase() + "%";
            baseCountSql.append(" AND (LOWER(CAST(POID AS CHAR)) LIKE ? OR LOWER(PONumber) LIKE ? OR LOWER(PODate) LIKE ? OR LOWER(Status) LIKE ? OR LOWER(CAST(TotalAmount AS CHAR)) LIKE ?)");
            baseDataSql.append(" AND (LOWER(CAST(POID AS CHAR)) LIKE ? OR LOWER(PONumber) LIKE ? OR LOWER(PODate) LIKE ? OR LOWER(Status) LIKE ? OR LOWER(CAST(TotalAmount AS CHAR)) LIKE ?)");
        }

        baseDataSql.append(" ORDER BY PODate DESC LIMIT ? OFFSET ?");

        countSql = baseCountSql.toString();
        dataSql = baseDataSql.toString();


        int totalItemsForFilter = 0;
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

         if ("Pending".equals(statusFilter)) {
             totalPendingOrders = totalItemsForFilter;
         } else {
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
                          int poId = rs.getInt("POID");

                         modelToUse.addRow(new Object[]{
                            false,
                            poId,
                            rs.getString("PONumber"),
                            rs.getDate("PODate"),
                            status,
                            totalAmount != null ? totalAmount : BigDecimal.ZERO,
                            "Print"
                         });
                     } else if ("Completed".equals(statusFilter)) {
                          String status = rs.getString("Status");
                          BigDecimal totalAmount = rs.getBigDecimal("TotalAmount");
                          int poId = rs.getInt("POID");

                         modelToUse.addRow(new Object[]{
                            poId,
                            rs.getString("PONumber"),
                            rs.getDate("PODate"),
                            status,
                            totalAmount != null ? totalAmount : BigDecimal.ZERO,
                            "Print"
                         });
                     }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading " + statusFilter + " orders with search: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error loading " + statusFilter + " orders: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if ("Pending".equals(statusFilter) && selectAllPendingCheckbox != null) {
                selectAllPendingCheckbox.setSelected(false);
            }
            updatePaginationControls(statusFilter);
        }
    }

    private void loadItemsNeedingReorder() {
         if (conn == null) {
            System.err.println("Database connection not available. Cannot load items needing reorder.");
            return;
        }

        itemsNeedingReorderTableModel.setRowCount(0);

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

        String dataSql = "SELECT i.ItemID, i.ItemName, c.CategoryName, i.Quantity, i.ReorderLevel, i.UnitPrice " +
                         "FROM Items i " +
                         "LEFT JOIN Categories c ON i.CategoryID = c.CategoryID " +
                         "WHERE i.Quantity <= i.ReorderLevel " +
                         "AND i.IsMachinery = FALSE " +
                         "AND NOT EXISTS ( " +
                         "    SELECT 1 " +
                         "    FROM PurchaseOrderItems poi " +
                         "    JOIN PurchaseOrders po ON poi.POID = po.POID " +
                         "    WHERE poi.ItemID = i.ItemID AND po.Status NOT IN ('Received', 'Cancelled') " +
                         ") " +
                         "ORDER BY i.ItemName LIMIT ? OFFSET ?";

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

                    int suggestedOrderQty = Math.max(reorderLevel - currentQty + REORDER_BUFFER, MIN_ORDER_QUANTITY);

                    Vector<Object> row = new Vector<>();
                    row.add(false);
                    row.add(rs.getInt("ItemID"));
                    row.add(rs.getString("ItemName"));
                    String categoryName = rs.getString("CategoryName");
                    row.add(categoryName != null ? categoryName : "N/A");
                    row.add(currentQty);
                    row.add(reorderLevel);
                    row.add(suggestedOrderQty);
                    row.add(unitPrice);
                    itemsNeedingReorderTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading items needing reorder: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading items needing reorder.", "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (selectAllItemsCheckbox != null) {
                selectAllItemsCheckbox.setSelected(false);
            }
            updatePaginationControls("ItemsNeedingReorder");
        }
    }

    private void loadMachineryNeedingReorder() {
         if (conn == null) {
            System.err.println("Database connection not available. Cannot load machinery needing reorder.");
            return;
        }

        machineryNeedingReorderTableModel.setRowCount(0);

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

        String dataSql = "SELECT i.ItemID, i.ItemName, c.CategoryName, i.Quantity, i.ReorderLevel, i.UnitPrice " +
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
                         ") " +
                         "ORDER BY i.ItemName LIMIT ? OFFSET ?";

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

                    int suggestedOrderQty = Math.max(reorderLevel - currentQty + REORDER_BUFFER, MIN_ORDER_QUANTITY);

                    Vector<Object> row = new Vector<>();
                    row.add(false);
                    row.add(rs.getInt("ItemID"));
                    row.add(rs.getString("ItemName"));
                    String categoryName = rs.getString("CategoryName");
                    row.add(categoryName != null ? categoryName : "N/A");
                    row.add(currentQty);
                    row.add(reorderLevel);
                    row.add(suggestedOrderQty);
                    row.add(unitPrice);
                    machineryNeedingReorderTableModel.addRow(row);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading machinery needing reorder: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading machinery needing reorder.", "Database Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (selectAllMachineryCheckbox != null) {
                selectAllMachineryCheckbox.setSelected(false);
            }
            updatePaginationControls("MachineryNeedingReorder");
        }
    }


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

    private void createPurchaseOrder() {
        if (currentUser == null || (!"Admin".equals(currentUser.getRole()) && !"Custodian".equals(currentUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "You do not have permission to create purchase orders.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        createPurchaseOrderFromTable(itemsNeedingReorderTable, itemsNeedingReorderTableModel);
    }

    private void createMachineryPurchaseOrder() {
        if (currentUser == null || (!"Admin".equals(currentUser.getRole()) && !"Custodian".equals(currentUser.getRole()))) {
            JOptionPane.showMessageDialog(this, "You do not have permission to create purchase orders.", "Permission Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
         createPurchaseOrderFromTable(machineryNeedingReorderTable, machineryNeedingReorderTableModel);
    }


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

        List<Integer> itemIds = new ArrayList<>();
        List<String> itemDescriptions = new ArrayList<>();
        List<Integer> quantitiesOrdered = new ArrayList<>();
        List<BigDecimal> itemUnitPrices = new ArrayList<>();

        BigDecimal totalAmount = BigDecimal.ZERO;

        boolean hasItemsToOrder = false;

        int selectColumnIndex = 0;
        int itemIdColumnIndex = getColumnIndex("Item ID", model);
        int suggestedQtyColumnIndex = getColumnIndex("Suggested Order Qty", model);
        int unitPriceColumnIndex = getColumnIndex("Unit Price", model);
        int itemNameColumnIndex = getColumnIndex("Item Name", model);

        if (itemIdColumnIndex == -1 || suggestedQtyColumnIndex == -1 || unitPriceColumnIndex == -1 || itemNameColumnIndex == -1) {
             System.err.println("Error: Required columns not found in the table model.");
             JOptionPane.showMessageDialog(this, "Internal error: Could not find required table columns.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }


        for (int rowIndex : selectedRows) {
             try {
                int itemId = (Integer) model.getValueAt(rowIndex, itemIdColumnIndex);
                String itemName = (String) model.getValueAt(rowIndex, itemNameColumnIndex);
                int suggestedQty = (Integer) model.getValueAt(rowIndex, suggestedQtyColumnIndex);
                BigDecimal unitPrice = (BigDecimal) model.getValueAt(rowIndex, unitPriceColumnIndex);

                int quantityToOrder = suggestedQty;

                if (quantityToOrder > 0) {
                    itemIds.add(itemId);
                    itemDescriptions.add(itemName);
                    quantitiesOrdered.add(quantityToOrder);
                    itemUnitPrices.add(unitPrice);

                    BigDecimal subtotal = unitPrice.multiply(new BigDecimal(quantityToOrder));
                    totalAmount = totalAmount.add(subtotal);

                    hasItemsToOrder = true;
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

         if (!hasItemsToOrder) {
             JOptionPane.showMessageDialog(this, "Selected items have a suggested order quantity of 0. No purchase order will be created.", "No Items to Order", JOptionPane.WARNING_MESSAGE);
             if (table == itemsNeedingReorderTable && selectAllItemsCheckbox != null) {
                selectAllItemsCheckbox.setSelected(false);
            } else if (table == machineryNeedingReorderTable && selectAllMachineryCheckbox != null) {
                 selectAllMachineryCheckbox.setSelected(false);
            }
             loadItemsNeedingReorder();
             loadMachineryNeedingReorder();
             return;
         }


        try {
            conn.setAutoCommit(false);

            String insertPoSql = "INSERT INTO PurchaseOrders (PONumber, PODate, Status, CreatedBy, TotalAmount) VALUES (?, ?, ?, ?, ?)";
            int poId = -1;
            try (PreparedStatement pstmtPo = conn.prepareStatement(insertPoSql, Statement.RETURN_GENERATED_KEYS)) {

                String poNumber = generatePONumber();
                LocalDate currentDate = LocalDate.now();

                pstmtPo.setString(1, poNumber);
                pstmtPo.setDate(2, java.sql.Date.valueOf(currentDate));
                pstmtPo.setString(3, "Draft");
                pstmtPo.setInt(4, currentUser.getUserId());
                pstmtPo.setBigDecimal(5, totalAmount);

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

            String insertPoItemSql = "INSERT INTO PurchaseOrderItems (POID, ItemID, Description, QuantityOrdered) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmtPoItem = conn.prepareStatement(insertPoItemSql)) {
                for (int i = 0; i < itemIds.size(); i++) {
                     if (quantitiesOrdered.get(i) > 0) {
                         pstmtPoItem.setInt(1, poId);
                         pstmtPoItem.setInt(2, itemIds.get(i));
                         pstmtPoItem.setString(3, itemDescriptions.get(i));
                         pstmtPoItem.setInt(4, quantitiesOrdered.get(i));
                         pstmtPoItem.addBatch();
                     }
                }
                pstmtPoItem.executeBatch();
            }

            conn.commit();

            JOptionPane.showMessageDialog(this, "Purchase Order " + poNumber + " created successfully as Draft.", "Success", JOptionPane.INFORMATION_MESSAGE);


            int[] selectedViewRows = table.getSelectedRows();
            List<Integer> selectedModelRows = new ArrayList<>();
            for(int viewRow : selectedViewRows) {
                selectedModelRows.add(table.convertRowIndexToModel(viewRow));
            }
            selectedModelRows.sort((a, b) -> b.compareTo(a));

            for (int modelRowIndex : selectedModelRows) {
                 model.removeRow(modelRowIndex);
            }

            if (table == itemsNeedingReorderTable && selectAllItemsCheckbox != null) {
                selectAllItemsCheckbox.setSelected(false);
            } else if (table == machineryNeedingReorderTable && selectAllMachineryCheckbox != null) {
                 selectAllMachineryCheckbox.setSelected(false);
            }


            tabbedPane.setSelectedIndex(2);
            loadOrders("Pending");

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
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
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                System.err.println("Error restoring auto-commit: " + ex.getMessage());
            }
        }
    }


    private String generatePONumber() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = today.format(formatter);

        long sequence = System.currentTimeMillis() % 100000;

        return "PO-" + datePart + "-" + sequence;
    }


    private void viewPurchaseOrderDetails(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase order to view.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (table.getSelectedRowCount() > 1) {
             JOptionPane.showMessageDialog(this, "Please select only one purchase order to view details.", "Multiple Selection", JOptionPane.WARNING_MESSAGE);
             return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int poIdColumnIndex = (table == pendingOrdersTable) ? 1 : 0;
        int poId = (Integer) table.getModel().getValueAt(modelRow, poIdColumnIndex);


        Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(this);

        PurchaseOrderDetailsDialog detailsDialog = new PurchaseOrderDetailsDialog(parentFrame, true, conn, currentUser, poId);
        detailsDialog.setPurchaseOrderActionListener(this);
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setVisible(true);
    }

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
                    poContent.append(String.format("%-15s: %s\n", "Created By", rsPo.getInt("CreatedBy")));
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


    private void printPurchaseOrder(int poId) {
        System.out.println("Attempting to print PO ID: " + poId);
        String poContent = generatePurchaseOrderContent(poId);

        if (poContent == null || poContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Could not generate content for printing.", "Printing Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextArea printableArea = new JTextArea(poContent);
        printableArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        printableArea.setBackground(Color.WHITE);
        printableArea.setForeground(Color.BLACK);


        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

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

    private void approveSelectedPOs() {
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
                String currentStatus = getPoStatus(poId);
                if ("Draft".equals(currentStatus) || "Pending Approval".equals(currentStatus)) {
                     boolean success = updatePurchaseOrderStatus(poId, "Approved", null);
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

            loadOrders("Pending");
            loadItemsNeedingReorder();
            loadMachineryNeedingReorder();
        }
    }

    private void receiveSelectedPOs() {
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
                String currentStatus = getPoStatus(poId);
                 if ("Approved".equals(currentStatus) || "Ordered".equals(currentStatus) || "Partially Received".equals(currentStatus)) {
                     boolean success = handleReceiveItems(poId);
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

            loadOrders("Pending");
            loadOrders("Completed");
            loadItemsNeedingReorder();
            loadMachineryNeedingReorder();
        }
    }


    private List<Integer> getSelectedPoIds(JTable table, DefaultTableModel model) {
        List<Integer> selectedPoIds = new ArrayList<>();
        int selectColumnIndex = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean isSelected = (Boolean) model.getValueAt(i, selectColumnIndex);
            if (isSelected != null && isSelected) {
                 int poIdColumnIndex = (table == pendingOrdersTable) ? 1 : 0;
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


    private boolean updatePurchaseOrderStatus(int poId, String newStatus, String expectedCurrentStatus) {
         if (conn == null) {
            System.err.println("Database connection not available. Cannot update PO status for PO ID: " + poId);
            return false;
        }

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

        if (expectedCurrentStatus != null && !actualCurrentStatus.equals(expectedCurrentStatus)) {
            System.err.println("Action cannot be performed for PO ID " + poId + ". Status is '" + actualCurrentStatus + "', but expected '" + expectedCurrentStatus + "'.");
            return false;
        }

        if ("Approved".equals(newStatus) && !("Draft".equals(actualCurrentStatus) || "Pending Approval".equals(actualCurrentStatus))) {
             System.err.println("Cannot approve PO ID " + poId + " with status '" + actualCurrentStatus + "'. It must be 'Draft' or 'Pending Approval'.");
             return false;
        }

        if ("Cancelled".equals(newStatus) && ("Received".equals(actualCurrentStatus) || "Cancelled".equals(actualCurrentStatus))) {
             System.err.println("Cannot cancel PO ID " + poId + " that is already '" + actualCurrentStatus + "'.");
             return false;
        }

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

    private boolean handleReceiveItems(int poId) {
         if (conn == null) {
            System.err.println("Database connection not available. Cannot receive items for PO ID: " + poId);
            return false;
        }

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


         if ("Approved".equals(currentStatusFromDB) || "Ordered".equals(currentStatusFromDB) || "Partially Received".equals(currentStatusFromDB)) {
             try {
                 conn.setAutoCommit(false);

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

                 conn.commit();
                 return true;

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
                 return false;

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
             return false;
         }
    }

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

    @Override
    public void purchaseOrderChanged() {
        System.out.println("Purchase order status changed. Refreshing tables.");
        currentPendingPage = 1;
        currentCompletedPage = 1;
        currentItemsPage = 1;
        currentMachineryPage = 1;
        loadOrders("Pending");
        loadOrders("Completed");
        loadItemsNeedingReorder();
        loadMachineryNeedingReorder();
    }

    private void updateButtonVisibility() {
        boolean isAdmin = currentUser != null && "Admin".equals(currentUser.getRole());
        boolean isCustodian = currentUser != null && "Custodian".equals(currentUser.getRole());

        createPoButton.setVisible(isAdmin || isCustodian);
        createMachineryPoButton.setVisible(isAdmin || isCustodian);
        selectAllItemsCheckbox.setVisible(isAdmin || isCustodian);
        selectAllMachineryCheckbox.setVisible(isAdmin || isCustodian);


        approveSelectedButton.setVisible(isAdmin);
        receiveSelectedButton.setVisible(isAdmin);
        selectAllPendingCheckbox.setVisible(isAdmin || isCustodian);
        viewPendingPoButton.setVisible(currentUser != null);

        viewCompletedPoButton.setVisible(currentUser != null);

        updatePaginationControlsVisibility();
    }

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
                return;
        }

        int totalPages = (int) Math.ceil((double) totalItemsForTab / itemsPerPage);
        if (totalPages == 0) totalPages = 1;

        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }

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
        pendingSearchField.setVisible(isUserLoggedIn);
        pendingSearchButton.setVisible(isUserLoggedIn);


        completedPrevButton.setVisible(isUserLoggedIn);
        completedNextButton.setVisible(isUserLoggedIn);
        completedPageLabel.setVisible(isUserLoggedIn);
        completedSearchField.setVisible(isUserLoggedIn);
        completedSearchButton.setVisible(isUserLoggedIn);
    }


    private int getColumnIndex(String columnName, DefaultTableModel model) {
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (model.getColumnName(i).equals(columnName)) {
                return i;
            }
        }
        return -1;
    }

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


    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
             setForeground(Color.BLACK);
             setBackground(UIManager.getColor("Button.background"));
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

    class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

        private JButton button;
        private String label;
        private boolean isPushed;
        private JTable table;
        private int poId;


        public ButtonEditor(JCheckBox checkBox) {
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(this);
             button.setForeground(Color.BLACK);
             button.setBackground(UIManager.getColor("Button.background"));
             button.setBorderPainted(true);
             button.setFocusPainted(false);
        }

        public void setPoId(Integer poId) {
            this.poId = poId;
        }


        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
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
            if (isPushed) {
            }
            isPushed = false;
            return label;
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
                System.out.println("Print button clicked for PO ID: " + poId);
                printPurchaseOrder(poId);

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
