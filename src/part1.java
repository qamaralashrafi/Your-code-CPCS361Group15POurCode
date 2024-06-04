
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template

    Qamar Alashrafi    2106097
    Maria Alandeajni   2106021
    Ayatun Ara         2110295   


Maria's device infos:
    Compiler Name and Version: using NetBeans IDE 8.2
    Hardware processor: 1.4 GHz Quad-Core Intel Core i5
    Operating System and Version: macOS 14.1 (23B74)

Qamars's device infos:
    Compiler Name and Version: using Apache NetBeans IDE 12.2
    Hardware processor: AMD Ryzen 7 5700U with Radeon Graphics 1.80 GHz
    Operating System and Version: MSI Modern 14 B5M

Ayatun's device infos:
    Compiler Name and Version: using Apache NetBeans IDE 12.2
    Hardware processor: Intel(R) Core(TM) i7-4600U CPU @ 2.10GHz 2.69 GHz
    Operating System and Version: windows10
 */
/**
 *
 * @author Qamar
 */
public class part1 {

    /**
     * @param args the command line arguments
     */
    //declare queues
    static Queue<Job> ReadyQueue = new LinkedList<>();
    static Queue<Job> HoldQueue1 = new LinkedList<>();
    static Queue<Job> HoldQueue2 = new LinkedList<>();
    static Queue<Job> SubmitQueue = new LinkedList<>();
    static LinkedList<Job> CompleteQueue = new LinkedList<>();

    static Job exeJob = null; // process currently executing in CPU
    public static int startingTime;  // system start time
    public static int mainMemorySize;  // M: system main memory
    public static int devices;  // S: system total devices
    public static int currentTime;  // system current time
    public static int avbMemory;  // available main memory
    public static int avbDevices;  // available devices
    public static int jobNum;  // number of jobs
    public static int quantum;
    public static int i; // internal: job in CPU
    public static int e; // external: jobs in queue (known by arrival time)
    public static int display;
    public static int SR; // sum of remained burst time
    public static int AR; // average of remained burst time
    public static int maximumValue = Integer.MAX_VALUE;
    public static PrintWriter write;

    public static void main(String[] args) throws FileNotFoundException {
        {

            //declare input file
            File file = new File("inputnew4.txt");
            Scanner input = new Scanner(file);

            //create output file and printer to write in it
            write = new PrintWriter("outputnew4.txt");

            String line;
            String[] command;

            //loop throught jobs
            while (input.hasNextLine()) {
                // read line by line from the input file
                line = input.nextLine().replaceAll("[a-zA-Z]=", "");
                // separate the info in an array
                command = line.split(" ");
                //------------------------------------------------------------------
                // read system configuration
                if (command[0].equals("C")) {

                    startingTime = Integer.parseInt(command[1]);

                    currentTime = startingTime;

                    mainMemorySize = Integer.parseInt(command[2]);

                    devices = Integer.parseInt(command[3]);

                    avbMemory = mainMemorySize;

                    avbDevices = devices;

                    //--------------------------------------------------------------
                    // add A
                } else if (command[0].equals("A")) {

                    //set job characteristics
                    int arrivingTime = Integer.parseInt(command[1]);

                    int jobNo = Integer.parseInt(command[2]);

                    int requestedMM = Integer.parseInt(command[3]);

                    int requestedDevices = Integer.parseInt(command[4]);

                    int burstTime = Integer.parseInt(command[5]);

                    int JobPriority = Integer.parseInt(command[6]);

                    // create process for all valid jobs then add them to SubmitQueue
                    if (requestedMM <= mainMemorySize && requestedDevices <= devices) {

                        SubmitQueue.add(new Job(arrivingTime, jobNo, requestedMM, requestedDevices, burstTime, JobPriority));
                        //to count the number of job entered to the queue
                        jobNum++;
                    } else {//if mem/dev not enough for job 
                        write.println("Job " + jobNo + " has been rejected due to insufficient resources.");
                    }
                    // add D
                } else if (command[0].equals("D")) {
                    int systemStateTime = Integer.parseInt(command[1]);
                    if (systemStateTime != 999999) {
                        // write the state of the system at a specified time
                        SubmitQueue.add(new Job(systemStateTime));
                    }
                    if (!input.hasNext()) {

                        display = systemStateTime;

                        //----------------------------------------------------------
                        // poll out first job to be executed
                        Job job1 = SubmitQueue.poll();
                        currentTime = job1.getArrivingTime();
                        avbMemory = mainMemorySize - job1.getRequestedMemory();
                        avbDevices = devices - job1.getRequestedDevices();
                        quantum = job1.getburstTime();
                        exeJob = job1;
                        exeJob.setStartTime(currentTime);//start time of running on cpu
                        exeJob.setFinishTime(quantum + currentTime);//end time of running on cpu

                        //----------------------------------------------------------
                        // send the rest of jobs to cpu
                        while (jobNum != CompleteQueue.size()) {

                            if (SubmitQueue.isEmpty()) {
                                i = maximumValue; // if submit queue is empty then
                            } else {//if submit queue is not empty
                                i = SubmitQueue.peek().getArrivingTime(); //arrival time of next job
                            }

                            if (exeJob == null) {//if no current running process
                                e = maximumValue;
                            } else {
                                e = exeJob.getFinishTime();
                            }
                            //------------------------------------------------------
                            // update system time each iteration
                            currentTime = Math.min(i, e);
                            //------------------------------------------------------
                            // the system works acccording to i and e values
                            if (i < e) {
                                externalEvent(write);
                            } else if (i > e) {
                                internalEvent();
                            } else if (i == e) {
                                // when i == e
                                // perform internal events before external events
                                internalEvent();
                                externalEvent(write);
                            }
                        }
                        //----------------------------------------------------------
                        // write system final state & reset variables
                        if (display == 999999 || CompleteQueue.size() == jobNum) {

                            finalState(write);
                            CompleteQueue.clear();
                            HoldQueue1.clear();
                            HoldQueue2.clear();
                            ReadyQueue.clear();
                            SubmitQueue.clear();
                            exeJob = null;
                            startingTime = 0;
                            currentTime = 0;
                            quantum = 0;
                            jobNum = 0;
                            display = 0;
                            SR = 0;
                            AR = 0;
                        }
                    }

                }

            }
            input.close();
            write.close();
        }

    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//remove running process from CPU and add new job to CPU

    public static void internalEvent() throws FileNotFoundException {

        if (exeJob != null) {

            exeJob.setremainingTime(exeJob.getremainingTime() - quantum);
            // if job burst time is done

            if (exeJob.getremainingTime() <= 0) { //job is all done

                CPUrelease();
                //Check if we can alloate an processes from hold queues to ready queues
                toReadyQueue(null);

                // next job is sent to CPU
                if (!ReadyQueue.isEmpty()) {

                    exeJob = ReadyQueue.poll();

                    ComputeAvgBT();

                    quantum = exeJob.getremainingTime();

                    // set the job start time of execution
                    exeJob.setStartTime(currentTime);

                    // set the executing job finish time
                    int finish = Math.min(exeJob.getremainingTime(), quantum);

                    exeJob.setFinishTime(currentTime + finish);
                }
            } else if (exeJob.getremainingTime() > 0) {
                Dynamic_Round_Robin(exeJob);
            }
        }

    }
/////////////////////////////////////////////////////////////////////////////////////////////
// change SR and AR if jobs exit/ente ready queue

    public static void ComputeAvgBT() {
        if (!ReadyQueue.isEmpty()) {
            SR = 0;

            //Compute AvgBT in ReadyQueue
            for (Job job : ReadyQueue) {
                SR += job.getburstTime();
            }
            AR = SR / ReadyQueue.size();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //release mem and dev back to be free for other processes to use
    public static void CPUrelease() throws FileNotFoundException {
        // release memory and devices
        avbMemory = avbMemory + exeJob.getRequestedMemory();
        avbDevices = avbDevices + exeJob.getRequestedDevices();
        // add the finished job to complete queue 
        ReadyQueue.remove(exeJob);
        if (exeJob.getJobNumber() != -1) {
            CompleteQueue.add(exeJob);
        }
        // no jobs in CPU
        exeJob = null;

    }
    ////////////////////////////////////////////////////////////////////////////////////////////
//senfd job from hold queue to ready queue

    public static void toReadyQueue(Job j) {
        if (j == null) {
            if (!HoldQueue1.isEmpty()) {

                for (int y = 0; y < HoldQueue1.size(); y++) {

                    Job job = HoldQueue1.peek();
                    if (avbMemory >= job.getRequestedMemory() && avbDevices >= job.getRequestedDevices()) {

                        if (ReadyQueue.isEmpty()) {
                            AR = job.getburstTime(); //if ready queue is empty, the average burst time will be current process's
                        }

                        //put the process in ready queue
                        ReadyQueue.add(job);
                        HoldQueue1.remove(job);
                        avbMemory -= job.getRequestedMemory();
                        avbDevices -= job.getRequestedDevices();
                        ComputeAvgBT();
                    }
                }
            }

            if (!HoldQueue2.isEmpty()) {

                for (int y = 0; y < HoldQueue2.size(); y++) {
                    Job job = HoldQueue2.peek();
                    if (avbMemory >= job.getRequestedMemory() && avbDevices >= job.getRequestedDevices()) {

                        if (ReadyQueue.isEmpty()) {
                            AR = job.getburstTime(); //if queue 1 is empty, the average burst time will be current process's
                        }

                        //put the process in ready queue
                        ReadyQueue.add(job);
                        HoldQueue2.remove(job);
                        avbMemory -= job.getRequestedMemory();
                        avbDevices -= job.getRequestedDevices();
                        ComputeAvgBT();
                    }
                }
            }
        } else {//add specific job j to ready queue

            if (ReadyQueue.isEmpty()) {
                AR = j.getburstTime(); //if queue 1 is empty, the average burst time will be current process's
            }
            ReadyQueue.add(j);
            avbMemory -= j.getRequestedMemory();
            avbDevices -= j.getRequestedDevices();
            ComputeAvgBT();
        }

    }
////////////////////////////////////////////////////////////////////////////////////////////
//sort based on mem size

    public static void Sort_Queue1() {
        if (HoldQueue1.isEmpty()) {
            return;
        }
        //If not empty
        if (!HoldQueue1.isEmpty()) {

            // convert the queue to an array of objects to perform sorting
            Object[] array = HoldQueue1.toArray();
            Job temp;
            HoldQueue1.clear();
            for (int i = 0; i < array.length; i++) {
                for (int k = i + 1; k < array.length; k++) {
                    if (((Job) array[i]).getRequestedMemory() > ((Job) array[k]).getRequestedMemory()) {
                        temp = ((Job) array[i]);
                        array[i] = ((Job) array[k]);
                        array[k] = temp;
                    } else if (((Job) array[i]).getRequestedMemory() == ((Job) array[k]).getRequestedMemory()) {
                        if (((Job) array[i]).getArrivingTime() > ((Job) array[k]).getArrivingTime()) {
                            temp = ((Job) array[i]);
                            array[i] = ((Job) array[k]);
                            array[k] = temp;
                        }
                    }
                }
            }
            // return array to the holdqueue1
            for (Object array1 : array) {
                HoldQueue1.add((Job) array1);
            }
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////
//sort based on fifo
    public static void Sort_Queue2() {
        if (HoldQueue2.isEmpty()) {
            return;
        }
        //If not empty
        if (!HoldQueue2.isEmpty()) {

            // convert the queue to an array of objects to perform sorting
            Object[] array = HoldQueue2.toArray();
            Job temp;
            HoldQueue2.clear();
            for (int i = 0; i < array.length; i++) {
                for (int k = i + 1; k < array.length; k++) {
                    if (((Job) array[i]).getArrivingTime() > ((Job) array[k]).getArrivingTime()) {
                        temp = ((Job) array[i]);
                        array[i] = ((Job) array[k]);
                        array[k] = temp;
                    } else if (((Job) array[i]).getArrivingTime() == ((Job) array[k]).getArrivingTime()) {
                        if (((Job) array[i]).getRequestedMemory() > ((Job) array[k]).getRequestedMemory()) {
                            temp = ((Job) array[i]);
                            array[i] = ((Job) array[k]);
                            array[k] = temp;
                        }
                    }
                }
            }
            // return array to the holdqueue2
            for (Object array1 : array) {
                HoldQueue2.add((Job) array1);
            }
        }

    }

////////////////////////////////////////////////////////////////////////////////////////////
//execute dynamic round robin
    public static void Dynamic_Round_Robin(Job P) {
        if (ReadyQueue.isEmpty()) {
            // If there are no jobs in the ready queue
            // The current executing job takes its time to finish
            quantum = exeJob.getremainingTime();
            // Set the job start time of execution
            exeJob.setStartTime(currentTime);
            // Set the executing job finish time
            int finish = Math.min(exeJob.getremainingTime(), quantum);

            exeJob.setFinishTime(currentTime + finish);
        } else {
            // Executing job is sent to the ready queue
            ReadyQueue.add(exeJob);

            // Update SR & AR
            ComputeAvgBT();

            // Start executing the next job
            Job p = ReadyQueue.poll();

            ComputeAvgBT();
            if (AR > 0 && p.getremainingTime() > 0) {
                // Set the time quantum (TQ) for the next job
                quantum = Math.min(p.getremainingTime(), AR);
            }
            // Send the next job to the CPU for execution
            exeJob = p;

            // Set the job start time of execution
            exeJob.setStartTime(currentTime);

            // Set the executing job finish time
            int finish = Math.min(exeJob.getremainingTime(), quantum);

            exeJob.setFinishTime(currentTime + finish);

            if (exeJob.getremainingTime() > quantum) {
                // If the executing job is not terminated
                // Return it to the ready queue with its updated burst time
                ReadyQueue.add(exeJob);
                ComputeAvgBT();
            }
        }
    }
////////////////////////////////////////////////////////////////////////////////////////////
//send job from subit to hold queue/ready queue

    public static void externalEvent(PrintWriter write) throws FileNotFoundException {
        //get through SubmitQueue
        if (!SubmitQueue.isEmpty()) {

            Job job = SubmitQueue.poll();

            //case of "D" job
            if (job.getJobNumber() == -1) {
                //SubmitQueue.remove(job);
                currentState(write, job);
            }
            //case of "A" job
            if (job.getRequestedMemory() <= avbMemory && job.getRequestedDevices() <= avbDevices) {
                // if there were available main memory and devices 
                // the job is sent to ready queue
                toReadyQueue(job);
            } else {
                // if there were not available main memory and devices
                QueueingJobs(job);
            }

        } else {
            System.out.println("submit queue is empty");
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////
//check priority to queue each job
    public static void QueueingJobs(Job p) {

        if (p.getPriority() == 1) {
            HoldQueue1.add(p);
            Sort_Queue1();

        } else if (p.getPriority() == 2) {
            HoldQueue2.add(p);
            Sort_Queue2();
        }
    }

    //-------------------------------------------------------------------------
    public static void finalState(PrintWriter write) throws FileNotFoundException {
        write.println("<< Final state of system: ");
        write.println("  Current Available Main Memory = " + mainMemorySize);
        write.println("  Current Devices               = " + devices);
        write.println("\n  Completed jobs: \n" + "  ----------------");
        write.println("  Job ID   Burst Time    Arrival Time    Finish Time  Turnaround Time   Waiting Time");
        write.println("  =======================================================================");
        double avgTA = 0;

        Collections.sort(CompleteQueue, new sortbyID());
        int size = CompleteQueue.size();
        for (int j = 0; j < size; j++) {
            Job p = CompleteQueue.poll();
            write.printf("%5d%11d%12d%17d%16d%16d\n", p.getJobNumber(),p.getburstTime(), p.getArrivingTime(),
                    p.getFinishTime(), p.getTurnAT(), p.getWaitingTime());

            avgTA += p.getFinishTime() - p.getArrivingTime();
        }
        write.printf("\n\n  System Turnaround Time =  %.3f\n\n", avgTA / size);
        write.println("\n*********************************************************************\n");

    }

    //-------------------------------------------------------------------------
    public static void currentState(PrintWriter write, Job job) {
        write.println("\n<< At time " + job.getArrivingTime() + ":");
        write.println("  Current Available Main Memory = " + avbMemory);
        write.println("  Current Devices               = " + avbDevices);
        write.println("\n  Completed jobs: \n  ----------------");
        write.println("  Job ID   Burst Time   Arrival Time    Finish Time  Turnaround Time   Waiting Time \n"
                + "  ================================================================================");
        Collections.sort(CompleteQueue, new sortbyID());
        int size = CompleteQueue.size();
        for (int j = 0; j < size; j++) {
            Job p = CompleteQueue.poll();

            write.printf("%5d%9d%13d%17d%15d%15d\n", p.getJobNumber(), p.getburstTime(), p.getArrivingTime(),
                    p.getFinishTime(), p.getTurnAT(), p.getWaitingTime());
            CompleteQueue.add(p);

        }

        write.println();
        size = HoldQueue1.size();
        write.println("\n\n  Hold Queue 1: \n  ----------------");
        for (int j = 0; j < size; j++) {
            Job p = HoldQueue1.poll();
            write.printf("%6d", p.getJobNumber());
            HoldQueue1.add(p);
        }
        
        write.println();
        size = HoldQueue2.size();
        write.println("\n\n  Hold Queue2: \n  ----------------");
        for (int j = 0; j < size; j++) {
            Job p = HoldQueue2.poll();
            write.printf("%5d\n\n", p.getJobNumber());
            HoldQueue2.add(p);
        }
        
        write.println();
        size = ReadyQueue.size();
        write.println("\n\n (Ready Queue): \n  ----------------");
        for (int j = 0; j < size; j++) {
            Job p = ReadyQueue.poll();
            write.printf("%5d\t", p.getJobNumber());
            ReadyQueue.add(p);
        }
        
        write.println();
        write.println();
        /*
        write.println("  JobID    Remaining Time    Total Execution Time \n"
                + "  ===============================");
        for (int j = 0; j < size; j++) {
            Job p = ReadyQueue.poll();
            if (p.getJobNumber() != -1) {
                write.printf("%5d%10d%15d\n\n", p.getJobNumber(), p.getremainingTime(), (p.getburstTime() - p.getremainingTime()));
            }
            ReadyQueue.add(p);
        }
        */
        write.println("\n\n  Process running on the CPU: \n  ----------------------------");
        write.println("  Job ID   Run Time    Time Left");
        if (exeJob != null) {
            write.printf("%5d%10d%15d\n\n\n", exeJob.getJobNumber(), exeJob.getburstTime(), exeJob.getremainingTime());
        } else {
            write.println("No running processes");
        }

    }
    //-------------------------------------------------------------------------

    static class sortbyID implements Comparator<Job> {

        // Used for sorting jobs in ascending order
        @Override
        public int compare(Job a, Job b) {
            return (int) (a.getJobNumber() - b.getJobNumber());
        }
    }

}
