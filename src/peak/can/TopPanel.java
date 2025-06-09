package peak.can;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TopPanel extends JPanel {
    public TopPanel() {
        setPreferredSize(new Dimension(1300, 75));
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // Left: Logo
        JLabel logoLabel = new JLabel();
        ImageIcon logoIcon = getScaledImageIcon("/peak/can/dubas.jpg", 120, 60);
        if (logoIcon != null) {
            logoLabel.setIcon(logoIcon);
        }
        logoLabel.setHorizontalAlignment(SwingConstants.LEFT);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(5, 1, 5, 10));
        add(logoLabel, BorderLayout.WEST);

        // Center: Application Name
        JLabel appNameLabel = new JLabel("Battery Simulation Software", SwingConstants.CENTER);
        appNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        appNameLabel.setForeground(Color.WHITE);
        add(appNameLabel, BorderLayout.CENTER);

        // Right: Controls Panel
        JPanel rightPanel = new JPanel();
        rightPanel.setOpaque(false);
        rightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        JLabel userPlaceholder = new JLabel("Welcome, User");
        userPlaceholder.setFont(new Font("Arial", Font.PLAIN, 14));
        userPlaceholder.setForeground(Color.white);
        rightPanel.add(userPlaceholder);
        
//        JButton batterySettingButton = new JButton("Battery Setting");
//        batterySettingButton.addActionListener(e -> openBatterySettingWindow());
//        rightPanel.add(batterySettingButton);

        add(rightPanel, BorderLayout.EAST);
    }

    private void openBatterySettingWindow() {
        JFrame settingFrame = new JFrame("Battery Type Setting");
        settingFrame.setSize(400, 300);
        settingFrame.setLocationRelativeTo(null);
        settingFrame.setLayout(new BorderLayout());
        settingFrame.getContentPane().setBackground(Color.DARK_GRAY);
        
        JLabel headingLabel = new JLabel("Battery Type Setting", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headingLabel.setForeground(Color.CYAN);
        settingFrame.add(headingLabel, BorderLayout.NORTH);
        
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(3, 1, 10, 10));
        optionsPanel.setBackground(Color.DARK_GRAY);
        
        ButtonGroup batteryGroup = new ButtonGroup();
        JRadioButton battery1 = new JRadioButton("Battery 1");
        JRadioButton battery2 = new JRadioButton("Battery 2");
        JRadioButton battery3 = new JRadioButton("Battery 3");
        
        battery1.setForeground(Color.WHITE);
        battery2.setForeground(Color.WHITE);
        battery3.setForeground(Color.WHITE);
        battery1.setBackground(Color.DARK_GRAY);
        battery2.setBackground(Color.DARK_GRAY);
        battery3.setBackground(Color.DARK_GRAY);
        
        batteryGroup.add(battery1);
        batteryGroup.add(battery2);
        batteryGroup.add(battery3);
        
        optionsPanel.add(battery1);
        optionsPanel.add(battery2);
        optionsPanel.add(battery3);
        
        settingFrame.add(optionsPanel, BorderLayout.CENTER);
        
        JButton applyButton = new JButton("Apply");
        applyButton.setBackground(Color.CYAN);
        applyButton.setForeground(Color.BLACK);
        applyButton.setFont(new Font("Arial", Font.BOLD, 14));
        applyButton.addActionListener(e -> {
            if (battery1.isSelected()) {
                transmitBatteryData(1);
            } else if (battery2.isSelected()) {
                transmitBatteryData(2);
            } else if (battery3.isSelected()) {
                transmitBatteryData(3);
            }
            settingFrame.dispose();
        });
        
        settingFrame.add(applyButton, BorderLayout.SOUTH);
        settingFrame.setVisible(true);
    }

    private void transmitBatteryData(int batteryNumber) {
        int txId = 0x111;
        byte firstByte = (byte) 0xFF;
        byte secondByte = (byte) 0xE7;
        byte thirdByte = (byte) batteryNumber;
        
        System.out.println("PCAN Transmitting: TX ID: " + Integer.toHexString(txId) + 
                " Data: [" + String.format("%02X", firstByte) + ", " + 
                String.format("%02X", secondByte) + ", " + 
                String.format("%02X", thirdByte) + "]");
    }

    private ImageIcon getScaledImageIcon(String resourcePath, int width, int height) {
        try {
            java.net.URL resourceURL = getClass().getResource(resourcePath);
            if (resourceURL != null) {
                ImageIcon icon = new ImageIcon(resourceURL);
                Image image = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(image);
            } else {
                System.err.println("Resource not found: " + resourcePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
