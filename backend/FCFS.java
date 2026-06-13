package backend;

import java.util.*;

public class FCFS  {
    
    public List<MyProcess> execute(List<MyProcess> processes) {
        List<MyProcess> result = new ArrayList<>();
        for (MyProcess p : processes) {
            result.add(new MyProcess(p));
        }

        result.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = 0;

        for (MyProcess p : result) {
            if (currentTime < p.arrivalTime) {
                currentTime = p.arrivalTime;
            }
            p.startTime = currentTime;
            p.finishTime = currentTime + p.burstTime;
            currentTime  = p.finishTime;
        }

        return result;

    }
}