package peak.can;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CurrentGraphPanel extends JPanel {
    private final ArrayList<Integer> currentValues = new ArrayList<>();
    private final ArrayList<Long> timestamps = new ArrayList<>();
    private final XYSeries series;
    private final JFreeChart chart;
    private final ChartPanel chartPanel;
    private long nextTimestamp = 0;
    private boolean isCycleStarted = false;

    // Labels to show CycleProcessing and BatterySOC
    private final JLabel cycleProcessingLabel;
    private final JLabel batterySOCLabel;

    // Progress tracking variables
    private ArrayList<Integer> expectedCurrents = new ArrayList<>();
    private int matchedCount = 0;
    private JProgressBar progressBar;
    private JFrame progressFrame;

    public CurrentGraphPanel() {
        setPreferredSize(new Dimension(600, 400));
        setBackground(Color.BLACK);
        setLayout(new BorderLayout());

        // Initialize the XY Series for graph
        series = new XYSeries("Current (A)");
        chart = createChart();
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setMouseZoomable(true);
        chartPanel.setBackground(Color.BLACK);

        // Create a panel for the buttons and labels
        JPanel buttonPanel = new JPanel();
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearGraph();
            }
        });

        JButton progressButton = new JButton("Progress");
        progressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!expectedCurrents.isEmpty()) {
                    showProgressWindow();
                } else {
                    JOptionPane.showMessageDialog(CurrentGraphPanel.this, "Please import a file first!", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        buttonPanel.add(clearButton);
        buttonPanel.add(progressButton);

        // Info panel with labels
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        cycleProcessingLabel = new JLabel("Cycle Processing:");
        batterySOCLabel = new JLabel("Battery SOC:");

        Font digitalFont = new Font("Monospaced", Font.BOLD, 18);
        cycleProcessingLabel.setFont(digitalFont);
        batterySOCLabel.setFont(digitalFont);

        infoPanel.add(cycleProcessingLabel);
        infoPanel.add(Box.createHorizontalStrut(10));
        infoPanel.add(batterySOCLabel);
        infoPanel.add(buttonPanel);

        // Graph panel with chart and info
        JPanel graphPanel = new JPanel(new BorderLayout());
        graphPanel.add(chartPanel, BorderLayout.CENTER);
        graphPanel.add(infoPanel, BorderLayout.SOUTH);

        setLayout(new BorderLayout());
        add(graphPanel, BorderLayout.CENTER);
    }

    public void addCurrentValue(int current) {
        if (isCycleStarted) {
            currentValues.add(current);
            timestamps.add(nextTimestamp);
            series.add(nextTimestamp / 5, current);
            nextTimestamp++;

            checkAndUpdateProgress(current);
            repaint();
        }
    }

    // ✅ Corrected version
public void loadExpectedCurrents(List<String> dataLines) {
        expectedCurrents.clear();
        matchedCount = 0;

        for (String line : dataLines) {
            String[] parts = line.split(","); // adjust delimiter if needed
            if (parts.length >= 2) {
                try {
                    int current = Integer.parseInt(parts[1].trim());
                    expectedCurrents.add(current);
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    private void checkAndUpdateProgress(int receivedCurrent) {
        if (matchedCount >= expectedCurrents.size()) return;

        int expected = expectedCurrents.get(matchedCount);
        int tolerance = 3; // ±3 units

        if (Math.abs(receivedCurrent - expected) <= tolerance) {
            matchedCount++;
            updateProgressBar();
        }
    }

    private void showProgressWindow() {
        progressFrame = new JFrame("Progress");
        progressFrame.setSize(400, 100);
        progressFrame.setLocationRelativeTo(null);

        progressBar = new JProgressBar(0, expectedCurrents.size());
        progressBar.setStringPainted(true);
        progressBar.setValue(matchedCount);
        progressBar.setString(String.format("Progress: %.2f%%", (matchedCount * 100.0 / expectedCurrents.size())));

        progressFrame.add(progressBar);
        progressFrame.setVisible(true);
    }

    private void updateProgressBar() {
        if (progressBar != null) {
            progressBar.setValue(matchedCount);
            progressBar.setString(String.format("Progress: %.2f%%", (matchedCount * 100.0 / expectedCurrents.size())));
        }
    }

    private JFreeChart createChart() {
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Current Graph",
                "Time (s)",
                "Current (A)",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.getRenderer().setSeriesPaint(0, Color.BLUE);

        return chart;
    }

    public void showFullGraph() {
        CurrentGraphPanel fullGraphPanel = new CurrentGraphPanel();
        for (int i = 0; i < currentValues.size(); i++) {
            fullGraphPanel.addCurrentValue(currentValues.get(i));
        }

        JFrame fullGraphFrame = new JFrame("Full Graph View");
        fullGraphFrame.setSize(800, 400);
        fullGraphFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        fullGraphFrame.setLocationRelativeTo(null);
        fullGraphFrame.setBackground(Color.BLACK);
        fullGraphFrame.add(fullGraphPanel);
        fullGraphFrame.setVisible(true);
    }

    public void clearGraph() {
        currentValues.clear();
        timestamps.clear();
        series.clear();
        nextTimestamp = 0;
        matchedCount = 0;
        if (progressBar != null) progressBar.setValue(0);
        repaint();
    }

    public void startCycle() {
        isCycleStarted = true;
    }

    public void stopCycle() {
        isCycleStarted = false;
    }

    public void updateCycleProcessing(int cycleProcessing) {
        DecimalFormat decimalFormat = new DecimalFormat("###,###");
        int displayValue = Math.min(cycleProcessing, 100);
        cycleProcessingLabel.setText("Cycle Processing:" + decimalFormat.format(displayValue) + "%");
    }

    public void updateBatterySOC(int batterySOC) {
        DecimalFormat decimalFormat = new DecimalFormat("###,###");
        batterySOCLabel.setText("Battery SOC:" + decimalFormat.format(batterySOC) + "%");
    }
}
