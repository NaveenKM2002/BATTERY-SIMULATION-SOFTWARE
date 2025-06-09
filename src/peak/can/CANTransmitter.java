package peak.can;

import peak.can.basic.*;
import javax.swing.*;
import java.util.List;

public class CANTransmitter {
    private PCANBasic can;
    private CANReceptionThread receptionThread;
    private List<String> dataToTransmit;
    private boolean transmitting = false;
     private int manualCounter = 0;
     
    private JLabel currentFrameLabel; // Label to display the current frame count

    public CANTransmitter(PCANBasic can, CANReceptionThread receptionThread) {
        this.can = can;
        this.receptionThread = receptionThread;
    }

    public synchronized void transmitData(List<String> dataToTransmit) {
    this.dataToTransmit = dataToTransmit;

    if (transmitting) {
        JOptionPane.showMessageDialog(null, "Already transmitting. Please wait for the current batch to finish.", "Transmission in Progress", JOptionPane.WARNING_MESSAGE);
        return;
    }

    if (can == null) {
        JOptionPane.showMessageDialog(null, "CAN interface is not initialized.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    new Thread(() -> {
        transmitting = true;
        int index = 0;
        final int initialBatchSize = 1000;

        try {
            // Step 1: Transmit initial batch of 1000 rows
            while (index < Math.min(initialBatchSize, dataToTransmit.size())) {
                String line = dataToTransmit.get(index);
                transmitCANMessage(line);
                index++;
                Thread.sleep(50);
            }

            sendBufferFinalMessageWithA4(); // End of first 1000 batch
            System.out.println("Sent batch-end message with 0xA4.");

            // Step 2: Wait for request to send one row at a time
            while (index < dataToTransmit.size()) {
                while (receptionThread != null && !receptionThread.shouldTransmitNextBatch()) {
                    Thread.sleep(50);
                }

                // Send one row only on request
                String line = dataToTransmit.get(index);
                transmitCANMessage(line);
                index++;
                Thread.sleep(50);

                sendBufferFinalMessageWithA4(); // Signal end of this single-frame batch
                if (receptionThread != null) {
                    receptionThread.resetTransmitFlag();
                }
            }

            // Finalize transmission
            Thread.sleep(100);
            sendFinalMessageWithE4();
            System.out.println("Sent final message with 0xA5.");
            JOptionPane.showMessageDialog(null, "All data transmitted successfully.\nTotal frames: " + dataToTransmit.size(), "Transmission Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            transmitting = false;
        }
    }).start();
}


    public boolean isTransmitting() {
        return transmitting;
    }

    private void sendFinalMessageWithE4() {
        byte[] data = new byte[8];
        data[0] = (byte) 0xEB;  // Byte 1
        data[1] = (byte) 0xA5;  // Byte 2 

        for (int i = 2; i < data.length; i++) {
            data[i] = 0;
        }

        // Create the CAN message
        TPCANMsg canMessage = new TPCANMsg();
        canMessage.setID(0x111);  // Set CAN ID (you can adjust as needed)
        canMessage.setLength((byte) 8);  // Set the message length to 8 bytes
        canMessage.setData(data, (byte) 8);  // Set the data for the message

        // Send the CAN message
        TPCANStatus status = can.Write(TPCANHandle.PCAN_USBBUS1, canMessage);
        if (status != TPCANStatus.PCAN_ERROR_OK) {
            JOptionPane.showMessageDialog(null, "Error sending final CAN message.", "Transmit Error", JOptionPane.ERROR_MESSAGE);
        }

        // Debugging: Log the transmitted message
        System.out.println("Final Message Sent: ID: " + canMessage.getID() + " Data: " + java.util.Arrays.toString(data));
    }

    private void sendBufferFinalMessageWithA4() {
        byte[] data = new byte[8];
        data[0] = (byte) 0xEB;  // Byte 1
        data[1] = (byte) 0xA4;  // Byte 2 

        for (int i = 2; i < data.length; i++) {
            data[i] = 0;
        }

        // Create the CAN message
        TPCANMsg canMessage = new TPCANMsg();
        canMessage.setID(0x111);  // Set CAN ID (you can adjust as needed)
        canMessage.setLength((byte) 8);  // Set the message length to 8 bytes
        canMessage.setData(data, (byte) 8);  // Set the data for the message

        // Send the CAN message
        TPCANStatus status = can.Write(TPCANHandle.PCAN_USBBUS1, canMessage);
        if (status != TPCANStatus.PCAN_ERROR_OK) {
            JOptionPane.showMessageDialog(null, "Error sending buffer final CAN message.", "Transmit Error", JOptionPane.ERROR_MESSAGE);
        }

        // Debugging: Log the transmitted message
        System.out.println("Buffer Final Message Sent: ID: " + canMessage.getID() + " Data: " + java.util.Arrays.toString(data));
    }

    
   private void transmitCANMessage(String line) throws InterruptedException {
    String[] decimalStrings = line.split("\\s*,\\s*");

    if (decimalStrings.length >= 3) {
        byte[] data = new byte[8];

//        data[0] = (byte) 0xEB; // Byte 1
//        data[1] = (byte) 0xAA; // Byte 2

        // Byte 3 & 4: Manual counter from 0 to 99 (cycling)
        data[0] = (byte) (manualCounter & 0xFF);
        data[1] = (byte) ((manualCounter >> 8) & 0xFF);

        // Increment the counter and reset to 0 if it reaches 100
        manualCounter = (manualCounter + 1) % 100;

        try {
            // Byte 5 & 6: Use the second value from the imported file
            int secondValue = Integer.parseInt(decimalStrings[1].trim());
            data[2] = (byte) (secondValue & 0xFF);
            data[3] = (byte) ((secondValue >> 8) & 0xFF);

            // Byte 7 & 8: Use the third value from the imported file
            int thirdValue = Integer.parseInt(decimalStrings[2].trim());
            data[4] = (byte) (thirdValue & 0xFF);
            data[5] = (byte) ((thirdValue >> 8) & 0xFF);
        } catch (NumberFormatException e) {
            // If parsing fails, set bytes to 0
            data[4] = 0;
            data[5] = 0;
            data[6] = 0;
            data[7] = 0;
        }

        // Create and send the CAN message
        TPCANMsg canMessage = new TPCANMsg();
        canMessage.setID(0x129); // Set CAN ID (you can adjust as needed)
        canMessage.setLength((byte) 8);
        canMessage.setData(data, (byte) 8);

        TPCANStatus status = can.Write(TPCANHandle.PCAN_USBBUS1, canMessage);
        if (status != TPCANStatus.PCAN_ERROR_OK) {
            JOptionPane.showMessageDialog(null, "Error sending CAN message.", "Transmit Error", JOptionPane.ERROR_MESSAGE);
        }

        // Debugging: Log the transmitted message
        System.out.println("Message Sent: ID: " + canMessage.getID() + " Data: " + java.util.Arrays.toString(data));
    }
}
public void sendSingleFrame(int canId, int data1) {
    byte[] data = new byte[8];
//    data[0] = (byte) data1;
            data[0] = (byte) (data1 & 0xFF);
        data[1] = (byte) ((data1 >> 8) & 0xFF);
    if (data.length > 8) {
        JOptionPane.showMessageDialog(null, "Data length must not exceed 8 bytes.", "Data Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    if (can == null) {
        JOptionPane.showMessageDialog(null, "CAN interface is not initialized.", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    TPCANMsg canMessage = new TPCANMsg();
    canMessage.setID(canId);
    canMessage.setLength((byte) data.length);
    canMessage.setData(data, (byte) data.length);

    TPCANStatus status = can.Write(TPCANHandle.PCAN_USBBUS1, canMessage);
    if (status != TPCANStatus.PCAN_ERROR_OK) {
        JOptionPane.showMessageDialog(null, "Failed to send CAN frame. Error code: " + status, "Transmission Error", JOptionPane.ERROR_MESSAGE);
    } else {
        System.out.println("Single Frame Sent: ID: " + canMessage.getID() + " Data: " + data1);
    }
}

    
    public PCANBasic getCAN() {
        return can;
    }
    
}
