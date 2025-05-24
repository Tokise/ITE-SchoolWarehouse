package Package1;

import modules.Inventory;
import modules.Dashboard;
import modules.Users;
import modules.PurchaseOrder;
import modules.Reports;
import modules.Settings;
import modules.KioskDashboard;

import javax.swing.*;
import java.awt.*;

public class DashBoardFrame1 extends javax.swing.JFrame {

    private User user;
    private Dashboard adminDashboard;
    private Inventory inventory;
    private Users usersPanel;
    private PurchaseOrder purchaseOrder;
    private Reports reports;
    private Settings settings;

    private KioskDashboard kioskDashboard;

    public DashBoardFrame1(User user) {
        initComponents();
        this.user = user;
        sidebar1.setDashboardFrame(this);
        sidebar1.setUser(user);
        this.setSize(1200, 710);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        contentPanel1.setLayout(new BorderLayout());
        header2.setParentFrame(this);

        initializeAndShowDashboard();
    }

    private void initializeAndShowDashboard() {
        if (user != null) {
            System.out.println("Logged in user role: " + user.getRole());

            if ("KioskUser".equals(user.getRole())) {
                sidebar1.setVisible(false);
                 getContentPane().revalidate();
                 getContentPane().repaint();

                showKioskDashboard();
            } else {
                sidebar1.setVisible(true);
                showAdminDashboard();
            }
        } else {
            System.err.println("User is null in initializeAndShowDashboard!");
            sidebar1.setVisible(true);
            showAdminDashboard();
        }
    }


    void showAdminDashboard() {
        if (adminDashboard == null) {
            adminDashboard = new Dashboard();
        }
        setForm(adminDashboard);
    }

    void showKioskDashboard() {
         if (kioskDashboard == null) {
            kioskDashboard = new KioskDashboard();
             if (user != null) {
                 kioskDashboard.setCurrentUser(user);
             } else {
                 System.err.println("User is null when setting KioskDashboard user!");
             }
        }
        setForm(kioskDashboard);
    }


    public void setForm(JPanel form) {
        contentPanel1.removeAll();
        contentPanel1.add(form, BorderLayout.CENTER);
        contentPanel1.repaint();
        contentPanel1.revalidate();
    }

    public void setForm(Dashboard dashboard) {
        setForm((JPanel) dashboard);
    }

    public void setForm(Inventory inventory) {
        this.inventory = inventory;
        System.out.println("DashBoardFrame1: setForm(Inventory) called");
        if (user != null) {
            System.out.println("DashBoardFrame1: User ID before setting Inventory: " + user.getUserId());
            inventory.setCurrentUserId(user.getUserId());
            System.out.println("DashBoardFrame1: Setting Inventory's User ID to: " + user.getUserId());
        } else {
            System.err.println("DashBoardFrame1: User is null when trying to set Inventory's User ID!");
        }
        setForm((JPanel) inventory);
    }


    public void setForm(Users users) {
        this.usersPanel = users;

        setForm((JPanel) usersPanel);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (DashBoardFrame1.this.user != null) {

                    usersPanel.setCurrentUserId(DashBoardFrame1.this.user);
                } else {

                    System.err.println("Error: Current user not set in DashBoardFrame1 after invokeLater for Users form!");

                }
            }
        });
    }

    public void setForm(PurchaseOrder purchaseOrder) {
         this.purchaseOrder = purchaseOrder;
         if (user != null) {
         }
        setForm((JPanel) purchaseOrder);
    }

    public void setForm(Reports reports) {
         this.reports = reports;
         if (user != null) {
         }
        setForm((JPanel) reports);
    }

    public void setForm(Settings settings) {
         this.settings = settings;
         if (user != null) {
         }
        setForm((JPanel) settings);
    }

     public void setForm(KioskDashboard kioskDashboard) {
         this.kioskDashboard = kioskDashboard;
         setForm((JPanel) kioskDashboard);
     }


    public void logout() {
        this.dispose();
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentPanel1 = new form.contentPanel();
        header2 = new components.Header();
        sidebar1 = new components.Sidebar();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AssetWise Academia");
        setResizable(false);

        contentPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true), null));

        javax.swing.GroupLayout contentPanel1Layout = new javax.swing.GroupLayout(contentPanel1);
        contentPanel1.setLayout(contentPanel1Layout);
        contentPanel1Layout.setHorizontalGroup(
            contentPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        contentPanel1Layout.setVerticalGroup(
            contentPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 622, Short.MAX_VALUE)
        );

        header2.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true), null));

        sidebar1.setBorder(javax.swing.BorderFactory.createCompoundBorder(new javax.swing.border.LineBorder(new java.awt.Color(102, 102, 102), 1, true), null));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(sidebar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(contentPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 929, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(header2, javax.swing.GroupLayout.PREFERRED_SIZE, 1200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(header2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(contentPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
                    .addComponent(sidebar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run(User user) {
                new DashBoardFrame1(user).setVisible(true);
            }

            @Override
            public void run() {
                System.out.println("DashBoardFrame1 main method called without a user.");
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private form.contentPanel contentPanel1;
    private components.Header header2;
    private components.Sidebar sidebar1;
    // End of variables declaration//GEN-END:variables

}
