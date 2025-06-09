package peak.can;

import peak.can.basic.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProtectionSettingPanel extends JPanel {
    private final JTextField voltageField;
    private final JTextField currentField;
    private final JTextField powerField;
    private final JButton submitButton;
    private final JButton clearButton;
    private final CANTransmitter can;

    public ProtectionSettingPanel(CANTransmitter can) {
        this.can = can;

        setPreferredSize(new Dimension(400, 255));
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        setLayout(new BorderLayout());

        JLabel headingLabel = new JLabel("Limit Settings", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headingLabel.setForeground(Color.YELLOW);
        headingLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(headingLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setOpaque(false);

        Font labelFont = new Font("Digital-7", Font.BOLD, 18);

        JLabel voltageLabel = new JLabel(" Voltage Limit(V):");
        voltageLabel.setFont(labelFont);
        voltageLabel.setForeground(Color.CYAN);
        voltageField = createDigitalTextField();
        voltageField.setPreferredSize(new Dimension(150, 40));

        JLabel powerLabel = new JLabel(" Power Limit(kW):");
        powerLabel.setFont(labelFont);
        powerLabel.setForeground(Color.CYAN);
        powerField = createDigitalTextField();
        powerField.setPreferredSize(new Dimension(150, 40));

        JLabel currentLabel = new JLabel(" Current Max(A):");
        currentLabel.setFont(labelFont);
        currentLabel.setForeground(Color.CYAN);
        currentField = createDigitalTextField();
        currentField.setPreferredSize(new Dimension(150, 40));

        inputPanel.add(voltageLabel);
        inputPanel.add(voltageField);
        inputPanel.add(powerLabel);
        inputPanel.add(powerField);
        inputPanel.add(currentLabel);
        inputPanel.add(currentField);

        submitButton = createDigitalButton("Submit");
        submitButton.addActionListener(new SubmitButtonListener());

        clearButton = createDigitalButton("Clear");
        clearButton.addActionListener(new ClearButtonListener());

        JButton protectionSetButton = createDigitalButton("Protection Set");
        protectionSetButton.addActionListener(e -> openProtectionSettingsDialog());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(submitButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(protectionSetButton);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(inputPanel, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        centerPanel.setOpaque(false);

        add(centerPanel, BorderLayout.CENTER);
    }

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

    private JButton createDigitalButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Digital-7", Font.BOLD, 20));
        button.setForeground(Color.GREEN);
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        button.setFocusPainted(false);
        return button;
    }

    private class SubmitButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int voltage = Integer.parseInt(voltageField.getText()) - 10;
                int current = Integer.parseInt(currentField.getText());
                int power = Integer.parseInt(powerField.getText());

                if (voltage < 50 || voltage > 1200 || current < 0 || current > 1200 || power < 0 || power > 540) {
                    JOptionPane.showMessageDialog(ProtectionSettingPanel.this, "Values must be within valid ranges.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                sendCANMessage(voltage, current, power, (byte) 0xA3, "Message Sent Successfully!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(ProtectionSettingPanel.this, "Please enter valid integer values.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(ProtectionSettingPanel.this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class ClearButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            voltageField.setText("");
            currentField.setText("");
            powerField.setText("");

            try {
                sendCANMessage(0, 0, 0, (byte) 0xA0, "Fields Cleared and CAN Message Sent Successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(ProtectionSettingPanel.this, "Error while sending clear CAN message: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void sendCANMessage(int voltage, int current, int power, byte byte2, String successMessage) throws Exception {
        byte[] data = new byte[8];
//        data[0] = (byte) 0xEA;
//        data[1] = byte2;

        data[2] = (byte) (voltage & 0xFF);
        data[3] = (byte) ((voltage >> 8) & 0xFF);
        data[4] = (byte) (current & 0xFF);
        data[5] = (byte) ((current >> 8) & 0xFF);
//        data[6] = (byte) (power & 0xFF);
//        data[7] = (byte) ((power >> 8) & 0xFF);

        TPCANMsg canMessage = new TPCANMsg();
        canMessage.setID(0x128);
        canMessage.setLength((byte) 8);
        canMessage.setData(data, (byte) 8);

        TPCANStatus status = can.getCAN().Write(TPCANHandle.PCAN_USBBUS1, canMessage);
        if (status != TPCANStatus.PCAN_ERROR_OK) {
            throw new Exception("Error sending CAN message.");
        } else {
            JOptionPane.showMessageDialog(this, successMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void openProtectionSettingsDialog() {
        JDialog dialog = new JDialog((Frame) null, "Protection Setting", true);
        dialog.setSize(400, 400);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.BLACK);

        JLabel heading = new JLabel("PROTECTION SETTING", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 20));
        heading.setForeground(Color.YELLOW);
        heading.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        dialog.add(heading, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setOpaque(false);

        JLabel underVoltLabel = new JLabel(" Under Voltage Set (V):");
        underVoltLabel.setFont(new Font("Digital-7", Font.BOLD, 18));
        underVoltLabel.setForeground(Color.CYAN);
        JTextField underVoltField = createDigitalTextField();

        JLabel overVoltLabel = new JLabel(" Over Voltage Set (V):");
        overVoltLabel.setFont(new Font("Digital-7", Font.BOLD, 18));
        overVoltLabel.setForeground(Color.CYAN);
        JTextField overVoltField = createDigitalTextField();

        JLabel overloadLabel = new JLabel(" Overload (A):");
        overloadLabel.setFont(new Font("Digital-7", Font.BOLD, 18));
        overloadLabel.setForeground(Color.CYAN);
        JTextField overloadField = createDigitalTextField();

        JLabel slewRateLabel = new JLabel(" Slew Rate:");
        slewRateLabel.setFont(new Font("Digital-7", Font.BOLD, 18));
        slewRateLabel.setForeground(Color.CYAN);
        JTextField slewRateField = createDigitalTextField();

        inputPanel.add(underVoltLabel);
        inputPanel.add(underVoltField);
        inputPanel.add(overVoltLabel);
        inputPanel.add(overVoltField);
        inputPanel.add(overloadLabel);
        inputPanel.add(overloadField);
        inputPanel.add(slewRateLabel);
        inputPanel.add(slewRateField);

        JButton submitProtectionButton = createDigitalButton("Submit");
        submitProtectionButton.addActionListener(e -> {
            try {
                int underVolt = Integer.parseInt(underVoltField.getText());
                int overVolt = Integer.parseInt(overVoltField.getText());
                int overload = Integer.parseInt(overloadField.getText());
                int slewRate =  Integer.parseInt(slewRateField.getText());

                sendProtectionCANMessage(slewRate, underVolt, overVolt, overload);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Enter valid numeric values!", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error sending CAN message: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.BLACK);
        bottomPanel.add(submitProtectionButton);

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void sendProtectionCANMessage(int slewRate, int underVolt, int overVolt, int overload) throws Exception {
        byte[] data = new byte[8];
        data[0] = (byte) (overVolt & 0xFF);
        data[1] = (byte) ((overVolt >> 8) & 0xFF);
        data[2] = (byte) (underVolt & 0xFF);
        data[3] = (byte) ((underVolt >> 8) & 0xFF);
        data[4] = (byte) (overload & 0xFF);
        data[5] = (byte) ((overload >> 8) & 0xFF);
        data[6] = (byte) (slewRate & 0xFF);
        data[7] = (byte) ((slewRate >> 8) & 0xFF);


        TPCANMsg msg = new TPCANMsg();
        msg.setID(0x124);
        msg.setLength((byte) 8);
        msg.setData(data, (byte) 8);

        TPCANStatus status = can.getCAN().Write(TPCANHandle.PCAN_USBBUS1, msg);
        if (status != TPCANStatus.PCAN_ERROR_OK) {
            throw new Exception("CAN transmission failed.");
        } else {
            JOptionPane.showMessageDialog(this, "Protection Settings Sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
