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
import java.util.Date; // Import java.util.Date
import java.util.List; // Import List
import java.util.ArrayList; // Import ArrayList
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
import javax.swing.JTable; // Import JTable
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel; // Import DefaultTableModel
import javax.swing.table.DefaultTableCellRenderer; // Import for centering text


public class BorrowItemDialog extends JDialog {

    // Remove single item labels
    // private JLabel itemLabel;
    // private JLabel availableQuantityLabel;

    // Use a table to display items being borrowed
    private JTable itemsToBorrowTable;
    private DefaultTableModel itemsToBorrowTableModel;

    private JTextField borrowerNameField;
    private JTextField borrowerDepartmentField;
    private JTextField borrowerGradeLevelField;
    private JTextField borrowerSectionField;
    private JTextField schoolYearField;
    private JTextArea purposeArea;
    private JDateChooser expectedReturnDateField;
    private JLabel expectedReturnDateLabel; // Label for the return date field

    private JButton borrowButton;
    private JButton cancelButton;

    private Connection conn;
    private User kioskUser; // The logged-in Kiosk user performing the borrow

    // Use a list to store details of items being borrowed
    private List<BorrowItemDetails> itemsToBorrow;

    // Flag to check if any of the selected items is machinery
    private boolean hasMachineryItem = false;


    // Callback interface to notify the parent (KioskDashboard)
    public interface BorrowCompleteListener {
        void onBorrowComplete();
    }

    private BorrowCompleteListener listener;

    /**
     * Creates new form BorrowItemDialog for multiple items.
     * @param parent The parent Frame.
     * @param modal Whether the dialog is modal.
     * @param conn The database connection.
     * @param kioskUser The logged-in Kiosk user.
     * @param itemsToBorrow The list of items selected for borrowing.
     * @param listener The listener to notify upon completion.
     */
    public BorrowItemDialog(java.awt.Frame parent, boolean modal, Connection conn, User kioskUser, List<BorrowItemDetails> itemsToBorrow, BorrowCompleteListener listener) {
        super(parent, modal);
        this.conn = conn;
        this.kioskUser = kioskUser;
        this.itemsToBorrow = itemsToBorrow; // Store the list of items
        this.listener = listener;

        // Check if any item in the list is machinery
        for (BorrowItemDetails item : itemsToBorrow) {
            if (item.isMachinery()) {
                hasMachineryItem = true;
                break; // Found at least one machinery item, no need to check further
            }
        }

        initComponents(); // NetBeans generated - should remain untouched
        setupDialog();
        populateFields(); // Populate item details in the table
    }

    /**
     * Sets up the properties and layout of the dialog.
     */
    private void setupDialog() {
        setTitle("Request Item(s)"); // Updated title
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 30));
        setResizable(true); // Allow resizing for the table

        JPanel formPanel = createFormPanel();
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(BorderFactory.createEmptyBorder());
        formScrollPane.getViewport().setBackground(new Color(30, 30, 30));
        formScrollPane.setOpaque(false);

        JPanel buttonPanel = createButtonPanel();

        add(formScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(500, 650)); // Initial size, adjusted for table
        pack();
        setLocationRelativeTo(getParent()); // Center the dialog
    }

    /**
     * Creates the panel containing the form fields for borrowing details.
     * @return The JPanel for the form.
     */
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Add a label for the list of items
        JLabel itemsLabel = new JLabel("Items to Request:");
        itemsLabel.setFont(new Font("Verdana", Font.BOLD, 14));
        itemsLabel.setForeground(Color.WHITE);

        // Setup the table for items to borrow
        String[] itemColumns = {"Item ID", "Item Name", "Available Qty", "Quantity to Borrow"};
        itemsToBorrowTableModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the "Quantity to Borrow" column is editable (index 3)
                return column == 3;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 2 || columnIndex == 3) {
                    return Integer.class; // Item ID, Available Qty, Quantity to Borrow are integers
                }
                return super.getColumnClass(columnIndex);
            }
        };
        itemsToBorrowTable = new JTable(itemsToBorrowTableModel);
        styleTable(itemsToBorrowTable); // Apply styling
        itemsToBorrowTable.setRowHeight(25); // Set row height

        // Center text in all cells of the table
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < itemsToBorrowTable.getColumnCount(); i++) {
            itemsToBorrowTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }


        JScrollPane itemsTableScrollPane = new JScrollPane(itemsToBorrowTable);
        itemsTableScrollPane.getViewport().setBackground(new Color(40, 40, 40));
        itemsTableScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        itemsTableScrollPane.setPreferredSize(new Dimension(400, 150)); // Give the table a preferred size


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

        expectedReturnDateField = new JDateChooser();
        expectedReturnDateField.setDateFormatString("yyyy-MM-dd");
        expectedReturnDateField.setFont(new Font("Verdana", Font.PLAIN, 12));
        expectedReturnDateField.setBackground(new Color(50, 50, 50));
        expectedReturnDateField.setForeground(Color.WHITE);

        expectedReturnDateLabel = new JLabel("Expected Return:"); // Initialize the label


        int y = 0;
        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(itemsLabel, gbc); // Add the label for items list

        gbc.gridy = y++; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH; // Allow table to grow
        panel.add(itemsTableScrollPane, gbc); // Add the table scroll pane

        gbc.gridwidth = 1; // Reset grid width
        gbc.weighty = 0.0; // Reset weighty after table

        addField(panel, gbc, y++, "Name:", borrowerNameField, true);
        addField(panel, gbc, y++, "Department:", borrowerDepartmentField, true);
        addField(panel, gbc, y++, "Grade Level:", borrowerGradeLevelField, true);
        addField(panel, gbc, y++, "Section:", borrowerSectionField, true);
        addField(panel, gbc, y++, "School Year:", schoolYearField, true);
        addField(panel, gbc, y++, "Purpose:", purposeScrollPane, true);

        // Conditionally add the Expected Return Date field and label based on if any item is machinery
        if (hasMachineryItem) { // Only add if at least one item is machinery
             addField(panel, gbc, y++, expectedReturnDateLabel.getText(), expectedReturnDateField, true);
        } else {
             // If no machinery, skip adding the field and label.
             // The row index 'y' is not incremented here.
        }


        gbc.gridx = 0;
        // Adjust the starting gridy for the spacer based on whether the return date field was added
        gbc.gridy = hasMachineryItem ? y : y;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        panel.add(new JLabel(), gbc); // Spacer

        return panel;
    }

    /**
     * Creates the panel containing the action buttons (Borrow and Cancel).
     * @return The JPanel for the buttons.
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setOpaque(false);

        borrowButton = new JButton("Request Item(s)"); // Updated button text
        styleActionButton(borrowButton, new Color(46, 204, 113)); // Green for Borrow
        borrowButton.addActionListener((ActionEvent e) -> performBorrowTransaction());
        panel.add(borrowButton);

        cancelButton = new JButton("Cancel");
        styleActionButton(cancelButton, new Color(149, 165, 166)); // Gray for Cancel
        cancelButton.addActionListener((ActionEvent e) -> dispose()); // Close the dialog
        panel.add(cancelButton);

        return panel;
    }

    /**
     * Styles a JButton with custom background, foreground, and font.
     * @param button The button to style.
     * @param bgColor The background color.
     */
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

    /**
     * Creates a styled JTextField.
     * @return The styled JTextField.
     */
    private JTextField createEditableTextField() {
        JTextField textField = new JTextField(15);
        textField.setFont(new Font("Verdana", Font.PLAIN, 12));
        textField.setBackground(new Color(50, 50, 50));
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        return textField;
    }

    /**
     * Adds a labeled component to the panel using GridBagLayout.
     * @param panel The panel to add to.
     * @param gbc The GridBagConstraints.
     * @param y The row index.
     * @param labelText The text for the label.
     * @param component The component to add.
     * @param stretchHorizontally Whether the component should stretch horizontally.
     */
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
             gbc.fill = GridBagConstraints.BOTH; // Allow description area or table to grow vertically
             gbc.weighty = 1.0; // Give vertical space
         } else {
             gbc.weighty = 0.0; // Reset weighty
         }

        panel.add(component, gbc);

        if (component instanceof JScrollPane) {
            gbc.weighty = 0.0; // Reset weighty after placing the scroll pane
        }
    }

    /**
     * Styles a JTable with custom colors and fonts.
     * @param table The table to style.
     */
     private void styleTable(JTable table) {
        table.setForeground(Color.WHITE);
        table.setBackground(new Color(40, 40, 40));
        table.setGridColor(new Color(60, 60, 60));
        table.setSelectionBackground(new Color(51, 153, 255));
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("Verdana", Font.PLAIN, 12));
        // Row height is set separately
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(new Font("Verdana", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(50, 50, 50));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Keep single selection for the table within the dialog
    }


    /**
     * Populates the items table with the details of the items to be borrowed.
     */
    private void populateFields() {
        // Populate the items table with details from the list passed to the constructor
        itemsToBorrowTableModel.setRowCount(0); // Clear existing rows
        for (BorrowItemDetails item : itemsToBorrow) {
             itemsToBorrowTableModel.addRow(new Object[]{
                 item.getItemId(),
                 item.getItemName(),
                 item.getMaxAvailableQuantity(),
                 1 // Default quantity to borrow is 1
             });
        }
        // Borrower info, purpose, and return date fields are left empty
    }

    /**
     * Validates the user input in the form fields and the quantities in the table.
     * @return true if the input is valid, false otherwise.
     */
    private boolean validateInput() {
        String borrowerName = borrowerNameField.getText().trim();
        String purpose = purposeArea.getText().trim();
        // Get date regardless of visibility, but will only be validated if hasMachineryItem is true
        Date expectedReturnDate = expectedReturnDateField.getDate();

        // Validate quantities for each item in the table
        for (int i = 0; i < itemsToBorrowTableModel.getRowCount(); i++) {
            int itemId = (Integer) itemsToBorrowTableModel.getValueAt(i, 0);
            String itemName = (String) itemsToBorrowTableModel.getValueAt(i, 1);
            int availableQty = (Integer) itemsToBorrowTableModel.getValueAt(i, 2);
            Object borrowQtyObj = itemsToBorrowTableModel.getValueAt(i, 3);

            if (borrowQtyObj == null || borrowQtyObj.toString().trim().isEmpty()) {
                 JOptionPane.showMessageDialog(this, "Quantity to Borrow cannot be empty for item: " + itemName, "Input Error", JOptionPane.WARNING_MESSAGE);
                 itemsToBorrowTable.requestFocus(); // Focus the table
                 itemsToBorrowTable.changeSelection(i, 3, false, false); // Select the cell
                 return false;
            }

            int borrowQty;
            try {
                borrowQty = Integer.parseInt(borrowQtyObj.toString().trim());
                if (borrowQty <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity to Borrow must be a positive number for item: " + itemName, "Input Error", JOptionPane.WARNING_MESSAGE);
                     itemsToBorrowTable.requestFocus();
                     itemsToBorrowTable.changeSelection(i, 3, false, false);
                    return false;
                }
                if (borrowQty > availableQty) {
                    JOptionPane.showMessageDialog(this, "Quantity to Borrow (" + borrowQty + ") exceeds available quantity (" + availableQty + ") for item: " + itemName, "Input Error", JOptionPane.WARNING_MESSAGE);
                     itemsToBorrowTable.requestFocus();
                     itemsToBorrowTable.changeSelection(i, 3, false, false);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Quantity to Borrow format for item: " + itemName + ". Must be a number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                 itemsToBorrowTable.requestFocus();
                 itemsToBorrowTable.changeSelection(i, 3, false, false);
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

        // Only validate Expected Return Date if at least one item IS a machinery
        if (hasMachineryItem && expectedReturnDate == null) {
             JOptionPane.showMessageDialog(this, "Expected Return Date cannot be empty as at least one item is machinery.", "Input Error", JOptionPane.WARNING_MESSAGE);
             expectedReturnDateField.requestFocus();
            return false;
        }


        return true;
    }

    /**
     * Performs the database transaction to record the borrowing of items.
     */
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
        // Get the date only if it's a machinery item
        Date expectedReturnDate = hasMachineryItem ? expectedReturnDateField.getDate() : null;

        // List to store details of items actually borrowed for the receipt
        List<BorrowedItemInfo> borrowedItemsInfo = new ArrayList<>();


        // Use TransactionType 'Issued' for borrowing
        String sql = "INSERT INTO Transactions (ItemID, TransactionType, Quantity, UserID, IssuedToPersonName, IssuedToDepartment, IssuedToGradeLevel, IssuedToSection, SchoolYear, Purpose, ExpectedReturnDate) " +
                     "VALUES (?, 'Issued', ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            conn.setAutoCommit(false); // Start transaction

            for (int i = 0; i < itemsToBorrowTableModel.getRowCount(); i++) {
                int itemId = (Integer) itemsToBorrowTableModel.getValueAt(i, 0);
                String itemName = (String) itemsToBorrowTableModel.getValueAt(i, 1); // Get item name for logging/receipt
                int borrowQty = Integer.parseInt(itemsToBorrowTableModel.getValueAt(i, 3).toString().trim());

                // Find the BorrowItemDetails for this item to get IsMachinery status
                boolean isMachinery = false;
                for(BorrowItemDetails itemDetail : itemsToBorrow) {
                    if (itemDetail.getItemId() == itemId) {
                        isMachinery = itemDetail.isMachinery();
                        break;
                    }
                    // If the item is not found in the original list, it might be an issue,
                    // but for now, we'll assume it exists and its machinery status is needed.
                    // A more robust solution might re-fetch machinery status here if not found.
                }


                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, itemId);
                    pstmt.setInt(2, borrowQty);
                    pstmt.setInt(3, kioskUser.getUserId()); // The Kiosk user performing the issue
                    pstmt.setString(4, borrowerName);
                    pstmt.setString(5, borrowerDepartment.isEmpty() ? null : borrowerDepartment);
                    pstmt.setString(6, borrowerGradeLevel.isEmpty() ? null : borrowerGradeLevel);
                    pstmt.setString(7, borrowerSection.isEmpty() ? null : borrowerSection);
                    pstmt.setString(8, schoolYear.isEmpty() ? null : schoolYear);
                    pstmt.setString(9, purpose);
                    // Set ExpectedReturnDate to SQL NULL if it's not a machinery item or the date is null
                    if (isMachinery && expectedReturnDate != null) { // Only set date if it's machinery AND date is provided
                         pstmt.setDate(10, new java.sql.Date(expectedReturnDate.getTime()));
                    } else {
                         pstmt.setNull(10, Types.DATE); // Set to SQL NULL
                    }


                    int rowsAffected = pstmt.executeUpdate();

                    if (rowsAffected > 0) {
                        // Add item details to the list for the receipt
                        borrowedItemsInfo.add(new BorrowedItemInfo(itemId, itemName, borrowQty, isMachinery));

                         // Log the activity for each item transaction
                         String logDetails = String.format("Borrowed %d unit(s) of '%s' (ID: %d) to %s.",
                                                           borrowQty, itemName, itemId, borrowerName);
                         logActivity("Transaction: Issued", logDetails); // Log each item transaction

                    } else {
                         // If a single item transaction fails, rollback the whole transaction
                         throw new SQLException("Failed to record borrowing transaction for item: " + itemName);
                    }
                }
            }

            conn.commit(); // Commit the transaction if all items were processed successfully

            JOptionPane.showMessageDialog(this, "Item(s) borrowed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Generate and show receipt for all borrowed items
            if (!borrowedItemsInfo.isEmpty()) {
                 generateAndShowReceipt(borrowedItemsInfo, borrowerName, purpose, expectedReturnDate);
            }


            // Notify the listener (KioskDashboard) to refresh the available items list
            if (listener != null) {
                listener.onBorrowComplete();
            }

            dispose(); // Close the dialog after successful transaction

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback(); // Rollback transaction on error
                    System.err.println("Transaction rolled back due to error during borrowing multiple items.");
                }
            } catch (SQLException ex) {
                System.err.println("Error during transaction rollback: " + ex.getMessage());
            }
            e.printStackTrace();
             // Check for insufficient stock error from trigger
            if (e.getSQLState() != null && e.getSQLState().equals("45000")) {
                 JOptionPane.showMessageDialog(this, e.getMessage(), "Transaction Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Database error during borrowing: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
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
      * Logs an activity in the database.
      * @param activityType The type of activity.
      * @param details The details of the activity.
      */
     private void logActivity(String activityType, String details) {
        if (conn == null || kioskUser == null || kioskUser.getUserId() <= 0) {
            System.err.println("Cannot log activity: DB connection null or invalid UserID (" + (kioskUser != null ? kioskUser.getUserId() : "null") + ")");
            return;
        }
        String sql = "INSERT INTO RecentActivities (ActivityType, UserID, UserName, Details, ActivityDate) VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, activityType);
            pstmt.setInt(2, kioskUser.getUserId());
            pstmt.setString(3, kioskUser.getUsername()); // Log the kiosk user's username
            pstmt.setString(4, details);
            pstmt.executeUpdate();
            System.out.println("Activity logged: Type=" + activityType + ", User=" + kioskUser.getUsername() + ", Details=" + details); // Debug print logging success
        } catch (SQLException e) {
            System.err.println("Error logging activity: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Generates and shows a receipt for the borrowed items.
     * @param borrowedItemsInfo The list of items that were successfully borrowed.
     * @param borrowerName The name of the person who borrowed the items.
     * @param purpose The purpose of borrowing.
     * @param expectedReturnDate The expected return date (can be null).
     */
    private void generateAndShowReceipt(List<BorrowedItemInfo> borrowedItemsInfo, String borrowerName, String purpose, Date expectedReturnDate) {
        // Create and show the receipt dialog
        ReceiptDialog receiptDialog = new ReceiptDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            true, // modal
            borrowedItemsInfo, // Pass the list of borrowed items
            borrowerName,
            purpose,
            expectedReturnDate
        );
        receiptDialog.setVisible(true);
    }

    /**
     * Simple class to hold details of a borrowed item for the receipt.
     */
    public static class BorrowedItemInfo {
        private final int itemId;
        private final String itemName;
        private final int quantityBorrowed;
        private final boolean isMachinery; // Include isMachinery status for receipt

        public BorrowedItemInfo(int itemId, String itemName, int quantityBorrowed, boolean isMachinery) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.quantityBorrowed = quantityBorrowed;
            this.isMachinery = isMachinery;
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

        public boolean isMachinery() {
            return isMachinery;
        }
    }

     /**
      * Simple class to hold item details needed in this dialog.
      */
    public static class BorrowItemDetails {
        private final int itemId;
        private final String itemName;
        private final int maxAvailableQuantity;
        private final boolean isMachinery;

        public BorrowItemDetails(int itemId, String itemName, int maxAvailableQuantity, boolean isMachinery) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.maxAvailableQuantity = maxAvailableQuantity;
            this.isMachinery = isMachinery;
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

        public boolean isMachinery() {
            return isMachinery;
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
