package modules;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Date;
import java.text.SimpleDateFormat;
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
import javax.swing.SwingConstants;

import modules.BorrowItemDialog.BorrowedItemInfo;


public class ReceiptDialog extends JDialog {

    private JLabel titleLabel;
    private JTextArea receiptArea;
    private JButton printButton;
    private JButton closeButton;

    private List<BorrowedItemInfo> borrowedItemsInfoList;

    private String borrowerName;
    private String purpose;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public ReceiptDialog(java.awt.Frame parent, boolean modal, List<BorrowedItemInfo> borrowedItemsInfoList, String borrowerName, String purpose) {
        super(parent, modal);
        this.borrowedItemsInfoList = borrowedItemsInfoList;
        this.borrowerName = borrowerName;
        this.purpose = purpose;

        initComponents();
        setupDialog();
        generateReceiptContent();
    }

    private void setupDialog() {
        setTitle("Borrow Receipt");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(30, 30, 30));
        setResizable(true);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setOpaque(false);
        titleLabel = new JLabel("Item Borrow Receipt", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Verdana", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);
        add(topPanel, BorderLayout.NORTH);

        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setBackground(new Color(50, 50, 50));
        receiptArea.setForeground(Color.WHITE);
        receiptArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.getViewport().setBackground(new Color(50, 50, 50));
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(450, 600));
        pack();
        setLocationRelativeTo(getParent());
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setOpaque(false);

        printButton = new JButton("Print Receipt");
        styleActionButton(printButton, new Color(52, 152, 219));
        printButton.addActionListener((ActionEvent e) -> printReceipt());
        panel.add(printButton);

        closeButton = new JButton("Close");
        styleActionButton(closeButton, new Color(149, 165, 166));
        closeButton.addActionListener((ActionEvent e) -> dispose());
        panel.add(closeButton);

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


    private void generateReceiptContent() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("==========================================\n");
        receipt.append("       AssetWise Academia Warehouse\n");
        receipt.append("            Item Borrow Receipt\n");
        receipt.append("==========================================\n");
        receipt.append("Transaction Date: ").append(dateTimeFormat.format(new Date())).append("\n");
        receipt.append("------------------------------------------\n");
        receipt.append(String.format("%-18s: %s\n", "Borrowed By", borrowerName));
        receipt.append(String.format("%-18s: %s\n", "Purpose", purpose));
        receipt.append("------------------------------------------\n\n");

        List<BorrowedItemInfo> returnableItems = new ArrayList<>();
        List<BorrowedItemInfo> consumableItems = new ArrayList<>();

        for (BorrowedItemInfo item : borrowedItemsInfoList) {
            if (item.isReturnable()) {
                returnableItems.add(item);
            } else {
                consumableItems.add(item);
            }
        }

        if (!returnableItems.isEmpty()) {
            receipt.append("ITEMS TO RETURN (Machinery/Furniture):\n");
            receipt.append(String.format("%-8s %-20s %-5s %-10s\n", "ID", "Item Name", "Qty", "Return By"));
            receipt.append("------------------------------------------\n");
            for (BorrowedItemInfo item : returnableItems) {
                 receipt.append(String.format("%-8d %-20.20s %-5d %-10s\n",
                                              item.getItemId(),
                                              item.getItemName(),
                                              item.getQuantityBorrowed(),
                                              item.getExpectedReturnDate() != null ? dateFormat.format(item.getExpectedReturnDate()) : "N/A"));
            }
            receipt.append("------------------------------------------\n\n");
        }

        if (!consumableItems.isEmpty()) {
            receipt.append("CONSUMABLE ITEMS:\n");
            receipt.append(String.format("%-8s %-25s %-8s\n", "ID", "Item Name", "Quantity"));
            receipt.append("------------------------------------------\n");
            for (BorrowedItemInfo item : consumableItems) {
                 receipt.append(String.format("%-8d %-25.25s %-8d\n",
                                              item.getItemId(),
                                              item.getItemName(),
                                              item.getQuantityBorrowed()));
            }
            receipt.append("------------------------------------------\n\n");
        }

        receipt.append("==========================================\n");
        receipt.append("Please return items by their expected date.\n");
        receipt.append("Thank you!\n");
        receipt.append("==========================================\n");


        receiptArea.setText(receipt.toString());
    }

    private void printReceipt() {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                receiptArea.printAll(g2d);

                return Printable.PAGE_EXISTS;
            }
        });

        boolean doPrint = printerJob.printDialog();
        if (doPrint) {
            try {
                printerJob.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this, "Error during printing: " + e.getMessage(), "Printing Error", JOptionPane.ERROR_MESSAGE);
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
