package modules;

import Package1.User;
import Package1.DBConnection;
import Package1.PasswordHasher;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreateUserDialog extends javax.swing.JDialog {

    private User currentUser;
    private Users parentPanel;

    private javax.swing.JLabel jLabelTitle;
    private javax.swing.JLabel jLabelUsername;
    private javax.swing.JTextField jTextFieldUsername;
    private javax.swing.JLabel jLabelPassword;
    private javax.swing.JPasswordField jPasswordFieldPassword;
    private javax.swing.JLabel jLabelFullName;
    private javax.swing.JTextField jTextFieldFullName;
    private javax.swing.JLabel jLabelEmail;
    private javax.swing.JTextField jTextFieldEmail;
    private javax.swing.JLabel jLabelRole;
    private javax.swing.JComboBox<String> jComboBoxRole;
    private javax.swing.JButton jButtonCreateUser;

    private static final Color DARK_BACKGROUND = new Color(30, 30, 30);
    private static final Color MEDIUM_DARK_BACKGROUND = new Color(50, 50, 50);
    private static final Color LIGHT_GRAY_BORDER = new Color(70, 70, 70);
    private static final Color FOCUS_COLOR = new Color(41, 128, 185);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color SUCCESS_BUTTON_COLOR = new Color(46, 204, 113);

    public CreateUserDialog(java.awt.Frame parent, boolean modal, User currentUser, Users parentPanel) {
        super(parent, modal);
        this.currentUser = currentUser;
        this.parentPanel = parentPanel;
        setupDialogComponents();
        setupDialogProperties();
    }

    private void setupDialogComponents() {
        this.getContentPane().setLayout(new GridBagLayout());
        this.getContentPane().setBackground(DARK_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);


        jLabelTitle = new javax.swing.JLabel("Create New User Account");
        jLabelUsername = new javax.swing.JLabel("Username:");
        jTextFieldUsername = new javax.swing.JTextField(20);
        jLabelPassword = new javax.swing.JLabel("Password:");
        jPasswordFieldPassword = new javax.swing.JPasswordField(20);
        jLabelFullName = new javax.swing.JLabel("Full Name:");
        jTextFieldFullName = new javax.swing.JTextField(20);
        jLabelEmail = new javax.swing.JLabel("Email:");
        jTextFieldEmail = new javax.swing.JTextField(20);
        jLabelRole = new javax.swing.JLabel("Role:");
        jComboBoxRole = new javax.swing.JComboBox<>(new String[] { "Custodian", "Admin" });
        jButtonCreateUser = new javax.swing.JButton("Create User");

        jLabelTitle.setFont(new java.awt.Font("Segoe UI", 1, 18));
        jLabelTitle.setForeground(TEXT_COLOR);

        jLabelUsername.setForeground(TEXT_COLOR);
        jLabelPassword.setForeground(TEXT_COLOR);
        jLabelFullName.setForeground(TEXT_COLOR);
        jLabelEmail.setForeground(TEXT_COLOR);
        jLabelRole.setForeground(TEXT_COLOR);

        styleTextField(jTextFieldUsername);
        stylePasswordField(jPasswordFieldPassword);
        styleTextField(jTextFieldFullName);
        styleTextField(jTextFieldEmail);

        jComboBoxRole.setBackground(MEDIUM_DARK_BACKGROUND);
        jComboBoxRole.setForeground(TEXT_COLOR);
        jComboBoxRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jComboBoxRole.setBorder(new CompoundBorder(
            new LineBorder(LIGHT_GRAY_BORDER, 1),
            new EmptyBorder(3, 5, 3, 5)
        ));


        jButtonCreateUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        jButtonCreateUser.setBackground(SUCCESS_BUTTON_COLOR);
        jButtonCreateUser.setForeground(TEXT_COLOR);
        jButtonCreateUser.setFocusPainted(false);
        jButtonCreateUser.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SUCCESS_BUTTON_COLOR.darker(), 1),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));


        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 10, 15, 10);
        this.getContentPane().add(jLabelTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(8, 10, 8, 10);
        this.getContentPane().add(jLabelUsername, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.getContentPane().add(jTextFieldUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        this.getContentPane().add(jLabelPassword, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.getContentPane().add(jPasswordFieldPassword, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        this.getContentPane().add(jLabelFullName, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.getContentPane().add(jTextFieldFullName, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        this.getContentPane().add(jLabelEmail, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.getContentPane().add(jTextFieldEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        this.getContentPane().add(jLabelRole, gbc);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        this.getContentPane().add(jComboBoxRole, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(20, 10, 10, 10);
        this.getContentPane().add(jButtonCreateUser, gbc);

        jButtonCreateUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                jButtonCreateUserActionPerformed(evt);
            }
        });
    }

    private void setupDialogProperties() {
        this.setTitle("Create New User");
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.pack();
    }

    private void styleTextField(JTextField textField) {
        textField.setBackground(MEDIUM_DARK_BACKGROUND);
        textField.setForeground(TEXT_COLOR);
        textField.setCaretColor(TEXT_COLOR);
        textField.setBorder(new CompoundBorder(
            new LineBorder(LIGHT_GRAY_BORDER, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.setBorder(new CompoundBorder(
                    new LineBorder(FOCUS_COLOR, 1),
                    new EmptyBorder(5, 5, 5, 5)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                textField.setBorder(new CompoundBorder(
                    new LineBorder(LIGHT_GRAY_BORDER, 1),
                    new EmptyBorder(5, 5, 5, 5)
                ));
            }
        });
    }

    private void stylePasswordField(JPasswordField passwordField) {
        passwordField.setBackground(MEDIUM_DARK_BACKGROUND);
        passwordField.setForeground(TEXT_COLOR);
        passwordField.setCaretColor(TEXT_COLOR);
        passwordField.setBorder(new CompoundBorder(
            new LineBorder(LIGHT_GRAY_BORDER, 1),
            new EmptyBorder(5, 5, 5, 5)
        ));
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                passwordField.setBorder(new CompoundBorder(
                    new LineBorder(FOCUS_COLOR, 1),
                    new EmptyBorder(5, 5, 5, 5)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                passwordField.setBorder(new CompoundBorder(
                    new LineBorder(LIGHT_GRAY_BORDER, 1),
                    new EmptyBorder(5, 5, 5, 5)
                ));
            }
        });
    }


    private void jButtonCreateUserActionPerformed(java.awt.event.ActionEvent evt) {
        if (currentUser == null || !currentUser.isAdmin()) {
            JOptionPane.showMessageDialog(this, "You do not have permission to create users.", "Permission Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = jTextFieldUsername.getText().trim();
        String password = new String(jPasswordFieldPassword.getPassword());
        String fullName = jTextFieldFullName.getText().trim();
        String email = jTextFieldEmail.getText().trim();
        String role = (String) jComboBoxRole.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || email.isEmpty() || role == null) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String hashedPassword = PasswordHasher.hashPassword(password);
        if (hashedPassword == null) {
            JOptionPane.showMessageDialog(this, "Error processing password.", "Creation Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO Users (Username, Password, FullName, Email, Role, CreatedBy, IsActive) VALUES (?, ?, ?, ?, ?, ?, ?)";

        new Thread(new Runnable() {
            @Override
            public void run() {
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, username);
                    pstmt.setString(2, hashedPassword);
                    pstmt.setString(3, fullName);
                    pstmt.setString(4, email);
                    pstmt.setString(5, role);
                    pstmt.setInt(6, currentUser.getUserId());
                    pstmt.setBoolean(7, true);

                    int rowsAffected = pstmt.executeUpdate();

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (rowsAffected > 0) {
                                JOptionPane.showMessageDialog(CreateUserDialog.this, "User '" + username + "' created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                jTextFieldUsername.setText("");
                                jPasswordFieldPassword.setText("");
                                jTextFieldFullName.setText("");
                                jTextFieldEmail.setText("");
                                jComboBoxRole.setSelectedIndex(0);

                                if (parentPanel != null) {
                                    parentPanel.fetchAndDisplayUsers();
                                    parentPanel.fetchAndDisplayCharts();
                                }
                                dispose();

                            } else {
                                JOptionPane.showMessageDialog(CreateUserDialog.this, "Failed to create user.", "Creation Failed", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });


                } catch (SQLException e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                                 JOptionPane.showMessageDialog(CreateUserDialog.this, "Username or Email already exists.", "Creation Failed", JOptionPane.WARNING_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(CreateUserDialog.this, "Database error: " + e.getMessage(), "Creation Failed", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                } catch (RuntimeException e) {
                     SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(CreateUserDialog.this, "Error during user creation: " + e.getMessage(), "Creation Failed", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                    });
                }
            }
        }).start();
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
