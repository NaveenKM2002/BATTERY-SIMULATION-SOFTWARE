package peak.can;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RealTimeParametersPanel extends JPanel {
    private final JLabel voltageField;
    private final JLabel currentField;
    private final JLabel powerField;
    private final JLabel liveVoltageField;
    private final JLabel batteryVoltField;
    private final JButton exportButton;
    private Timer loggingTimer;
    private StringBuilder logData;

    public RealTimeParametersPanel() {
        setPreferredSize(new Dimension(400, 300));
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        setLayout(new BorderLayout());

        JLabel headingLabel = new JLabel("Real Time Parameters", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 26));
        headingLabel.setForeground(Color.YELLOW);
        headingLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(headingLabel, BorderLayout.NORTH);

        JPanel gridPanel = new JPanel(new GridLayout(5, 2, 10, 8));
        gridPanel.setBackground(Color.BLACK);

        voltageField = createDigitalLabel();
        currentField = createDigitalLabel();
        powerField = createDigitalLabel();
        liveVoltageField = createDigitalLabel();
        batteryVoltField = createDigitalLabel();

        gridPanel.add(createTitleLabel("Voltage (V):"));
        gridPanel.add(voltageField);
        gridPanel.add(createTitleLabel("Current (A):"));
        gridPanel.add(currentField);
        gridPanel.add(createTitleLabel("Power (KW):"));
        gridPanel.add(powerField);
        gridPanel.add(createTitleLabel("CC Volts Limit:"));
        gridPanel.add(liveVoltageField);
        gridPanel.add(createTitleLabel("Battery Volts:"));
        gridPanel.add(batteryVoltField);

        add(gridPanel, BorderLayout.CENTER);

        exportButton = new JButton("Export");
        exportButton.setFont(new Font("Arial", Font.BOLD, 18));
        exportButton.addActionListener(e -> exportLog());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(exportButton);
        add(buttonPanel, BorderLayout.SOUTH);

        logData = new StringBuilder();
        startLogging();
    }

    private JLabel createDigitalLabel() {
        JLabel label = new JLabel("0", SwingConstants.CENTER);
        label.setFont(new Font("Digital-7", Font.BOLD, 34));
        label.setOpaque(true);
        label.setBackground(Color.BLACK);
        label.setForeground(Color.GREEN);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        return label;
    }

    private JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(Color.CYAN);
        return label;
    }

    public void updateParameters(int voltage, int current, int power) {
        SwingUtilities.invokeLater(() -> {
            voltageField.setText(String.format("%d V", voltage));
            currentField.setText(String.format("%d A", current));
            powerField.setText(String.format("%d KW", power));
        });
    }

    public void updateVoltages(int batteryVolts, int ccVolts) {
        int adjustedCcVolts = ccVolts + 10;
        if (adjustedCcVolts < 40) {
            adjustedCcVolts = 0;
        }

        int finalCcVolts = adjustedCcVolts;
        SwingUtilities.invokeLater(() -> {
            batteryVoltField.setText(String.format("%d V", batteryVolts));
            liveVoltageField.setText(String.format("%d V", finalCcVolts));
        });
    }

    private void startLogging() {
        loggingTimer = new Timer();
        loggingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                logCurrentData();
            }
        }, 0, 1000); // Logs every second
    }

    private void logCurrentData() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String currentTime = formatter.format(new Date());
        String current = currentField.getText().replace(" A", "");
        logData.append(current).append(",").append(currentTime).append("\n");
    }

    private void exportLog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Log File");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write("Current(A),Time(HH:mm:ss)\n");
                writer.write(logData.toString());
                writer.flush();
                JOptionPane.showMessageDialog(this, "Log File Saved Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error Saving File!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
