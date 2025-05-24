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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import com.toedter.calendar.JDateChooser;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;


public class BorrowItemDialog extends JDialog {

    private JTable itemsToBorrowTable;
    private DefaultTableModel itemsToBorrowTableModel;

    private JTextField borrowerNameField;
    private JTextField borrowerDepartmentField;
    private JTextField borrowerGradeLevelField;
    private JTextField borrowerSectionField;
    private JTextField schoolYearField;
    private JTextArea purposeArea;
    private JDateChooser expectedReturnDateField;
    private JLabel expectedReturnDateLabel;
    private JPanel expectedReturnDatePanel;


    private JButton borrowButton;
    private JButton cancelButton;

    private Connection conn;
    private User kioskUser;

    private List<BorrowItemDetails> itemsToBorrow;

    private boolean requiresReturnDate = false;


    public interface BorrowCompleteListener {
        void onBorrowComplete();
    }

    private BorrowCompleteListener listener;

    public BorrowItemDialog(java.awt.Frame parent, boolean modal, Connection conn, User kioskUser, List<BorrowItemDetails> itemsToBorrow, BorrowCompleteListener listener) {
        super(parent, modal);
        this.conn = conn;
        this.kioskUser = kioskUser;
        this.itemsToBorrow = itemsToBorrow;
        this.listener = listener;

        for (BorrowItemDetails item : itemsToBorrow) {
            if (!item.isConsumable()) { 
                requiresReturnDate = true;
                break;
            }
        }

        initComponents();
        setupDialog();
        populateFields();
    }

    private void setupDialog() {
        setTitle("Request Item(s)");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 30));
        setResizable(true);

        JPanel formPanel = createFormPanel();
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(BorderFactory.createEmptyBorder());
        formScrollPane.getViewport().setBackground(new Color(30, 30, 30));
        formScrollPane.setOpaque(false);

        JPanel buttonPanel = createButtonPanel();

        add(formScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(550, 700));
        pack();
        setLocationRelativeTo(getParent());
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel itemsLabel = new JLabel("Items to Request:");
        itemsLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        itemsLabel.setForeground(Color.WHITE);

        String[] itemColumns = {"Item ID", "Item Name", "Type", "Available Qty", "Quantity to Borrow"};
        itemsToBorrowTableModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 3 || columnIndex == 4) {
                    return Integer.class;
                }
                return super.getColumnClass(columnIndex);
            }
        };
        itemsToBorrowTable = new JTable(itemsToBorrowTableModel);
        styleTable(itemsToBorrowTable);
        itemsToBorrowTable.setRowHeight(25);
        itemsToBorrowTable.getColumnModel().getColumn(2).setPreferredWidth(100);


        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < itemsToBorrowTable.getColumnCount(); i++) {
            itemsToBorrowTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }


        JScrollPane itemsTableScrollPane = new JScrollPane(itemsToBorrowTable);
        itemsTableScrollPane.getViewport().setBackground(new Color(40, 40, 40));
        itemsTableScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        itemsTableScrollPane.setPreferredSize(new Dimension(450, 150));


        borrowerNameField = createEditableTextField();
        borrowerDepartmentField = createEditableTextField();
        borrowerGradeLevelField = createEditableTextField();
        borrowerSectionField = createEditableTextField();
        schoolYearField = createEditableTextField();
        purposeArea = new JTextArea(3, 15);
        purposeArea.setLineWrap(true);
        purposeArea.setWrapStyleWord(true);
        purposeArea.setFont(new Font("Verdana", Font.PLAIN, 12));
        purposeArea.setBackground(new Color(50, 50, 50));
        purposeArea.setForeground(Color.WHITE);
        purposeArea.setCaretColor(Color.WHITE);
        JScrollPane purposeScrollPane = new JScrollPane(purposeArea);
        purposeScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        expectedReturnDatePanel = new JPanel(new BorderLayout(5,0));
        expectedReturnDatePanel.setOpaque(false);
        expectedReturnDateLabel = new JLabel("Expected Return (Machinery/Furniture):");
        expectedReturnDateLabel.setForeground(Color.WHITE);
        expectedReturnDateLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
        expectedReturnDateField = new JDateChooser();
        expectedReturnDateField.setDateFormatString("yyyy-MM-dd");
        expectedReturnDateField.setFont(new Font("Verdana", Font.PLAIN, 12));
        expectedReturnDateField.setBackground(new Color(50, 50, 50));
        expectedReturnDateField.setForeground(Color.WHITE);
        expectedReturnDatePanel.add(expectedReturnDateLabel, BorderLayout.WEST);
        expectedReturnDatePanel.add(expectedReturnDateField, BorderLayout.CENTER);


        int y = 0;
        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(itemsLabel, gbc);

        gbc.gridy = y++; gbc.weighty = 0.5; gbc.fill = GridBagConstraints.BOTH;
        panel.add(itemsTableScrollPane, gbc);

        gbc.gridwidth = 1;
        gbc.weighty = 0.0;

        addField(panel, gbc, y++, "Name:", borrowerNameField, true);
        addField(panel, gbc, y++, "Department:", borrowerDepartmentField, true);
        addField(panel, gbc, y++, "Grade Level:", borrowerGradeLevelField, true);
        addField(panel, gbc, y++, "Section:", borrowerSectionField, true);
        addField(panel, gbc, y++, "School Year:", schoolYearField, true);
        addField(panel, gbc, y++, "Purpose:", purposeScrollPane, true);

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        expectedReturnDatePanel.setVisible(requiresReturnDate);
        panel.add(expectedReturnDatePanel, gbc);
        y++;


        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.weighty = 0.5;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel.add(new JLabel(), gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setOpaque(false);

        borrowButton = new JButton("Request Item(s)");
        styleActionButton(borrowButton, new Color(46, 204, 113));
        borrowButton.addActionListener((ActionEvent e) -> performBorrowTransaction());
        panel.add(borrowButton);

        cancelButton = new JButton("Cancel");
        styleActionButton(cancelButton, new Color(149, 165, 166));
        cancelButton.addActionListener((ActionEvent e) -> dispose());
        panel.add(cancelButton);

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

    private JTextField createEditableTextField() {
        JTextField textField = new JTextField(15);
        textField.setFont(new Font("Verdana", Font.PLAIN, 12));
        textField.setBackground(new Color(50, 50, 50));
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        return textField;
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int y, String labelText, Component component, boolean stretchHorizontally) {
        gbc.gridx = 0;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.0;
        JLabel label = new JLabel(labelText);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Verdana", Font.PLAIN, 12));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.fill = stretchHorizontally ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        gbc.weightx = stretchHorizontally ? 1.0 : 0.0;
         if (component instanceof JScrollPane) {
             gbc.fill = GridBagConstraints.BOTH;
             gbc.weighty = 0.2; 
         } else {
             gbc.weighty = 0.0;
         }

        panel.add(component, gbc);

        if (component instanceof JScrollPane) {
            gbc.weighty = 0.0;
        }
    }

     private void styleTable(JTable table) {
        table.setForeground(Color.WHITE);
        table.setBackground(new Color(40, 40, 40));
        table.setGridColor(new Color(60, 60, 60));
        table.setSelectionBackground(new Color(51, 153, 255));
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("Verdana", Font.PLAIN, 12));
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(50, 50, 50));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }


    private void populateFields() {
        itemsToBorrowTableModel.setRowCount(0);
        for (BorrowItemDetails item : itemsToBorrow) {
             itemsToBorrowTableModel.addRow(new Object[]{
                 item.getItemId(),
                 item.getItemName(),
                 item.isConsumable() ? "Consumable" : "Machinery/Furniture",
                 item.getMaxAvailableQuantity(),
                 1
             });
        }
    }

    private boolean validateInput() {
        String borrowerName = borrowerNameField.getText().trim();
        String purpose = purposeArea.getText().trim();
        Date expectedReturnDate = expectedReturnDateField.getDate();

        for (int i = 0; i < itemsToBorrowTableModel.getRowCount(); i++) {
            String itemName = (String) itemsToBorrowTableModel.getValueAt(i, 1);
            int availableQty = (Integer) itemsToBorrowTableModel.getValueAt(i, 3);
            Object borrowQtyObj = itemsToBorrowTableModel.getValueAt(i, 4);

            if (borrowQtyObj == null || borrowQtyObj.toString().trim().isEmpty()) {
                 JOptionPane.showMessageDialog(this, "Quantity to Borrow cannot be empty for item: " + itemName, "Input Error", JOptionPane.WARNING_MESSAGE);
                 itemsToBorrowTable.requestFocus();
                 itemsToBorrowTable.changeSelection(i, 4, false, false);
                 return false;
            }

            int borrowQty;
            try {
                borrowQty = Integer.parseInt(borrowQtyObj.toString().trim());
                if (borrowQty <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity to Borrow must be a positive number for item: " + itemName, "Input Error", JOptionPane.WARNING_MESSAGE);
                     itemsToBorrowTable.requestFocus();
                     itemsToBorrowTable.changeSelection(i, 4, false, false);
                    return false;
                }
                if (borrowQty > availableQty) {
                    JOptionPane.showMessageDialog(this, "Quantity to Borrow (" + borrowQty + ") exceeds available quantity (" + availableQty + ") for item: " + itemName, "Input Error", JOptionPane.WARNING_MESSAGE);
                     itemsToBorrowTable.requestFocus();
                     itemsToBorrowTable.changeSelection(i, 4, false, false);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Quantity to Borrow format for item: " + itemName + ". Must be a number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                 itemsToBorrowTable.requestFocus();
                 itemsToBorrowTable.changeSelection(i, 4, false, false);
                return false;
            }
        }


        if (borrowerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            borrowerNameField.requestFocus();
            return false;
        }

        if (purpose.isEmpty()) {
             JOptionPane.showMessageDialog(this, "Purpose cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
             purposeArea.requestFocus();
            return false;
        }

        if (requiresReturnDate && expectedReturnDate == null) {
             JOptionPane.showMessageDialog(this, "Expected Return Date cannot be empty as some items are Machinery/Furniture.", "Input Error", JOptionPane.WARNING_MESSAGE);
             expectedReturnDateField.requestFocus();
            return false;
        }
        if (requiresReturnDate && expectedReturnDate != null && expectedReturnDate.before(new Date(System.currentTimeMillis() - 86400000))) { 
            JOptionPane.showMessageDialog(this, "Expected Return Date cannot be in the past.", "Input Error", JOptionPane.WARNING_MESSAGE);
            expectedReturnDateField.requestFocus();
            return false;
        }


        return true;
    }

    private void performBorrowTransaction() {
        if (!validateInput()) {
            return;
        }
         if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection is not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
         if (kioskUser == null || kioskUser.getUserId() <= 0) {
            JOptionPane.showMessageDialog(this, "No valid Kiosk user session. Cannot perform transaction.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String borrowerName = borrowerNameField.getText().trim();
        String borrowerDepartment = borrowerDepartmentField.getText().trim();
        String borrowerGradeLevel = borrowerGradeLevelField.getText().trim();
        String borrowerSection = borrowerSectionField.getText().trim();
        String schoolYear = schoolYearField.getText().trim();
        String purpose = purposeArea.getText().trim();
        Date selectedExpectedReturnDate = expectedReturnDateField.getDate();

        List<BorrowedItemInfo> borrowedItemsInfoList = new ArrayList<>();


        String sql = "INSERT INTO Transactions (ItemID, TransactionType, Quantity, UserID, IssuedToPersonName, IssuedToDepartment, IssuedToGradeLevel, IssuedToSection, SchoolYear, Purpose, ExpectedReturnDate) " +
                     "VALUES (?, 'Issued', ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false);

            for (int i = 0; i < itemsToBorrowTableModel.getRowCount(); i++) {
                int itemId = (Integer) itemsToBorrowTableModel.getValueAt(i, 0);
                String itemName = (String) itemsToBorrowTableModel.getValueAt(i, 1);
                int borrowQty = Integer.parseInt(itemsToBorrowTableModel.getValueAt(i, 4).toString().trim());

                boolean isConsumableItem = false; 
                Date itemExpectedReturnDate = null;

                for(BorrowItemDetails itemDetail : itemsToBorrow) {
                    if (itemDetail.getItemId() == itemId) {
                        isConsumableItem = itemDetail.isConsumable();
                        break;
                    }
                }

                if (!isConsumableItem) {
                    itemExpectedReturnDate = selectedExpectedReturnDate;
                }


                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, itemId);
                    pstmt.setInt(2, borrowQty);
                    pstmt.setInt(3, kioskUser.getUserId());
                    pstmt.setString(4, borrowerName);
                    pstmt.setString(5, borrowerDepartment.isEmpty() ? null : borrowerDepartment);
                    pstmt.setString(6, borrowerGradeLevel.isEmpty() ? null : borrowerGradeLevel);
                    pstmt.setString(7, borrowerSection.isEmpty() ? null : borrowerSection);
                    pstmt.setString(8, schoolYear.isEmpty() ? null : schoolYear);
                    pstmt.setString(9, purpose);
                    if (itemExpectedReturnDate != null) {
                         pstmt.setDate(10, new java.sql.Date(itemExpectedReturnDate.getTime()));
                    } else {
                         pstmt.setNull(10, Types.DATE);
                    }


                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                        borrowedItemsInfoList.add(new BorrowedItemInfo(itemId, itemName, borrowQty, !isConsumableItem, itemExpectedReturnDate));

                         String logDetails = String.format("Borrowed %d unit(s) of '%s' (ID: %d, Type: %s) to %s.",
                                                           borrowQty, itemName, itemId, isConsumableItem ? "Consumable" : "Returnable", borrowerName);
                         logActivity("Transaction: Issued", logDetails);

                    } else {
                         throw new SQLException("Failed to record borrowing transaction for item: " + itemName);
                    }
                }
            }

            conn.commit();

            JOptionPane.showMessageDialog(this, "Item(s) borrowed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            if (!borrowedItemsInfoList.isEmpty()) {
                 generateAndShowReceipt(borrowedItemsInfoList, borrowerName, purpose);
            }


            if (listener != null) {
                listener.onBorrowComplete();
            }

            dispose();

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
            }
            if (e.getSQLState() != null && e.getSQLState().equals("45000")) {
                 JOptionPane.showMessageDialog(this, e.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Database error during borrowing: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
            }
        }
    }

     private void logActivity(String activityType, String details) {
        if (conn == null || kioskUser == null || kioskUser.getUserId() <= 0) {
            return;
        }
        String sql = "INSERT INTO RecentActivities (ActivityType, UserID, UserName, Details, ActivityDate) VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, activityType);
            pstmt.setInt(2, kioskUser.getUserId());
            pstmt.setString(3, kioskUser.getUsername());
            pstmt.setString(4, details);
            pstmt.executeUpdate();
        } catch (SQLException e) {
        }
    }


    private void generateAndShowReceipt(List<BorrowedItemInfo> borrowedItemsInfo, String borrowerName, String purpose) {
        ReceiptDialog receiptDialog = new ReceiptDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            true,
            borrowedItemsInfo,
            borrowerName,
            purpose
        );
        receiptDialog.setVisible(true);
    }

    public static class BorrowedItemInfo {
        private final int itemId;
        private final String itemName;
        private final int quantityBorrowed;
        private final boolean isReturnable; 
        private final Date expectedReturnDate;


        public BorrowedItemInfo(int itemId, String itemName, int quantityBorrowed, boolean isReturnable, Date expectedReturnDate) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.quantityBorrowed = quantityBorrowed;
            this.isReturnable = isReturnable;
            this.expectedReturnDate = expectedReturnDate;
        }

        public int getItemId() {
            return itemId;
        }

        public String getItemName() {
            return itemName;
        }

        public int getQuantityBorrowed() {
            return quantityBorrowed;
        }

        public boolean isReturnable() {
            return isReturnable;
        }
        
        public Date getExpectedReturnDate() {
            return expectedReturnDate;
        }
    }

    public static class BorrowItemDetails {
        private final int itemId;
        private final String itemName;
        private final int maxAvailableQuantity;
        private final boolean isConsumable; 
        private final String itemCategory;


        public BorrowItemDetails(int itemId, String itemName, int maxAvailableQuantity, boolean isConsumable, String itemCategory) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.maxAvailableQuantity = maxAvailableQuantity;
            this.isConsumable = isConsumable;
            this.itemCategory = itemCategory;
        }

        public int getItemId() {
            return itemId;
        }

        public String getItemName() {
            return itemName;
        }

        public int getMaxAvailableQuantity() {
            return maxAvailableQuantity;
        }

        public boolean isConsumable() {
            return isConsumable;
        }
        
        public String getItemCategory() {
            return itemCategory;
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
