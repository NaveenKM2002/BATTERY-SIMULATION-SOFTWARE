# BATTERY-SIMULATION-SOFTWARE
Java-based Battery Simulation Software with real-time CAN (PCAN) communication. Features include status/fault monitoring, real-time data display, graphical current analysis, control commands, and multiple battery simulation modes (CV, CC, CP, CC-CV).

A **Java-based desktop application** designed for simulating and monitoring battery systems using **CAN (Controller Area Network) communication**, specifically via the **PCAN interface**. This software is ideal for use in battery testing, system simulation, and educational environments.

---

## 🧩 Key Features

- **Real-time CAN Communication (PCAN)**  
  Communicates with external battery systems using the PCAN protocol for sending and receiving CAN frames.

- **Status & Fault Monitoring**  
  Displays system indicators like:
  - Ready to Start
  - Remote Mode
  - Contactor On
  - Error States
  - DCDC Trip
  - DCPS Health
  - Cycle Processing

- **Real-Time Parameters Display**
  Shows live data from the battery system:
  - Voltage (V)
  - Current (A)
  - Power (KW)
  - CC Volts Limit
  - Battery Voltage

- **Graphical Visualization**
  Real-time plot of current (A) vs. time (s) for easy battery behavior analysis.

- **Mode Selection**
  Supports multiple charge/discharge modes:
  - CV (Constant Voltage)
  - CC (Constant Current)
  - CP (Constant Power)
  - CC-CV (Combined Mode)

- **Limit Settings**
  Define thresholds for:
  - Voltage Limit (V)
  - Power Limit (kW)
  - Current Max (A)

- **Battery Connection Monitoring**
  Monitors:
  - Battery Voltage
  - Contactor Acknowledgment
  - Reserved indicators

- **Control Commands**
  Full control of the simulation process:
  - DCPS ON / OFF
  - Cycle Start / Stop
  - Reset
  - Data Clear

- **Data Management**
  - Import configuration or simulation files
  - Export real-time data logs
  - Transmit and view CAN messages

---

## 🛠 Technology Stack

- **Language:** Java  
- **UI Framework:** Java Swing  
- **Communication Protocol:** CAN (via PCAN)  
- **Platform:** Windows  

---

## 🗂 Project Structure

```bash
Battery-Simulation-Software/
├── src/                 # Java source code
├── resources/           # Icons, config files, CAN logs
├── README.md            # This documentation
└── LICENSE              # Project license
