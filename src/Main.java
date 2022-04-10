import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;

public class Main {
    static final int multiple = (int) Math.pow(10,3);
    static int count = 0;
    List<Task> taskList = new ArrayList<>();
    static BlockingQueue<Jobs> blockingQueue = new LinkedBlockingDeque<>();
    static final Object object = new Object();
    static CountDownLatch latch;

    public static void main(String[] args) throws InterruptedException, FileNotFoundException, NoTimeLeft {

        // creating processors;
//        Thread allocator = new Thread(new Allocator());


        System.out.println("Allocator has been created");

        // reading tasks from json , and storing them in the list.
        File file = new File("C:\\Users\\Shashank Pal\\Dropbox\\IntelliJ Projects\\MixedCriticality\\src\\tasks.json");
        FileReader fileReader = new FileReader(file);
        Gson gson = new Gson();
        List<Task> list= Arrays.asList(gson.fromJson(fileReader,Task[].class));

        double hyperPeriod = findHyperPeriod(list); // calculating hyperPeriod.

        System.out.println("Tasks has been read from json and the list is : ");
        Collections.sort(list);
        System.out.println(list);
        System.out.println("HyperPeriod : "+hyperPeriod);

        // Designing a DS to store tasks that are allcoated to processors.
//        List<List<List<Task>>> allocatedList = new ArrayList<>();
//        List<List<Task>> processor1 = new ArrayList<>();
//        List<List<Task>> processor2 = new ArrayList<>();
//        List<List<Task>> processor3 = new ArrayList<>();
//        List<List<Task>> processor4 = new ArrayList<>();
//        allocatedList.add(processor1);
//        allocatedList.add(processor2);
//        allocatedList.add(processor3);
//        allocatedList.add(processor4);
//
//        for(int i = 0 ; i < 4 ; i ++)
//        {
//            List<Task> primary_task = new ArrayList<>();
//            List<Task> backup_task = new ArrayList<>();
//            allocatedList.get(i).add(primary_task);
//            allocatedList.get(i).add(backup_task);
//        }
//
//
//        // Call Allocator
//        Allocator allocator = new Allocator();
//        allocator.allocate(list,allocatedList);
//
//        for(int i = 0 ; i < 4 ; i++)
//        {
//            System.out.println("Processor : "+ (i+1));
//            System.out.println("Active Task List : "+allocatedList.get(i).get(0));
//            System.out.println("Backup Task List : "+allocatedList.get(i).get(1));
//            System.out.println("#############");
//        }
//        System.out.println(allocatedList);


        // Designing a general DS which will store tasks and their backup tasks allocated to which processors.

        List<List<Map<String,Integer>>> allocatedList = new ArrayList<>();

        Allocator allocator = new Allocator();
        allocator.allocate(list,allocatedList);

        for(int i = 0 ; i < allocatedList.size() ; i++)
        {
            System.out.println("Processor : "+ (i+1));
            System.out.println("Active Task List : "+allocatedList.get(i).get(0));
            System.out.println("Backup Task List : "+allocatedList.get(i).get(1));
            System.out.println("#############");
        }


        // Allocating criticality_distributed

        List<Processor> criticality_allocated_list = new ArrayList<>();
        allocator.criticality_Distributed(criticality_allocated_list,list);

        System.out.println("With Criticality Distributed");
        for(int i = 0 ; i < criticality_allocated_list.size() ; i++)
        {
            System.out.println("Processor : "+ (i+1));
            System.out.println("Active Task List : "+criticality_allocated_list.get(i).active_map);
            System.out.println("Backup Task List : "+criticality_allocated_list.get(i).backup_map);
            System.out.println("#############");
        }

//                System.out.println(allocatedList);

//        latch = new CountDownLatch(1);
//
//        allocator.start();
//        latch.await();
//
//        System.out.println("Allocator has been started");
//
//            for (Task temp : list) {
//                System.out.println("Inside task loop");
//                Thread temp1 = new Thread(new Jobs(temp.period, temp.deadline, temp.WCET, temp.backup_WCET, temp.taskName));
//                temp1.start();
//            }


    }

    private static double findHyperPeriod(List<Task> list) {
        int tempLCM = lcm((int)list.get(0).period,(int)list.get(1).period);
        for(int i = 2; i < list.size();i++)
        {
            tempLCM = lcm(tempLCM,(int)list.get(i).period);
        }

        return tempLCM;
    }

    static int gcd(int a, int b)
    {
        if (a == 0)
            return b;
        return gcd(b % a, a);
    }

    // method to return LCM of two numbers
    static int lcm(int a, int b)
    {
        return (a / gcd(a, b)) * b;
    }
}
