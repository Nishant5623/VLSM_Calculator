# VLSM Calculator

A modern, fast, and feature-rich Variable Length Subnet Mask (VLSM) Calculator built in Java. This tool allows network administrators and students to easily design and allocate IP addressing plans by optimizing subnet sizes based on host requirements, eliminating IP address waste.

## 🚀 Features

- **Dual Interfaces:** Includes both a lightweight Command Line Interface (CLI) and a fully-featured Graphical User Interface (GUI).
- **Modern Dark UI:** The Swing GUI features a sleek, modern dark-mode aesthetic with interactive hover effects and custom styling.
- **Dynamic Allocation:** Automatically calculates and allocates subnets starting from the largest host requirement to ensure optimal IP distribution without overlaps.
- **Comprehensive Results:** Instantly generates the Network Address, Subnet Mask, Usable IP Range, and Broadcast Address for every required subnet.
- **Export Data:** Built-in capability to export calculation results for reporting and documentation.
- **Standalone Executable:** Can be packaged into a standalone `.exe` bundle that doesn't require users to install Java locally.

## 🛠️ Built With

- **Java (JDK)**
- **Java Swing** (for the graphical interface)
- **jpackage** (for standalone executable generation)

## 💻 How to Run

### Running the GUI version
Compile and run the `VLSMCalculator` from your terminal:
```bash
javac VLSMCalculator.java
java VLSMCalculator
```

### Running the CLI version
If you prefer a fast, text-based terminal experience:
```bash
javac VLSM.java
java VLSM
```
