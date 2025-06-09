package peak.can;

import peak.can.basic.*;
import javax.swing.*;

public class CANReceptionThread extends Thread {
    private final PCANBasic can;
    private final JTextArea receptionArea;
    private final JLabel cycleTimeLabel;
    private final RealTimeParametersPanel realTimeParametersPanel; // Reference to the RealTimeParametersPanel
    private final CurrentGraphPanel currentGraphPanel; 
//    private final StatusLivePanel statusLivePanel;
    private final CANInterfaceGUI canInterfaceGUI;
//    private final ModeSelectionPanel modeSelectionPanel;
    private long lastReceivedTimestamp = System.currentTimeMillis();
    private boolean shouldTransmitNextBatch = false; // Flag to trigger transmission
    private int voltage = 0, current = 0, power = 0;
//    private final StatusPanel statusPanel; 

       public CANReceptionThread(PCANBasic can, JTextArea receptionArea, JLabel cycleTimeLabel, RealTimeParametersPanel realTimeParametersPanel,CurrentGraphPanel currentGraphPanel, CANInterfaceGUI canInterfaceGUI) {
        this.can = can;
        this.receptionArea = receptionArea;
        this.cycleTimeLabel = cycleTimeLabel;
        this.realTimeParametersPanel = realTimeParametersPanel;  // Initialize the reference
        this.currentGraphPanel = currentGraphPanel;
        this.canInterfaceGUI = canInterfaceGUI;
//        this.statusPanel = statusPanel; // Initialize the StatusPanel
    }

    public boolean shouldTransmitNextBatch() {
        return shouldTransmitNextBatch;
    }

    public void resetTransmitFlag() {
        this.shouldTransmitNextBatch = false;
    }

    @Override
    public void run() {
        while (true) {
            TPCANTimestamp timestamp = new TPCANTimestamp();
            TPCANMsg canMessage = new TPCANMsg();
            TPCANStatus status = can.Read(TPCANHandle.PCAN_USBBUS1, canMessage, timestamp);

            if (status == TPCANStatus.PCAN_ERROR_OK) {
                // Check if the message ID is 0x211
                if (canMessage.getID() == 0x211) {
                    byte[] data = canMessage.getData();

                    // Case 1: If Byte 1 == 0xF0, extract voltage and current
                    if (data.length >= 8 && data[0] == (byte) 0xF0) {
                        voltage = ((data[3] & 0xFF) << 8) | (data[2] & 0xFF); // Combine Byte 3 and Byte 4
//                        current = ((data[5] & 0xFF) << 8) | (data[4] & 0xFF); // Combine Byte 5 and Byte 6
//                        int status_Faults = ((data[7] & 0xFF) << 8) | (data[6] & 0xFF);
                        int rawCurrent = ((data[5] & 0xFF) << 8) | (data[4] & 0xFF); // Combine Byte 5 and Byte 6
                        if ((rawCurrent & 0x8000) != 0) { // Check if the MSB is set (negative value)
                            current = rawCurrent - 0x10000; // Convert to negative value using two's complement
                        } else {
                            current = rawCurrent; // Positive value
                        }

                        power = (voltage * current)/1000; // Calculate power

                        // Update RealTimeParametersPanel with the new data
                        SwingUtilities.invokeLater(() -> {
                            if (realTimeParametersPanel != null) {
                                realTimeParametersPanel.updateParameters(voltage, current, power);
                            }
                        });
                        currentGraphPanel.addCurrentValue(current);
//                          int statusFaults = 1; // Byte 7 & 8
//                          int statusFaults = 307; // Byte 7 & 8
                          int statusFaults = ((data[7] & 0xFF) << 8) | (data[6] & 0xFF); // Byte 7 & 8
//                        statusLivePanel.updateStatusFromBits(status_Faults);
                        canInterfaceGUI.updateStatusFromBits(statusFaults);
                        
                        if ((statusFaults & 0x100) != 0) { // 0x100 is 1 << 8 (the 9th bit)
                            System.out.println("9th bit is set, starting cycle...");
                            currentGraphPanel.startCycle(); // Start the graph cycle
                        } else {
//                            System.out.println("9th bit is not set, stopping cycle...");
                            currentGraphPanel.stopCycle(); // Stop adding new data to the graph
                        }

                        
//                    SwingUtilities.invokeLater(() -> statusLivePanel.updateBatteryStatusIndicators(255));

                    // Log the status faults for debugging
//                    SwingUtilities.invokeLater(() -> receptionArea.append(
//                        String.format("Received Status Faults: 0x%04X\n", statusFaults)
//                    ));
                        // Log the data in the reception area
                        SwingUtilities.invokeLater(() -> receptionArea.append(
                                String.format("Received Voltage: %d V, Current: %d A, Power: %d W\n", voltage, current, power)
                        ));
                    }
                      if (data.length > 1 && data[1] == (byte) 0xD1) {
                        SwingUtilities.invokeLater(() -> {
                            receptionArea.append("Received ID: 0x211 with second byte 0xD1, ready to transmit next batch\n");
                            // Set flag to transmit the next batch
                            shouldTransmitNextBatch = true;

                            // Calculate and display cycle time
                            long currentTimestamp = System.currentTimeMillis();
                            long cycleTime = currentTimestamp - lastReceivedTimestamp;
                            cycleTimeLabel.setText("Cycle Time: " + cycleTime + " ms");
                            lastReceivedTimestamp = currentTimestamp;
                        });
                    }
//                      statusLivePanel.updateDCPSOnStatus(true);
//                      canInterfaceGUI.updateStatusFromBits(255);
                    // Case 2: If Byte 2 == 0xD1, handle batch transmission
                     if (data.length >= 8 && data[0] == (byte) 0xF1) {
                         int batteryVolt = ((data[3] & 0xFF) << 8) | (data[2] & 0xFF); // Combine Byte 3 and Byte 4
                       int CCVolts = ((data[5] & 0xFF) << 8) | (data[4] & 0xFF);
                       
                        SwingUtilities.invokeLater(() -> {
                            if (realTimeParametersPanel != null) {
//                                realTimeParametersPanel.updateVoltages(100, 200 );
                                realTimeParametersPanel.updateVoltages(batteryVolt, CCVolts );
                            }
                         });

                         if (data[6] == (byte) 0xA1) { // Ready To Start is active (0xA1)
                            canInterfaceGUI.updateReadyToStartStatus(true); // Set to green
//                            statusPanel.enableStartButton(true); // Enable Start button
                        } else {
                            canInterfaceGUI.updateReadyToStartStatus(false); // Set to red
//                            statusPanel.enableStartButton(false); // Disable Start button
                        }

                        if (data[7] == (byte) 0xA2) { // DCPS ON is active (0xA1)
                            canInterfaceGUI.updateDCPSOnStatus(true); // Set to green
                        } else {
                            canInterfaceGUI.updateDCPSOnStatus(false); // Set to red
                        }
                         
                        SwingUtilities.invokeLater(() -> receptionArea.append(
                            String.format("Received Battery Voltage: %d V, CCVoltage: %d V W\n", batteryVolt,CCVolts)
                            // Set flag to transmit the next batch
                        ));
                    }
                     if (data.length >= 8 && data[0] == (byte) 0xF2) {
                         int batterySOC = ((data[3] & 0xFF) << 8) | (data[2] & 0xFF); // Combine Byte 3 and Byte 4
                       int CycleProcessing = ((data[5] & 0xFF) << 8) | (data[4] & 0xFF);
                         SwingUtilities.invokeLater(() -> {
                             currentGraphPanel.updateCycleProcessing(CycleProcessing);
                             currentGraphPanel.updateBatterySOC(batterySOC);
                         });
                       
                     }
                     
//                       if (data.length > 1 && data[1] == (byte) 0xD1) {
//                        SwingUtilities.invokeLater(() -> {
//                            receptionArea.append("Received ID: 0x211 with second byte 0xD1, ready to transmit next batch\n");
//                            // Set flag to transmit the next batch
//                            shouldTransmitNextBatch = true;
//
//                            // Calculate and display cycle time
//                            long currentTimestamp = System.currentTimeMillis();
//                            long cycleTime = currentTimestamp - lastReceivedTimestamp;
//                            cycleTimeLabel.setText("Cycle Time: " + cycleTime + " ms");
//                            lastReceivedTimestamp = currentTimestamp;
//                        });
//                    }
                     
//                     if (data.length >= 2 && data[1] == (byte) 0xD5) {
//                System.out.println("Received 0xD5 in Byte 2.");
//
//                // Call CANTransmitter to handle sending 0xAA and 0xA4
//                SwingUtilities.invokeLater(() -> {
//                    CANTransmitter transmitter = new CANTransmitter(can, this);
//                    transmitter.sendSequenceAfterD5();
//                });
//            }
                    // Default: Log the received data if neither condition is met
                    else {
                        SwingUtilities.invokeLater(() -> {
                            receptionArea.append("Received ID: 0x211, Data: " + byteArrayToHex(data) + "\n");
                        });
                    }
                }
                if (canMessage.getID() == 0x212) {
                    byte[] data = canMessage.getData();
                    if (data.length >= 8){
                   int VOLT_ACK = ((data[1] & 0xFF) << 8) | (data[0] & 0xFF);
                   int CUR_ACK = ((data[3] & 0xFF) << 8) | (data[2] & 0xFF);
                   int POW_ACK = ((data[5] & 0xFF) << 8) | (data[4] & 0xFF);
                   canInterfaceGUI.updateAckValues1(VOLT_ACK,CUR_ACK,POW_ACK);
                        System.out.println("data recievd is" + VOLT_ACK);
                    }
                }
//                canInterfaceGUI.updateAckValues1(1,22,33);

            }

            // Sleep before checking for the next message
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    // Utility method to convert byte array to hexadecimal string for display
    private String byteArrayToHex(byte[] data) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : data) {
            hexString.append(String.format("%02X ", b));
        }
        return hexString.toString();
    }
}
