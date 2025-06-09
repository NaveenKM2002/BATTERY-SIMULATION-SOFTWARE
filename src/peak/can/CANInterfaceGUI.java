package peak.can;

import peak.can.basic.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CANInterfaceGUI extends JPanel {
    // GUI components for CAN interface
    private JComboBox<String> baudRateComboBox;
    private JTextArea transmissionArea;
    private JTextArea receptionArea;
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton exportButton;
    private JButton sendFileButton;
    private JButton transmitButton;
    private JLabel cycleTimeLabel;
    private JFileChooser fileChooser;
    private JLabel currentFrameLabel;
     private JButton viewButton;
     int a1,v1,p1;
    // PCANBasic object for interacting with the CAN interface
    private PCANBasic can;
    private CANTransmitter canTransmitter;
    private CANReceptionThread receptionThread;

    private final List<String> dataToTransmit;
    private Application application;
    private RealTimeParametersPanel realTimeParametersPanel;
    private StatusPanel statusPanel;
    private CurrentGraphPanel currentGraphPanel;
    private StatusLivePanel statusLivePanel;
    private boolean isCycleStarted = false;
//    private CANInterfacePanel canInterfacePanel;
  private  JPanel[] indicators; // Array of 10 status indicator panels
    private final String[] statusNames = {
        "Ready To Start",
        "Fill Complete",
        "Remote Mode",
        "<html>Ready to <br>Switch On Contactor</html>",
        "<html>Ready to <br>Switch On</html>",
        "Error",
        "Contactor On",
        "DCPS Healthy",
        "DCDC Trip",
        "Cycle Process"
    };
 public void startCycle() {
        isCycleStarted = true; // Start the cycle
    }
    public CANInterfaceGUI(Application application) {
        this.application = application;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1400, 610));

        dataToTransmit = new ArrayList<>();

        JPanel firstRowPanel = new JPanel(new BorderLayout());
        firstRowPanel.setPreferredSize(new Dimension(1400, 350));

        JPanel leftPanel = createCANInterfaceControls();

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setPreferredSize(new Dimension(600, 350));
        currentGraphPanel = new CurrentGraphPanel();
        centerPanel.add(currentGraphPanel, BorderLayout.CENTER);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(450, 350));
        realTimeParametersPanel = new RealTimeParametersPanel();
        rightPanel.add(realTimeParametersPanel, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, centerPanel);
        splitPane.setDividerLocation(330);
        JSplitPane splitPane2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitPane, rightPanel);
        splitPane2.setDividerLocation(950);

        firstRowPanel.add(splitPane2, BorderLayout.CENTER);

        JPanel secondRowPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        secondRowPanel.setPreferredSize(new Dimension(1300, 255));

        // Create CANTransmitter before passing it to ProtectionSettingPanel
        canTransmitter = new CANTransmitter(can, receptionThread);

        ProtectionSettingPanel protectionSettingPanel = new ProtectionSettingPanel(canTransmitter);
        protectionSettingPanel.setPreferredSize(new Dimension(330, 255));
        secondRowPanel.add(protectionSettingPanel);

        BMSPanel bmsPanel = new BMSPanel(canTransmitter);
        bmsPanel.setPreferredSize(new Dimension(330, 255));
        secondRowPanel.add(bmsPanel);

//        statusLivePanel = new StatusLivePanel();
//        statusLivePanel.setPreferredSize(new Dimension(330, 255));
//        secondRowPanel.add(statusLivePanel);

//        statusPanel = new StatusPanel(canTransmitter,currentGraphPanel);
//        statusPanel.setPreferredSize(new Dimension(330, 255));
//        secondRowPanel.add(statusPanel);

        
        
        add(firstRowPanel, BorderLayout.NORTH);
//        add(secondRowPanel, BorderLayout.SOUTH);
    }

  private JPanel createCANInterfaceControls() {
    // Create the main controls panel
     JPanel controlsPanel = new JPanel();
    controlsPanel.setBackground(Color.BLACK);  // Dark background for digital style
    controlsPanel.setLayout(new BorderLayout()); // Use BorderLayout for a structured layout

    // Create a panel for controls (connect, disconnect, etc.)
    JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 10, 10));
    buttonPanel.setBackground(Color.BLACK);

    // Baud rate dropdown
    String[] baudRates = {"50 Kbps", "250 Kbps", "500 Kbps", "1 Mbps"};
    baudRateComboBox = new JComboBox<>(baudRates);
    baudRateComboBox.setSelectedIndex(0);
    baudRateComboBox.setFont(new Font("Digital-7", Font.BOLD, 16));
    baudRateComboBox.setBackground(Color.BLACK);
    baudRateComboBox.setForeground(Color.CYAN); 

    // Text areas for transmission and reception
    transmissionArea = new JTextArea(30, 30);
    receptionArea = new JTextArea(30, 30);
    receptionArea.setEditable(false);

    // Labels for cycle time and current frame
    cycleTimeLabel = new JLabel("Cycle Time: 0 ms");
    currentFrameLabel = new JLabel("Current Frame: Not Started");

    // Buttons
    connectButton = createStyledButton("Connect");
    connectButton.addActionListener(e -> connectToCAN());

    disconnectButton = createStyledButton("Disconnect");
    disconnectButton.setEnabled(false);
    disconnectButton.addActionListener(e -> disconnectFromCAN());

    exportButton = createStyledButton("Export Data");
    exportButton.setEnabled(false);
    exportButton.addActionListener(e -> exportData());

    sendFileButton =createStyledButton("File to Import");
    sendFileButton.addActionListener(e -> selectFileToImport());

    transmitButton = createStyledButton("Transmit");
    transmitButton.setEnabled(false);
    transmitButton.addActionListener(e -> transmitData());

    fileChooser = new JFileChooser();
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    viewButton = createStyledButton("View");
      viewButton.setEnabled(false);
      viewButton.addActionListener(e -> openViewWindow());
    // Add controls to the buttonPanel
//    buttonPanel.add(new JLabel("Select Baud Rate:"));
//    buttonPanel.add(baudRateComboBox);
//    buttonPanel.add(new JLabel("Cycle Time:"));
//    buttonPanel.add(cycleTimeLabel);
//    buttonPanel.add(new JLabel("Current Frame:"));
//    buttonPanel.add(currentFrameLabel);
    buttonPanel.add(connectButton);
    buttonPanel.add(disconnectButton);
    buttonPanel.add(sendFileButton);
    buttonPanel.add(transmitButton);
    buttonPanel.add(exportButton);
    buttonPanel.add(viewButton);
 indicators = new JPanel[statusNames.length];
    // Create the status indicator panel
    JPanel statusIndicatorPanel = new JPanel(new GridBagLayout());
    statusIndicatorPanel.setBackground(Color.BLACK);
//    statusIndicatorPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2), "Status/Faults", 0, 0, new Font("Arial", Font.BOLD, 16)));
JLabel headingLabel = new JLabel("Status/Faults", SwingConstants.CENTER);
    headingLabel.setFont(new Font("Digital-7", Font.BOLD, 24)); // Digital font for heading
    headingLabel.setForeground(Color.YELLOW); // Cyan text for heading
    headingLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Add spacing around the heading

    JPanel headingPanel = new JPanel(new BorderLayout());
    headingPanel.setBackground(Color.BLACK); // Black background for heading panel
    headingPanel.add(headingLabel, BorderLayout.NORTH);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 10, 5, 10); // Padding between components
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

//    indicators = new JPanel[statusNames.length]; // Initialize the array of status indicator panels
    for (int i = 0; i < statusNames.length; i+=2) {
 
addStatusRow(statusIndicatorPanel, gbc, i);
    }
    
 JPanel statusPanelContainer = new JPanel(new BorderLayout());
    statusPanelContainer.setBackground(Color.BLACK); // Black background for status panel
    statusPanelContainer.add(headingPanel, BorderLayout.NORTH); // Add the heading at the top
    statusPanelContainer.add(statusIndicatorPanel, BorderLayout.CENTER);
    // Add the statusIndicatorPanel and buttonPanel to the controlsPanel
    controlsPanel.add(buttonPanel, BorderLayout.SOUTH);
    controlsPanel.add(statusPanelContainer, BorderLayout.CENTER);

    return controlsPanel;
}
  private JButton createStyledButton(String text) {
    JButton button = new JButton(text);
    button.setPreferredSize(new Dimension(110, 30)); // Set button size
    button.setFont(new Font("Digital-7", Font.BOLD, 18)); // Use digital font
    button.setForeground(Color.GREEN); // Green text for digital effect
    button.setBackground(Color.BLACK); // Black background for button
    button.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2)); // Cyan border for digital effect
    button.setFocusPainted(false); // Remove the focus border

    // Hover effect
    button.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            button.setBackground(new Color(30, 144, 255)); // Light blue when hovered
        }

        @Override
        public void mouseExited(MouseEvent e) {
            button.setBackground(Color.BLACK); // Return to black when exited
        }

        @Override
        public void mousePressed(MouseEvent e) {
            button.setBackground(new Color(0, 255, 0)); // Bright green when pressed
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            button.setBackground(Color.BLACK); // Return to black when released
        }
    });

    return button;
}
 private void addStatusRow(JPanel panel, GridBagConstraints gbc, int startIndex) {
    gbc.gridx = 0;
    gbc.gridy = startIndex / 2;

    // Add the first indicator and label
    JPanel firstIndicator = new JPanel();
    firstIndicator.setPreferredSize(new Dimension(25, 25)); // Set size for the status box
    firstIndicator.setBackground(Color.white); // Default color is white
    indicators[startIndex] = firstIndicator; // Assign to the corresponding array index
    panel.add(firstIndicator, gbc);

    gbc.gridx++;

    // Create the first label with white text and larger font size
    JLabel firstLabel = new JLabel(statusNames[startIndex]);
    firstLabel.setForeground(Color.WHITE); // Set text color to white
    firstLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Set font size (adjust the size if needed)
    panel.add(firstLabel, gbc);

    // Add the second indicator and label if it exists
    if (startIndex + 1 < statusNames.length) {
        gbc.gridx += 1;

        JPanel secondIndicator = new JPanel();
        secondIndicator.setPreferredSize(new Dimension(25, 25)); // Set size for the status box
        secondIndicator.setBackground(Color.white); // Default color is white
        indicators[startIndex + 1] = secondIndicator; // Assign to the corresponding array index
        panel.add(secondIndicator, gbc);

        gbc.gridx++;

        // Create the second label with white text and larger font size
        JLabel secondLabel = new JLabel(statusNames[startIndex + 1]);
        secondLabel.setForeground(Color.WHITE); // Set text color to white
        secondLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Set font size (adjust the size if needed)
        panel.add(secondLabel, gbc);
    }
}

public void updateStatusFromBits(int status) {
    for (int i = 0; i < indicators.length; i++) {
        int mappedIndex = i + 2; // Shift to start from "Remote Mode"
        if (mappedIndex < indicators.length) { // Ensure no out-of-bounds error
            boolean isActive = ((status >> i) & 1) == 1; // Extract the i-th bit
            updateIndicatorColor(mappedIndex, isActive); // Update the correct indicator
        }
    }
}


    // Method to update the color of the indicator based on status (1 = green, 0 = red)
    private void updateIndicatorColor(int index, boolean isActive) {
        JPanel indicator = indicators[index]; // Get the indicator panel at the specified index
        indicator.setBackground(isActive ? Color.GREEN : Color.white); // Set the color (green for active, red for inactive)
        revalidate(); // Ensure layout updates correctly
        repaint(); // Redraw the component
    }

    // Separate method for updating the "Ready To Start" indicator
    public void updateReadyToStartStatus(boolean isActive) {
        updateIndicatorColor(0, isActive); // "Ready To Start" is at index 0
    }

    // Separate method for updating the "DCPS ON" indicator
    public void updateDCPSOnStatus(boolean isActive) {
        updateIndicatorColor(1, isActive); // "DCPS ON" is at index 1
    }
   // Method to handle connection to CAN interface
    private void connectToCAN() {
//         updateStatusFromBits(15);
//        currentGraphPanel.addCurrentValue(10);
        try {
            // Initialize the PCANBasic API
            can = new PCANBasic();

            // Initialize the API
            if (!can.initializeAPI()) {
                JOptionPane.showMessageDialog(this, "Unable to initialize the PCAN API.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Get the selected baud rate
            String selectedBaudRate = (String) baudRateComboBox.getSelectedItem();
            TPCANBaudrate baudRateValue = getBaudRateValue(selectedBaudRate);

            // Set up the CAN interface
            TPCANStatus status = can.Initialize(TPCANHandle.PCAN_USBBUS1, baudRateValue, TPCANType.PCAN_TYPE_NONE, 0, (short) 0);

            if (status != TPCANStatus.PCAN_ERROR_OK) {
                JOptionPane.showMessageDialog(this, "Failed to initialize the CAN interface.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } else {
                JOptionPane.showMessageDialog(this, "Successfully connected to the CAN interface.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            // Initialize the reception thread and start it
            receptionThread = new CANReceptionThread(can, receptionArea, cycleTimeLabel, realTimeParametersPanel, currentGraphPanel,this );
            receptionThread.start(); // Start reception thread first

            // Initialize CANTransmitter and pass the reception thread after it has started
            canTransmitter = new CANTransmitter(can, receptionThread);

            // Enable the export and transmit buttons after connection
            exportButton.setEnabled(true);
            transmitButton.setEnabled(true);
            disconnectButton.setEnabled(true);
            connectButton.setEnabled(false);

        } catch (UnsatisfiedLinkError e) {
            JOptionPane.showMessageDialog(this, "Failed to load PCANBasic DLLs.", "Fatal error", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    private void disconnectFromCAN() {
        if (can != null) {
            if (receptionThread != null) {
                receptionThread.interrupt();  // Stop the reception thread
            }

            can.Uninitialize(TPCANHandle.PCAN_USBBUS1);  // Uninitialize the CAN interface

            exportButton.setEnabled(false);
            transmitButton.setEnabled(false);
            disconnectButton.setEnabled(false);
            connectButton.setEnabled(true);

            JOptionPane.showMessageDialog(this, "Disconnected from the CAN interface.", "Disconnected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private TPCANBaudrate getBaudRateValue(String selectedBaudRate) {
        switch (selectedBaudRate) {
            case "50 Kbps":
                return TPCANBaudrate.PCAN_BAUD_50K;
            case "250 Kbps":
                return TPCANBaudrate.PCAN_BAUD_250K;
            case "500 Kbps":
                return TPCANBaudrate.PCAN_BAUD_500K;
            case "1 Mbps":
                return TPCANBaudrate.PCAN_BAUD_1M;
            default:
                return TPCANBaudrate.PCAN_BAUD_250K;
        }
    }
private void selectFileToImport() {
    int result = fileChooser.showOpenDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
            String line;
            dataToTransmit.clear();
            
            while ((line = reader.readLine()) != null) {
                dataToTransmit.add(line);
            }
            reader.close();

            currentGraphPanel.loadExpectedCurrents(dataToTransmit);
            JOptionPane.showMessageDialog(this, "File imported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            viewButton.setEnabled(true); // Enable the view button

            // === Count and transmit number of cycles ===
            int totalCycles = dataToTransmit.size();
//            int totalCycles = dataToTransmit.size() / 3;
            byte[] cycleMessage = new byte[8];
            cycleMessage[0] = (byte) totalCycles; // First byte = cycle count
            for (int i = 1; i < 8; i++) {
                cycleMessage[i] = 0x00;
            }

            // Transmit the CAN message with ID 0x128
            canTransmitter.sendSingleFrame(0x123, totalCycles);
//System.out.println("Total data is "+ totalCycles);
//System.out.println("Total data is : "+ dataToTransmit.size());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading the file: " + e.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

private void openViewWindow() {
    JFrame viewFrame = new JFrame("Imported Data Viewer");
    viewFrame.setSize(400, 300);
    viewFrame.setLocationRelativeTo(null);
    
    JTextArea viewTextArea = new JTextArea();
    viewTextArea.setEditable(false);
    for (String line : dataToTransmit) {
        viewTextArea.append(line + "\n");
    }
    
    JScrollPane scrollPane = new JScrollPane(viewTextArea);
    viewFrame.add(scrollPane);
    
    viewFrame.setVisible(true);
}
    private void exportData() {
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File exportFile = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile))) {
                writer.write(receptionArea.getText());
                JOptionPane.showMessageDialog(this, "Data exported successfully to " + exportFile.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error exporting data: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

     private void transmitData() {
    canTransmitter.transmitData(dataToTransmit);
    
}
     public void updateAckValues1(int volt_ack,int cur_ack,int pow_ack){
          v1=volt_ack;
          a1=cur_ack;
          p1=pow_ack;
         application.canstart(v1, a1, p1);
     }
   
}
