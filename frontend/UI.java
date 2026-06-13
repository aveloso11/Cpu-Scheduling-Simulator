package frontend;
import backend.*;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.canvas.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;
import java.util.*;

public class UI extends Application {

    // ═══════════════════════════════════════════════════════════════════════════
    //  FIELDS
    // ═══════════════════════════════════════════════════════════════════════════

    // ── Data ─────────────────────────────────────────────────────────────────
    private final ObservableList<ProcessRow> processList = FXCollections.observableArrayList();
    private int pidCounter = 1;

    // ── Left Panel Controls ──────────────────────────────────────────────────
    private ComboBox<String> algorithmCombo;
    private Spinner<Integer> quantumSpinner;
    private Label quantumLabel;
    private Label statusLabel;

    // ── Main Panel Controls ──────────────────────────────────────────────────
    private TableView<ProcessRow> inputTable;
    private TableView<ResultRow> resultTable;
    private Canvas ganttCanvas;
    private ScrollPane ganttScrollPane;
    private List<MyProcess> lastScheduled;
    private Label avgTTLabel, avgWTLabel, cpuUtilLabel;

    // ── Gantt Chart Colors ───────────────────────────────────────────────────
    private static final Color[] PROCESS_COLORS = {
        Color.web("#4F86C6"), Color.web("#E07B54"), Color.web("#5BAD8F"),
        Color.web("#9B72CF"), Color.web("#D4855A"), Color.web("#5B9FBF"),
        Color.web("#C96E82"), Color.web("#7AAF5B")
    };


    // ═══════════════════════════════════════════════════════════════════════════
    //  APPLICATION ENTRY POINT
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void start(Stage stage) {
        stage.setTitle("CPU Scheduling Simulator");
        stage.setMinWidth(900);
        stage.setMinHeight(660);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #152055;");
        root.setLeft(buildControlPanel());
        root.setCenter(buildMainPanel());

        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.show();
    }


    // ═══════════════════════════════════════════════════════════════════════════
    //  LEFT PANEL — Control Panel
    // ═══════════════════════════════════════════════════════════════════════════

    /** Builds the entire left sidebar panel. */
    private VBox buildControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(20, 16, 20, 16));
        panel.setPrefWidth(230);
        panel.setStyle("-fx-background-color: #040D43; -fx-border-color: transparent; -fx-border-width: 0 1 0 0;");

        Label title = new Label("CPU Scheduling\nSimulator");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #FFD862;"
                + "-fx-font-family: 'Segoe UI'; -fx-line-spacing: 2;");

        panel.getChildren().add(title);
        panel.getChildren().add(separator());
        panel.getChildren().addAll(buildAlgorithmSection());
        panel.getChildren().add(separator());
        panel.getChildren().addAll(buildAddProcessSection());
        panel.getChildren().add(separator());
        panel.getChildren().addAll(buildPresetsSection());
        panel.getChildren().add(separator());
        panel.getChildren().add(buildAdvisorButton());
        panel.getChildren().add(separator());
        panel.getChildren().add(buildRunButton());

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #FFD862; -fx-font-family: 'Segoe UI';");
        statusLabel.setWrapText(true);
        panel.getChildren().add(statusLabel);

        return panel;
    }

    /** Algorithm selector and quantum spinner section. */
    private List<javafx.scene.Node> buildAlgorithmSection() {
        List<javafx.scene.Node> nodes = new ArrayList<>();
        nodes.add(sectionLabel("Algorithm"));

        algorithmCombo = new ComboBox<>();
        algorithmCombo.getItems().addAll("FCFS", "SJF", "Priority", "Round Robin");
        algorithmCombo.setValue("FCFS");
        algorithmCombo.setMaxWidth(Double.MAX_VALUE);
        styleCombo(algorithmCombo);
        algorithmCombo.setOnAction(e -> updateQuantumVisibility());

        quantumLabel = new Label("Time Quantum");
        quantumLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888580; -fx-font-family: 'Segoe UI';");

        quantumSpinner = new Spinner<>(1, 100, 4);
        quantumSpinner.setMaxWidth(Double.MAX_VALUE);
        quantumSpinner.setEditable(true);
        styleSpinner(quantumSpinner);

        updateQuantumVisibility();
        nodes.addAll(List.of(algorithmCombo, quantumLabel, quantumSpinner));
        return nodes;
    }

    /** PID/Arrival/Burst/Priority form and Add/Clear buttons. */
    private List<javafx.scene.Node> buildAddProcessSection() {
        List<javafx.scene.Node> nodes = new ArrayList<>();
        nodes.add(sectionLabel("ADD PROCESS"));

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);

        TextField pidField      = styledField("P" + pidCounter, 80);
        TextField arrivalField  = styledField("0", 60);
        TextField burstField    = styledField("5", 60);
        TextField priorityField = styledField("5", 60);

        form.add(fieldLabel("PID"),      0, 0); form.add(pidField,      1, 0);
        form.add(fieldLabel("Arrival"),  0, 1); form.add(arrivalField,  1, 1);
        form.add(fieldLabel("Burst"),    0, 2); form.add(burstField,    1, 2);
        form.add(fieldLabel("Priority"), 0, 3); form.add(priorityField, 1, 3);

        Button addBtn = actionButton("ADD PROCESS");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> {
            try {
                String pid  = pidField.getText().trim();
                int arrival = Integer.parseInt(arrivalField.getText().trim());
                int burst   = Integer.parseInt(burstField.getText().trim());
                int prio    = Integer.parseInt(priorityField.getText().trim());
                if (pid.isEmpty() || burst <= 0) throw new NumberFormatException();
                processList.add(new ProcessRow(pid, arrival, burst, prio));
                pidCounter++;
                pidField.setText("P" + pidCounter);
                arrivalField.setText("0");
                burstField.setText("5");
                priorityField.setText("5");
                setStatus("Process " + pid + " added.");
            } catch (NumberFormatException ex) {
                setStatus("Invalid input. Check fields.");
            }
        });

        Button clearBtn = secondaryButton("CLEAR ALL");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> {
            processList.clear();
            clearResults();
            pidCounter = 1;
            setStatus("Cleared.");
        });

        nodes.addAll(List.of(form, addBtn, clearBtn));
        return nodes;
    }

    /** Preset loader section. */
    private List<javafx.scene.Node> buildPresetsSection() {
        List<javafx.scene.Node> nodes = new ArrayList<>();
        nodes.add(sectionLabel("Presets"));

        Button preset1 = actionButton("LOAD (5 Presets)");
        preset1.setMaxWidth(Double.MAX_VALUE);
        preset1.setOnAction(e -> loadPreset());

        nodes.add(preset1);
        return nodes;
    }

    /** Smart Advisor button. */
    private Button buildAdvisorButton() {
        Button advisorBtn = advisorButton("💡 SMART ADVICE");
        advisorBtn.setMaxWidth(Double.MAX_VALUE);
        advisorBtn.setOnAction(e -> showSmartAdvice());
        return advisorBtn;
    }

    /** Run Simulation button. */
    private Button buildRunButton() {
        Button runBtn = runButton("▶  SIMULATE");
        runBtn.setMaxWidth(Double.MAX_VALUE);
        runBtn.setOnAction(e -> runSimulation());
        return runBtn;
    }


    // ═══════════════════════════════════════════════════════════════════════════
    //  LEFT PANEL — Algorithm & Quantum Logic
    // ═══════════════════════════════════════════════════════════════════════════

    private void updateQuantumVisibility() {
        boolean rr = "Round Robin".equals(algorithmCombo.getValue());
        quantumLabel.setVisible(rr);
        quantumLabel.setManaged(rr);
        quantumSpinner.setVisible(rr);
        quantumSpinner.setManaged(rr);
    }


    // ═══════════════════════════════════════════════════════════════════════════
    //  LEFT PANEL — Preset Loader
    // ═══════════════════════════════════════════════════════════════════════════

    private void loadPreset() {
        processList.clear();
        Random random = new Random();
        int numberOfProcesses = 5;
        int scenario = random.nextInt(4);
        String advisorTarget = "";

        for (int i = 1; i <= numberOfProcesses; i++) {
            String pid = "P" + i;
            int arrivalTime = 0, burstTime = 1, priority = 5;

            switch (scenario) {
                case 0 -> { // Favors FCFS — staggered arrivals, increasing burst
                    advisorTarget = "FCFS";
                    arrivalTime = (i - 1) * 2;
                    burstTime = i * 3;
                }
                case 1 -> { // Favors SJF — one long process, rest short with varied arrivals
                    advisorTarget = "SJF";
                    arrivalTime = (i == 1) ? 0 : i;
                    burstTime   = (i == 1) ? 30 : (2 + random.nextInt(4));
                }
                case 2 -> { // Favors Priority — assign priority FIRST, then use it for burst
                    advisorTarget = "Priority";
                    priority    = i;
                    arrivalTime = (i - 1);
                    burstTime   = (priority == 1) ? 2 : (5 + random.nextInt(10));
                }
                case 3 -> { // Favors Round Robin — all same burst, all arrive at 0
                    advisorTarget = "Round Robin";
                    arrivalTime = 0;
                    burstTime = 8;
                }
            }
            processList.add(new ProcessRow(pid, arrivalTime, burstTime, priority));
        }

        pidCounter = numberOfProcesses + 1;
        setStatus("Random preset loaded (5 processes). Bias: " + advisorTarget);
    }


    // ═══════════════════════════════════════════════════════════════════════════
    //  LEFT PANEL — Smart Advisor
    // ═══════════════════════════════════════════════════════════════════════════

    private void showSmartAdvice() {
        if (processList.isEmpty()) {
            setStatus("⚠️ Add at least one process first to get advice.");
            return;
        }

        List<MyProcess> processes = new ArrayList<>();
        for (ProcessRow row : processList)
            processes.add(new MyProcess(row.getPid(), row.getArrivalTime(), row.getBurstTime(), row.getPriority()));

        String advice = SmartAdvisor.recommend(processes);
        showAdviceDialog(advice);
    }

    private void showAdviceDialog(String advice) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Smart Algorithm Advisor");
        dialogStage.setResizable(false);

        Map<String, String> stats   = parseSection(advice, "WORKLOAD ANALYSIS");
        Map<String, String> recMap  = parseSection(advice, "RECOMMENDATION");
        List<String>        tips    = parseTips(advice);
        String              warning = parseWarning(advice);

        String algoName   = recMap.getOrDefault("Algorithm", "—");
        String reason     = recMap.getOrDefault("Reason",    "—");
        String processes  = stats.getOrDefault("Number of processes", "—");
        String avgBurst   = stats.getOrDefault("Avg Burst Time",      "—");
        String burstRange = stats.getOrDefault("Burst Time Range",    "—");
        String variance   = stats.getOrDefault("Burst Time Variance", "—");
        String hasPrio    = stats.getOrDefault("Has Priorities",      "—");
        String pattern    = stats.getOrDefault("Detected Pattern",    "—");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #152055;");
        root.setTop(buildAdviceHeader(algoName));
        root.setCenter(buildAdviceBody(algoName, reason, processes, avgBurst, burstRange, variance, hasPrio, pattern, warning, tips));
        root.setBottom(buildAdviceFooter(dialogStage, algoName));

        Scene scene = new Scene(root, 660, 560);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    private HBox buildAdviceHeader(String algoName) {
        HBox header = new HBox(10);
        header.setPadding(new Insets(14, 20, 14, 20));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3; -fx-border-width: 0 0 1 0;");

        Label headerTitle = new Label("SMART ALGORITHM ADVISOR");
        headerTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #FFD862;"
                + "-fx-font-family: 'Segoe UI'; -fx-letter-spacing: 1;");

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Label headerMeta = new Label(algorithmCombo.getValue() + "  ·  " + processList.size() + " processes");
        headerMeta.setStyle("-fx-font-size: 11px; -fx-text-fill: #E0E6ED; -fx-font-family: 'Segoe UI';");

        header.getChildren().addAll(headerTitle, hSpacer, headerMeta);
        return header;
    }

    private ScrollPane buildAdviceBody(String algoName, String reason, String processes, String avgBurst,
            String burstRange, String variance, String hasPrio, String pattern,
            String warning, List<String> tips) {
        VBox body = new VBox(16);
        body.setPadding(new Insets(20, 20, 20, 20));
        body.setStyle("-fx-background-color: #152055;"); 
        // Workload analysis
        body.getChildren().add(sectionDivider("Workload Analysis"));
        HBox statRow = new HBox(8);
        statRow.getChildren().addAll(
            statPill(processes,  "Processes"),
            statPill(avgBurst,   "Avg Burst"),
            statPill(burstRange, "Burst Range"),
            statPill(variance,   "Variance"),
            statPill(hasPrio.contains("NO") ? "No" : "Yes", "Priorities")
        );
        HBox patternRow = new HBox(8);
        patternRow.setAlignment(Pos.CENTER_LEFT);
        Label patLbl = new Label("Detected pattern");
        patLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #FFD862; -fx-font-family: 'Segoe UI';");
        Label patBadge = new Label(pattern);
        patBadge.setStyle("-fx-font-size: 11px; -fx-text-fill: #1DCA59; -fx-background-color: #040D43;"
                + "-fx-border-color: #7F8EE3; -fx-border-radius: 20; -fx-background-radius: 20;"
                + "-fx-padding: 2 10; -fx-font-family: 'Segoe UI'; -fx-font-weight: bold;");
        patternRow.getChildren().addAll(patLbl, patBadge);
        body.getChildren().addAll(statRow, patternRow);

        // Recommendation
        body.getChildren().add(sectionDivider("Recommendation"));
        VBox recCard = new VBox(6);
        recCard.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3;"
                + "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 16 18;");
        HBox recTop = new HBox(10);
        recTop.setAlignment(Pos.CENTER_LEFT);
        Label algoLabel = new Label(algoName.contains("(") ? algoName.replaceAll("\\s*\\(.*\\)", "").trim() : algoName);
        algoLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1DCA59; -fx-font-family: 'Segoe UI';");
        Label algoFull = new Label(algoName.contains("(") ? algoName.replaceAll(".*\\((.*)\\).*", "$1") : "");
        algoFull.setStyle("-fx-font-size: 13px; -fx-text-fill: #E0E6ED; -fx-font-family: 'Segoe UI';");
        Label recBadge = new Label("Recommended");
        recBadge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #152055;"
                + "-fx-background-color: #FFBA09; -fx-background-radius: 5; -fx-border-radius: 5;"
                + "-fx-padding: 3 9; -fx-font-family: 'Segoe UI';");
        recTop.getChildren().addAll(algoLabel, algoFull, recBadge);
        Label recReason = new Label(reason);
        recReason.setStyle("-fx-font-size: 12px; -fx-text-fill: #E0E6ED; -fx-font-family: 'Segoe UI';");
        recReason.setWrapText(true);
        recCard.getChildren().addAll(recTop, recReason);
        body.getChildren().add(recCard);

        // Status / Warning
        body.getChildren().add(sectionDivider("Status"));
        if (warning != null && !warning.isBlank()) {
            Label warnCard = new Label("  " + warning);
            warnCard.setStyle("-fx-font-size: 12px; -fx-text-fill: #E0E6ED; -fx-font-family: 'Segoe UI';"
                    + "-fx-background-color: #ff0000; -fx-border-color: #E07B54;"
                    + "-fx-border-width: 0 0 0 3; -fx-border-radius: 0; -fx-background-radius: 0 8 8 0; -fx-padding: 10 14;");
            warnCard.setWrapText(true);
            body.getChildren().add(warnCard);
        } else {
            Label okCard = new Label("  \u2713  No major concerns for this workload.");
            okCard.setStyle("-fx-font-size: 12px; -fx-text-fill: #E0E6ED; -fx-font-family: 'Segoe UI';"
                    + "-fx-background-color: #040D43; -fx-border-color: #1DCA59;"
                    + "-fx-border-width: 0 0 0 3; -fx-border-radius: 0; -fx-background-radius: 0 8 8 0; -fx-padding: 10 14;");
            body.getChildren().add(okCard);
        }

        // Tips
        if (!tips.isEmpty()) {
            body.getChildren().add(sectionDivider("Additional Tips"));
            VBox tipList = new VBox(8);
            for (String tip : tips) {
                HBox tipRow = new HBox(10);
                tipRow.setAlignment(Pos.TOP_LEFT);
                tipRow.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3 ;"
                        + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 14;");
                Label dot = new Label("•");
                dot.setStyle("-fx-text-fill: #E0E6ED; -fx-font-size: 14px;");
                Label tipLbl = new Label(tip);
                tipLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #E0E6ED; -fx-font-family: 'Segoe UI';");
                tipLbl.setWrapText(true);
                HBox.setHgrow(tipLbl, Priority.ALWAYS);
                tipRow.getChildren().addAll(dot, tipLbl);
                tipList.getChildren().add(tipRow);
            }
            body.getChildren().add(tipList);
        }

        ScrollPane scrollPane = new ScrollPane(body);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #152055; -fx-background-color: #152055; -fx-border-color: transparent;");
        scrollPane.setPrefHeight(440);
        styleScrollBars(scrollPane);
        return scrollPane;
    }

    private HBox buildAdviceFooter(Stage dialogStage, String algoName) {
        HBox footer = new HBox(10);
        footer.setPadding(new Insets(12, 20, 14, 20));
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3; -fx-border-width: 1 0 0 0;");

        Button closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px;"
                + "-fx-background-color: #e74c3c; -fx-text-fill: #E0E6ED;"
                + "-fx-border-color: transparent; -fx-border-radius: 6; -fx-background-radius: 6;"
                + "-fx-padding: 9 20; -fx-cursor: hand; -fx-font-weight: bold;");
        closeBtn.setOnAction(e -> dialogStage.close());

        Button runBtn = new Button("RUN RECOMMENDED ALGORITHM");
        runBtn.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: bold;"
                + "-fx-background-color: #FFBA09; -fx-text-fill: #152055;"
                + "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 9 20; -fx-cursor: hand;");
        runBtn.setOnAction(e -> {
            String match = algorithmCombo.getItems().stream()
                .filter(item -> algoName.toLowerCase().contains(item.toLowerCase())
                             || item.toLowerCase().contains(algoName.toLowerCase()))
                .findFirst()
                .orElse(null);
            if (match != null) {
                algorithmCombo.setValue(match);
                updateQuantumVisibility();
            }
            dialogStage.close();
            runSimulation();
        });

        footer.getChildren().addAll(closeBtn, runBtn);
        return footer;
    }


    // ═══════════════════════════════════════════════════════════════════════════
    //  LEFT PANEL — Advice Dialog Parsing Helpers
    // ═══════════════════════════════════════════════════════════════════════════

    private Map<String, String> parseSection(String text, String sectionName) {
        Map<String, String> map = new LinkedHashMap<>();
        String[] lines = text.split("\n");
        boolean inSection = false;
        for (String line : lines) {
            if (line.contains(sectionName)) { inSection = true; continue; }
            if (inSection && line.trim().startsWith("=") || (inSection && line.trim().isEmpty() && map.size() > 2)) break;
            if (inSection && line.contains(":")) {
                String clean = line.replaceAll("[|\\[\\]╔╗╚╝═─┌┐└┘│+]", "").trim();
                int idx = clean.indexOf(":");
                if (idx > 0) {
                    String k = clean.substring(0, idx).trim();
                    String v = clean.substring(idx + 1).trim();
                    if (!k.isEmpty()) map.put(k, v);
                }
            }
        }
        return map;
    }

    private List<String> parseTips(String text) {
        List<String> tips = new ArrayList<>();
        for (String line : text.split("\n")) {
            String clean = line.replaceAll("[•\\*\\-|\\[\\]╔╗╚╝═─┌┐└┘│+]", "").trim();
            if (clean.length() > 20 && (line.trim().startsWith("•")
                    || line.trim().startsWith("*") || line.trim().startsWith("-")))
                tips.add(clean);
        }
        return tips;
    }

    private String parseWarning(String text) {
        for (String line : text.split("\n")) {
            String lower = line.toLowerCase();
            if ((lower.contains("warning") || lower.contains("concern") || lower.contains("caution"))
                    && !lower.contains("no major"))
                return line.replaceAll("[|\\[\\]╔╗╚╝═─┌┐└┘│+⚠]", "").trim();
        }
        return null;
    }


    // ═══════════════════════════════════════════════════════════════════════════
    //  MAIN PANEL — Layout
    // ═══════════════════════════════════════════════════════════════════════════

    /** Builds the entire main (center) panel. */
    private VBox buildMainPanel() {
        VBox main = new VBox(0);
        main.setPadding(new Insets(20, 20, 20, 20));
        main.setSpacing(16);

        inputTable = buildInputTable();
        inputTable.setItems(processList);
        inputTable.setPrefHeight(220);
        inputTable.setFocusTraversable(false);

        ScrollPane ganttScroll = buildGanttScroll();

        resultTable = buildResultTable();
        resultTable.setMinHeight(110);
        resultTable.setFocusTraversable(false);
        VBox.setVgrow(resultTable, Priority.ALWAYS);

        StackPane inputTableWrapper = new StackPane(inputTable);
        inputTableWrapper.setStyle("-fx-border-color: #7F8EE3; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");

        StackPane resultTableWrapper = new StackPane(resultTable);
        resultTableWrapper.setStyle("-fx-border-color: #7F8EE3; -fx-border-width: 1; -fx-border-radius: 6; -fx-background-radius: 6;");
        VBox.setVgrow(resultTableWrapper, Priority.ALWAYS);

        main.getChildren().addAll(
            sectionLabel("Process Queue"),
            inputTableWrapper,
            buildMetricsRow(),
            sectionLabel("Gantt Chart"),
            ganttScroll,
            sectionLabel("Scheduling Results"),
            resultTableWrapper
        );
        return main;
    }


    // ═══════════════════════════════════════════════════════════════════════════
    //  MAIN PANEL — Process Queue Table
    // ═══════════════════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private TableView<ProcessRow> buildInputTable() {
        TableView<ProcessRow> table = new TableView<>();
        styleTable(table);

        TableColumn<ProcessRow, String>  colPid     = new TableColumn<>("PID");
        TableColumn<ProcessRow, Integer> colArrival = new TableColumn<>("Arrival");
        TableColumn<ProcessRow, Integer> colBurst   = new TableColumn<>("Burst");
        TableColumn<ProcessRow, Integer> colPrio    = new TableColumn<>("Priority");

        colPid.setCellValueFactory(new PropertyValueFactory<>("pid"));         colPid.setPrefWidth(80);
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrivalTime")); colArrival.setPrefWidth(80);
        colBurst.setCellValueFactory(new PropertyValueFactory<>("burstTime"));  colBurst.setPrefWidth(80);
        colPrio.setCellValueFactory(new PropertyValueFactory<>("priority"));    colPrio.setPrefWidth(80);

        for (TableColumn<ProcessRow, ?> c0 : new TableColumn[] {colPid, colArrival, colBurst, colPrio}) {
            TableColumn<ProcessRow, Object> c = (TableColumn<ProcessRow, Object>) c0;
            c.setCellFactory(column -> new TableCell<ProcessRow, Object>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.toString());
                    setAlignment(Pos.CENTER);
                }
            });
        }

        TableColumn<ProcessRow, Void> colDel = new TableColumn<>("");
        colDel.setPrefWidth(60);
        colDel.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✕");
            {
                btn.setStyle("-fx-font-size: 11px; -fx-background-color: transparent;"
                        + "-fx-text-fill: #C96E82; -fx-cursor: hand; -fx-border-color: transparent;");
                btn.setOnAction(e -> processList.remove(getTableRow().getItem()));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(colPid, colArrival, colBurst, colPrio, colDel);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Label placeholder = new Label("No processes yet.");
        placeholder.setStyle("-fx-text-fill: #e0e6ed83; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
        table.setPlaceholder(placeholder);
        table.setStyle("-fx-background-color: #040D43; -fx-text-fill: #e0e6ed83; -fx-control-inner-background: #040D43; -fx-table-cell-border-color: transparent;");
        return table;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  MAIN PANEL — Metrics Row
    // ═══════════════════════════════════════════════════════════════════════════

    private HBox buildMetricsRow() {
        avgTTLabel   = metricCard("Avg Turnaround", "—");
        avgWTLabel   = metricCard("Avg Waiting",    "—");
        cpuUtilLabel = metricCard("CPU Utilization","—");
        HBox row = new HBox(12, avgTTLabel, avgWTLabel, cpuUtilLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label metricCard(String title, String value) {
        Label card = new Label(title + "\n" + value);
        card.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-text-fill: #E0E6ED;"
                + "-fx-background-color: #040D43; -fx-border-color: #7F8EE3 ; -fx-border-radius: 6;"
                + "-fx-background-radius: 6; -fx-padding: 10 18 10 18; -fx-line-spacing: 4;");
        card.setAlignment(Pos.CENTER);
        card.setMinHeight(Region.USE_PREF_SIZE);
        card.setMinWidth(Region.USE_PREF_SIZE);
        card.setWrapText(false);
        return card;
    }

    private void updateMetricCard(Label card, String title, String value) {
        card.setText(title + "\n" + value);
    }


    // ═══════════════════════════════════════════════════════════════════════════
    //  MAIN PANEL — Gantt Chart
    // ═══════════════════════════════════════════════════════════════════════════

    private ScrollPane buildGanttScroll() {
        ganttCanvas = new Canvas(800, 85);
        ScrollPane ganttScroll = new ScrollPane(ganttCanvas);
        ganttScrollPane = ganttScroll;
        ganttScroll.setFitToHeight(true);
        ganttScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        ganttScroll.setStyle("-fx-background: #040D43; -fx-background-color: #040D43; -fx-border-color: #7F8EE3; -fx-border-width: 1; -fx-border-radius: 6;");
        ganttScroll.setPrefHeight(95);
        ganttScroll.setMinHeight(95);
        styleScrollBars(ganttScroll);
        ganttScroll.viewportBoundsProperty().addListener((obs, o, n) -> {
            if (lastScheduled != null) drawGantt(lastScheduled);
        });
        return ganttScroll;
    }

    private void styleScrollBars(ScrollPane sp) {
        sp.skinProperty().addListener((obs, o, n) -> {
            sp.lookupAll(".scroll-bar").forEach(node ->
                node.setStyle("-fx-background-color: transparent;"));
            sp.lookupAll(".scroll-bar .track").forEach(node ->
                node.setStyle("-fx-background-color: transparent;"));
            sp.lookupAll(".scroll-bar .thumb").forEach(node ->
                node.setStyle("-fx-background-color: transparent;"));
            sp.lookupAll(".increment-button, .decrement-button").forEach(node ->
                node.setStyle("-fx-background-color: transparent;"));
            sp.lookupAll(".corner").forEach(node ->
                node.setStyle("-fx-background-color: transparent;"));
        });
    }

    private void drawGantt(List<MyProcess> scheduled) {
        if (scheduled.isEmpty()) return;
        lastScheduled = scheduled;

        // Keep every slice as its own bar — no merging, accurate timeline
        List<int[]> bars = new ArrayList<>();
        List<String> barPids = new ArrayList<>();
        for (MyProcess p : scheduled) {
            bars.add(new int[]{p.startTime, p.finishTime});
            barPids.add(p.pid);
        }

        int maxFinish = scheduled.stream().mapToInt(p -> p.finishTime).max().orElse(1);
        int minStart  = scheduled.stream().mapToInt(p -> p.startTime).min().orElse(0);
        int span      = Math.max(maxFinish - minStart, 1);

        double padding = 20, barH = 42, barY = 15;
        double viewportW = (ganttScrollPane != null && ganttScrollPane.getViewportBounds() != null)
            ? ganttScrollPane.getViewportBounds().getWidth() : 800;
        double minNeeded = span * 28 + padding * 2;
        double canvasW = Math.max(viewportW > 0 ? viewportW : 800, minNeeded);
        double scaleX  = (canvasW - padding * 2) / span;

        ganttCanvas.setWidth(canvasW);
        GraphicsContext gc = ganttCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasW, 85);

        gc.setFill(Color.web("#040D43"));
        gc.fillRect(0, 0, canvasW, 85);

        Map<String, Color> colorMap = new LinkedHashMap<>();
        int ci = 0;
        for (MyProcess p : scheduled)
            colorMap.putIfAbsent(p.pid, PROCESS_COLORS[ci++ % PROCESS_COLORS.length]);

        for (int i = 0; i < bars.size(); i++) {
            int[] bar = bars.get(i);
            String pid = barPids.get(i);
            double x = padding + (bar[0] - minStart) * scaleX;
            double w = (bar[1] - bar[0]) * scaleX;

            gc.setFill(colorMap.get(pid));
            gc.fillRoundRect(x, barY, w - 2, barH, 6, 6);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            if (w > 20) gc.fillText(pid, x + w / 2 - 1, barY + barH / 2);

            gc.setFill(Color.web("#E0E6ED"));
            gc.setFont(Font.font("Segoe UI", 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(bar[0]), x, barY + barH + 14);
        }

        if (!bars.isEmpty()) {
            int[] lastBar = bars.get(bars.size() - 1);
            double lastX = padding + (lastBar[1] - minStart) * scaleX;
            gc.setFill(Color.web("#E0E6ED"));
            gc.setFont(Font.font("Segoe UI", 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(lastBar[1]), lastX, barY + barH + 14);
        }
    }


    // ═══════════════════════════════════════════════════════════════════════════
    //  MAIN PANEL — Scheduling Results Table
    // ═══════════════════════════════════════════════════════════════════════════

    private TableView<ResultRow> buildResultTable() {
        TableView<ResultRow> table = new TableView<>();
        styleTable(table);

        String[][] cols = {
            {"PID","pid","70"}, {"Arrival","arrivalTime","70"}, {"Burst","burstTime","70"},
            {"Start","startTime","70"}, {"Finish","finishTime","70"},
            {"Turnaround","turnaround","100"}, {"Waiting","waiting","80"}
        };
        for (int i = 0; i < cols.length; i++) {
            String[] c = cols[i];
            TableColumn<ResultRow, Object> col = new TableColumn<>(c[0]);
            col.setCellValueFactory(new PropertyValueFactory<>(c[1]));
            col.setPrefWidth(Double.parseDouble(c[2]));
            col.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item.toString());
                        setStyle("");
                    }
                    setAlignment(Pos.CENTER);
                }
            });
            table.getColumns().add(col);
        }
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Label placeholder = new Label("Run the simulation to see results.");
        placeholder.setStyle("-fx-text-fill: #e0e6ed83; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px;");
        table.setPlaceholder(placeholder);
        table.setStyle("-fx-background-color: #040D43; -fx-text-fill: #e0e6ed83; -fx-control-inner-background: #040D43; -fx-table-cell-border-color: transparent;");
        return table;
    }


    // ═══════════════════════════════════════════════════════════════════════════
    //  SIMULATION — Core Logic
    // ═══════════════════════════════════════════════════════════════════════════

    private void runSimulation() {
        if (processList.isEmpty()) { setStatus("Add at least one process first."); return; }

        List<MyProcess> input = new ArrayList<>();
        for (ProcessRow r : processList)
            input.add(new MyProcess(r.getPid(), r.getArrivalTime(), r.getBurstTime(), r.getPriority()));

        // Safely read quantum spinner value, fall back to 4 if invalid
        int quantum = 4;
        try {
            quantumSpinner.commitValue();
            quantum = quantumSpinner.getValue() != null ? quantumSpinner.getValue() : 4;
        } catch (Exception ex) {
            quantum = 4;
        }

        String algo = algorithmCombo.getValue();
        if (algo == null) algo = "FCFS";

        List<MyProcess> ganttData;   // slices for the Gantt chart
        List<MyProcess> tableData;   // one row per process for the results table
        try {
            final int q = quantum;
            if ("Round Robin".equals(algo)) {
                RoundRobin rr = new RoundRobin();
                ganttData = rr.execute(input, q);
                tableData = rr.executeForTable(input, q);
            } else {
                ganttData = switch (algo) {
                    case "FCFS"     -> new FCFS().execute(input);
                    case "SJF"      -> new SJF().execute(input);
                    case "Priority" -> new PriorityScheduler().execute(input);
                    default         -> new FCFS().execute(input);
                };
                tableData = ganttData;
            }
        } catch (Exception ex) {
            setStatus("Scheduler error: " + ex.getMessage());
            return;
        }

        if (ganttData == null || ganttData.isEmpty()) {
            setStatus("Scheduler returned no results.");
            return;
        }

        // Populate result table (one row per process)
        ObservableList<ResultRow> results = FXCollections.observableArrayList();
        int totalTT = 0, totalWT = 0, minArrival = Integer.MAX_VALUE, maxFinish = 0;

        for (MyProcess p : tableData) {
            int tt = p.finishTime - p.arrivalTime;
            int wt = tt - p.burstTime;
            totalTT += tt; totalWT += wt;
            minArrival = Math.min(minArrival, p.arrivalTime);
            maxFinish  = Math.max(maxFinish,  p.finishTime);
            results.add(new ResultRow(p.pid, p.arrivalTime, p.burstTime, p.startTime, p.finishTime, tt, wt));
        }

        resultTable.setItems(results);

        int size = tableData.size();
        double avgTT   = size > 0 ? (double) totalTT / size : 0;
        double avgWT   = size > 0 ? (double) totalWT / size : 0;
        int    span    = maxFinish - (minArrival == Integer.MAX_VALUE ? 0 : minArrival);
        int    busyTime = tableData.stream().mapToInt(p -> p.burstTime).sum();
        double cpuUtil = span > 0 ? (busyTime * 100.0 / span) : 100.0;

        updateMetricCard(avgTTLabel,   "Avg Turnaround",  String.format("%.2f", avgTT));
        updateMetricCard(avgWTLabel,   "Avg Waiting",     String.format("%.2f", avgWT));
        updateMetricCard(cpuUtilLabel, "CPU Utilization", String.format("%.1f%%", cpuUtil));

        drawGantt(ganttData);
        setStatus(algo + " completed — " + size + " processes scheduled.");
    }

    private void clearResults() {
        resultTable.getItems().clear();
        updateMetricCard(avgTTLabel,   "Avg Turnaround",  "—");
        updateMetricCard(avgWTLabel,   "Avg Waiting",     "—");
        updateMetricCard(cpuUtilLabel, "CPU Utilization", "—");
        ganttCanvas.getGraphicsContext2D().clearRect(0, 0, ganttCanvas.getWidth(), ganttCanvas.getHeight());
    }

    private void setStatus(String msg) { statusLabel.setText(msg); }


    // ═══════════════════════════════════════════════════════════════════════════
    //  SHARED STYLE HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private Label sectionLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #FFD862;"
                + "-fx-font-family: 'Segoe UI'; -fx-letter-spacing: 1;");
        return l;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #E0E6ED; -fx-font-family: 'Segoe UI';");
        l.setMinWidth(55);
        return l;
    }

    private TextField styledField(String prompt, double w) {
        TextField tf = new TextField(prompt);
        tf.setPrefWidth(w);
        tf.setStyle("-fx-font-size: 12px; -fx-font-family: 'Segoe UI';"
                + "-fx-background-color: #152055; -fx-border-color: #7F8EE3;"
                + "-fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8;"
                + "-fx-text-fill: #E0E6ED; -fx-prompt-text-fill: #A0AED4;");
        return tf;
    }

   private void styleCombo(ComboBox<?> cb) {
    cb.setStyle("-fx-font-size: 13px; -fx-font-family: 'Segoe UI';"
            + "-fx-background-color: #152055; -fx-border-color: #7F8EE3;"
            + "-fx-border-radius: 4; -fx-background-radius: 4;");

    @SuppressWarnings("unchecked")
    ComboBox<String> strCombo = (ComboBox<String>) cb;
    strCombo.setButtonCell(new ListCell<String>() {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(item);
            }
            setStyle("-fx-text-fill: white; -fx-font-size: 13px;"
                   + "-fx-font-family: 'Segoe UI'; -fx-background-color: #152055;");
        }
    });
}

    private void styleSpinner(Spinner<?> s) {
        s.setStyle("-fx-font-size: 13px; -fx-font-family: 'Segoe UI';"
                + "-fx-background-color: #040D43; -fx-border-color: #C8C6C0;"
                + "-fx-border-radius: 4; -fx-background-radius: 4;");
    }

  private void styleTable(TableView<?> t) {
    t.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 12px;"
            + "-fx-border-color: transparent; -fx-border-width: 0; -fx-border-radius: 6; -fx-background-radius: 6;"
            + "-fx-background-color: #040D43; -fx-control-inner-background: #152055;"
            + "-fx-table-cell-border-color: transparent;"
            + "-fx-selection-bar: transparent; -fx-selection-bar-non-focused: transparent;");
    Runnable applyHeaderStyle = () -> {
        Pane header = (Pane) t.lookup("TableHeaderRow");
        if (header != null) {
            header.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3;"
                    + "-fx-border-width: 0 0 1 0;");
            header.lookupAll(".column-header").forEach(node ->
                node.setStyle("-fx-background-color: linear-gradient(to bottom, #1e3275, #0d1a4a);"
                        + "-fx-border-color: #7F8EE3; -fx-border-width: 0 1 0 0;"));
            header.lookupAll(".column-header .label").forEach(node ->
                node.setStyle("-fx-text-fill: #E0E6ED; -fx-font-weight: bold;"
                        + "-fx-font-family: 'Segoe UI'; -fx-font-size: 12px;"));
            header.lookupAll(".filler").forEach(node ->
                node.setStyle("-fx-background-color: #040D43;"));
        }
    };
    t.widthProperty().addListener((obs, o, n) -> applyHeaderStyle.run());
    applyHeaderStyle.run();
    javafx.application.Platform.runLater(applyHeaderStyle);
    applyRowFactory(t);
    t.skinProperty().addListener((obs, o, n) -> {
        t.lookupAll(".scroll-bar").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
        t.lookupAll(".scroll-bar .track").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
        t.lookupAll(".scroll-bar .thumb").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
        t.lookupAll(".increment-button, .decrement-button").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
        t.lookupAll(".corner").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
        t.lookupAll(".filler").forEach(node ->
            node.setStyle("-fx-background-color: transparent;"));
        applyHeaderStyle.run();
    });
}
    @SuppressWarnings({"unchecked", "rawtypes"})
   private void applyRowFactory(TableView t) {
    t.setRowFactory(tv -> {
        TableRow row = new TableRow();
        row.itemProperty().addListener((obs, oldItem, newItem) -> {
            if (newItem == null) {
                row.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            } else {
                int index = row.getIndex();
                String bg = (index % 2 == 0) ? "#1a2a6e" : "#040D43";
                row.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: #E0E6ED;"
                        + "-fx-border-color: transparent; -fx-selection-bar: " + bg + ";"
                        + "-fx-selection-bar-non-focused: " + bg + ";");
            }
        });
        return row;
    });
}
    private Button actionButton(String text) {
    return actionButton(text, "#7F8EE3", "#9AA6EB", "#F0EDE4");
}

    private Button actionButton(String text, String baseColor, String hoverColor, String textColor) {
    Button b = new Button(text);
    String base = "-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-font-weight: bold;"
            + "-fx-background-color: " + baseColor + "; -fx-text-fill: " + textColor + ";"
            + "-fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 7 12; -fx-cursor: hand;";
    String hover = base.replace(baseColor, hoverColor);
    b.setStyle(base);
    b.setOnMouseEntered(e -> b.setStyle(hover));
    b.setOnMouseExited(e -> b.setStyle(base));
    return b;
}

    private Button secondaryButton(String text) {
    return secondaryButton(text, "#e74c3c", "#ec7063", "#ffffff");
}

    private Button secondaryButton(String text, String baseColor, String hoverColor, String textColor) {
    Button b = new Button(text);
    String base = "-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-font-weight: bold;"
            + "-fx-background-color: " + baseColor + "; -fx-text-fill: " + textColor + ";"
            + "-fx-border-color: transparent; -fx-border-radius: 5; -fx-background-radius: 5;"
            + "-fx-padding: 6 12; -fx-cursor: hand;";
    String hover = base.replace(baseColor, hoverColor);
    b.setStyle(base);
    b.setOnMouseEntered(e -> b.setStyle(hover));
    b.setOnMouseExited(e -> b.setStyle(base));
    return b;
}

    private Button runButton(String text) {
    Button b = new Button(text);
    String base = "-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-font-weight: bold;"
            + "-fx-background-color: #FFBA09; -fx-text-fill: #152055;"
            + "-fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 9 14; -fx-cursor: hand;";
    String hover = base.replace("#FFBA09", "#FFCB42");
    b.setStyle(base);
    b.setOnMouseEntered(e -> b.setStyle(hover));
    b.setOnMouseExited(e -> b.setStyle(base));
    return b;
}

    private Button advisorButton(String text) {
    Button b = new Button(text);
    String base = "-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-font-weight: bold;"
            + "-fx-background-color: #9B59B6; -fx-text-fill: #FFFFFF;"
            + "-fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 7 12; -fx-cursor: hand;";
    String hover = base.replace("#9B59B6", "#B370CF");
    b.setStyle(base);
    b.setOnMouseEntered(e -> b.setStyle(hover));
    b.setOnMouseExited(e -> b.setStyle(base));
    return b;
}

    private Separator separator() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: #152055;");
        return s;
    }

    private HBox sectionDivider(String text) {
        Label lbl = new Label(text.toUpperCase());
        lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #FFD862;"
                + "-fx-font-family: 'Segoe UI'; -fx-letter-spacing: 1;");
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #7F8EE3 ;");
        HBox row = new HBox(8, lbl, sep);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(sep, Priority.ALWAYS);
        return row;
    }

    private VBox statPill(String value, String label) {
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1DCA59; -fx-font-family: 'Segoe UI';");
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #E0E6ED; -fx-font-family: 'Segoe UI';");
        VBox box = new VBox(2, val, lbl);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #040D43; -fx-border-color: #7F8EE3 ;"
                + "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 8;");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }


    // ═══════════════════════════════════════════════════════════════════════════
    //  DATA MODELS — Row Classes
    // ═══════════════════════════════════════════════════════════════════════════

    public static class ProcessRow {
        private final SimpleStringProperty  pid;
        private final SimpleIntegerProperty arrivalTime, burstTime, priority;

        public ProcessRow(String pid, int arrival, int burst, int prio) {
            this.pid         = new SimpleStringProperty(pid);
            this.arrivalTime = new SimpleIntegerProperty(arrival);
            this.burstTime   = new SimpleIntegerProperty(burst);
            this.priority    = new SimpleIntegerProperty(prio);
        }

        public String  getPid()         { return pid.get(); }
        public int     getArrivalTime() { return arrivalTime.get(); }
        public int     getBurstTime()   { return burstTime.get(); }
        public int     getPriority()    { return priority.get(); }
        public StringProperty  pidProperty()         { return pid; }
        public IntegerProperty arrivalTimeProperty() { return arrivalTime; }
        public IntegerProperty burstTimeProperty()   { return burstTime; }
        public IntegerProperty priorityProperty()    { return priority; }
    }

    public static class ResultRow {
        private final SimpleStringProperty  pid;
        private final SimpleIntegerProperty arrivalTime, burstTime, startTime, finishTime, turnaround, waiting;

        public ResultRow(String pid, int arrival, int burst, int start, int finish, int tt, int wt) {
            this.pid         = new SimpleStringProperty(pid);
            this.arrivalTime = new SimpleIntegerProperty(arrival);
            this.burstTime   = new SimpleIntegerProperty(burst);
            this.startTime   = new SimpleIntegerProperty(start);
            this.finishTime  = new SimpleIntegerProperty(finish);
            this.turnaround  = new SimpleIntegerProperty(tt);
            this.waiting     = new SimpleIntegerProperty(wt);
        }

        public String  getPid()         { return pid.get(); }
        public int     getArrivalTime() { return arrivalTime.get(); }
        public int     getBurstTime()   { return burstTime.get(); }
        public int     getStartTime()   { return startTime.get(); }
        public int     getFinishTime()  { return finishTime.get(); }
        public int     getTurnaround()  { return turnaround.get(); }
        public int     getWaiting()     { return waiting.get(); }
        public StringProperty  pidProperty()         { return pid; }
        public IntegerProperty arrivalTimeProperty() { return arrivalTime; }
        public IntegerProperty burstTimeProperty()   { return burstTime; }
        public IntegerProperty startTimeProperty()   { return startTime; }
        public IntegerProperty finishTimeProperty()  { return finishTime; }
        public IntegerProperty turnaroundProperty()  { return turnaround; }
        public IntegerProperty waitingProperty()     { return waiting; }
    }
}