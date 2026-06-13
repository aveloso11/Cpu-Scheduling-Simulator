# 🖥️ CPU Scheduling Simulator

A JavaFX-based CPU scheduling simulator that visualizes and compares classic CPU scheduling algorithms with a real-time Gantt chart, results table, performance metrics, and a Smart Algorithm Advisor.

---

## 📋 Table of Contents

- [About](#about)
- [Features](#features)
- [Algorithms](#algorithms)
- [How to Use](#how-to-use)
- [Smart Advisor](#smart-advisor)
- [Metrics Explained](#metrics-explained)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [How to Run](#how-to-run)

---

## About

CPU Scheduling Simulator lets you add processes, choose a scheduling algorithm, and instantly see a color-coded Gantt chart alongside a detailed results table showing turnaround time, waiting time, and CPU utilization. A built-in Smart Advisor analyzes your workload and recommends the best algorithm to use.

---

## Features

- 🎨 Dark-themed JavaFX GUI (1100×700, resizable)
- ➕ Add/remove processes with PID, Arrival Time, Burst Time, and Priority
- 🎲 Random preset loader (5 processes, 4 scenario types)
- 📊 Color-coded scrollable Gantt chart (canvas-based, per-slice accurate)
- 📋 Results table: Start, Finish, Turnaround, Waiting per process
- 📈 Live metrics: Avg Turnaround, Avg Waiting, CPU Utilization
- 💡 Smart Algorithm Advisor with workload analysis
- 4 scheduling algorithms supported
- ⚙️ Configurable Time Quantum for Round Robin

---

## Algorithms

| Algorithm | Type | Description |
|-----------|------|-------------|
| **FCFS** | Non-Preemptive | Processes run in arrival order; first to arrive is first to execute |
| **SJF** | Non-Preemptive | Picks the process with the shortest burst time among all arrived processes |
| **Priority** | Non-Preemptive | Lower priority number = higher priority (1 is highest); runs selected process to completion |
| **Round Robin** | Preemptive | Time-sharing with a configurable time quantum; processes cycle through the CPU queue until complete |

---

## How to Use

1. **Select an algorithm** from the dropdown on the left panel.
   - For Round Robin, set the **Time Quantum** (default: 4).
2. **Add processes** by filling in the form fields:
   - **PID** — process name (auto-increments)
   - **Arrival** — time the process arrives
   - **Burst** — CPU time required
   - **Priority** — lower number = higher priority (used by Priority Scheduling)
3. Click **ADD PROCESS** to add it to the queue.
4. Optionally click **LOAD (5 Presets)** for a random scenario.
5. Click **💡 SMART ADVICE** to get an algorithm recommendation.
6. Click **▶ SIMULATE** to run the scheduler.
7. View the **Gantt Chart**, **Results Table**, and **Metrics** in the main panel.
8. Click **CLEAR ALL** to reset and start over.

---

## Smart Advisor

The Smart Advisor analyzes your process list and recommends the most suitable algorithm. It detects four workload patterns:

| Detected Pattern | Recommended Algorithm | Trigger Condition |
|------------------|-----------------------|-------------------|
| PRIORITY | Priority Scheduling | Any process has priority 0–4 |
| SJF | Shortest Job First | One long process (>20ms) + 3+ short processes (<10ms) |
| FCFS | First Come First Serve | Staggered arrivals with increasing burst times |
| ROUND ROBIN | Round Robin | All processes have identical burst times |

The advisor dialog also shows:
- Number of processes, Avg/Min/Max Burst Time, Variance
- Whether priorities are set
- Detected workload pattern
- Recommendation reason and warnings
- A **"RUN RECOMMENDED ALGORITHM"** button that auto-selects and runs it

---

## Metrics Explained

| Metric | Formula |
|--------|---------|
| **Turnaround Time** | Finish Time − Arrival Time |
| **Waiting Time** | Turnaround Time − Burst Time |
| **Avg Turnaround** | Sum of all Turnaround Times ÷ Number of Processes |
| **Avg Waiting** | Sum of all Waiting Times ÷ Number of Processes |
| **CPU Utilization** | Total Burst Time ÷ Total Span × 100% |

---

## Project Structure

```
Simulation CPU/
├── backend/
│   ├── FCFS.java                # First Come First Serve algorithm
│   ├── SJF.java                 # Shortest Job First algorithm
│   ├── RoundRobin.java          # Round Robin algorithm (with Gantt slices)
│   ├── PriorityScheduler.java   # Non-preemptive Priority Scheduling
│   ├── MyProcess.java           # Process data model
│   ├── MetricsCalculator.java   # Console metrics printer (utility)
│   └── SmartAdvisor.java        # Workload analyzer and algorithm recommender
├── frontend/
│   └── UI.java                  # JavaFX GUI — all panels, tables, Gantt chart
├── Main.java                    # Entry point (launches JavaFX Application)
└── README.md
```

---

## Requirements

- Java 17 or higher
- JavaFX 17 SDK (OpenJFX)

> The project uses the `javafx.application`, `javafx.scene`, `javafx.beans`, and `javafx.collections` modules.

---

## How to Run

**1. Compile** (make sure JavaFX is on the module path):

```bash
javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml \
  backend/*.java frontend/UI.java Main.java
```

**2. Run:**

```bash
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml Main
```

> Replace `/path/to/javafx-sdk/lib` with your actual OpenJFX SDK path (e.g. `C:/Users/ADMIN/Documents/openjfx-17.0.19_windows-x64_bin-sdk/lib`).

**Or run directly from VS Code / Eclipse** with the JavaFX VM arguments already configured in your launch settings.

> **VS Code users:** Add the JavaFX module path to your `.vscode/launch.json` vmArgs locally. Do not commit the `.vscode` folder as it contains machine-specific paths.

---

## Credits

Developed by **aveloso11** and team. Built with Java 17 + JavaFX.
