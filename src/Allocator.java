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
            Processors processors = new Processors();
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

    public void allocate(List<Task> taskList, List<List<List<Task>>> allocatedList) throws NoTimeLeft {

        // Pair.first = WCET and Pair.second = backup_WCET
//        List<Pair<Double>> utilizationRatio = new ArrayList<>();

        // Finding out the utilization ratio.
        for(Task task : taskList)
        {
            double period = task.period;
            double WCET = task.WCET;
            double backup_WCET = task.backup_WCET;
            double utilization_WCET = WCET / period;
            double utilization_backup_WCET = backup_WCET / period;
            task.utilization_WCET = utilization_WCET;
            task.utilization_backup_WCET = utilization_backup_WCET;
        }

        // Sorting based on utilization ratio
        Collections.sort(taskList,new Comparator<Task>(){
            @Override
            public int compare(Task o1, Task o2) {
                double compare = o1.utilization_WCET - o2.utilization_WCET;
                if(compare > 0)
                    return 1;
                else if (compare == 0)
                    return 0;
                else
                    return -1;
            }
        });

        for(Task temp : taskList)
        {
            System.out.println("Task Name : "+temp.taskName + " : "+ temp.utilization_WCET+ " : "+temp.utilization_backup_WCET);
        }

        System.out.println(taskList);

        // Assuming we've 4 processors and at start each processor have 1.0 usage available.
        List<Double> processors_available_usage = new ArrayList<>();

        for(int i = 0 ; i < 4; i++)
        {
            processors_available_usage.add(1.0);
        }

        // Allocating to processors using first fit.
        for(int k = 0 ; k < taskList.size() ; k++)
        {
            double utilization_WCET = taskList.get(k).utilization_WCET;
            double utilization_backup_WCET = taskList.get(k).utilization_backup_WCET;

            int inner_check = 0;
            int outer_check = 0;

            for(int i = 0 ; i < 4 ; i++)
            {
                if(processors_available_usage.get(i) >= utilization_WCET)
                {
                    outer_check = 1;
                    // Trying to allocate backup task
                    for(int j = 0 ; j < 4; j++)
                    {
                        // Primary and backup task should not be allocated to the same processors.
                        if(i!=j)
                        {
                            if(processors_available_usage.get(j)>=utilization_backup_WCET)
                            {
                                inner_check = 1;

                                double outer_processor = processors_available_usage.get(i) - utilization_WCET;
                                double inner_processor = processors_available_usage.get(j) - utilization_backup_WCET;

                                processors_available_usage.set(i,outer_processor);
                                processors_available_usage.set(j,inner_processor);

                                List<List<Task>> outer_processor_taskList = allocatedList.get(i);
                                List<List<Task>> inner_processor_taskList = allocatedList.get(j);

                                outer_processor_taskList.get(0).add(taskList.get(k)); // adding to primary task list.
                                inner_processor_taskList.get(1).add(taskList.get(k)); // adding to backup task list.

                                break;
                            }
                        }
                    }

                    if(inner_check != 1)
                        throw new NoTimeLeft("Not able to allocate backup_task");
                    else
                    {
                        inner_check = 0;
                        break;
                    }
                }
            }

            if(outer_check != 1)
                throw new NoTimeLeft("Not able to allocate primary_task");
            else
                outer_check = 0;
        }


        System.out.println(processors_available_usage);
    }
}
