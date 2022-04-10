import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Allocator implements Runnable{
    List<Node> processorsList;

    public Allocator(){
        processorsList = new ArrayList<>();
    }

    static final int multiple = (int) Math.pow(10,3);
    static CountDownLatch cLatch = new CountDownLatch(4);

    @Override
    public void run() {
        synchronized (Main.object) {
            Processor processors = new Processor();
            for (int i = 0; i < 4; i++) {
                processors.generateProcessors((long) (Math.random() * multiple), processorsList);
            }
            try {
                cLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//        processors.stopProcessors();
            System.out.println("Allocator : Process creation has been finished");

            System.out.println("Allocator : Moving onto allocation part");
            while (true) {
                System.out.println("I'm in while loop");
                Jobs temp = null;
                try {
//                    Main.object.wait();
                    Main.latch.countDown();
                    temp = Main.blockingQueue.take();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    canAllocate(temp);
                } catch (NoTimeLeft e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void canAllocate(Jobs temp) throws NoTimeLeft {
        System.out.println("IM IN CAN ALLOCATE");
        int innerFlag = 0;
        int flag = 0;
        System.out.println(temp.WCET);
        for(Node node : processorsList)
        {
            if(node.time >= temp.WCET)
            {
                flag = 1;
                for(Node innerNode : processorsList)
                {
                    if(node!=innerNode) {
                        if (innerNode.time >= temp.backup_WCET) {
                            node.time = node.time - temp.WCET;
                            innerNode.time = innerNode.time - temp.backup_WCET;
                            System.out.println(node.time +" : "+ innerNode.time);
                            innerFlag = 1;
                            break;
                        }
                    }
                }
                if(innerFlag != 1)
                    throw new NoTimeLeft("No processor has an adequate amount of time left");
                break;
            }
        }
        if(flag != 1)
            throw new NoTimeLeft("No processor has an adequate amount of time left");
    }

    public void allocate(List<Task> taskList, List<List<Map<String,Integer>>> allocatedList) throws NoTimeLeft {

        // calculate utilization ratio of each primary WCET and list of backup_WCET

        for(Task task : taskList)
        {
            double period = task.period;
            double WCET = task.WCET;
            List<Double> backup_WCET = task.backup_WCET;
            double utilization_WCET = WCET / period;

            List<Double> utilization_backup_WCET = new ArrayList<>();

            for(Double backup : backup_WCET)
            {
                double temp = backup / period;
                utilization_backup_WCET.add(temp);
            }

            task.utilization_backup_WCET = utilization_backup_WCET;
            task.utilization_WCET = utilization_WCET;
        }

        for(int i = 0 ; i < taskList.size();i++)
        {
            System.out.println("Task Name : "+taskList.get(i).taskName+" Primary Task Utilization : "+taskList.get(i).utilization_WCET);
            for(Double temp : taskList.get(i).utilization_backup_WCET)
            {
                System.out.print(temp+ " ");
            }
            System.out.println();
        }

        List<Double> processors_available_usage = new ArrayList<>();

        // Allocation starts
        for(Task task : taskList)
        {
            // First will try to allocate primary task
            tryToAllocate(allocatedList,processors_available_usage,1,task.utilization_WCET,task,0);

            // Second will try to allocate all the backup task of the current task
            List<Double> backup_task = task.utilization_backup_WCET;
            for(int i = 0 ; i < backup_task.size();i++) {
                tryToAllocate(allocatedList, processors_available_usage, 0, backup_task.get(i), task, i);
            }
        }

        System.out.println("Processor usage available : ");
        System.out.println(processors_available_usage);

    }

    private void tryToAllocate(List<List<Map<String,Integer>>> allocatedList, List<Double> processors_available_usage, int primary,double utilization_ratio, Task task, int backup_task_number){

        int check = 0;
        for(int i = 0 ; i < processors_available_usage.size();i++)
        {
            if(primary == 1) {
                if (processors_available_usage.get(i) >= utilization_ratio) {
                    check = 1;
                    List<Map<String, Integer>> temp = allocatedList.get(i);
                    temp.get(0).put(task.taskName, -1);
                    double temp1 = processors_available_usage.get(i) - utilization_ratio;
                    processors_available_usage.set(i,temp1);
                    break;
                }
            }
            else
            {
                if(processors_available_usage.get(i)>= utilization_ratio)
                {
                    List<Map<String, Integer>> temp = allocatedList.get(i);
                    if(!temp.get(1).containsKey(task.taskName) && !temp.get(0).containsKey(task.taskName))
                    {
                        check = 1;
                        temp.get(1).put(task.taskName, backup_task_number);
                        double temp1 = processors_available_usage.get(i) - utilization_ratio;
                        processors_available_usage.set(i,temp1);
                        break;
                    }
                }
            }
        }

        if(check == 0) // means that the current task was not allocated to any processor. Generate a new processor
        {
            Map<String,Integer> primary_task = new HashMap<>();
            Map<String,Integer> backup_task = new HashMap<>();

            List<Map<String,Integer>> temp = new ArrayList<>();


            if(primary == 1)
            {
                primary_task.put(task.taskName,-1);
                double temp1 = 1.0 - utilization_ratio;
                processors_available_usage.add(temp1);
            }
            else
            {
                backup_task.put(task.taskName, backup_task_number);
                double temp1 = 1.0 - utilization_ratio;
                processors_available_usage.add(temp1);
            }

            temp.add(primary_task);
            temp.add(backup_task);

            allocatedList.add(temp);
        }

        }

    public void criticality_Distributed(List<Processor> processors_list,List<Task> taskList)
    {
        // Have already calculated utilization before, so need to calculate it again

        List<Double> processors_available_usage = new ArrayList<>();

        taskList.sort(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return o2.criticality_level - o1.criticality_level;
            }
        });

        System.out.println(taskList);
        // Using Min_Heap to get the processor with the lowest criticality_number
        PriorityQueue<Processor> pq = new PriorityQueue<>(new Comparator<Processor>() {
            @Override
            public int compare(Processor o1, Processor o2) {
                int compare = o1.criticality_number - o2.criticality_number;
                return compare;
            }
        });

        // Allocation starts
        for(Task task : taskList)
        {
            // First will try to allocate primary task
            try_to_allocate_criticality_distributed(processors_list,pq,1,task.utilization_WCET,task,0);

            // Second will try to allocate all the backup task of the current task
            List<Double> backup_task = task.utilization_backup_WCET;
            for(int i = 0 ; i < backup_task.size();i++) {
                try_to_allocate_criticality_distributed(processors_list, pq, 0, backup_task.get(i), task, i);
            }
        }


    }

    private void try_to_allocate_criticality_distributed(List<Processor> processors_list,PriorityQueue<Processor> pq,int primary,double utilization_ratio, Task task, int backup_task_number)
    {
        int check = 0;

        if(pq.size()!=0)
        {
            List<Processor> temp = new ArrayList<>();
            while(pq.size()!=0)
            {
                Processor temp1 = pq.remove();
                temp.add(temp1);
                if(temp1.available_usage >= utilization_ratio)
                {
                    if(!(temp1.active_map.containsKey(task.taskName)) && !(temp1.backup_map.containsKey(task.taskName)))
                    {
                        check = 1;
                        if(primary == 1)
                        {
                            temp1.active_map.put(task.taskName,-1);
                            temp1.available_usage = temp1.available_usage - utilization_ratio;
                            temp1.criticality_number += task.criticality_level;
                            break;
                        }
                        else
                        {
                            check = 1;
                            temp1.backup_map.put(task.taskName, backup_task_number);
                            temp1.criticality_number += task.criticality_level;
                            temp1.available_usage = temp1.available_usage - utilization_ratio;
                            break;
                        }
                    }
                }
            }

            pq.addAll(temp);
        }


        if(check == 0)
        {
            Processor processor = new Processor();

            if(primary == 1)
            {
                processor.active_map.put(task.taskName,-1);
                processor.criticality_number += task.criticality_level;
                pq.add(processor);
                processor.available_usage = processor.available_usage - utilization_ratio;
            }
            else
            {
                processor.backup_map.put(task.taskName, backup_task_number);
                processor.criticality_number += task.criticality_level;
                processor.available_usage = processor.available_usage - utilization_ratio;
                pq.add(processor);
            }

            processors_list.add(processor);
        }
    }

}

//    public void allocate(List<Task> taskList, List<List<List<Task>>> allocatedList) throws NoTimeLeft {
//
//        // Pair.first = WCET and Pair.second = backup_WCET
////        List<Pair<Double>> utilizationRatio = new ArrayList<>();
//
//        // Finding out the utilization ratio.
//        for(Task task : taskList)
//        {
//            double period = task.period;
//            double WCET = task.WCET;
//            double backup_WCET = task.backup_WCET;
//            double utilization_WCET = WCET / period;
//            double utilization_backup_WCET = backup_WCET / period;
//            task.utilization_WCET = utilization_WCET;
//            task.utilization_backup_WCET = utilization_backup_WCET;
//        }
//
//        // Sorting based on utilization ratio
//        Collections.sort(taskList,new Comparator<Task>(){
//            @Override
//            public int compare(Task o1, Task o2) {
//                double compare = o1.utilization_WCET - o2.utilization_WCET;
//                if(compare > 0)
//                    return 1;
//                else if (compare == 0)
//                    return 0;
//                else
//                    return -1;
//            }
//        });
//
//        for(Task temp : taskList)
//        {
//            System.out.println("Task Name : "+temp.taskName + " : "+ temp.utilization_WCET+ " : "+temp.utilization_backup_WCET);
//        }
//
//        System.out.println(taskList);
//
//        // Assuming we've 4 processors and at start each processor have 1.0 usage available.
//        List<Double> processors_available_usage = new ArrayList<>();
//
//        for(int i = 0 ; i < 4; i++)
//        {
//            processors_available_usage.add(1.0);
//        }
//
//        // Allocating to processors using first fit.
//        for(int k = 0 ; k < taskList.size() ; k++)
//        {
//            double utilization_WCET = taskList.get(k).utilization_WCET;
//            double utilization_backup_WCET = taskList.get(k).utilization_backup_WCET;
//
//            int inner_check = 0;
//            int outer_check = 0;
//
//            for(int i = 0 ; i < 4 ; i++)
//            {
//                if(processors_available_usage.get(i) >= utilization_WCET)
//                {
//                    outer_check = 1;
//                    // Trying to allocate backup task
//                    for(int j = 0 ; j < 4; j++)
//                    {
//                        // Primary and backup task should not be allocated to the same processors.
//                        if(i!=j)
//                        {
//                            if(processors_available_usage.get(j)>=utilization_backup_WCET)
//                            {
//                                inner_check = 1;
//
//                                double outer_processor = processors_available_usage.get(i) - utilization_WCET;
//                                double inner_processor = processors_available_usage.get(j) - utilization_backup_WCET;
//
//                                processors_available_usage.set(i,outer_processor);
//                                processors_available_usage.set(j,inner_processor);
//
//                                List<List<Task>> outer_processor_taskList = allocatedList.get(i);
//                                List<List<Task>> inner_processor_taskList = allocatedList.get(j);
//
//                                outer_processor_taskList.get(0).add(taskList.get(k)); // adding to primary task list.
//                                inner_processor_taskList.get(1).add(taskList.get(k)); // adding to backup task list.
//
//                                break;
//                            }
//                        }
//                    }
//
//                    if(inner_check != 1)
//                        throw new NoTimeLeft("Not able to allocate backup_task");
//                    else
//                    {
//                        inner_check = 0;
//                        break;
//                    }
//                }
//            }
//
//            if(outer_check != 1)
//                throw new NoTimeLeft("Not able to allocate primary_task");
//            else
//                outer_check = 0;
//        }
//
//
//        System.out.println(processors_available_usage);
//    }
//}
