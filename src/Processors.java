import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Processors {
    int count = 0;
    final int multiple = (int) Math.pow(10,6);
    int id;
//    List<Thread> processors = new ArrayList<>();

    public Processors(){

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
