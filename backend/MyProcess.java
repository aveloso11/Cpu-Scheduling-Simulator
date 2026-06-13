package backend;

public class MyProcess {
    public String pid; 
    public int arrivalTime;
    public int burstTime;
    public int priority;
    public int remainingTime;
    public int startTime;
    public int finishTime;

    public MyProcess(String pid, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority  = 5;
    }

    public MyProcess(String pid, int arrivalTime, int burstTime, int priority) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
    }

    public  MyProcess(MyProcess other) {
        this.pid = other.pid;
        this.arrivalTime = other.arrivalTime;
        this.burstTime = other.burstTime;
        this.remainingTime = other.remainingTime;
        this.priority = other.priority;
        this.startTime = other.startTime;
        this.finishTime = other.finishTime;
    }

    public String toString() {
        return pid;
    }

}
