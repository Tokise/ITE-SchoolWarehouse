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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import java.math.BigDecimal;

import modules.ItemDetailsDialog.ItemDetailsListener;


public class Inventory extends javax.swing.JPanel implements ItemDetailsListener {

    private JTable inventoryTable;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> viewFilter;
    private DefaultTableModel tableModel;

    private Connection conn = null;
    private User currentUser;

    private int currentPage = 1;
    private final int itemsPerPage = 10;
    private int totalItems = 0;
    private JButton jButtonPreviousPage;
    private JButton jButtonNextPage;
    private JLabel jLabelPageInfo;

    private JButton jButtonAddCategory;

    private InventoryLoader currentDataLoader;


    public Inventory() {
        initComponents();
        setupInventoryPanel();
        if (!connectToDatabase()) {
            JOptionPane.showMessageDialog(this, "Database connection failed. Inventory features disabled.", "Connection Error", JOptionPane.ERROR_MESSAGE);
        } else {
            loadCategories();
            fetchTotalItemCount();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (this.currentUser != null) {
            System.out.println("Inventory: User object set. UserID: " + this.currentUser.getUserId() + ", Username: " + this.currentUser.getUsername());
        } else {
            System.out.println("Inventory: Current user is null.");
        }
        currentPage = 1;
        fetchTotalItemCount();
    }

    public void setCurrentUserId(int userId) {
        System.out.println("Inventory: setCurrentUserId(int) called with UserID: " + userId);
        if (userId <= 0) {
            System.err.println("Inventory: Invalid UserID passed to setCurrentUserId: " + userId);
            setCurrentUser(null);
            return;
        }

        User user = fetchUserById(userId);
        if (user == null) {
            System.err.println("Inventory: Failed to fetch user details for UserID: " + userId + ". Operations requiring a user may fail or use default permissions.");
        }
        setCurrentUser(user);
    }

     private User fetchUserById(int userId) {
        if (conn == null) {
            if (!connectToDatabase()) {
                 System.err.println("Inventory.fetchUserById: Database connection is not available.");
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
                } else {
                    System.err.println("Inventory.fetchUserById: No user found with UserID: " + userId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Inventory.fetchUserById: SQL error fetching user with ID " + userId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    private boolean connectToDatabase() {
        try {
            conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connected successfully in Inventory");
                return true;
            } else {
                System.err.println("Failed to establish database connection in Inventory.");
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Inventory.class.getName()).log(Level.SEVERE, "Database connection error in Inventory", ex);
            return false;
        }
    }

    private void setupInventoryPanel() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.setBackground(new Color(30, 30, 30));

        JPanel searchFilterPanel = createSearchFilterPanel();
        this.add(searchFilterPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        JPanel tablePanel = createInventoryTablePanel();
        centerPanel.add(tablePanel, BorderLayout.CENTER);

        this.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JPanel addCategoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        addCategoryPanel.setOpaque(false);
        jButtonAddCategory = new JButton("Add Category");
        styleAddCategoryButton(jButtonAddCategory);
        jButtonAddCategory.addActionListener(e -> showAddCategoryDialog());
        addCategoryPanel.add(jButtonAddCategory);
        bottomPanel.add(addCategoryPanel, BorderLayout.WEST);

        JPanel actionPanel = createActionPanel();
        bottomPanel.add(actionPanel, BorderLayout.EAST);

        this.add(bottomPanel, BorderLayout.SOUTH);

        inventoryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                int selectedRow = inventoryTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int modelRow = inventoryTable.convertRowIndexToModel(selectedRow);
                    Object itemIdObj = tableModel.getValueAt(modelRow, 0);
                    if (itemIdObj != null) {
                        try {
                            int itemId = Integer.parseInt(itemIdObj.toString());
                            openItemDetailsDialog(itemId);
                        } catch (NumberFormatException e) {
                            System.err.println("Error parsing Item ID from table: " + itemIdObj);
                            JOptionPane.showMessageDialog(Inventory.this, "Invalid Item ID in table.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });
    }

    private JPanel createSearchFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        searchLabel.setForeground(Color.WHITE);
        panel.add(searchLabel);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Verdana", Font.PLAIN, 14));
        panel.add(searchField);

        JLabel categoryLabel = new JLabel("Category:");
        categoryLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        categoryLabel.setForeground(Color.WHITE);
        panel.add(categoryLabel);

        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        categoryFilter.setFont(new Font("Verdana", Font.PLAIN, 14));
        panel.add(categoryFilter);
        
        viewFilter = new JComboBox<>();
        viewFilter.addItem("Active Items");
        viewFilter.addItem("Archived Items");
        viewFilter.setFont(new Font("Verdana", Font.PLAIN, 14));
        viewFilter.addActionListener((ActionEvent e) -> searchInventory());
        panel.add(viewFilter);

        JButton searchBtn = new JButton("Search");
        searchBtn.setFont(new Font("Verdana", Font.BOLD, 14));
        searchBtn.setBackground(new Color(41, 128, 185));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.addActionListener((ActionEvent e) -> searchInventory());
        panel.add(searchBtn);

        return panel;
    }

    private JPanel createInventoryTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),
                "Inventory Items", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Verdana", Font.BOLD, 14), Color.WHITE));

        String[] columns = {"ID", "Name", "Category", "Qty", "Unit", "Unit Price", "Status", "Condition", "Machine Status", "Image"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) {
                    return BigDecimal.class;
                }
                if (columnIndex == 9) {
                    return ImageIcon.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setForeground(Color.WHITE);
        inventoryTable.setBackground(new Color(30, 30, 30));
        inventoryTable.setGridColor(new Color(50, 50, 50));
        inventoryTable.setSelectionBackground(new Color(41, 128, 185));
        inventoryTable.setSelectionForeground(Color.WHITE);
        inventoryTable.setFont(new Font("Verdana", Font.PLAIN, 12));
        inventoryTable.setRowHeight(60);
        inventoryTable.setAutoCreateRowSorter(true);

        inventoryTable.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 12));
        inventoryTable.getTableHeader().setBackground(new Color(40, 40, 40));
        inventoryTable.getTableHeader().setForeground(Color.WHITE);
        inventoryTable.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < inventoryTable.getColumnCount(); i++) {
            if (i != 9) {
                 inventoryTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        inventoryTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                if ("Low Stock".equalsIgnoreCase(status)) {
                    c.setForeground(new Color(231, 76, 60));
                } else if ("Out of Stock".equalsIgnoreCase(status)) {
                    c.setForeground(Color.ORANGE);
                } else if ("In Stock".equalsIgnoreCase(status)) {
                    c.setForeground(new Color(46, 204, 113));
                } else {
                    c.setForeground(isSelected ? table.getSelectionForeground() : Color.WHITE);
                }
                c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        inventoryTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setText("");
                if (value instanceof ImageIcon) {
                    ImageIcon originalIcon = (ImageIcon) value;
                     Image scaledImage = originalIcon.getImage().getScaledInstance(-1, 50, Image.SCALE_SMOOTH);
                    label.setIcon(new ImageIcon(scaledImage));
                } else {
                    label.setIcon(null);
                }
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setVerticalAlignment(JLabel.CENTER);
                label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                return label;
            }
        });

        inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        inventoryTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        inventoryTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        inventoryTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        inventoryTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        inventoryTable.getColumnModel().getColumn(7).setPreferredWidth(90);
        inventoryTable.getColumnModel().getColumn(8).setPreferredWidth(100);
        inventoryTable.getColumnModel().getColumn(9).setPreferredWidth(70);


        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        paginationPanel.setOpaque(false);
        jButtonPreviousPage = new JButton("Previous");
        stylePaginationButton(jButtonPreviousPage);
        jButtonPreviousPage.addActionListener(e -> gotoPreviousPage());
        paginationPanel.add(jButtonPreviousPage);

        jLabelPageInfo = new JLabel("Page 1 of 1");
        jLabelPageInfo.setFont(new Font("Verdana", Font.PLAIN, 12));
        jLabelPageInfo.setForeground(Color.WHITE);
        paginationPanel.add(jLabelPageInfo);

        jButtonNextPage = new JButton("Next");
        stylePaginationButton(jButtonNextPage);
        jButtonNextPage.addActionListener(e -> gotoNextPage());
        paginationPanel.add(jButtonNextPage);
        panel.add(paginationPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setOpaque(false);

        JButton addButton = new JButton("Add New");
        styleActionButton(addButton, new Color(46, 204, 113));
        addButton.addActionListener((ActionEvent e) -> openItemDetailsDialog(-1));
        panel.add(addButton);

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

    private void styleAddCategoryButton(JButton button) {
        button.setFont(new Font("Verdana", Font.BOLD, 14));
        button.setBackground(new Color(155, 89, 182));
        button.setForeground(Color.WHITE);
         button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(128, 57, 153), 1), BorderFactory.createEmptyBorder(5, 15, 5, 15)));
    }


    private void stylePaginationButton(JButton button) {
        button.setFont(new Font("Verdana", Font.PLAIN, 12));
        button.setBackground(new Color(70, 70, 70));
        button.setForeground(Color.WHITE);
         button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(3, 8, 3, 8)));
    }


    private void loadCategories() {
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All Categories");

        if (conn == null) {
            System.err.println("Cannot load categories: DB connection is null.");
            return;
        }

        String sql = "SELECT CategoryName FROM Categories ORDER BY CategoryName";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                String categoryName = rs.getString("CategoryName");
                categoryFilter.addItem(categoryName);
            }
        } catch (SQLException e) {
            System.err.println("Error loading categories: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to load categories: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void fetchTotalItemCount() {
        if (conn == null) return;
        String searchText = searchField.getText().trim().toLowerCase();
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        String selectedView = (String) viewFilter.getSelectedItem();

        StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) AS total FROM Items i");
        if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
            sqlBuilder.append(" JOIN Categories c ON i.CategoryID = c.CategoryID");
        }

        sqlBuilder.append(" WHERE 1=1");

        if ("Active Items".equals(selectedView)) {
            sqlBuilder.append(" AND i.IsArchived = FALSE");
        } else if ("Archived Items".equals(selectedView)) {
            sqlBuilder.append(" AND i.IsArchived = TRUE");
        }


        if (!searchText.isEmpty()) {
            sqlBuilder.append(" AND (LOWER(i.ItemName) LIKE ? OR LOWER(i.Description) LIKE ? OR LOWER(i.SerialNumber) LIKE ?)");
        }
        if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
            sqlBuilder.append(" AND c.CategoryName = ?");
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
                    }
                    if (selectedCategory != null && !selectedCategory.equals("All Categories")) {
                        pstmt.setString(paramIndex++, selectedCategory);
                    }
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            count = rs.getInt("total");
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Error fetching total item count: " + e.getMessage());
                }
                return count;
            }

            @Override
            protected void done() {
                try {
                    totalItems = get();
                    int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
                    totalPages = Math.max(totalPages, 1);
                    if (currentPage > totalPages) {
                        currentPage = totalPages;
                    }
                    updatePaginationControls();
                    refreshTableData();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(Inventory.this, "Error updating item count: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void refreshTableData() {
         if (conn == null) {
            System.err.println("Cannot refresh table data: DB connection is null.");
            SwingUtilities.invokeLater(() -> tableModel.setRowCount(0));
            return;
        }

        if (currentDataLoader != null && !currentDataLoader.isDone()) {
             System.out.println("Cancelling previous Inventory data loader task.");
            currentDataLoader.cancel(true);
        }

        String searchText = searchField.getText().trim().toLowerCase();
        String selectedCategoryFilter = (String) categoryFilter.getSelectedItem();
        String selectedViewFilter = (String) viewFilter.getSelectedItem();
        int offset = (currentPage - 1) * itemsPerPage;

        StringBuilder sqlBuilder = new StringBuilder(
            "SELECT i.ItemID, i.ItemName, c.CategoryName, i.Quantity, i.Unit, i.UnitPrice, i.Status, " +
            "i.ItemCondition, i.MachineStatus, i.ItemImage " +
            "FROM Items i " +
            "LEFT JOIN Categories c ON i.CategoryID = c.CategoryID"
        );

        sqlBuilder.append(" WHERE 1=1");

        if ("Active Items".equals(selectedViewFilter)) {
            sqlBuilder.append(" AND i.IsArchived = FALSE");
        } else if ("Archived Items".equals(selectedViewFilter)) {
            sqlBuilder.append(" AND i.IsArchived = TRUE");
        }

        if (!searchText.isEmpty()) {
            sqlBuilder.append(" AND (LOWER(i.ItemName) LIKE ? OR LOWER(i.Description) LIKE ? OR LOWER(i.SerialNumber) LIKE ?)");
        }
        if (selectedCategoryFilter != null && !selectedCategoryFilter.equals("All Categories")) {
            sqlBuilder.append(" AND c.CategoryName = ?");
        }
        sqlBuilder.append(" ORDER BY i.ItemID DESC LIMIT ? OFFSET ?");

        final String finalSql = sqlBuilder.toString();
        currentDataLoader = new InventoryLoader(finalSql, searchText, selectedCategoryFilter, offset, itemsPerPage, selectedViewFilter);
        currentDataLoader.execute();
    }


    private class InventoryLoader extends SwingWorker<Void, Object[]> {
        private final String sql;
        private final String searchText;
        private final String categoryNameFilter;
        private final int offset;
        private final int limit;
        private final String selectedViewFilter;

        public InventoryLoader(String sql, String searchText, String categoryNameFilter, int offset, int limit, String selectedViewFilter) {
            this.sql = sql;
            this.searchText = searchText;
            this.categoryNameFilter = categoryNameFilter;
            this.offset = offset;
            this.limit = limit;
            this.selectedViewFilter = selectedViewFilter;
             System.out.println("InventoryLoader created for page " + (offset / limit + 1));
            SwingUtilities.invokeLater(() -> {
                 System.out.println("Clearing Inventory table model on EDT before loading page " + (offset / limit + 1));
                 tableModel.setRowCount(0);
            });
        }

        @Override
        protected Void doInBackground() throws Exception {
             System.out.println("InventoryLoader doInBackground started for page " + (offset / limit + 1));
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int paramIndex = 1;

                if (!searchText.isEmpty()) {
                    String searchTerm = "%" + searchText + "%";
                    pstmt.setString(paramIndex++, searchTerm);
                    pstmt.setString(paramIndex++, searchTerm);
                    pstmt.setString(paramIndex++, searchTerm);
                }
                if (categoryNameFilter != null && !categoryNameFilter.equals("All Categories")) {
                    pstmt.setString(paramIndex++, categoryNameFilter);
                }
                pstmt.setInt(paramIndex++, limit);
                pstmt.setInt(paramIndex++, offset);

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        if (isCancelled()) {
                            System.out.println("InventoryLoader task cancelled.");
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
                                System.err.println("Error scaling image for table: " + ix.getMessage());
                            }
                        }
                        publish(new Object[]{
                            rs.getInt("ItemID"),
                            rs.getString("ItemName"),
                            rs.getString("CategoryName"),
                            rs.getInt("Quantity"),
                            rs.getString("Unit"),
                            rs.getBigDecimal("UnitPrice"),
                            rs.getString("Status"),
                            rs.getString("ItemCondition"),
                            rs.getString("MachineStatus"),
                            thumb
                        });
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error in InventoryLoader: " + e.getMessage());
                 SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(Inventory.this, "Error loading inventory data: " + e.getMessage() + "\nEnsure your database schema is up to date, especially the 'Items' table (e.g., ItemCondition, UnitPrice columns).", "Database Error", JOptionPane.ERROR_MESSAGE)
                );
            }
            return null;
        }

        @Override
        protected void process(java.util.List<Object[]> chunks) {
             System.out.println("InventoryLoader process called with chunk size: " + chunks.size());
            if (!isCancelled()) {
                for (Object[] rowData : chunks) {
                    tableModel.addRow(rowData);
                }
            } else {
                 System.out.println("InventoryLoader process skipping adding rows due to cancellation.");
            }
        }
         @Override
        protected void done() {
             System.out.println("InventoryLoader done called. Is cancelled: " + isCancelled());
            try {
                if (!isCancelled()) {
                    get();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (!isCancelled()) {
                    JOptionPane.showMessageDialog(Inventory.this, "Failed to complete inventory loading: " + e.getMessage(), "Loading Error", JOptionPane.ERROR_MESSAGE);
                }
            } finally {
                 currentDataLoader = null;
            }
        }
    }

    private void searchInventory() {
        currentPage = 1;
        fetchTotalItemCount();
    }

    private void updatePaginationControls() {
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        totalPages = Math.max(totalPages, 1);
        jLabelPageInfo.setText("Page " + currentPage + " of " + totalPages);
        jButtonPreviousPage.setEnabled(currentPage > 1);
        jButtonNextPage.setEnabled(currentPage < totalPages);
        if (totalItems <= 0) {
            jLabelPageInfo.setText("No items found");
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
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
         totalPages = Math.max(totalPages, 1);
        if (currentPage < totalPages) {
            currentPage++;
            refreshTableData();
            updatePaginationControls();
        }
    }

    private void openItemDetailsDialog(int itemId) {
        ItemDetailsDialog dialog = new ItemDetailsDialog((Frame) SwingUtilities.getWindowAncestor(this), true, conn, currentUser, this);
        if (itemId == -1) {
            dialog.prepareNewItem();
        } else {
            dialog.loadItemDetails(itemId);
        }
    }

     @Override
    public void itemSavedOrArchived() {
        fetchTotalItemCount();
    }


    private void showAddCategoryDialog() {
        String newCategoryName = JOptionPane.showInputDialog(this, "Enter new category name:", "Add Category", JOptionPane.PLAIN_MESSAGE);
        if (newCategoryName != null && !newCategoryName.trim().isEmpty()) {
            addNewCategory(newCategoryName.trim());
        } else if (newCategoryName != null) {
            JOptionPane.showMessageDialog(this, "Category name cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addNewCategory(String categoryName) {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection is not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (categoryExists(categoryName)) {
            JOptionPane.showMessageDialog(this, "Category '" + categoryName + "' already exists.", "Duplicate Category", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO Categories (CategoryName) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryName);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Category '" + categoryName + "' added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCategories();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add category.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.err.println("Error adding new category: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Database error adding category: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean categoryExists(String categoryName) {
        if (conn == null) return true;
        String sql = "SELECT 1 FROM Categories WHERE CategoryName = ? LIMIT 1";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, categoryName);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking if category exists: " + e.getMessage());
            return true;
        }
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();

        setPreferredSize(new java.awt.Dimension(834, 644));

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));
        jPanel1.setForeground(new java.awt.Color(255, 255, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(834, 644));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 834, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
