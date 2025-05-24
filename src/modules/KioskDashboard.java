package modules;

import Package1.DBConnection;
import Package1.User;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.JCheckBox;
import javax.swing.table.JTableHeader;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableColumn;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.JTabbedPane;
import javax.swing.UIManager; // Import UIManager
import javax.swing.plaf.ColorUIResource; // Import ColorUIResource


import modules.BorrowItemDialog.BorrowCompleteListener;
import modules.BorrowItemDialog.BorrowItemDetails;


public class KioskDashboard extends javax.swing.JPanel implements BorrowCompleteListener {

  

    private JTable consumableItemsTable;
    private DefaultTableModel consumableTableModel;
    private JTable returnableItemsTable;
    private DefaultTableModel returnableTableModel;

    private JTextField searchField;

    private Connection conn = null;
    private User currentUser;

    private int currentPage = 1;
    private final int itemsPerPage = 10;
    private int totalAvailableItems = 0;
    private JButton jButtonPreviousPage;
    private JButton jButtonNextPage;
    private JLabel jLabelPageInfo;

    private AvailableItemsLoader currentDataLoader;

    private JCheckBox selectAllCheckBoxConsumable;
    private JCheckBox selectAllCheckBoxReturnable;

    private JTabbedPane mainTabbedPane;


    public KioskDashboard() {
        setupKioskDashboardPanel();
        if (!connectToDatabase()) {
            JOptionPane.showMessageDialog(this, "Database connection failed. Kiosk features disabled.", "Connection Error", JOptionPane.ERROR_MESSAGE);
        } else {
            fetchTotalAvailableItemCount();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        currentPage = 1;
        fetchTotalAvailableItemCount();
    }

    public void setCurrentUserId(int userId) {
        if (userId <= 0) {
            setCurrentUser(null);
            return;
        }

        User user = fetchUserById(userId);
        setCurrentUser(user);
    }

     private User fetchUserById(int userId) {
        if (conn == null) {
            if (!connectToDatabase()) {
                 return null;
            }
        }
        String sql = "SELECT UserID, Username, FullName, Role FROM Users WHERE UserID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("UserID"));
                    user.setUsername(rs.getString("Username"));
                    user.setFullName(rs.getString("FullName"));
                    user.setRole(rs.getString("Role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(KioskDashboard.class.getName()).log(Level.SEVERE, "Error fetching user by ID", e);
        }
        return null;
    }


    private boolean connectToDatabase() {
        try {
            conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(KioskDashboard.class.getName()).log(Level.SEVERE, "Error connecting to database", ex);
            return false;
        }
    }

    private void setupKioskDashboardPanel() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new Color(30, 30, 30));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel searchPanel = createSearchPanel();
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        this.add(topPanel, BorderLayout.NORTH);

        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setBackground(new Color(30, 30, 30));
        mainTabbedPane.setForeground(new Color(220, 220, 220));
        mainTabbedPane.setFont(new Font("Verdana", Font.BOLD, 14));
        // Removed mainTabbedPane.setBorder(null) here as it's now handled by UIManager.put("TabbedPane.contentBorder", ...)

        JPanel consumableItemsPanel = createItemsTablePanel("Consumable Items", true);
        mainTabbedPane.addTab("Request Item(s)", consumableItemsPanel);

        JPanel returnableItemsPanel = createItemsTablePanel("Borrow Item(s)", false);
        mainTabbedPane.addTab("Borrow Item(s)", returnableItemsPanel);

        this.add(mainTabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setOpaque(false);

        JButton borrowButton = new JButton("Process Selected Item(s)");
        // Changed borrow button color to green to match the reference image
        styleActionButton(borrowButton, new Color(46, 204, 113));
        borrowButton.addActionListener((ActionEvent e) -> openBorrowItemDialog());
        bottomPanel.add(borrowButton);

        this.add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search Available Items:");
        searchLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        searchLabel.setForeground(new Color(200, 200, 200));
        panel.add(searchLabel);

        searchField = new JTextField(25);
        searchField.setFont(new Font("Verdana", Font.PLAIN, 14));
        searchField.setBackground(new Color(60, 60, 60));
        searchField.setForeground(new Color(220, 220, 220));
        searchField.setCaretColor(new Color(220, 220, 220));
        panel.add(searchField);

        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(new Font("Verdana", Font.BOLD, 14));
        searchBtn.setBackground(new Color(41, 128, 185));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.addActionListener((ActionEvent e) -> searchAvailableItems());
        panel.add(searchBtn);

        return panel;
    }

    private JPanel createItemsTablePanel(String title, boolean isConsumableTab) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(30, 30, 30)),
                title, javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Verdana", Font.BOLD, 14), new Color(30, 30, 30)));

        String[] columns = {"Select", "ID", "Name", "Category", "Type", "Qty", "Unit", "Status", "Condition", "Location", "Image"};
        DefaultTableModel currentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                 if (columnIndex == 0) {
                    return Boolean.class;
                } else if (columnIndex == 10) {
                    return ImageIcon.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };

        JTable currentTable = new JTable(currentTableModel);
        currentTable.setForeground(new Color(220, 220, 220));
        currentTable.setBackground(new Color(30, 30, 30)); // Changed table background to match overall theme
        currentTable.setGridColor(new Color(40, 40, 40)); // Keep this for internal grid lines
        currentTable.setSelectionBackground(new Color(40, 40, 40));
        currentTable.setSelectionForeground(new Color(255, 255, 255));
        currentTable.setFont(new Font("Verdana", Font.PLAIN, 12));
        currentTable.setRowHeight(60);
        currentTable.setAutoCreateRowSorter(true);
        currentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader tableHeader = currentTable.getTableHeader();
        tableHeader.setFont(new Font("Verdana", Font.BOLD, 12));
        tableHeader.setBackground(new Color(35, 35, 35)); // Changed table header background to be slightly lighter
        tableHeader.setForeground(new Color(255, 255, 255));
        tableHeader.setReorderingAllowed(false);

        JCheckBox currentSelectAllCheckBox = new JCheckBox();
        currentSelectAllCheckBox.setBackground(new Color(35, 35, 35)); // Aligned with table header background
        currentSelectAllCheckBox.setOpaque(false);
        currentSelectAllCheckBox.setHorizontalAlignment(SwingConstants.CENTER);

        TableColumn selectColumn = currentTable.getColumnModel().getColumn(0);
        selectColumn.setHeaderRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                return currentSelectAllCheckBox;
            }
        });

        selectColumn.setCellRenderer(currentTable.getDefaultRenderer(Boolean.class));
        selectColumn.setCellEditor(currentTable.getDefaultEditor(Boolean.class));

        tableHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int column = tableHeader.columnAtPoint(e.getPoint());
                if (column == 0) {
                    boolean newState = !currentSelectAllCheckBox.isSelected();
                    currentSelectAllCheckBox.setSelected(newState);
                    for (int i = 0; i < currentTableModel.getRowCount(); i++) {
                        currentTableModel.setValueAt(newState, i, 0);
                    }
                }
            }
        });

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 1; i < currentTable.getColumnCount(); i++) {
             if (i != 10) {
                currentTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
             }
        }

         currentTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                if ("Low Stock".equalsIgnoreCase(status)) {
                    c.setForeground(new Color(231, 76, 60));
                } else if ("Out of Stock".equalsIgnoreCase(status)) {
                    c.setForeground(new Color(255, 165, 0));
                } else if ("In Stock".equalsIgnoreCase(status)) {
                    c.setForeground(new Color(46, 204, 113));
                } else {
                    c.setForeground(isSelected ? table.getSelectionForeground() : new Color(220, 220, 220));
                }
                c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

         currentTable.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setText("");
                if (value instanceof ImageIcon) {
                    ImageIcon originalIcon = (ImageIcon) value;
                    Image scaled = originalIcon.getImage().getScaledInstance(-1, 50, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaled));
                } else {
                    label.setIcon(null);
                }
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setVerticalAlignment(JLabel.CENTER);
                 label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return label;
            }
        });

        currentTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        currentTable.getColumnModel().getColumn(1).setPreferredWidth(40);
        currentTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        currentTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        currentTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        currentTable.getColumnModel().getColumn(5).setPreferredWidth(50);
        currentTable.getColumnModel().getColumn(6).setPreferredWidth(60);
        currentTable.getColumnModel().getColumn(7).setPreferredWidth(80);
        currentTable.getColumnModel().getColumn(8).setPreferredWidth(90);
        currentTable.getColumnModel().getColumn(9).setPreferredWidth(100);
        currentTable.getColumnModel().getColumn(10).setPreferredWidth(70);

        currentTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getColumn() == 0 || e.getColumn() == TableModelEvent.ALL_COLUMNS) {
                    updateSelectAllCheckBox(currentTableModel, currentSelectAllCheckBox);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(currentTable);
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 30)));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paginationPanel.setOpaque(false);
        jButtonPreviousPage = new JButton("Previous");
        // Changed pagination button color to blue to match the reference image
        stylePaginationButton(jButtonPreviousPage, new Color(52, 152, 219));
        jButtonPreviousPage.addActionListener(e -> gotoPreviousPage());
        paginationPanel.add(jButtonPreviousPage);

        jLabelPageInfo = new JLabel("Page 1 of 1");
        jLabelPageInfo.setFont(new Font("Verdana", Font.PLAIN, 12));
        jLabelPageInfo.setForeground(new Color(30, 30, 30));
        paginationPanel.add(jLabelPageInfo);

        jButtonNextPage = new JButton("Next");
        // Changed pagination button color to blue to match the reference image
        stylePaginationButton(jButtonNextPage, new Color(52, 152, 219));
        jButtonNextPage.addActionListener(e -> gotoNextPage());
        paginationPanel.add(jButtonNextPage);
        panel.add(paginationPanel, BorderLayout.SOUTH);

        if (isConsumableTab) {
            consumableItemsTable = currentTable;
            consumableTableModel = currentTableModel;
            selectAllCheckBoxConsumable = currentSelectAllCheckBox;
        } else {
            returnableItemsTable = currentTable;
            returnableTableModel = currentTableModel;
            selectAllCheckBoxReturnable = currentSelectAllCheckBox;
        }

        return panel;
    }


     private void styleActionButton(JButton button, Color bgColor) {
        button.setFont(new Font("Verdana", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }

     private void stylePaginationButton(JButton button, Color bgColor) { // Added bgColor parameter
        button.setFont(new Font("Verdana", Font.PLAIN, 12));
        button.setBackground(bgColor); // Use bgColor
        button.setForeground(Color.WHITE);
         button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1), BorderFactory.createEmptyBorder(3, 8, 3, 8)));
    }


    private void fetchTotalAvailableItemCount() {
        if (conn == null) return;
        String searchText = searchField.getText().trim().toLowerCase();

        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) AS total FROM Items i");
        sqlBuilder.append(" LEFT JOIN Categories cat ON i.CategoryID = cat.CategoryID");
        sqlBuilder.append(" WHERE i.IsArchived = FALSE AND i.Quantity > 0");

        if (!searchText.isEmpty()) {
            sqlBuilder.append(" AND (LOWER(i.ItemName) LIKE ? OR LOWER(i.Description) LIKE ? OR LOWER(i.SerialNumber) LIKE ? OR LOWER(cat.CategoryName) LIKE ?)");
        }

        final String sql = sqlBuilder.toString();

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int count = 0;
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    int paramIndex = 1;
                    if (!searchText.isEmpty()) {
                        String searchTerm = "%" + searchText + "%";
                        pstmt.setString(paramIndex++, searchTerm);
                        pstmt.setString(paramIndex++, searchTerm);
                        pstmt.setString(paramIndex++, searchTerm);
                        pstmt.setString(paramIndex++, searchTerm);
                    }
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            count = rs.getInt("total");
                        }
                    }
                } catch (SQLException e) {
                    Logger.getLogger(KioskDashboard.class.getName()).log(Level.SEVERE, "Error fetching total item count", e);
                }
                return count;
            }

            @Override
            protected void done() {
                try {
                    totalAvailableItems = get();
                    int totalPages = (int) Math.ceil((double) totalAvailableItems / itemsPerPage);
                    totalPages = Math.max(totalPages, 1);
                    if (currentPage > totalPages) {
                        currentPage = totalPages;
                    }
                    updatePaginationControls();
                    refreshTableData();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(KioskDashboard.this, "Error updating available item count: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void refreshTableData() {
         if (conn == null) {
            SwingUtilities.invokeLater(() -> {
                consumableTableModel.setRowCount(0);
                returnableTableModel.setRowCount(0);
            });
            return;
        }

        if (currentDataLoader != null && !currentDataLoader.isDone()) {
            currentDataLoader.cancel(true);
        }

        String searchText = searchField.getText().trim().toLowerCase();
        int offset = (currentPage - 1) * itemsPerPage;

        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT i.ItemID, i.ItemName, c.CategoryName, i.Quantity, i.Unit, i.Status, " +
            "i.ItemCondition, i.Location, i.ItemImage, NOT i.IsMachinery AS IsConsumable " +
            "FROM Items i " +
            "LEFT JOIN Categories c ON i.CategoryID = c.CategoryID"
        );

        sqlBuilder.append(" WHERE i.IsArchived = FALSE AND i.Quantity > 0");

        if (!searchText.isEmpty()) {
            sqlBuilder.append(" AND (LOWER(i.ItemName) LIKE ? OR LOWER(i.Description) LIKE ? OR LOWER(i.SerialNumber) LIKE ? OR LOWER(c.CategoryName) LIKE ?)");
        }
        sqlBuilder.append(" ORDER BY i.ItemID DESC LIMIT ? OFFSET ?");

        final String finalSql = sqlBuilder.toString();
        currentDataLoader = new AvailableItemsLoader(finalSql, searchText, offset, itemsPerPage);
        currentDataLoader.execute();
    }


    private class AvailableItemsLoader extends SwingWorker<Void, Object[]> {
        private final String sql;
        private final String searchText;
        private final int offset;
        private final int limit;


        public AvailableItemsLoader(String sql, String searchText, int offset, int limit) {
            this.sql = sql;
            this.searchText = searchText;
            this.offset = offset;
            this.limit = limit;
            SwingUtilities.invokeLater(() -> {
                 consumableTableModel.setRowCount(0);
                 returnableTableModel.setRowCount(0);
                 updateSelectAllCheckBox(consumableTableModel, selectAllCheckBoxConsumable);
                 updateSelectAllCheckBox(returnableTableModel, selectAllCheckBoxReturnable);
            });
        }

        @Override
        protected Void doInBackground() throws Exception {
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int paramIndex = 1;
                if (!searchText.isEmpty()) {
                    String searchTerm = "%" + searchText + "%";
                    pstmt.setString(paramIndex++, searchTerm);
                    pstmt.setString(paramIndex++, searchTerm);
                    pstmt.setString(paramIndex++, searchTerm);
                    pstmt.setString(paramIndex++, searchTerm);
                }
                pstmt.setInt(paramIndex++, limit);
                pstmt.setInt(paramIndex++, offset);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        if (isCancelled()) {
                            break;
                        }
                        ImageIcon thumb = null;
                        byte[] imgData = rs.getBytes("ItemImage");
                        if (imgData != null && imgData.length > 0) {
                            try {
                                ImageIcon orig = new ImageIcon(imgData);
                                Image scaled = orig.getImage().getScaledInstance(-1, 50, Image.SCALE_SMOOTH);
                                thumb = new ImageIcon(scaled);
                            } catch (Exception ix) {
                                Logger.getLogger(KioskDashboard.class.getName()).log(Level.WARNING, "Error scaling image for item ID: " + rs.getInt("ItemID"), ix);
                            }
                        }
                        boolean isConsumable = rs.getBoolean("IsConsumable");

                        publish(new Object[]{
                            false,
                            rs.getInt("ItemID"),
                            rs.getString("ItemName"),
                            rs.getString("CategoryName"),
                            isConsumable ? "Consumable" : "Machinery/Furniture",
                            rs.getInt("Quantity"),
                            rs.getString("Unit"),
                            rs.getString("Status"),
                            rs.getString("ItemCondition"),
                            rs.getString("Location"),
                            thumb,
                            isConsumable
                        });
                    }
                }
            } catch (SQLException e) {
                 SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(KioskDashboard.this, "Error loading available item data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
                );
                Logger.getLogger(KioskDashboard.class.getName()).log(Level.SEVERE, "SQL Error loading available items", e);
            }
            return null;
        }

        @Override
        protected void process(java.util.List<Object[]> chunks) {
            if (!isCancelled()) {
                for (Object[] rowDataWithHidden : chunks) {
                    boolean isConsumable = (boolean) rowDataWithHidden[rowDataWithHidden.length - 1];
                    Object[] displayRowData = new Object[rowDataWithHidden.length - 1];
                    System.arraycopy(rowDataWithHidden, 0, displayRowData, 0, displayRowData.length);
                    
                    if (isConsumable) {
                        consumableTableModel.addRow(displayRowData);
                    } else {
                        returnableTableModel.addRow(displayRowData);
                    }
                }
            }
        }
         @Override
        protected void done() {
            try {
                if (!isCancelled()) {
                    get();
                }
            } catch (Exception e) {
                if (!isCancelled()) {
                     JOptionPane.showMessageDialog(KioskDashboard.this, "Failed to complete available items loading: " + e.getMessage(), "Loading Error", JOptionPane.ERROR_MESSAGE);
                }
                Logger.getLogger(KioskDashboard.class.getName()).log(Level.SEVERE, "Error in AvailableItemsLoader done()", e);
            } finally {
                 currentDataLoader = null;
                 updateSelectAllCheckBox(consumableTableModel, selectAllCheckBoxConsumable);
                 updateSelectAllCheckBox(returnableTableModel, selectAllCheckBoxReturnable);
            }
        }
    }


    private void searchAvailableItems() {
        currentPage = 1;
        fetchTotalAvailableItemCount();
    }

    private void updatePaginationControls() {
        int totalPages = (int) Math.ceil((double) totalAvailableItems / itemsPerPage);
        totalPages = Math.max(totalPages, 1);
        jLabelPageInfo.setText("Page " + currentPage + " of " + totalPages);
        jButtonPreviousPage.setEnabled(currentPage > 1);
        jButtonNextPage.setEnabled(currentPage < totalPages);
        if (totalAvailableItems <= 0) {
            jLabelPageInfo.setText("No available items found");
        }
    }

    private void updateSelectAllCheckBox(DefaultTableModel model, JCheckBox checkBox) {
        boolean allSelected = true;
        if (model.getRowCount() == 0) {
            allSelected = false;
        } else {
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 0) instanceof Boolean && !(Boolean) model.getValueAt(i, 0)) {
                    allSelected = false;
                    break;
                }
            }
        }
        ItemListener[] listeners = checkBox.getItemListeners();
        for (ItemListener listener : listeners) {
            checkBox.removeItemListener(listener);
        }

        checkBox.setSelected(allSelected);

        for (ItemListener listener : listeners) {
            checkBox.addItemListener(listener);
        }
    }


    private void gotoPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            refreshTableData();
            updatePaginationControls();
        }
    }

    private void gotoNextPage() {
        int totalPages = (int) Math.ceil((double) totalAvailableItems / itemsPerPage);
         totalPages = Math.max(totalPages, 1);
        if (currentPage < totalPages) {
            currentPage++;
            refreshTableData();
            updatePaginationControls();
        }
    }

    private void openBorrowItemDialog() {
         List<BorrowItemDetails> itemsToBorrow = new ArrayList<>();
         DefaultTableModel activeTableModel = null;

         int selectedTabIndex = mainTabbedPane.getSelectedIndex();
         if (selectedTabIndex == 0) {
             activeTableModel = consumableTableModel;
         } else if (selectedTabIndex == 1) {
             activeTableModel = returnableTableModel;
         }

         if (activeTableModel == null) {
             JOptionPane.showMessageDialog(this, "No active item table found.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
         }

        for (int i = 0; i < activeTableModel.getRowCount(); i++) {
            if (activeTableModel.getValueAt(i, 0) instanceof Boolean && (Boolean) activeTableModel.getValueAt(i, 0)) {
                 Object itemIdObj = activeTableModel.getValueAt(i, 1);
                 Object itemNameObj = activeTableModel.getValueAt(i, 2);
                 Object itemCategoryObj = activeTableModel.getValueAt(i, 3);
                 String itemTypeStr = activeTableModel.getValueAt(i, 4).toString();


                 if (itemIdObj != null && itemNameObj != null) {
                      try {
                         int itemId = Integer.parseInt(itemIdObj.toString());
                         String itemName = itemNameObj.toString();
                         String itemCategory = itemCategoryObj != null ? itemCategoryObj.toString() : "Unknown";
                         boolean isConsumable = "Consumable".equalsIgnoreCase(itemTypeStr);


                         ItemDetailsForBorrow itemDetails = fetchItemDetailsForBorrow(itemId);

                         if (itemDetails != null && itemDetails.getQuantity() > 0) {
                              itemsToBorrow.add(new BorrowItemDetails(
                                  itemId,
                                  itemName,
                                  itemDetails.getQuantity(),
                                  isConsumable, 
                                  itemCategory
                              ));
                         } else {
                              JOptionPane.showMessageDialog(this, "Selected item '" + itemName + "' is no longer available or details missing.", "Item Unavailable", JOptionPane.WARNING_MESSAGE);
                              activeTableModel.setValueAt(false, i, 0);
                         }

                      } catch (NumberFormatException e) {
                          JOptionPane.showMessageDialog(this, "Error reading selected item data.", "Data Error", JOptionPane.ERROR_MESSAGE);
                          Logger.getLogger(KioskDashboard.class.getName()).log(Level.SEVERE, "NumberFormatException in openBorrowItemDialog", e);
                      }
                 }
            }
        }


        if (itemsToBorrow.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Please select one or more available items to process.", "No Items Selected", JOptionPane.WARNING_MESSAGE);
             return;
        }


        BorrowItemDialog dialog;
        dialog = new BorrowItemDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                true,
                conn,
                currentUser,
                itemsToBorrow,
                this
        );
        dialog.setVisible(true);
    }

    private ItemDetailsForBorrow fetchItemDetailsForBorrow(int itemId) {
         if (conn == null) {
             return null;
         }
         String sql = "SELECT Quantity, NOT IsMachinery AS IsConsumable FROM Items WHERE ItemID = ? AND IsArchived = FALSE";
         try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, itemId);
             try (ResultSet rs = pstmt.executeQuery()) {
                 if (rs.next()) {
                     int quantity = rs.getInt("Quantity");
                     boolean isConsumable = rs.getBoolean("IsConsumable");
                     return new ItemDetailsForBorrow(quantity, isConsumable);
                 }
             }
         } catch (SQLException e) {
             Logger.getLogger(KioskDashboard.class.getName()).log(Level.SEVERE, "Error fetching item details for borrow", e);
         }
         return null;
    }

    private static class ItemDetailsForBorrow {
        private final int quantity;
        private final boolean isConsumable;


        public ItemDetailsForBorrow(int quantity, boolean isConsumable) {
            this.quantity = quantity;
            this.isConsumable = isConsumable;
        }

        public int getQuantity() {
            return quantity;
        }

        public boolean isConsumable() {
            return isConsumable;
        }
    }


    public void onBorrowComplete() {
        fetchTotalAvailableItemCount();
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
