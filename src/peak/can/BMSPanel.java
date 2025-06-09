package peak.can;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import peak.can.basic.*;

public class BMSPanel extends JPanel {
    
    private final JTextField voltageField;
    private final JTextField powerField;
    private final JTextField powerlabelMaxField;
    private final JButton submitButton;
    private final JButton clearButton;
    private CANTransmitter can;

    public BMSPanel(CANTransmitter can) {
         this.can = can;

        setPreferredSize(new Dimension(400, 255));
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2)); // Dark gray border
        setLayout(new BorderLayout());

        // Add heading with yellow text
        JLabel headingLabel = new JLabel("Battery Connections", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headingLabel.setForeground(Color.YELLOW); // Yellow text
        headingLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Padding above and below
        add(headingLabel, BorderLayout.NORTH);

        // Create input panel with grid layout
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setOpaque(false);

        Font labelFont = new Font("Digital-7", Font.BOLD, 18); // Digital font for labels

        // Labels with cyan color and digital font
        JLabel voltageLabel = new JLabel(" Battery Volt(V):");
        voltageLabel.setFont(labelFont);
        voltageLabel.setForeground(Color.CYAN);
        voltageField = createDigitalTextField();

        JLabel powerLabel = new JLabel(" Contactor Ack:");
        powerLabel.setFont(labelFont);
        powerLabel.setForeground(Color.CYAN);
        powerField = createDigitalTextField();

        JLabel powerlabelMax = new JLabel(" Reserved:");
        powerlabelMax.setFont(labelFont);
        powerlabelMax.setForeground(Color.CYAN);
        powerlabelMaxField = createDigitalTextField();

        // Add labels and input fields to the input panel
        inputPanel.add(voltageLabel);
        inputPanel.add(voltageField);
        inputPanel.add(powerLabel);
        inputPanel.add(powerField);
        inputPanel.add(powerlabelMax);
        inputPanel.add(powerlabelMaxField);

        // Add buttons with digital style
        submitButton = createDigitalButton("Submit");
        submitButton.addActionListener(new SubmitButtonListener());

        clearButton = createDigitalButton("Clear");
        clearButton.addActionListener(new ClearButtonListener());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(submitButton);
        buttonPanel.add(clearButton);

        // Center the input panel and button panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(inputPanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        centerPanel.setOpaque(false); // Inherit background color

        // Add everything to the main panel
        add(centerPanel, BorderLayout.CENTER);
    }

    // Create digital text field with green text and black background
    private JTextField createDigitalTextField() {
        JTextField textField = new JTextField("0");
        textField.setFont(new Font("Digital-7", Font.BOLD, 31));
        textField.setForeground(Color.GREEN);
        textField.setBackground(Color.BLACK);
        textField.setHorizontalAlignment(SwingConstants.CENTER);
        textField.setOpaque(true);
        textField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        return textField;
    }

    // Create digital button with green text and black background
    private JButton createDigitalButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Digital-7", Font.BOLD, 20));
        button.setForeground(Color.GREEN);
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        button.setFocusPainted(false); // Remove focus painting for cleaner look
        return button;
    }

    private class SubmitButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                 if (can == null || can.getCAN() == null) {
                JOptionPane.showMessageDialog(BMSPanel.this, 
                    "CAN interface is not connected! Please connect before submitting.", 
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
                return; // Stop execution if CAN is not connected
            }
                // Parse user input
                int voltage = Integer.parseInt(voltageField.getText());
                int power = Integer.parseInt(powerlabelMaxField.getText());
                int current = Integer.parseInt(powerField.getText());

                // Validate input ranges
                if (voltage < 0 || voltage > 1200 || current < 0 || current > 1000 || power < 0 || power > 1000) {
                    JOptionPane.showMessageDialog(BMSPanel.this, "Values must be within valid ranges.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Send CAN message with the entered values
                sendCANMessage(voltage, current, power, (byte) 0xA9, "Message Sent Successfully!");
            }  catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(BMSPanel.this, 
                "Please enter valid integer values.", 
                "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(BMSPanel.this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ClearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Clear the text fields
            voltageField.setText("");
            powerlabelMaxField.setText("");
            powerField.setText("");

            // Send CAN message indicating cleared fields
            try {
                sendCANMessage(0, 0, 0, (byte) 0xA7, "Fields Cleared and CAN Message Sent Successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(BMSPanel.this, "Error while sending clear CAN message: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void sendCANMessage(int voltage, int current, int power, byte byte2, String successMessage) throws Exception {
        // Prepare CAN message data
        byte[] data = new byte[8];
        data[0] = (byte) 0xEC;
        data[1] = byte2;

        // Voltage
        data[2] = (byte) (voltage & 0xFF);
        data[3] = (byte) ((voltage >> 8) & 0xFF);

        // Current
        data[4] = (byte) (current & 0xFF);
        data[5] = (byte) ((current >> 8) & 0xFF);

        // Power
        data[6] = (byte) (power & 0xFF);
        data[7] = (byte) ((power >> 8) & 0xFF);

        // Create CAN message
        TPCANMsg canMessage = new TPCANMsg();
        canMessage.setID(0x111); // Sample CAN ID
        canMessage.setLength((byte) 8);
        canMessage.setData(data, (byte) 8);

        // Send CAN message
        TPCANStatus status = can.getCAN().Write(TPCANHandle.PCAN_USBBUS1, canMessage);
        if (status != TPCANStatus.PCAN_ERROR_OK) {
            throw new Exception("Error sending CAN message.");
        } else {
            JOptionPane.showMessageDialog(BMSPanel.this, successMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
