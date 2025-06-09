package peak.can;

import javax.swing.*;
import java.awt.*;
import peak.can.basic.*;

public class Application {
    private  CANTransmitter canTransmitter ;
    
        private boolean isCycleStarted = false; 
//private RealTimeParametersPanel realTimeParametersPanel;
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Start the GUI
                new Application();
            }
        });
    }
private JLabel currentFrameLabel;
private ModeSelectionPanel modeSelectionPanel;
    public Application() {
        PCANBasic can = new PCANBasic();

        // Create the required components for CANReceptionThread
        JTextArea textArea = new JTextArea();
        JLabel statusLabel = new JLabel("Status: Idle");
        CANInterfaceGUI canInterfacePanel = new CANInterfaceGUI(this);
        RealTimeParametersPanel realTimeParametersPanel = new RealTimeParametersPanel();   
         CurrentGraphPanel currentGraphPanel = new CurrentGraphPanel();
         StatusLivePanel statusLivePanel = new StatusLivePanel();
        // Initialize CANReceptionThread with required arguments
         StatusPanel statusPanel = new StatusPanel(canTransmitter);
        CANReceptionThread receptionThread = new CANReceptionThread(can, textArea, statusLabel, realTimeParametersPanel, currentGraphPanel,canInterfacePanel);
        CANTransmitter canTransmitter = new CANTransmitter(can, receptionThread);
////
////        // Create CANTransmitter

        // Create the main frame
        JFrame frame = new JFrame("CAN Interface Application");
        frame.setSize(1400, 730);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout()); // Use BorderLayout or another layout for the JFrame

        // Create the main panel (JPanel that will hold all components)
        JPanel mainPanel = new JPanel();

        // --- Row 1: Top panel ---
        JPanel topPanel = new TopPanel();
        topPanel.setPreferredSize(new Dimension(1300, 50)); // Set the preferred size for the top panel
        mainPanel.add(topPanel);

        // --- Row 2: Three columns ---
        JPanel row2Panel = new JPanel();

        // CAN Interface panel
        
        canInterfacePanel.setPreferredSize(new Dimension(1300, 350)); // Set the specific size for CANInterfaceGUI
        row2Panel.add(canInterfacePanel);

        // Current Graph panel
//        CurrentGraphPanel currentGraphPanel = new CurrentGraphPanel();
//        currentGraphPanel.setPreferredSize(new Dimension(550, 350)); // Set the specific size for CurrentGraphPanel
//        row2Panel.add(currentGraphPanel);

        // Status panel
//        StatusPanel statusPanel = new StatusPanel(canTransmitter);
//        statusPanel.setPreferredSize(new Dimension(350, 350)); // Set the specific size for StatusPanel
//        row2Panel.add(statusPanel);

        mainPanel.add(row2Panel);

//         --- Row 3: Three columns ---
        JPanel row3Panel = new JPanel();
         modeSelectionPanel = new ModeSelectionPanel(canTransmitter);
        modeSelectionPanel.setPreferredSize(new Dimension(330, 255));
        row3Panel.add(modeSelectionPanel);
        // Protection Setting panel
        ProtectionSettingPanel protectionSettingPanel = new ProtectionSettingPanel(canTransmitter);
        protectionSettingPanel.setPreferredSize(new Dimension(330, 255)); // Set the specific size for ProtectionSettingPanel
        row3Panel.add(protectionSettingPanel);
//        
        BMSPanel bmsPanel = new BMSPanel(canTransmitter);
        bmsPanel.setPreferredSize(new Dimension(330, 255)); // Set the specific size for BMSPanel
        row3Panel.add(bmsPanel);
        // Real Time Parameters panel
//        realTimeParametersPanel = new RealTimeParametersPanel();
//        realTimeParametersPanel.setPreferredSize(new Dimension(330, 255)); // Set the specific size for RealTimeParametersPanel
//        row3Panel.add(realTimeParametersPanel);
//        StatusLivePanel importdata = new StatusLivePanel();
//        importdata.setPreferredSize(new Dimension(330, 255)); // Set the specific size for BMSPanel
//        row3Panel.add(importdata);
        
        statusPanel = new StatusPanel(canTransmitter);
        statusPanel.setPreferredSize(new Dimension(330, 255)); // Set the specific size for StatusPanel
        row3Panel.add(statusPanel);
        // BMS panel

        
        


        mainPanel.add(row3Panel);

        // Set the mainPanel as the content pane of the frame
        frame.setContentPane(mainPanel);

        // Set the frame to be visible
        frame.setVisible(true);
        
        
    }
    public void canstart(int voltage, int current, int power){
//    CANReceptionThread receptionThread = new CANReceptionThread(can, textArea, statusLabel, realTimeParametersPanel, currentGraphPanel, canInterfacePanel);
         int v1=voltage;
         int a1=current;
         int p1=power;
modeSelectionPanel.updateAckValues(v1, a1, p1);
}
   
}
