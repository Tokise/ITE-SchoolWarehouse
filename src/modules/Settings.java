package modules;

import Package1.DBConnection;
import Package1.User;
import Package1.PasswordHasher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Settings extends javax.swing.JPanel {

    private Connection conn = null;
    private User currentUser;

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmNewPasswordField;
    private JButton changePasswordButton;

    private JLabel profilePictureLabel;
    private JButton changeProfilePictureButton;
    private JButton saveProfilePictureButton;
    private byte[] selectedProfilePictureBytes;

    public Settings() {
        initComponents();
        connectToDatabase();
        setupSettingsPanel();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (this.currentUser != null) {
            System.out.println("Settings: User object set. UserID: " + this.currentUser.getUserId() + ", Username: " + this.currentUser.getUsername());
            loadProfilePicture();
        } else {
            System.out.println("Settings: Current user is null.");
            profilePictureLabel.setIcon(null);
        }
    }


    private boolean connectToDatabase() {
        try {
            conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connected successfully in Settings");
                return true;
            } else {
                System.err.println("Failed to establish database connection in Settings.");
                return false;
            }
        } catch (SQLException ex) {
            System.err.println("Database connection error in Settings: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    private void setupSettingsPanel() {
        this.setLayout(new BorderLayout(10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.setBackground(new Color(30, 30, 30));

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new javax.swing.BoxLayout(contentPanel, javax.swing.BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY),
                "User Settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Verdana", Font.BOLD, 18), Color.WHITE));

        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        passwordPanel.setOpaque(false);
        passwordPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY),
                "Change Password", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Verdana", Font.BOLD, 14), Color.LIGHT_GRAY));

        passwordPanel.add(new JLabel("Current Password: ") {{ setForeground(Color.WHITE); setFont(new Font("Verdana", Font.PLAIN, 12)); }});
        currentPasswordField = new JPasswordField(20);
        passwordPanel.add(currentPasswordField);

        passwordPanel.add(new JLabel("New Password:     ") {{ setForeground(Color.WHITE); setFont(new Font("Verdana", Font.PLAIN, 12)); }});
        newPasswordField = new JPasswordField(20);
        passwordPanel.add(newPasswordField);

        passwordPanel.add(new JLabel("Confirm Password: ") {{ setForeground(Color.WHITE); setFont(new Font("Verdana", Font.PLAIN, 12)); }});
        confirmNewPasswordField = new JPasswordField(20);
        passwordPanel.add(confirmNewPasswordField);

        changePasswordButton = new JButton("Change Password");
        styleButton(changePasswordButton, new Color(231, 76, 60));
        changePasswordButton.addActionListener(e -> changePassword());
        JPanel passwordButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        passwordButtonPanel.setOpaque(false);
        passwordButtonPanel.add(changePasswordButton);
        passwordPanel.add(passwordButtonPanel);


        JPanel profilePicturePanel = new JPanel(new BorderLayout(10, 10));
        profilePicturePanel.setOpaque(false);
         profilePicturePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY),
                "Profile Picture", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Verdana", Font.BOLD, 14), Color.LIGHT_GRAY));

        profilePictureLabel = new JLabel();
        profilePictureLabel.setPreferredSize(new java.awt.Dimension(100, 100));
        profilePictureLabel.setHorizontalAlignment(SwingConstants.CENTER);
        profilePictureLabel.setVerticalAlignment(SwingConstants.CENTER);
        profilePictureLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        profilePicturePanel.add(profilePictureLabel, BorderLayout.WEST);

        JPanel profilePictureButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        profilePictureButtonPanel.setOpaque(false);
        changeProfilePictureButton = new JButton("Choose Image");
        styleButton(changeProfilePictureButton, new Color(52, 152, 219));
        changeProfilePictureButton.addActionListener(e -> chooseProfilePicture());
        profilePictureButtonPanel.add(changeProfilePictureButton);

        saveProfilePictureButton = new JButton("Save Profile Picture");
        styleButton(saveProfilePictureButton, new Color(46, 204, 113));
        saveProfilePictureButton.addActionListener(e -> saveProfilePicture());
        profilePictureButtonPanel.add(saveProfilePictureButton);
        profilePicturePanel.add(profilePictureButtonPanel, BorderLayout.CENTER);


        contentPanel.add(passwordPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(20));
        contentPanel.add(profilePicturePanel);


        this.add(contentPanel, BorderLayout.NORTH);
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


    private void changePassword() {
        if (currentUser == null) {
             JOptionPane.showMessageDialog(this, "User not logged in. Cannot change password.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
         if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String currentPassword = new String(currentPasswordField.getPassword());
        String newPassword = new String(newPasswordField.getPassword());
        String confirmNewPassword = new String(confirmNewPasswordField.getPassword());

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All password fields must be filled.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        System.out.println("Attempting to change password for User ID: " + currentUser.getUserId());

        String sql = "SELECT Password FROM Users WHERE UserID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, currentUser.getUserId());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHashedPassword = rs.getString("Password");

                    if (PasswordHasher.verifyPassword(currentPassword, storedHashedPassword)) {
                        String newHashedPassword = PasswordHasher.hashPassword(newPassword);

                        String updateSql = "UPDATE Users SET Password = ? WHERE UserID = ?";
                        try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                            updatePstmt.setString(1, newHashedPassword);
                            updatePstmt.setInt(2, currentUser.getUserId());
                            int affectedRows = updatePstmt.executeUpdate();
                            if (affectedRows > 0) {
                                JOptionPane.showMessageDialog(this, "Password changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                currentPasswordField.setText("");
                                newPasswordField.setText("");
                                confirmNewPasswordField.setText("");
                            } else {
                                JOptionPane.showMessageDialog(this, "Failed to update password.", "Database Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "Incorrect current password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during password change: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error during password change: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
             System.err.println("Error during password hashing/verification: " + e.getMessage());
             e.printStackTrace();
             JOptionPane.showMessageDialog(this, "An internal error occurred during password change.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chooseProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            try {
                FileInputStream fis = new FileInputStream(fileToLoad);
                selectedProfilePictureBytes = new byte[(int) fileToLoad.length()];
                fis.read(selectedProfilePictureBytes);
                fis.close();

                ImageIcon originalIcon = new ImageIcon(selectedProfilePictureBytes);
                Image scaledImage = originalIcon.getImage().getScaledInstance(profilePictureLabel.getWidth(), profilePictureLabel.getHeight(), Image.SCALE_SMOOTH);
                profilePictureLabel.setIcon(new ImageIcon(scaledImage));

            } catch (IOException e) {
                System.err.println("Error reading image file: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error reading image file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                selectedProfilePictureBytes = null;
                profilePictureLabel.setIcon(null);
            }
        }
    }

    private void saveProfilePicture() {
        if (currentUser == null) {
             JOptionPane.showMessageDialog(this, "User not logged in. Cannot save profile picture.", "Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database connection not available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (selectedProfilePictureBytes == null || selectedProfilePictureBytes.length == 0) {
            JOptionPane.showMessageDialog(this, "No profile picture selected.", "Save Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE Users SET ProfilePicture = ? WHERE UserID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBytes(1, selectedProfilePictureBytes);
            pstmt.setInt(2, currentUser.getUserId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Profile picture saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                 currentUser.setProfilePicture(selectedProfilePictureBytes);
                notifySidebarProfilePictureChanged();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save profile picture.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.err.println("Database error saving profile picture: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error saving profile picture: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadProfilePicture() {
         if (currentUser == null || conn == null) {
             profilePictureLabel.setIcon(null);
             return;
         }

         String sql = "SELECT ProfilePicture FROM Users WHERE UserID = ?";
         try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, currentUser.getUserId());
             try (ResultSet rs = pstmt.executeQuery()) {
                 if (rs.next()) {
                     byte[] imgBytes = rs.getBytes("ProfilePicture");
                     currentUser.setProfilePicture(imgBytes);

                     if (imgBytes != null && imgBytes.length > 0) {
                          ImageIcon originalIcon = new ImageIcon(imgBytes);
                          Image scaledImage = originalIcon.getImage().getScaledInstance(profilePictureLabel.getWidth(), profilePictureLabel.getHeight(), Image.SCALE_SMOOTH);
                          profilePictureLabel.setIcon(new ImageIcon(scaledImage));
                     } else {
                         profilePictureLabel.setIcon(null);
                     }
                 } else {
                     profilePictureLabel.setIcon(null);
                 }
             }
         } catch (SQLException e) {
             System.err.println("Database error loading profile picture: " + e.getMessage());
             e.printStackTrace();
             profilePictureLabel.setIcon(null);
         }
    }

    private void notifySidebarProfilePictureChanged() {
        System.out.println("Notification sent to update sidebar profile picture.");
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
