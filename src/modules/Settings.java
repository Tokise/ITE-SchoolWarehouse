/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package modules;

import Package1.DBConnection; // Assuming DBConnection is in Package1
import Package1.User; // Assuming User class is in Package1
import Package1.PasswordHasher; // Corrected import to use PasswordHasher

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

/**
 * JPanel for managing user settings (password and profile picture).
 */
public class Settings extends javax.swing.JPanel {

    private Connection conn = null;
    private User currentUser; // The current logged-in user

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmNewPasswordField;
    private JButton changePasswordButton;

    private JLabel profilePictureLabel; // To display the current/selected profile picture
    private JButton changeProfilePictureButton;
    private JButton saveProfilePictureButton;
    private byte[] selectedProfilePictureBytes; // To hold the bytes of the newly selected image

    /**
     * Creates new form Settings
     */
    public Settings() {
        initComponents();
        connectToDatabase(); // Establish database connection
        setupSettingsPanel(); // Setup the UI components
    }

    /**
     * Sets the current user for the Settings panel.
     * Details for this user will be loaded and changes will apply to this user.
     * @param user The current logged-in user.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (this.currentUser != null) {
            System.out.println("Settings: User object set. UserID: " + this.currentUser.getUserId() + ", Username: " + this.currentUser.getUsername());
            loadProfilePicture(); // Load the user's current profile picture
        } else {
            System.out.println("Settings: Current user is null.");
            profilePictureLabel.setIcon(null); // Clear profile picture display
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

    /**
     * Sets up the UI components for the Settings panel.
     */
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

        // --- Password Change Section ---
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
        styleButton(changePasswordButton, new Color(231, 76, 60)); // Red color for caution
        changePasswordButton.addActionListener(e -> changePassword());
        JPanel passwordButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        passwordButtonPanel.setOpaque(false);
        passwordButtonPanel.add(changePasswordButton);
        passwordPanel.add(passwordButtonPanel); // Add button panel to password panel


        // --- Profile Picture Section ---
        JPanel profilePicturePanel = new JPanel(new BorderLayout(10, 10));
        profilePicturePanel.setOpaque(false);
         profilePicturePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.DARK_GRAY),
                "Profile Picture", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Verdana", Font.BOLD, 14), Color.LIGHT_GRAY));

        profilePictureLabel = new JLabel();
        profilePictureLabel.setPreferredSize(new java.awt.Dimension(100, 100)); // Set a preferred size for the image area
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


        // Add sections to the main content panel
        contentPanel.add(passwordPanel);
        contentPanel.add(javax.swing.Box.createVerticalStrut(20)); // Add some space
        contentPanel.add(profilePicturePanel);


        this.add(contentPanel, BorderLayout.NORTH); // Add content panel to the top
        // The rest of the panel will be empty or could contain other settings
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
     * Handles the password change process.
     */
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

                    // Verify current password using PasswordHasher

                    if (PasswordHasher.verifyPassword(currentPassword, storedHashedPassword)) {
                        // Hash the new password using PasswordHasher
                        String newHashedPassword = PasswordHasher.hashPassword(newPassword);

                        // Update the password in the database
                        String updateSql = "UPDATE Users SET Password = ? WHERE UserID = ?";
                        try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                            updatePstmt.setString(1, newHashedPassword);
                            updatePstmt.setInt(2, currentUser.getUserId());
                            int affectedRows = updatePstmt.executeUpdate();
                            if (affectedRows > 0) {
                                JOptionPane.showMessageDialog(this, "Password changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                                // Clear fields
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

    /**
     * Opens a file chooser to select a new profile picture.
     */
    private void chooseProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));

        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToLoad = fileChooser.getSelectedFile();
            try {
                // Read the file into a byte array
                FileInputStream fis = new FileInputStream(fileToLoad);
                selectedProfilePictureBytes = new byte[(int) fileToLoad.length()];
                fis.read(selectedProfilePictureBytes);
                fis.close();

                // Display a preview
                ImageIcon originalIcon = new ImageIcon(selectedProfilePictureBytes);
                // Scale to fit the label size - ensure label has a size set
                Image scaledImage = originalIcon.getImage().getScaledInstance(profilePictureLabel.getWidth(), profilePictureLabel.getHeight(), Image.SCALE_SMOOTH);
                profilePictureLabel.setIcon(new ImageIcon(scaledImage));

            } catch (IOException e) {
                System.err.println("Error reading image file: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error reading image file: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                selectedProfilePictureBytes = null; // Clear bytes on error
                profilePictureLabel.setIcon(null); // Clear preview
            }
        }
    }

    /**
     * Saves the selected profile picture to the database.
     */
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
                // Update the currentUser object's profile picture
                 currentUser.setProfilePicture(selectedProfilePictureBytes); // Assuming User object has this method
                // You'll need a way to signal the Sidebar to update its display.
                // This might involve a custom event or a direct method call if the Sidebar is accessible.
                 notifySidebarProfilePictureChanged(); // Placeholder call
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save profile picture.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            System.err.println("Database error saving profile picture: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error saving profile picture: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads the current user's profile picture from the database and displays it.
     */
    private void loadProfilePicture() {
         if (currentUser == null || conn == null) {
             profilePictureLabel.setIcon(null); // Clear display if no user or connection
             return;
         }

         String sql = "SELECT ProfilePicture FROM Users WHERE UserID = ?";
         try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, currentUser.getUserId());
             try (ResultSet rs = pstmt.executeQuery()) {
                 if (rs.next()) {
                     byte[] imgBytes = rs.getBytes("ProfilePicture");
                     // Update user object with the fetched picture bytes
                     currentUser.setProfilePicture(imgBytes);

                     if (imgBytes != null && imgBytes.length > 0) {
                          ImageIcon originalIcon = new ImageIcon(imgBytes);
                          // Scale to fit the label size - ensure label has a size set
                          Image scaledImage = originalIcon.getImage().getScaledInstance(profilePictureLabel.getWidth(), profilePictureLabel.getHeight(), Image.SCALE_SMOOTH);
                          profilePictureLabel.setIcon(new ImageIcon(scaledImage));
                     } else {
                         profilePictureLabel.setIcon(null); // No picture in DB
                     }
                 } else {
                     profilePictureLabel.setIcon(null); // User not found or no picture
                 }
             }
         } catch (SQLException e) {
             System.err.println("Database error loading profile picture: " + e.getMessage());
             e.printStackTrace();
             profilePictureLabel.setIcon(null); // Clear on error
         }
    }

    /**
     * Placeholder method to notify the Sidebar that the profile picture has changed.
     * You will need to implement a proper mechanism for this (e.g., using events
     * or passing references).
     */
    private void notifySidebarProfilePictureChanged() {
        System.out.println("Notification sent to update sidebar profile picture.");
        // TODO: Implement actual notification logic.
        // If your DashboardFrame holds references to both Sidebar and Settings,
        // the Settings panel could call a method on the DashboardFrame, which
        // in turn calls a method on the Sidebar (e.g., sidebar.setUser(updatedUser)).
        // For now, you might manually update the sidebar instance if you have access to it.
        // Example (assuming you have a reference to the sidebar instance):
        // if (sidebarInstance != null) {
        //     sidebarInstance.setUser(currentUser); // Re-set the user to refresh the picture
        // }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
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
