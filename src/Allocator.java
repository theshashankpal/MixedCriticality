import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

    public boolean allocate(List<Task> taskList, List<List<Task>> allocatedList)
    {
        // list[0] for WCET and list[1] for backup_WCET
        List<Pair<Double>> utilizationRatio = new ArrayList<>();

        // Finding out the utilzation ratio.
        for(Task task : taskList)
        {
            double period = task.period;
            double WCET = task.WCET;
            double backup_WCET = task.backup_WCET;
            double utilization_WCET = WCET / period;
            double utilization_backup_WCET = backup_WCET / period;
            Pair temp = new Pair(utilization_WCET,utilization_backup_WCET);

            utilizationRatio.add(temp);
        }

        Collections.sort(utilizationRatio);

        System.out.println(utilizationRatio);

        // Assuming we've 4 processors and at start each processor have 1.0 usage available.
        List<Double> processors_available_usage = new ArrayList<>();

        for(int i = 0 ; i < 4; i++)
        {
            processors_available_usage.add(1.0);
        }



        return false;
    }
}
