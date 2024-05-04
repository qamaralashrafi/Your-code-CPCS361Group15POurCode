
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
 */


/**
 *
 * @author Qamar
 */
public class part1 {
    /**
     * @param args the command line arguments
     */
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
    public static int AvgBT;
    public static PrintWriter write;

    public static void main(String[] args) throws FileNotFoundException {
        {
        File file = new File("input.txt");
        Scanner input = new Scanner(file);
        write = new PrintWriter("output.txt");
        String line;
        String[] command;
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
                // read "A" add jobs
            } else if (command[0].equals("A")) {                
                int arrivingTime = Integer.parseInt(command[1]);
                int jobNo = Integer.parseInt(command[2]);
                int requestedMM = Integer.parseInt(command[3]);
                int requestedDevices = Integer.parseInt(command[4]);
                int burstTime = Integer.parseInt(command[5]);
                int JobPriority = Integer.parseInt(command[6]);
                // create process for all valid jobs then add them to SubmitQueue
                if (requestedMM <= mainMemorySize && requestedDevices <= devices) {
                    SubmitQueue.add(new Job(arrivingTime, jobNo, requestedMM, requestedDevices, burstTime, JobPriority));
                    jobNum++; //to count the number of job entered to the queue
                }
                // read "D" display job
            } else if (command[0].equals("D")) {
                int systemStateTime = Integer.parseInt(command[1]);
                if (systemStateTime != 999999) {
                    // write the state of the system at a specified time
                    Job displayJob= new Job(systemStateTime);                   
                    SubmitQueue.add(displayJob);                    
                    displayJob.setJobNumber(-1);                   
                } else {                    
                        System.out.print("hello first");
                        System.out.print(systemStateTime);
                        Job displayJob= new Job(systemStateTime);                    
                        SubmitQueue.add(displayJob);                    
                        displayJob.setJobNumber(999999);}
                        //----------------------------------------------------------
                        // poll out first job to be executed                        
                        Dynamic_Round_Robin(SubmitQueue.poll());
                        //----------------------------------------------------------
                        // send the rest of jobs to cpu      
                    //while (jobNum >= CompleteQueue.size()) {
                    while (jobNum >= CompleteQueue.size()) {
                        System.out.println(CompleteQueue.size());
                        System.out.println(jobNum);
                        
                        if (SubmitQueue.isEmpty()) {
                            e = maximumValue; // if submet queue is empty then i=infinite
                        } else {
                            e = SubmitQueue.peek().getArrivingTime();
                        }
                        // set e value to perform external events
                        if (exeJob == null) {
                            i = maximumValue; //no running job, e= infinite
                        } else {
                            i = exeJob.getFinishTime();
                        }
                        System.out.println(i + "" + e);
                        //------------------------------------------------------
                        // update system time each iteration
                        currentTime = Math.min(i, e);
                        //------------------------------------------------------
                        // the system works acccording to i and e values
                        if (i > e) { //finish time of running process bigger than start time of next process
                            externalEvent(write); //put proccess in one of the hold queues
                        } else if (i < e) {//start time of next process bigger than finish time of running process
                            internalEvent(); //run process
                        } else if (i==e){
                            // when i == e
                            // perform internal events before external events
                            internalEvent();
                            externalEvent(write);
                        } }
                    // write system final state& reset variables
//                if ( display == 999999 && CompleteQueue.size() == jobNum) {
                    if (CompleteQueue.size() == jobNum) {
                        finalState(write);
                        CompleteQueue.clear();
                        ReadyQueue.clear();
                        HoldQueue1.clear();
                        HoldQueue2.clear();
                        SubmitQueue.clear();
                        exeJob = null;
                        startingTime = 0;
                        currentTime = 0;
                        quantum = 0;
                        jobNum = 0;
                        display = 0;
                        SR = 0;
                        AR = 0;
                    } }}
        input.close();
        write.close(); }}
    

       
    public static void internalEvent() throws FileNotFoundException {
    System.out.println("hello internal");
    System.out.println("Current exeJob: " + exeJob.toString());
        // CPU EXECUTION
        if(exeJob!=null){

        if (exeJob.getremainingTime() < quantum) {
            exeJob.setremainingTime(0); // Job finishes, set remaining time to 0
        } else {
            exeJob.setremainingTime(exeJob.getremainingTime() - quantum);
        }
        // if job burst time is done

            if (exeJob.getremainingTime() == 0) {

                CPUrelease();
                // next job is sent to CPU
                toReadyQueue(null);

                if(!ReadyQueue.isEmpty()){
                    exeJob = ReadyQueue.poll();
                    // set quantum time
                    quantum = Math.min(exeJob.getremainingTime(), AR);
                    // set the job start time of execution
                    exeJob.setStartTime(currentTime);
                    // set the executing job finish time
                    int finish = Math.min(exeJob.getremainingTime(), quantum);
                    exeJob.setFinishTime(currentTime + finish);
                    // update SR& AR
                    SRAR();
               //Dynamic_Round_Robin(exeJob);
            }else//if ready queue is empty 
                externalEvent(write);
            }else if(exeJob.getremainingTime() > 0){
                Dynamic_Round_Robin(exeJob);
            //}externalEvent(write);
        }else {
            
            // if the job is not finished, it is sent to hold ready queue
            //externalEvent(write);
            //Dynamic_Round_Robin(exeJob);
            externalEvent(write);
        }
    }
        for (Job p : ReadyQueue) {
            //To set the waiting time
            if(p!=null){
            p.setWaitingTime(currentTime - p.getArrivingTime());
            //p.setWaitingTime(p.getTurnAT() - p.getburstTime());
            }
        }
    }
    public static void ComputeAvgBT() {
    System.out.println("hello avgbt");
        if (!ReadyQueue.isEmpty()) {
            int sumBT = 0;

            //Compute AvgBT in Q1
            for (Job job : ReadyQueue) {
                sumBT += job.getburstTime();
            }
            AvgBT = sumBT / ReadyQueue.size();
        }
    }

    //-------------------------------------------------------------------------
    public static void CPUrelease() {
        System.out.println("hello release");
        // release memory and devices
        avbMemory = avbMemory + exeJob.getRequestedMemory();
        avbDevices = avbDevices + exeJob.getRequestedDevices();
        // add the finished job to complete queue 
        System.out.println("Current exeJob: " + exeJob);
        ReadyQueue.remove(exeJob);
        CompleteQueue.add(exeJob);
        // no jobs in CPU
        exeJob = null;
        

    }
    //-------------------------------------------------------------------------

    public static void toReadyQueue(Job j) {
        System.out.println("hello ready");
        if(j==null){
        //double AvgBT;
        
        if (!HoldQueue1.isEmpty()) {

            for (Job p : HoldQueue1) {
                //To set the waiting time
                p.setWaitingTime(currentTime - p.getArrivingTime());
                //p.setWaitingTime(p.getTurnAT() - p.getburstTime());
            }
            
            ComputeAvgBT();
            for (int y = 0; y < HoldQueue1.size(); y++) {
                
                Job job = HoldQueue1.peek();
                if (avbMemory >= (job).getRequestedMemory() && avbDevices >= (job).getRequestedDevices()) {
                    
                    ComputeAvgBT();
                    if (ReadyQueue.isEmpty()) {
                        AvgBT = job.getburstTime(); //if ready queue is empty, the average burst time will be current process's
                    }

                        //put the process in ready queue
                        ReadyQueue.add(job);
                        
                        HoldQueue1.remove(job);
                        avbMemory -= job.getRequestedMemory();
                        avbDevices -= job.getRequestedDevices();
                        ComputeAvgBT();
                        SRAR();
                }
            }
        }
                
        if (!HoldQueue2.isEmpty()) {
            
            for (Job p : HoldQueue2) {
               //To set the waiting time
               p.setWaitingTime(currentTime - p.getArrivingTime());
               //p.setWaitingTime(p.getTurnAT() - p.getburstTime());
            }
            
            for (int y = 0; y < HoldQueue2.size(); y++) {
                Job job = HoldQueue2.peek();
                if (avbMemory >= (job).getRequestedMemory() && avbDevices >= (job).getRequestedDevices()) {
                    
                    ComputeAvgBT();
                    if (ReadyQueue.isEmpty()) {
                        AvgBT = job.getburstTime(); //if queue 1 is empty, the average burst time will be current process's
                    }

                        //put the process in ready queue
                        ReadyQueue.add(job);
                        HoldQueue2.remove(job);
                        avbMemory -= job.getRequestedMemory();
                        avbDevices -= job.getRequestedDevices();
                        ComputeAvgBT();
                        SRAR();
                }
            }
        }}else{//add job j to ready queue
            ComputeAvgBT();
            if (ReadyQueue.isEmpty()) {
                AvgBT = j.getburstTime(); //if queue 1 is empty, the average burst time will be current process's
            }
            ReadyQueue.add(j);
            avbMemory -= j.getRequestedMemory();
            avbDevices -= j.getRequestedDevices();
            SRAR();
        }
    }

    public static void Sort_Queue1() {
        System.out.println("hello sort1");
        if (HoldQueue1.isEmpty()) {
            return;
        }
        //If not empty
        if (!HoldQueue1.isEmpty()) {

            // sort based on dynamic priority
            // convert the queue to an array of objects to perform sorting
            Object[] array = HoldQueue1.toArray();
            Job temp;
            HoldQueue1.clear();
            for (int i = 0; i < array.length; i++) {
                for (int k = i + 1; k < array.length; k++) {
                    if (((Job) array[i]).getRequestedMemory() < ((Job) array[k]).getRequestedMemory()) {
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


//            HoldQueue1.add(HoldQueue1.poll());
//            ComputeAvgBT();


        }

    }
    
        public static void Sort_Queue2() {
            System.out.println("hello sort2");
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
            // return array to the holdqueue1
            for (Object array1 : array) {
                HoldQueue2.add((Job) array1);
            }

//            // move from Q2 to holdqueue1 
//            HoldQueue2.add(HoldQueue2.poll());
//            ComputeAvgBT();

        }

    }

    //--------------------------------------------------------------------------
    public static void Dynamic_Round_Robin(Job P) {
    System.out.println("hello Dynamic");


    
    if (ReadyQueue.isEmpty()) {
        if(P.getJobNumber()==1){
        // set system time = first job arraival time
        currentTime = P.getArrivingTime();
        // allocate memory& devices to the job
        // available memory = system main memory - first job requested memory
        avbMemory = mainMemorySize - P.getRequestedMemory();
        // available devices = system serial devices - first job requested devices
        avbDevices = devices - P.getRequestedDevices();
        // set quantum = first job burst time
        quantum = P.getburstTime();
        // send first job to CPU
        exeJob = P;
        // set the job start time of execution
        exeJob.setStartTime(currentTime);
        // set the executing job finish time
        int finish = Math.min(exeJob.getremainingTime(), quantum);
        exeJob.setFinishTime(quantum + currentTime);
        }else{
        // If there are no jobs in the ready queue
        // The current executing job takes its time to finish
        quantum = exeJob.getremainingTime();
        System.out.print("quantum 1"+quantum );
        // Set the job start time of execution
        exeJob.setStartTime(currentTime);
        // Set the executing job finish time
        int finish = Math.min(exeJob.getremainingTime(), quantum);
        exeJob.setFinishTime(currentTime + finish);
        // Update SR & AR
        SRAR();}
    } else {
        // Executing job is sent to the ready queue
        ReadyQueue.add(exeJob);
        ComputeAvgBT();
        // Update SR & AR
        SRAR();
        // Start executing the next job
        Job p = ReadyQueue.poll();
        // Set the time quantum (TQ) for the next job
        quantum = Math.min(p.getremainingTime(), AR);
        System.out.print("quantum 2"+quantum);
        // Send the next job to the CPU for execution
        exeJob = p;
        // Set the job start time of execution
        exeJob.setStartTime(currentTime);
        // Set the executing job finish time
        int finish = Math.min(exeJob.getremainingTime(), quantum);
        exeJob.setFinishTime(currentTime + finish);
        // Update SR & AR
        SRAR();

        if (exeJob.getremainingTime() > quantum) {
            // If the executing job is not terminated
            // Return it to the ready queue with its updated burst time
            ReadyQueue.add(exeJob);
            // Update SR & AR
            SRAR();
        }
    }
}

    public static void externalEvent(PrintWriter write) throws FileNotFoundException {
        System.out.println("hello external");
        //get through SubmitQueue
        if (!SubmitQueue.isEmpty()) {
            System.out.println("into external");
            Job job = SubmitQueue.poll();
            
            if (job.getRequestedMemory() > mainMemorySize || job.getRequestedDevices() > devices) {
            // If job's resource requirements exceed system capacity, reject the job
            write.println("Job " + job.getJobNumber() + " has been rejected due to insufficient resources.");
            return;
            }
            
            //case of "D" job
            if (job.getJobNumber() == -1) {
                currentState(write, job);
                
            } else if(job.getJobNumber()==999999){
                finalState(write);}
            //case of "A" job
            if (job.getRequestedMemory() <= avbMemory && job.getRequestedDevices() <= avbDevices) {
                // if there were available main memory and devices 
                // the job is sent to ready queue
                toReadyQueue(job);
            } else {
                // if there were not available main memory and devices
                // the job is sent to hold queue 2 (waiting queue)
                QueueingJobs(job);
                SubmitQueue.remove(job);
                
                // save the entred time
                
            }

        }else{
            System.out.println("submit queue is empty");
        }
    }

    //-------------------------------------------------------------------------
    public static void QueueingJobs(Job p) {
         System.out.println("hello queueing");
        int burstTime = p.getburstTime();

        if (ReadyQueue.isEmpty()) {
            AvgBT = burstTime;

        } else {
            ComputeAvgBT();
        }
        

        switch (p.getPriority()) {
            case 1 -> {
                HoldQueue1.add(p);
                Sort_Queue1();
            }
            case 2 -> {
                HoldQueue2.add(p);
                Sort_Queue2();
            }
            default -> {
                    ReadyQueue.add(p);
                    avbMemory -= p.getRequestedMemory();
                    avbDevices -= p.getRequestedDevices();
                    ComputeAvgBT();
                    SRAR();
            }
        }

    }

    //-------------------------------------------------------------------------
    public static void SRAR() {
         System.out.println("hello SRAR");
        //AR = to store remaining average of burst times
        //SR = to store sum of burst times
        if (ReadyQueue.isEmpty()) {
            AR = 0;
        } else {
            SR = 0;
            for (Job p : ReadyQueue) {
                SR += (p.getremainingTime());
            }
            AR = SR / ReadyQueue.size();
        }
    }
    //-------------------------------------------------------------------------
    public static void finalState(PrintWriter write) throws FileNotFoundException {
        System.out.print("hello final");
        write.println("<< Final state of system: ");
        write.println("  Current Available Main Memory = " + mainMemorySize);
        write.println("  Current Devices               = " + devices);
        write.println("\n  Completed jobs: \n" + "  ----------------");
        write.println("  Job ID   Arrival Time    Finish Time  Turnaround Time   Waiting Time");
        write.println("  =================================================================");
        double avgTA = 0;
        
        Collections.sort(CompleteQueue, new sortbyID());
        int size = CompleteQueue.size();
        for (int j = 0; j < size; j++) {
            Job p = CompleteQueue.poll();
            
            p.setTurnAT(p.getArrivingTime()-p.getFinishTime());
            
            p.setWaitingTime(p.getTurnAT()- p.getburstTime())  ;

            write.printf("%5d%11d%17d%25d%35f\n", p.getJobNumber(), p.getArrivingTime(),
                    p.getFinishTime(), p.getTurnAT(), p.getWaitingTime());

            avgTA += p.getFinishTime() - p.getArrivingTime();
        }
        write.printf("\n\n  System Turnaround Time =  %.3f\n\n", avgTA / size);
        write.println("\n*********************************************************************\n");

    }

    //-------------------------------------------------------------------------
    public static void currentState(PrintWriter write, Job job) {
        System.out.print("hello current");
        write.println("\n<< At time " + job.getArrivingTime() + ":");
        write.println("  Current Available Main Memory = " + avbMemory);
        write.println("  Current Devices               = " + avbDevices);
        write.println("\n  Completed jobs: \n  ----------------");
        write.println("  Job ID   Burst Time   Arrival Time    Finish Time  Turnaround Time   Waiting Time \n"
                + "  =================================================================");
        Collections.sort(CompleteQueue, new sortbyID());
        int size = CompleteQueue.size();
        for (int j = 0; j < size; j++) {
            Job p = CompleteQueue.poll();
            p.setTurnAT(p.getArrivingTime()-p.getFinishTime());
            write.printf("%5d%9d%11d%17d%15d%11f\n", p.getJobNumber(),p.getburstTime(), p.getArrivingTime(),
                    p.getFinishTime(), p.getTurnAT(),p.getWaitingTime());
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
        write.println("  JobID    Remaining Time    Total Execution Time \n"
                + "  ===============================");
        for (int j = 0; j < size; j++) {
            Job p = ReadyQueue.poll();
            write.printf("%5d%10d%15d\n\n", p.getJobNumber(), p.getremainingTime(), (p.getburstTime() - p.getremainingTime()));
            ReadyQueue.add(p);
        }
        write.println("\n\n  Process running on the CPU: \n  ----------------------------");
        write.println("  Job ID   Run Time    Time Left");
        write.printf("%5d%10d%15d\n\n\n", exeJob.getJobNumber(), exeJob.getburstTime(), exeJob.getremainingTime());

       
       
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
