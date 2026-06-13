package backend;

import java.util.*;

public class SJF {

    public List<MyProcess> execute(List<MyProcess> processes) {
        List<MyProcess> result = new ArrayList<>();
        List<MyProcess> remaining = new ArrayList<>();

        for (MyProcess p : processes) {
            remaining.add(new MyProcess(p));
        }

        remaining.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = 0;
        int completed   = 0;
        int total       = processes.size();

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

            // Pick shortest burst
            MyProcess shortest = arrived.stream()
                .min(Comparator.comparingInt(p -> p.burstTime))
                .get();

            // Put the rest back into remaining
            for (MyProcess p : arrived) {
                if (p != shortest) remaining.add(p);
            }
            remaining.sort(Comparator.comparingInt(p -> p.arrivalTime));

            shortest.startTime  = currentTime;
            shortest.finishTime = currentTime + shortest.burstTime;
            currentTime         = shortest.finishTime;

            result.add(shortest);
            completed++;
        }

        return result;
    }
}