/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Qamar
 */
public class Job {
 
    private int arrivingTime;
    private int jobNumber;  //J
    private int requestedMemory;  //M
    private int requestedDevice;  //S
    private int priority;  //P
    private int burstTime;  //R
    private int startTime; //start time
    private int TaT;//turn around
    private int finishTime; //to finish 
    private int remainingTime; //the tie process still need to finish burst time
    private double waitingTime;
    private double AvgBt;

    // empty constructor 
    Job() {
    }

    // "A" job constractur
    Job(int arrivingTime, int jobNumber, int requestedMemory, int requestedDevice, int busrtTime, int priority) {
        this.arrivingTime = arrivingTime;
        this.jobNumber = jobNumber;
        this.requestedMemory = requestedMemory;
        this.requestedDevice = requestedDevice;
        this.burstTime = busrtTime;
        this.priority = priority;
        this.remainingTime = this.burstTime;
    }

    @Override
    public String toString() {
        return "Job{" + "jobNumber=" + jobNumber + '}';
    }

    // "D" job constructor
    Job(int time) {
        this.arrivingTime = time;
        this.jobNumber = 0;
    }

    public void setAvgBt(double AvgBt) {
        this.AvgBt = AvgBt;
    }

    public double getAvgBt() {
        return AvgBt;
    }

    public int getTurnAT() {
        return finishTime - arrivingTime;
    }

    
    public double getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(double waitingTime) {
        this.waitingTime = waitingTime;
    }
    
    public int getremainingTime() {
        return remainingTime;
    }

    public void setremainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public void setTurnAT(int TAT) {
        this.TaT = TAT;
    }

    public int getburstTime() {
        return burstTime;
    }

    public void setburstTime(int busrtTime) {
        this.burstTime = busrtTime;
    }

    public int getArrivingTime() {
        return arrivingTime;
    }

    public int getJobNumber() {
        return jobNumber;
    }

    public int getRequestedMemory() {
        return requestedMemory;
    }

    public int getRequestedDevices() {
        return requestedDevice;
    }

    public int getPriority() {
        return priority;
    }
    public void setArrivingTime(int arrivingTime) {
        this.arrivingTime = arrivingTime;
    }

    public void setJobNumber(int jobNumber) {
        this.jobNumber = jobNumber;
    }

    public void setRequestedMemory(int requestedMemory) {
        this.requestedMemory = requestedMemory;
    }

    public void setRequestedDevice(int requestedDevice) {
        this.requestedDevice = requestedDevice;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }


    public int getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(int finishTime) {
        this.finishTime = finishTime;
    }

   
}

