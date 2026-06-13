package backend;

import java.util.*;

public class SmartAdvisor {
    
    public static String recommend(List<MyProcess> processes) {
        StringBuilder advice = new StringBuilder();

        advice.append("\n  ╔══════════════════════════════════════════════════════════════════╗\n");
        advice.append("  ║                    📊 SMART ALGORITHM ADVISOR                    ║\n");
        advice.append("\n  ╚══════════════════════════════════════════════════════════════════╝\n");

        // PERFORM ALL ANALYSIS
        boolean hasPriorities = checkPriorities(processes);
        BurstAnalysis burstAnalysis = analyzeBurstTimes(processes);
        ArrivalAnalysis arrivalAnalysis = analyzeArrivalTimes(processes);
        
        // DETECT SCENARIO TYPE for matching bias
        String detectedScenario = detectScenario(processes, burstAnalysis, arrivalAnalysis, hasPriorities);

        // DISPLAY WORKLOAD SUMMARY
        advice.append("\n📈 WORKLOAD ANALYSIS: \n");
        advice.append("┌──────────────────────────────────────────────────────────────────┐\n");
        advice.append(String.format(" | Number of processes: %-44d|\n", processes.size()));
        advice.append(String.format(" | Avg Burst Time: %.2f ms %-39s|\n", burstAnalysis.avg, ""));
        advice.append(String.format(" | Burst Time Range: %d - %d ms %-31s|\n", burstAnalysis.min, burstAnalysis.max, ""));
        advice.append(String.format(" | Burst Time Variance: %.2f %-36s|\n", burstAnalysis.variance, ""));
        advice.append(String.format(" | Has Priorities: %-44s|\n", hasPriorities ? "✅ YES" : "❌ NO"));
        advice.append(String.format(" | Detected Pattern: %-41s|\n", detectedScenario));
        advice.append("└──────────────────────────────────────────────────────────────────┘\n");

        // MAKE RECOMMENDATION 
        advice.append("\n💡 RECOMMENDATION:\n");
        advice.append("┌───────────────────────────────────────────────────────────────┐\n ");

        String recommendation = "";
        String reason = "";
        String warning = "";

        // MATCH BIAS BASED ON DETECTED SCENARIO
        if (detectedScenario.equals("PRIORITY")) {
            recommendation = "PRIORITY SCHEDULING";
            reason = "Processes have explicit priority values (1-5). Priority Scheduling respects these values.";
            warning = "⚠️ Note: Low priority processes may starve!";
        }
        else if (detectedScenario.equals("ROUND ROBIN")) {
            recommendation = "ROUND ROBIN";
            reason = "All processes have identical burst times (" + burstAnalysis.min + "ms). Round Robin provides fair response time for time-sharing systems.";
            warning = "💡 Suggestion: Use quantum = 10-20ms for best performance.";
        }
        else if (detectedScenario.equals("SJF")) {
            recommendation = "SJF (Shortest Job First)";
            reason = "One very long process (" + burstAnalysis.max + "ms) with multiple short processes creates a convoy effect. SJF minimizes waiting time.";
            warning = "⚠️ Consider SRTF if preemption is allowed.";
        }
        else if (detectedScenario.equals("FCFS")) {
            recommendation = "FCFS (First Come First Serve)";
            reason = "Processes have staggered arrival times with increasing burst times. FCFS works efficiently here.";
            warning = "✅ No major concerns for this workload.";
        }
        else {
            // FALLBACK to original logic
            if (hasPriorities) {
                recommendation = "PRIORITY SCHEDULING";
                reason = "Process have explicit priority values (non-default).";
                warning = "⚠️ Note: Low priority processes may starve!";
            }
            else if (processes.size() > 5) {
                recommendation = "ROUND ROBIN";
                reason = "Many processes benefit from fair CPU time sharing.";
                warning = "💡 Suggestion: Use quantum = 10-20ms for best performance.";
            }
            else if (burstAnalysis.variance < 20 && burstAnalysis.avg < 20) {
                recommendation = "FCFS (First Come First Serve)";
                reason = "Burst times are similar and short. FCFS is simple and efficient.";
                warning = "✅ No major concerns for this workload.";
            }
            else {
                recommendation = "SJF (Shortest Job First)";
                reason = "SJF generally provides the best average waiting time for mixed work loads.";
                warning = "⚠️ Consider SRTF if preemption is allowed.";
            }
        }

        advice.append(String.format(" | Algorithm: %-48s|\n", recommendation));
        advice.append(String.format(" | Reason: %-52s|\n", reason));
        advice.append("└───────────────────────────────────────────────────────────────┘\n");

        if (!warning.isEmpty()) {
            advice.append(String.format("\n %s\n", warning));
        }

        // ADDITIONAL TIPS
        advice.append("\n📌 ADDITIONAL TIPS\n");
        advice.append(" • For interactive systems, use Round Robin with small quantum (10-20ms)\n");
        advice.append(" • Use Priority Scheduling when process importance varies\n");
        advice.append(" • Consider SRTF (Preemptive SJF) for real time responsiveness\n");

        advice.append("\n  ╔══════════════════════════════════════════════════════════════════╗\n");
        advice.append("  ║                    Run the recommended algorithm!                ║\n");
        advice.append("  ╚══════════════════════════════════════════════════════════════════╝\n");

        return advice.toString();
    }

    // DETECT WHICH SCENARIO TYPE THE WORKLOAD MATCHES
    private static String detectScenario(List<MyProcess> processes, BurstAnalysis burst, ArrivalAnalysis arrival, boolean hasPriorities) {
        
        // Check for PRIORITY scenario (priorities 1-5, not default)
        if (hasPriorities) {
            return "PRIORITY";
        }
        
        // Check for SJF scenario (one very long process, others short)
        int longProcesses = 0;
        int shortProcesses = 0;
        for (MyProcess p : processes) {
            if (p.burstTime > 20) {
                longProcesses++;
            } else if (p.burstTime < 10) {
                shortProcesses++;
            }
        }
        if (longProcesses == 1 && shortProcesses >= 3) {
            return "SJF";
        }
        
        // Check for FCFS scenario (staggered arrivals with increasing bursts)
        boolean staggeredArrival = true;
        boolean increasingBurst = true;
        List<MyProcess> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt(p -> p.arrivalTime));
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).arrivalTime <= sorted.get(i-1).arrivalTime) {
                staggeredArrival = false;
            }
            if (sorted.get(i).burstTime <= sorted.get(i-1).burstTime) {
                increasingBurst = false;
            }
        }
        if (staggeredArrival && increasingBurst) {
            return "FCFS";
        }
        
        // Check for ROUND ROBIN scenario (all identical burst times)
        boolean allSameBurst = true;
        int firstBurst = processes.get(0).burstTime;
        for (MyProcess p : processes) {
            if (p.burstTime != firstBurst) {
                allSameBurst = false;
                break;
            }
        }
        if (allSameBurst) {
            return "ROUND ROBIN";
        }
        
        return "UNKNOWN";
    }

    // CHECK IF ANY PROCESS HAS PRIORITY 0-4
    private static boolean checkPriorities(List<MyProcess> processes) {
        for (MyProcess p: processes) {
            if (p.priority >= 0 && p.priority <= 4) {
                return true;
            }
        }
        return false;
    }

    // ANALYZE BURST TIMES (min, max, avg, variance)
    private static BurstAnalysis analyzeBurstTimes(List<MyProcess> processes) {
        BurstAnalysis analysis = new BurstAnalysis();

        double sum = 0;
        analysis.min = Integer.MAX_VALUE;
        analysis.max = Integer.MIN_VALUE;

        for (MyProcess p: processes) {
            sum += p.burstTime;
            analysis.min = Math.min(analysis.min, p.burstTime);
            analysis.max = Math.max(analysis.max, p.burstTime);
        }
        analysis.avg = sum / processes.size();

        double varianceSum = 0;
        for (MyProcess p : processes) {
            varianceSum += Math.pow(p.burstTime - analysis.avg, 2);
        }
        analysis.variance = varianceSum / processes.size();

        return analysis;
    }

    // ANALYZE ARRIVAL TIMES FOR OVERLAP
    private static ArrivalAnalysis analyzeArrivalTimes(List<MyProcess> processes) {
        ArrivalAnalysis analysis = new ArrivalAnalysis();

        List<MyProcess> sorted = new ArrayList<>(processes);
        sorted.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = 0;
        for (MyProcess p : sorted) {
            if (p.arrivalTime < currentTime) {
                analysis.hasOverlap = true;
                break;
            }
            currentTime = Math.max(currentTime, p.arrivalTime) + p.burstTime;
        }
        return analysis;
    }

    // HELPER CLASSES FOR ANALYSIS RESULTS
    static class BurstAnalysis {
        double avg;
        int min;
        int max;
        double variance;
    }

    static class ArrivalAnalysis {
        boolean hasOverlap;
    }
}   