package peak.can;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import peak.can.basic.*;

public class ModeSelectionPanel extends JPanel {
    private JTextField voltageField;
    private JTextField currentField;
    private JTextField powerField;
    private JTextField ackVoltField;
    private JTextField ackCurrField;
    private JTextField ackPowerField;

    private CANTransmitter canTransmitter;
    private ArrayList<ModeBox> modeBoxes = new ArrayList<>();

    public ModeSelectionPanel(CANTransmitter canTransmitter) {
        this.canTransmitter = canTransmitter;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        JLabel heading = new JLabel("Mode Selection", JLabel.CENTER);
        heading.setForeground(Color.YELLOW);
        heading.setFont(new Font("Arial", Font.BOLD, 20));
        heading.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(heading, BorderLayout.NORTH);

        JPanel modesContainer = new JPanel(new GridLayout(2, 2, 10, 10));
        modesContainer.setBackground(Color.BLACK);
        modesContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ModeBox cvBox = new ModeBox("CV", "Voltage", "Current", "Power", (byte) 0xE2);
        ModeBox ccBox = new ModeBox("CC", "Current", "Voltage", "Power", (byte) 0xE3);
        ModeBox cpBox = new ModeBox("CP", "Power", "Voltage", "Current", (byte) 0xE4);
        ModeBox CC_CVBox = new ModeBox("CC-CV", "Voltage", "Current", "Power", (byte) 0xE5);

        modeBoxes.add(cvBox);
        modeBoxes.add(ccBox);
        modeBoxes.add(cpBox);
        modeBoxes.add(CC_CVBox);

        modesContainer.add(cvBox);
        modesContainer.add(ccBox);
        modesContainer.add(cpBox);
        modesContainer.add(CC_CVBox);

        add(modesContainer, BorderLayout.CENTER);
    }

    private class ModeBox extends JPanel {
        private final String mode, set1, lmt1, lmt2;
        private final byte commandByte;
        private final Color defaultColor = Color.DARK_GRAY;
        private final Color hoverColor = new Color(80, 80, 80);
        private final Color selectedColor = new Color(0, 120, 215);
        private boolean isSelected = false;

        public ModeBox(String mode, String set1, String lmt1, String lmt2, byte commandByte) {
            this.mode = mode;
            this.set1 = set1;
            this.lmt1 = lmt1;
            this.lmt2 = lmt2;
            this.commandByte = commandByte;
            setBackground(defaultColor);
            setPreferredSize(new Dimension(100, 60));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setLayout(new GridBagLayout());

            JLabel label = new JLabel(mode);
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 19));
            add(label);

            setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectThisBox();
                    openSettingsPage(mode, set1, lmt1, lmt2);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!isSelected) setBackground(hoverColor);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!isSelected) setBackground(defaultColor);
                }
            });
        }

        private void selectThisBox() {
            for (ModeBox box : modeBoxes) {
                box.isSelected = false;
                box.setBackground(box.defaultColor);
            }
            this.isSelected = true;
            this.setBackground(Color.GREEN);

            try {
                sendCANMessage1(commandByte);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(ModeSelectionPanel.this, "Failed to send CAN message.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void sendCANMessage1(byte modeCommandByte) throws Exception {
        byte[] data = new byte[8];
        data[0] = modeCommandByte;
        data[1] = 0;
        for (int i = 2; i < 8; i++) {
            data[i] = 0x00;
        }

        TPCANMsg message = new TPCANMsg();
        message.setID(0x123);
        message.setLength((byte) 8);
        message.setType(TPCANMessageType.PCAN_MESSAGE_STANDARD);
        message.setData(data, (byte) 8);

        TPCANStatus status = canTransmitter.getCAN().Write(TPCANHandle.PCAN_USBBUS1, message);
        System.out.println("Selected mode sent: " + (int) modeCommandByte);
        if (status != TPCANStatus.PCAN_ERROR_OK) {
            throw new Exception("CAN message send failed.");
        }
    }

    private void openSettingsPage(String mode, String set1, String lmt1, String lmt2) {
        JFrame dialog = new JFrame(mode + " Settings");
        dialog.setSize(400, 500);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(Color.BLACK);

        JLabel heading = new JLabel(mode + " Settings", SwingConstants.CENTER);
        heading.setFont(new Font("Digital-7", Font.BOLD, 28));
        heading.setForeground(Color.ORANGE);
        heading.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        dialog.add(heading, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setOpaque(false);

        voltageField = createDigitalTextField();
        currentField = createDigitalTextField();
        powerField = createDigitalTextField();

        inputPanel.add(createLabel(set1 + " Set    :"));
        inputPanel.add(voltageField);
        inputPanel.add(createLabel(lmt1 + " Limit Set    :"));
        inputPanel.add(currentField);
        inputPanel.add(createLabel(lmt2 + " Limit Set    :"));
        inputPanel.add(powerField);

        JPanel ackPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        ackPanel.setOpaque(false);
        ackPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.CYAN, 2, true),
            "ACKNOWLEDGEMENTS",
            0,
            0,
            new Font("Arial", Font.BOLD, 16),
            Color.CYAN
        ));

        ackVoltField = createAckField();
        ackCurrField = createAckField();
        ackPowerField = createAckField();

        ackPanel.add(createAckLabel("ACK " + set1));
        ackPanel.add(ackVoltField);
        ackPanel.add(createAckLabel("ACK " + lmt1));
        ackPanel.add(ackCurrField);
        ackPanel.add(createAckLabel("ACK " + lmt2));
        ackPanel.add(ackPowerField);

        JPanel allInputs = new JPanel(new BorderLayout());
        allInputs.setOpaque(false);
        allInputs.add(inputPanel, BorderLayout.CENTER);
        allInputs.add(ackPanel, BorderLayout.SOUTH);

        JButton submitButton = new JButton("Submit");
        submitButton.setPreferredSize(new Dimension(100, 30));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(submitButton);

        submitButton.addActionListener(e -> sendCANMessage(mode, voltageField, currentField, powerField));

        dialog.add(allInputs, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text, JLabel.RIGHT);
        label.setFont(new Font("Digital-7", Font.BOLD, 18));
        label.setForeground(Color.CYAN);
        return label;
    }

    private JLabel createAckLabel(String text) {
        JLabel label = new JLabel("📟 " + text + " :", JLabel.RIGHT);
        label.setFont(new Font("Consolas", Font.ITALIC, 16));
        label.setForeground(Color.ORANGE);
        return label;
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

    private JTextField createAckField() {
        JTextField textField = new JTextField("0");
        textField.setFont(new Font("Consolas", Font.BOLD, 28));
        textField.setForeground(Color.YELLOW);
        textField.setBackground(new Color(30, 30, 60));
        textField.setHorizontalAlignment(SwingConstants.CENTER);
        textField.setEditable(false);
        textField.setBorder(BorderFactory.createDashedBorder(Color.CYAN, 2, 2));
        return textField;
    }

    private void sendCANMessage(String mode, JTextField voltageField, JTextField currentField, JTextField powerField) {
        try {
            int voltage = Integer.parseInt(voltageField.getText());
            int current = Integer.parseInt(currentField.getText());
            int power = Integer.parseInt(powerField.getText());

            int setID;
            switch (mode) {
                case "CV":
                case "CC":
                    setID = 0x125;
                    break;
                case "CP":
                    setID = 0x126;
                    break;
                default:
                    throw new AssertionError();
            }

            byte[] data = new byte[8];
            data[0] = (byte) (voltage & 0xFF);
            data[1] = (byte) ((voltage >> 8) & 0xFF);
            data[2] = (byte) (current & 0xFF);
            data[3] = (byte) ((current >> 8) & 0xFF);
            data[4] = (byte) (power & 0xFF);
            data[5] = (byte) ((power >> 8) & 0xFF);
            data[6] = (byte) 161;

            TPCANMsg message = new TPCANMsg();
            message.setID(setID);
            message.setLength((byte) 8);
            message.setType(TPCANMessageType.PCAN_MESSAGE_STANDARD);
            message.setData(data, (byte) 8);

            TPCANStatus status = canTransmitter.getCAN().Write(TPCANHandle.PCAN_USBBUS1, message);
            if (status != TPCANStatus.PCAN_ERROR_OK) {
                JOptionPane.showMessageDialog(this, "Error sending CAN message", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter numeric values.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to call from CAN Reception Handler
    public void updateAckValues(int ackVolt, int ackCurrent, int ackPower) {
        if (ackVoltField != null) ackVoltField.setText(String.valueOf(ackVolt));
        if (ackCurrField != null) ackCurrField.setText(String.valueOf(ackCurrent));
        if (ackPowerField != null) ackPowerField.setText(String.valueOf(ackPower));
    }
}
