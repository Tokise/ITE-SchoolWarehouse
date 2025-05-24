package components;

import Package1.DashBoardFrame1;
import Package1.User;
import event.EventMenu;
import modules.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Image;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sidebar extends javax.swing.JPanel {


    private DashBoardFrame1 dashboardFrame;
    private User user;
    private List<ButtonMenu> menuButtons;
    private int selectedIndex = 0;

    public Sidebar() {
        initComponents();

        menuButtons = new ArrayList<>();
        menuButtons.add(buttonMenu1);
        menuButtons.add(buttonMenu2);
        menuButtons.add(buttonMenu3);
        buttonMenu4.setText("Purchase Order");
        menuButtons.add(buttonMenu4);
        menuButtons.add(buttonMenu5);
        menuButtons.add(buttonMenu6);
        buttonMenu7.setText("Borrowed Items");
        menuButtons.add(buttonMenu7); 

        setSelected(0);

        setupButtonActions();
    }

    public Sidebar(DashBoardFrame1 dashboardFrame, User user) {
        this();
        this.dashboardFrame = dashboardFrame;
        setUser(user);
    }


    private void configureSidebarBasedOnRole() {
        if (this.user != null && "Custodian".equals(this.user.getRole())) {
            buttonMenu3.setVisible(false);
        } else {
            buttonMenu3.setVisible(true);
        }
    }

    public void setDashboardFrame(DashBoardFrame1 dashboardFrame) {
        this.dashboardFrame = dashboardFrame;
    }


    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            jLabel1.setText(user.getFullName());
            jLabel2.setText(user.getRole());

            loadAndDisplayProfilePicture(user.getProfilePicture());

            configureSidebarBasedOnRole();
        } else {
            jLabel1.setText("Unknown");
            jLabel2.setText("Unknown Role");
            imageAvatar1.setIcon(loadIcon("/assets/AssetWise Academia.png"));

        }
    }

    private void loadAndDisplayProfilePicture(byte[] imageData) {
        if (imageData != null && imageData.length > 0) {
            try {
                ImageIcon originalIcon = new ImageIcon(imageData);

                int avatarSize = 86;
                Image scaledImage = originalIcon.getImage().getScaledInstance(avatarSize, avatarSize, Image.SCALE_SMOOTH);
                imageAvatar1.setIcon(new ImageIcon(scaledImage));
            } catch (Exception e) {
                System.err.println("Error loading or scaling profile picture: " + e.getMessage());
                e.printStackTrace();
                imageAvatar1.setIcon(loadIcon("/assets/AssetWise Academia.png"));
            }
        } else {
            imageAvatar1.setIcon(loadIcon("/assets/AssetWise Academia.png"));
        }
    }



    private ImageIcon loadIcon(String path) {
       try {

            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL != null) {
                return new ImageIcon(imgURL);
            } else {
                System.err.println("Couldn't find icon file: " + path);

                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void setSelected(int index) {
        for (int i = 0; i < menuButtons.size(); i++) {
            menuButtons.get(i).setSelected(i == index);
        }
        selectedIndex = index;
    }


    private void setupButtonActions() {

         for(ButtonMenu button : menuButtons) {
            for(ActionListener al : button.getActionListeners()) {
                button.removeActionListener(al);
            }
        }

        buttonMenu1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dashboardFrame != null) {
                    setSelected(menuButtons.indexOf(buttonMenu1));
                    dashboardFrame.setForm(new Dashboard());
                }
            }
        });

        buttonMenu2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dashboardFrame != null) {
                    setSelected(menuButtons.indexOf(buttonMenu2));
                    Inventory inventoryPanel = new Inventory();
                    dashboardFrame.setForm(inventoryPanel);
                }
            }
        });

        buttonMenu3.addActionListener(new ActionListener() {
           @Override
            public void actionPerformed(ActionEvent e) {

                if (user != null && "Admin".equals(user.getRole()) && dashboardFrame != null) {
                    setSelected(menuButtons.indexOf(buttonMenu3));
                    dashboardFrame.setForm(new Users());
                } else {

                    JOptionPane.showMessageDialog(null, "Access Denied. Only Admins can view Users.", "Permission Denied", JOptionPane.WARNING_MESSAGE);

                    setSelected(selectedIndex);
                }
            }
        });

        buttonMenu4.addActionListener(new ActionListener() {
            @Override
             public void actionPerformed(ActionEvent e) {
                if (dashboardFrame != null) {
                    setSelected(menuButtons.indexOf(buttonMenu4));
                    PurchaseOrder purchaseOrderPanel = new PurchaseOrder();
                    purchaseOrderPanel.setCurrentUser(user);
                    dashboardFrame.setForm(purchaseOrderPanel);
                }
            }
        });

        buttonMenu5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dashboardFrame != null) {
                    setSelected(menuButtons.indexOf(buttonMenu5));
                    dashboardFrame.setForm(new Reports());
                }
            }
        });

        buttonMenu6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dashboardFrame != null) {
                    setSelected(menuButtons.indexOf(buttonMenu6));
                    Settings settingsPanel = new Settings();
                    settingsPanel.setCurrentUser(user);
                    dashboardFrame.setForm(settingsPanel);
                }
            }
        });

      
        buttonMenu7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dashboardFrame != null) {
                    setSelected(menuButtons.indexOf(buttonMenu7));
                    dashboardFrame.setForm(new BorrowedTransactionsPanel());
                }
            }
        });
       

        setSelected(selectedIndex);
    }




    private void setIcons() {

         System.out.println("setIcons method called. Implement actual icon loading here if needed.");

    }


     
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panelMenu = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        imageAvatar1 = new swing.ImageAvatar();
        buttonMenu1 = new components.ButtonMenu();
        buttonMenu2 = new components.ButtonMenu();
        buttonMenu3 = new components.ButtonMenu();
        buttonMenu4 = new components.ButtonMenu();
        buttonMenu5 = new components.ButtonMenu();
        buttonMenu6 = new components.ButtonMenu();
        buttonMenu7 = new components.ButtonMenu();

        panelMenu.setBackground(new java.awt.Color(0, 0, 0));
        panelMenu.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel1.setFont(new java.awt.Font("Copperplate Gothic Bold", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(224, 224, 224));
        jLabel1.setText("Full Name");
        jLabel1.setToolTipText("");

        jLabel2.setFont(new java.awt.Font("Copperplate Gothic Light", 0, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(203, 203, 203));
        jLabel2.setText("Admin");

        buttonMenu1.setBackground(new java.awt.Color(102, 102, 102));
        buttonMenu1.setText("Dashboard");
        buttonMenu1.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        buttonMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenu1ActionPerformed(evt);
            }
        });

        buttonMenu2.setBackground(new java.awt.Color(102, 102, 102));
        buttonMenu2.setText("Inventory");
        buttonMenu2.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        buttonMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenu2ActionPerformed(evt);
            }
        });

        buttonMenu3.setBackground(new java.awt.Color(102, 102, 102));
        buttonMenu3.setText("Users");
        buttonMenu3.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        buttonMenu3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenu3ActionPerformed(evt);
            }
        });

        buttonMenu4.setBackground(new java.awt.Color(102, 102, 102));
        buttonMenu4.setText("Purchase Order");
        buttonMenu4.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N

        buttonMenu5.setBackground(new java.awt.Color(102, 102, 102));
        buttonMenu5.setText("Reports");
        buttonMenu5.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N

        buttonMenu6.setBackground(new java.awt.Color(102, 102, 102));
        buttonMenu6.setText("Settings");
        buttonMenu6.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N

        buttonMenu7.setBackground(new java.awt.Color(102, 102, 102));
        buttonMenu7.setText("Borrowed Items");
        buttonMenu7.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        buttonMenu7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonMenu7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelMenuLayout = new javax.swing.GroupLayout(panelMenu);
        panelMenu.setLayout(panelMenuLayout);
        panelMenuLayout.setHorizontalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuLayout.createSequentialGroup()
                .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelMenuLayout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(buttonMenu6, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                            .addComponent(buttonMenu2, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                            .addComponent(buttonMenu3, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                            .addComponent(buttonMenu4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonMenu5, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                            .addComponent(buttonMenu1, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                            .addGroup(panelMenuLayout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addGroup(panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel2)))
                            .addComponent(buttonMenu7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(panelMenuLayout.createSequentialGroup()
                        .addGap(84, 84, 84)
                        .addComponent(imageAvatar1, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(58, Short.MAX_VALUE))
        );

        panelMenuLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {buttonMenu1, buttonMenu2, buttonMenu3, buttonMenu4, buttonMenu5, buttonMenu6});

        panelMenuLayout.setVerticalGroup(
            panelMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelMenuLayout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addComponent(imageAvatar1, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addGap(32, 32, 32)
                .addComponent(buttonMenu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonMenu2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonMenu7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonMenu3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonMenu4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonMenu5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonMenu6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(75, Short.MAX_VALUE))
        );

        panelMenuLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {buttonMenu1, buttonMenu2, buttonMenu3, buttonMenu4, buttonMenu5, buttonMenu6});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelMenu, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void buttonMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenu2ActionPerformed
    if (dashboardFrame != null) {
        dashboardFrame.setForm(new Inventory());
    }

    }//GEN-LAST:event_buttonMenu2ActionPerformed

    private void buttonMenu1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenu1ActionPerformed
        if (dashboardFrame != null) {
            dashboardFrame.setForm(new Dashboard());
        }
    }//GEN-LAST:event_buttonMenu1ActionPerformed

    private void buttonMenu3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenu3ActionPerformed
        if (user != null && "Admin".equals(user.getRole()) && dashboardFrame != null) {
             setSelected(2); // Ensure button is selected on click
            dashboardFrame.setForm(new Users());
        }
    }//GEN-LAST:event_buttonMenu3ActionPerformed

    private void buttonMenu7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonMenu7ActionPerformed
          if (dashboardFrame != null) {
              setSelected(menuButtons.indexOf(buttonMenu7));
              dashboardFrame.setForm(new BorrowedTransactionsPanel());
        }
    }//GEN-LAST:event_buttonMenu7ActionPerformed

  
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private components.ButtonMenu buttonMenu1;
    private components.ButtonMenu buttonMenu2;
    private components.ButtonMenu buttonMenu3;
    private components.ButtonMenu buttonMenu4;
    private components.ButtonMenu buttonMenu5;
    private components.ButtonMenu buttonMenu6;
    private components.ButtonMenu buttonMenu7;
    private swing.ImageAvatar imageAvatar1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel panelMenu;
    // End of variables declaration//GEN-END:variables

    


   
}