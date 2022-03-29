import java.util.LinkedList;
import java.util.Queue;

public class Node extends Thread{
    double time; // time allocated to a processor.
    int id;
    Queue<Task> q =new LinkedList<>();
    Queue<Task> activeTasks = new LinkedList<>();
    Queue<Task> backupTasks = new LinkedList<>();

    public Node(double time,int id){
        this.time = time;
        this.id = id;
    }


    @Override
    public void run() {

        // do nothing as of now
        System.out.println("I'm : "+id+" And my allocated time is : "+this.time);
        Allocator.cLatch.countDown();
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        while(true)
//        {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("I'm : "+id);
//        }
    }
}
