public class Jobs extends Task implements Runnable{
    double WCET;
    double backup_WCET;

    public Jobs(double period,
                double deadline,
                double WCET,
                double backup_WCET,
                String taskName) {
//        super(period, deadline, WCET, backup_WCET, taskName);
        this.period = period;
        this.deadline = deadline;
        this.WCET = WCET;
        this.backup_WCET = backup_WCET;
        this.taskName = taskName;
    }

    @Override
    public void run() {
        while (true)
        {
//            System.out.println("I'm in jobs "+this.WCET+" : "+this.backup_WCET);
            Main.blockingQueue.add(this);
            System.out.println("IM A job thread and I keep on adding");
            try {
                Thread.sleep((long)this.period);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
