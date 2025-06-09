package peak.can;

import peak.can.basic.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StatusPanel extends JPanel {
    private CANTransmitter canTransmitter;

    public StatusPanel(CANTransmitter canTransmitter) {
        this.canTransmitter = canTransmitter;

        // Set the panel size and background color
        setPreferredSize(new Dimension(350, 400));
        setBackground(Color.BLACK);

        // Set layout and add a border
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));

        // Add a heading at the top
        JLabel headingLabel = new JLabel("Control Commands", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headingLabel.setForeground(Color.YELLOW);
        headingLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(headingLabel, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(3, 2, 10, 10));
        buttonsPanel.setBackground(Color.BLACK);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton onButton = createStyledButton("DCPS ON");
        JButton offButton = createStyledButton("DCPS OFF");
        JButton startButton = createStyledButton("CYCLE START");
        JButton stopButton = createStyledButton("CYCLE STOP");
        JButton pauseButton = createStyledButton("RESET");
        JButton clearDataButton = createStyledButton("<html><center>DATA CLEAR</center></html>");

        onButton.addActionListener(e -> sendCANMessage(0x129, (byte) 0x00, (byte) 161, 8));
        offButton.addActionListener(e -> sendCANMessage(0x129, (byte) 0x00, (byte) 160, 8));
        startButton.addActionListener(e -> sendCANMessage(0x129, (byte) 0x00, (byte) 173, 8));
        stopButton.addActionListener(e -> sendCANMessage(0x129, (byte) 0x00, (byte) 174, 8));
        pauseButton.addActionListener(e -> sendCANMessage(0x123, (byte) 161, (byte) 0x00, 2));
        clearDataButton.addActionListener(e -> sendCANMessage(0x129, (byte) 0x00, (byte) 167, 7));

        buttonsPanel.add(onButton);
        buttonsPanel.add(offButton);
        buttonsPanel.add(startButton);
        buttonsPanel.add(stopButton);
        buttonsPanel.add(pauseButton);
        buttonsPanel.add(clearDataButton);

        add(buttonsPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 50));
        button.setFont(new Font("Digital-7", Font.BOLD, 18));
        button.setForeground(Color.GREEN);
        button.setBackground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(30, 144, 255));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.BLACK);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(new Color(0, 255, 0));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(Color.BLACK);
            }
        });

        return button;
    }

    private void sendCANMessage(int id, byte byte2Value, byte byteValue, int position) {
        try {
            byte[] data = new byte[8];
            data[position - 1] = byteValue;
            data[1] = byte2Value;

            TPCANMsg canMessage = new TPCANMsg();
            canMessage.setID(id);
            canMessage.setLength((byte) 8);
            canMessage.setData(data, (byte) 8);

            TPCANStatus status = canTransmitter.getCAN().Write(TPCANHandle.PCAN_USBBUS1, canMessage);
            if (status != TPCANStatus.PCAN_ERROR_OK) {
                JOptionPane.showMessageDialog(this, "Error sending CAN message.", "Transmit Error", JOptionPane.ERROR_MESSAGE);
            } else {
                System.out.println("Message Sent: ID: " + canMessage.getID() + " Data: " + java.util.Arrays.toString(data));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
