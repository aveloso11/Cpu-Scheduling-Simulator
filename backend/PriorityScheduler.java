package backend;

import java.util.*;

public class PriorityScheduler {    
    
    public List<MyProcess> execute(List<MyProcess> processes) {
        List<MyProcess> result = new ArrayList<>();
        List<MyProcess> remaining = new ArrayList<>();

        for (MyProcess p : processes) {
            remaining.add(new MyProcess(p));
        }

        remaining.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = 0;
        int completed = 0;
        int total = processes.size();

        while (completed < total) {
            List<MyProcess> arrived = new ArrayList<>();
            Iterator<MyProcess> it = remaining.iterator();
            while (it.hasNext()) {
                MyProcess p = it.next();
                if (p.arrivalTime <= currentTime) {
                    arrived.add(p);
                    it.remove();
                }
            }

            if (arrived.isEmpty()) {
                if (!remaining.isEmpty()) {
                    currentTime = remaining.get(0).arrivalTime;
                } else {
                    break; // no more processes — avoid infinite loop
                }
                continue;
            }

            // Pick highest priority (lowest number)
            MyProcess highest = arrived.stream()
                .min(Comparator.comparingInt(p -> p.priority))
                .get();

            // Put the rest back into remaining
            for (MyProcess p : arrived) {
                if (p != highest) remaining.add(p);
            }
            remaining.sort(Comparator.comparingInt(p -> p.arrivalTime));

            highest.startTime  = currentTime;
            highest.finishTime = currentTime + highest.burstTime;
            currentTime        = highest.finishTime;

            result.add(highest);
            completed++;
        }

        return result;
    }
}