package backend;

import java.util.*;

public class RoundRobin {

    public List<MyProcess> execute(List<MyProcess> processes, int timeQuantum) {
        List<MyProcess> result = new ArrayList<>();
        Queue<MyProcess> queue = new LinkedList<>();
        List<MyProcess> remaining = new ArrayList<>();

        for (MyProcess p : processes) {
            MyProcess copy = new MyProcess(p);
            copy.remainingTime = copy.burstTime;
            copy.startTime = -1; // sentinel: not yet started
            remaining.add(copy);
        }

        remaining.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int currentTime = 0;
        int index = 0;
        int completed = 0;
        int total = processes.size();

        // Track finish time per PID for final result
        Map<String, MyProcess> processMap = new LinkedHashMap<>();
        for (MyProcess p : remaining) processMap.put(p.pid, p);

        // Seed queue with processes already available at time 0
        while (index < remaining.size() && remaining.get(index).arrivalTime <= currentTime) {
            queue.add(remaining.get(index++));
        }

        while (completed < total) {
            if (queue.isEmpty()) {
                if (index < remaining.size()) {
                    currentTime = remaining.get(index).arrivalTime;
                    while (index < remaining.size() && remaining.get(index).arrivalTime <= currentTime) {
                        queue.add(remaining.get(index++));
                    }
                } else {
                    break;
                }
            }

            MyProcess current = queue.poll();

            // Record first start time on the real process
            if (current.startTime == -1) {
                current.startTime = currentTime;
            }

            int sliceStart = currentTime;
            int runTime = Math.min(timeQuantum, current.remainingTime);
            currentTime += runTime;
            current.remainingTime -= runTime;

            // Create a slice entry for the Gantt chart
            MyProcess slice = new MyProcess(current.pid, current.arrivalTime, runTime, current.priority);
            slice.startTime  = sliceStart;
            slice.finishTime = currentTime;
            result.add(slice);

            // Enqueue processes that arrived during this slice
            while (index < remaining.size() && remaining.get(index).arrivalTime <= currentTime) {
                queue.add(remaining.get(index++));
            }

            if (current.remainingTime == 0) {
                current.finishTime = currentTime;
                completed++;
            } else {
                queue.add(current);
            }
        }

        return result;
    }

    /**
     * Returns one row per process (for the results table) by collapsing slices.
     * startTime = first slice start, finishTime = last slice finish, burstTime = original burst.
     */
    public List<MyProcess> executeForTable(List<MyProcess> processes, int timeQuantum) {
        List<MyProcess> slices = execute(processes, timeQuantum);

        // Collapse slices back into one row per PID
        Map<String, MyProcess> map = new LinkedHashMap<>();
        for (MyProcess p : processes) {
            MyProcess row = new MyProcess(p);
            row.startTime  = Integer.MAX_VALUE;
            row.finishTime = 0;
            map.put(p.pid, row);
        }
        for (MyProcess slice : slices) {
            MyProcess row = map.get(slice.pid);
            if (row != null) {
                row.startTime  = Math.min(row.startTime,  slice.startTime);
                row.finishTime = Math.max(row.finishTime, slice.finishTime);
            }
        }
        return new ArrayList<>(map.values());
    }
}