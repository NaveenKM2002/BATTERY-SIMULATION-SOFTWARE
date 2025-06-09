package peak.can;

import javax.swing.*;
import java.awt.*;

public class StatusLivePanel extends JPanel {
    private final JPanel[] indicators; // Array of 10 status indicator panels
    private final String[] statusNames = {
        "Ready To Start",
        "DCPS ON",
        "Local Mode",
        "Remote Mode",
        "DCPS Fault",
        "Error Alarm",
        "O/P UnderVoltage",
        "O/P OverVoltage",
        "Cycle Process",
        "Cycle Completed"
    };

    public StatusLivePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.LIGHT_GRAY);

        indicators = new JPanel[statusNames.length]; // Initialize 10 indicator panels

        // Create the main panel for status indicators
        JPanel statusIndicatorPanel = new JPanel(new GridBagLayout());
        statusIndicatorPanel.setBackground(Color.LIGHT_GRAY);
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        JLabel headingLabel = new JLabel("Status/Faults", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 20));
        headingLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(headingLabel, BorderLayout.NORTH);

        // Add status rows
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add some padding between components
        gbc.anchor = GridBagConstraints.WEST; // Align components to the left
        for (int i = 0; i < statusNames.length; i += 2) {
            addStatusRow(statusIndicatorPanel, gbc, i);
        }

        // Add the status indicators panel to the main panel
        add(statusIndicatorPanel, BorderLayout.CENTER);
    }

    private void addStatusRow(JPanel panel, GridBagConstraints gbc, int startIndex) {
        gbc.gridx = 0;
        gbc.gridy = startIndex / 2;

        // Add the first indicator and label
        JPanel firstIndicator = new JPanel();
        firstIndicator.setPreferredSize(new Dimension(25, 25)); // Set size for the status box
        firstIndicator.setBackground(Color.RED); // Default color is red (inactive)
        indicators[startIndex] = firstIndicator; // Assign to the corresponding array index
        panel.add(firstIndicator, gbc);
        gbc.gridx++;
        panel.add(new JLabel(statusNames[startIndex]), gbc);

        // Add the second indicator and label if it exists
        if (startIndex + 1 < statusNames.length) {
            gbc.gridx += 1;
            JPanel secondIndicator = new JPanel();
            secondIndicator.setPreferredSize(new Dimension(25, 25)); // Set size for the status box
            secondIndicator.setBackground(Color.RED); // Default color is red (inactive)
            indicators[startIndex + 1] = secondIndicator; // Assign to the corresponding array index
            panel.add(secondIndicator, gbc);
            gbc.gridx++;
            panel.add(new JLabel(statusNames[startIndex + 1]), gbc);
        }
    }

    public void updateStatusFromBits(int status) {
        System.out.printf("Status Bits: %s%n", Integer.toBinaryString(status));
        for (int i = 0; i < indicators.length; i++) {
            boolean isActive = ((status >> i) & 1) == 1; // Extract the i-th bit
            updateIndicatorColor(i, isActive); // Update the individual indicator color
        }
    }

    // Method to update the color of the indicator based on status (1 = green, 0 = red)
    private void updateIndicatorColor(int index, boolean isActive) {
        JPanel indicator = indicators[index]; // Get the indicator panel at the specified index
        indicator.setBackground(isActive ? Color.GREEN : Color.RED); // Set the color (green for active, red for inactive)
        revalidate(); // Ensure layout updates correctly
        repaint(); // Redraw the component
    }

    // Separate method for updating the "Ready To Start" indicator
    public void updateReadyToStartStatus(boolean isActive) {
        updateIndicatorColor(0, true); // "Ready To Start" is at index 0
    }

    // Separate method for updating the "DCPS ON" indicator
    public void updateDCPSOnStatus(boolean isActive) {
        updateIndicatorColor(1, isActive); // "DCPS ON" is at index 1
    }
}
