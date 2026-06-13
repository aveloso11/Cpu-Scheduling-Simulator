package backend;

import java.util.*;

public class MetricsCalculator {
    
    public static void printResults(String algorithmName, List<MyProcess> processes) {
        System.out.println("n========= " + algorithmName + " =========");
        System.out.println("PID\tArrival\tBurst\tStart\tFinish\tTurnaround\tWaiting");

        int totalTT = 0;
        int totalWT = 0;

        for (MyProcess p : processes) {
            int turnaround = p.finishTime - p.arrivalTime;
            int waiting = turnaround - p.burstTime;

            totalTT += turnaround;
            totalWT += waiting;

            System.out.printf("%s\t%d\t%d\t%d\t%d\t%d\t\t%d\n", p.pid, p.arrivalTime, p.burstTime, p.startTime, p.finishTime, turnaround, waiting); 
        }

        double avgTT =  (double) totalTT / processes.size();
        double avgWT = (double) totalWT / processes.size();

        System.out.printf("\nAverage Turnaround Time: %.2f\n", avgTT);
        System.out.printf("Average Waiting Time: %.2f\n", avgWT);
        System.out.println("=====================================\n");
    }
}
