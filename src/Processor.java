import java.util.*;

public class Processor {
    int criticality_number ;
    Map<String , Integer> active_map;
    Map<String , Integer> backup_map;
    double available_usage ;

    int num_high_tasks ;
    int num_low_tasks;
//    final int multiple = (int) Math.pow(10,6);
    static int count;
    int id;

    public Processor(){
        this.active_map = new HashMap<>();
        this.backup_map = new HashMap<>();
        this.available_usage = 1.0;
        this.criticality_number = 0;
        this.num_high_tasks = 0;
        this.num_low_tasks = 0;
        this.id = count;
        this.count++;
    }

    public void generateProcessors(long timeAllocated,List<Node> processors) {
        Node node = new Node(20000,id++);
        processors.add(node);
        processors.get(processors.size() - 1).start();
    }

    public void allocateJob()
    {

    }

//    public void stopProcessors() {
//        for(Thread temp : processors)
//        {
//            temp.stop();
//        }
//    }
}
