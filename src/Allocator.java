import java.util.*;
import java.util.concurrent.CountDownLatch;

public class Allocator implements Runnable {
    List<Node> processorsList;

    public Allocator() {
        processorsList = new ArrayList<>();
    }

    static final int multiple = (int) Math.pow(10, 3);
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
        for (Node node : processorsList) {
            if (node.time >= temp.WCET) {
                flag = 1;
                for (Node innerNode : processorsList) {
                    if (node != innerNode) {
                        if (innerNode.time >= temp.backup_WCET) {
                            node.time = node.time - temp.WCET;
                            innerNode.time = innerNode.time - temp.backup_WCET;
                            System.out.println(node.time + " : " + innerNode.time);
                            innerFlag = 1;
                            break;
                        }
                    }
                }
                if (innerFlag != 1)
                    throw new NoTimeLeft("No processor has an adequate amount of time left");
                break;
            }
        }
        if (flag != 1)
            throw new NoTimeLeft("No processor has an adequate amount of time left");
    }

    public void allocate(List<Task> taskList, List<Processor> processors_list) {

        // calculate utilization ratio of each primary WCET and list of backup_WCET

        for (Task task : taskList) {
            double period = task.period;

            double WCET_1 = task.WCET.get(0);
            double WCET_2 = task.WCET.get(1);

            List<Double> utilization_wcet = new ArrayList<>();

            utilization_wcet.add(WCET_1 / period);
            utilization_wcet.add(WCET_2 / period);

            List<List<Double>> backup_WCET = task.backup_WCET;

            List<List<Double>> utilization_backup_WCET = new ArrayList<>();

            for (List<Double> backup : backup_WCET) {
                List<Double> temp_inner_list = new ArrayList<>();
                double backup_WCET_1 = backup.get(0);
                double backup_WCET_2 = backup.get(1);

                temp_inner_list.add(backup_WCET_1 / period);
                temp_inner_list.add(backup_WCET_2 / period);

                utilization_backup_WCET.add(temp_inner_list);
            }

            task.utilization_backup_WCET = utilization_backup_WCET;
            task.utilization_WCET = utilization_wcet;
        }
        // ****************************************************

//        for(int i = 0 ; i < taskList.size();i++)
//        {
//            System.out.println("Task Name : "+taskList.get(i).taskName+" Primary Task Utilization : "+taskList.get(i).utilization_WCET);
//            for(Double temp : taskList.get(i).utilization_backup_WCET)
//            {
//                System.out.print(temp+ " ");
//            }
//            System.out.println();
//        }

        for (Task task : taskList) {
            // First will try to allocate primary task
            double temp_wcet_utilization = task.criticality_level == 2 ? task.utilization_WCET.get(1) : task.utilization_WCET.get(0);

            tryToAllocate(processors_list, 1, temp_wcet_utilization, task, 0);

            // Second will try to allocate all the backup task of the current task
            List<List<Double>> backup_task = task.utilization_backup_WCET;
            for (int i = 0; i < backup_task.size(); i++) {
                double temp_backup_wcet_utilization = task.criticality_level == 2 ? backup_task.get(i).get(1) : backup_task.get(i).get(0);
                tryToAllocate(processors_list, 0, temp_backup_wcet_utilization, task, i);
            }
        }
    }

    private void tryToAllocate(List<Processor> processors_list, int primary, double utilization_ratio, Task task, int backup_task_number) {

        int check = 0;
        for (int i = 0; i < processors_list.size(); i++) {
            if (primary == 1) {
                if (processors_list.get(i).available_usage >= utilization_ratio) {
                    check = 1;
                    processors_list.get(i).active_map.put(task.taskName, -1);
                    processors_list.get(i).available_usage = processors_list.get(i).available_usage - utilization_ratio;
                    break;
                }
            } else {
                if (processors_list.get(i).available_usage >= utilization_ratio) {

                    if (!processors_list.get(i).active_map.containsKey(task.taskName) && !processors_list.get(i).backup_map.containsKey(task.taskName)) {
                        check = 1;
                        processors_list.get(i).backup_map.put(task.taskName, backup_task_number);
                        processors_list.get(i).available_usage = processors_list.get(i).available_usage - utilization_ratio;
                        break;
                    }
                }
            }
        }

        if (check == 0) // means that the current task was not allocated to any processor. Generate a new processor
        {
            Processor processor = new Processor();

            if (primary == 1) {
                processor.active_map.put(task.taskName, -1);
                processor.available_usage = processor.available_usage - utilization_ratio;
            } else {
                processor.backup_map.put(task.taskName, backup_task_number);
                processor.available_usage = processor.available_usage - utilization_ratio;
            }
            processors_list.add(processor);
        }

    }

    public void criticality_Distributed(List<Processor> processors_list, List<Task> taskList) {
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
        for (Task task : taskList) {
            // First will try to allocate primary task
            double temp_wcet_utilization = task.criticality_level == 2 ? task.utilization_WCET.get(1) : task.utilization_WCET.get(0);
            try_to_allocate_criticality_distributed(processors_list, pq, 1, temp_wcet_utilization, task, 0);

            // Second will try to allocate all the backup task of the current task
            List<List<Double>> backup_task = task.utilization_backup_WCET;
            for (int i = 0; i < backup_task.size(); i++) {
                double temp_backup_wcet_utilization = task.criticality_level == 2 ? backup_task.get(i).get(1) : backup_task.get(i).get(0);
                try_to_allocate_criticality_distributed(processors_list, pq, 0, temp_backup_wcet_utilization, task, i);
            }
        }
    }

    private void try_to_allocate_criticality_distributed(List<Processor> processors_list, PriorityQueue<Processor> pq, int primary, double utilization_ratio, Task task, int backup_task_number) {
        int check = 0;

        if (pq.size() != 0) {
            List<Processor> temp = new ArrayList<>();
            while (pq.size() != 0) {
                Processor temp1 = pq.remove();
                temp.add(temp1);
                if (temp1.available_usage >= utilization_ratio) {
                    if (!(temp1.active_map.containsKey(task.taskName)) && !(temp1.backup_map.containsKey(task.taskName))) {
                        check = 1;
                        if (primary == 1) {
                            temp1.active_map.put(task.taskName, -1);
                            temp1.available_usage = temp1.available_usage - utilization_ratio;
                            temp1.criticality_number += task.criticality_level;
                            break;
                        } else {
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


        if (check == 0) {
            Processor processor = new Processor();

            if (primary == 1) {
                processor.active_map.put(task.taskName, -1);
                processor.criticality_number += task.criticality_level;
                pq.add(processor);
                processor.available_usage = processor.available_usage - utilization_ratio;
            } else {
                processor.backup_map.put(task.taskName, backup_task_number);
                processor.criticality_number += task.criticality_level;
                processor.available_usage = processor.available_usage - utilization_ratio;
                pq.add(processor);
            }

            processors_list.add(processor);
        }
    }


    public void new_allocation(List<Task> high_criticality_task, List<Task> low_criticality_task, List<Processor> new_allocation_list, Map<String, Task> tasks_map) {
        // calculate utilization ratio for high criticality task:
        for (Task task : high_criticality_task) {
            double period = task.period;

            double WCET_1 = task.WCET.get(0);
            double WCET_2 = task.WCET.get(1);

            List<Double> utilization_wcet = new ArrayList<>();

            utilization_wcet.add(WCET_1 / period);
            utilization_wcet.add(WCET_2 / period);

            List<List<Double>> backup_WCET = task.backup_WCET;

            List<List<Double>> utilization_backup_WCET = new ArrayList<>();

            for (int i = 0; i < 4; i++) // replicating 4 times
            {
                List<Double> temp_inner_list = new ArrayList<>();
//                double backup_WCET_1 = backup.get(0);
//                double backup_WCET_2 = backup.get(1);

                temp_inner_list.add(WCET_1 / period);
                temp_inner_list.add(WCET_2 / period);

                utilization_backup_WCET.add(temp_inner_list);
            }

            task.utilization_backup_WCET = utilization_backup_WCET;
            task.utilization_WCET = utilization_wcet;
        }

        // calculate utilization ratio for low criticality task:
        for (Task task : low_criticality_task) {
            double period = task.period;

            double WCET_1 = task.WCET.get(0);
            double WCET_2 = task.WCET.get(1);

            List<Double> utilization_wcet = new ArrayList<>();

            utilization_wcet.add(WCET_1 / period);
            utilization_wcet.add(WCET_2 / period);

            List<List<Double>> backup_WCET = task.backup_WCET;

            List<List<Double>> utilization_backup_WCET = new ArrayList<>();

            for (int i = 0; i < 2; i++) // replicating 4 times
            {
                List<Double> temp_inner_list = new ArrayList<>();
//                double backup_WCET_1 = backup.get(0);
//                double backup_WCET_2 = backup.get(1);

                temp_inner_list.add(WCET_1 / period);
                temp_inner_list.add(WCET_2 / period);

                utilization_backup_WCET.add(temp_inner_list);
            }

            task.utilization_backup_WCET = utilization_backup_WCET;
            task.utilization_WCET = utilization_wcet;
        }


        PriorityQueue<Processor> pq = new PriorityQueue<>(new Comparator<Processor>() {
            @Override
            public int compare(Processor o1, Processor o2) {
                return (Math.abs(o1.num_high_tasks - o1.num_low_tasks) - Math.abs(o2.num_low_tasks - o2.num_high_tasks));
            }
        });

        high_criticality_task.sort(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return (int) (o2.utilization_WCET.get(1) - o1.utilization_WCET.get(1));
            }
        });

        low_criticality_task.sort(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return (int) (o1.utilization_WCET.get(0) - o2.utilization_WCET.get(0));
            }
        });

        int size = Math.min(high_criticality_task.size(), low_criticality_task.size());

        // Allocating tasks.
        int i;
        for (i = 0; i < size; i++) {
            Task high = high_criticality_task.get(i);

            new_Task_Allocation(new_allocation_list, pq, 1, high.utilization_WCET.get(1), high, -1);

            List<List<Double>> backup_task = high.utilization_backup_WCET;
            for (int j = 0; j < backup_task.size(); j++) {
                new_Task_Allocation(new_allocation_list, pq, 0, backup_task.get(i).get(1), high, j);
            }

            Task low = high_criticality_task.get(i);

            new_Task_Allocation(new_allocation_list, pq, 1, low.utilization_WCET.get(1), low, -1);

            List<List<Double>> backup_task_low = low.utilization_backup_WCET;
            for (int j = 0; j < backup_task_low.size(); j++) {
                new_Task_Allocation(new_allocation_list, pq, 0, backup_task_low.get(i).get(1), low, j);
            }
        }

        if (i >= high_criticality_task.size()) {
            while (i < low_criticality_task.size()) {
                Task low = low_criticality_task.get(i);

                new_Task_Allocation(new_allocation_list, pq, 1, low.utilization_WCET.get(1), low, -1);

                List<List<Double>> backup_task_low = low.utilization_backup_WCET;
                for (int j = 0; j < backup_task_low.size(); j++) {
                    new_Task_Allocation(new_allocation_list, pq, 0, backup_task_low.get(i).get(1), low, j);
                }

                i++;
            }
        } else {
            while (i < high_criticality_task.size()) {
                Task high = high_criticality_task.get(i);

                new_Task_Allocation(new_allocation_list, pq, 1, high.utilization_WCET.get(1), high, -1);

                List<List<Double>> backup_task = high.utilization_backup_WCET;
                for (int j = 0; j < backup_task.size(); j++) {
                    new_Task_Allocation(new_allocation_list, pq, 0, backup_task.get(i).get(1), high, j);
                }

                i++;
            }
        }

        // Second phase starts where we try to allocate lower criticality tasks with lower utilization onto the processors
        // which have higher idle time.

        new_allocation_list.sort(new Comparator<Processor>() {
            @Override
            public int compare(Processor o1, Processor o2) {
                double compare = o2.available_usage - o1.available_usage;
                if (compare > 0.0)
                    return 1;
                else if (compare == 0.0)
                    return 0;
                else
                    return -1;
            }
        });

        PriorityQueue<Processor> pq1 = new PriorityQueue<>(new Comparator<Processor>() {
            @Override
            public int compare(Processor o1, Processor o2) {
                double compare = o2.available_usage - o1.available_usage;
                if (compare > 0.0)
                    return 1;
                else if (compare == 0.0)
                    return 0;
                else
                    return -1;
            }
        });

        for (Processor processor : new_allocation_list) {
            pq1.add(processor);
        }

        while (true) {
            int check = 0;
            Processor temp = pq.remove();
            Map<String, Integer> primary_map = temp.active_map;

            for (Map.Entry<String, Integer> entry : primary_map.entrySet()) {
                String key = entry.getKey(); // Task Name
                int value = entry.getValue(); // Backup Number , here we don't need that.

                for (int j = 0; j < new_allocation_list.size(); j++) {
                    if (j != temp.id) {
                        if (new_allocation_list.get(j).available_usage >= tasks_map.get(key).utilization_WCET.get(1)) {
                            new_allocation_list.get(j).active_map.put(key, -1);
                            new_allocation_list.get(j).available_usage = new_allocation_list.get(j).available_usage - tasks_map.get(key).utilization_WCET.get(1); // cannot do this , need to save it in entirely different pair class.
                            check++;
                        }
                    }
                }

                if (check == primary_map.size()) {
                    // allocate backup list
                    check = 0;
                    Map<String, Integer> backup_map = temp.backup_map;

                    for (Map.Entry<String, Integer> entry1 : backup_map.entrySet()) {
                        String key1 = entry.getKey(); // Task Name
                        int value1 = entry.getValue(); // Backup Number , here we don't need that.

                        for (int j = 0; j < new_allocation_list.size(); j++) {
                            if (j != temp.id) {
                                if (new_allocation_list.get(j).available_usage >= tasks_map.get(key1).utilization_WCET.get(1)) {
                                    new_allocation_list.get(j).active_map.put(key1, -1);
                                    new_allocation_list.get(j).available_usage = new_allocation_list.get(j).available_usage - tasks_map.get(key).utilization_WCET.get(1); // cannot do this , need to save it in entirely different pair class.
                                    check++;
                                }
                            }
                        }

                    }

                    if (check != backup_map.size())
                        break;
                }
                else
                    break;

            }

        }
    }

        private void new_Task_Allocation (List < Processor > processors_list, PriorityQueue < Processor > pq,
        int primary, double utilization_ratio, Task task,int backup_task_number)
        {
            int check = 0;

            if (pq.size() != 0) {
                List<Processor> temp = new ArrayList<>();
                while (pq.size() != 0) {
                    Processor temp1 = pq.remove();
                    temp.add(temp1);
                    if (temp1.available_usage >= utilization_ratio) {
                        if (!(temp1.active_map.containsKey(task.taskName)) && !(temp1.backup_map.containsKey(task.taskName))) {
                            check = 1;
                            if (primary == 1) {
                                temp1.active_map.put(task.taskName, -1);
                                temp1.available_usage = temp1.available_usage - utilization_ratio;
                                temp1.criticality_number += task.criticality_level;
                                break;
                            } else {
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


            if (check == 0) {
                Processor processor = new Processor();

                if (primary == 1) {
                    processor.active_map.put(task.taskName, -1);
                    processor.criticality_number += task.criticality_level;
                    pq.add(processor);
                    processor.available_usage = processor.available_usage - utilization_ratio;
                } else {
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
