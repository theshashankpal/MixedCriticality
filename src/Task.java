import java.util.List;

public class Task implements Comparable<Task> {
    String taskName;
    double period;
    double deadline;
    double WCET;
    List<Double> backup_WCET;
    double utilization_WCET;
    List<Double> utilization_backup_WCET;
    int criticality_level;

    public Task(double period, double deadline, double WCET, List<Double> backup_WCET, String taskName,int criticality_level){
        this.deadline = deadline;
        this.period = period;
        this.WCET = WCET;
        this.backup_WCET = backup_WCET;
        this.taskName = taskName;
        this.criticality_level = criticality_level;
    }

    public Task(){}

    @Override
    public String toString() {
        return "Task{" +
                "taskName='" + taskName + '\'' +
//                ", period=" + period +
//                ", deadline=" + deadline +
//                ", WCET=" + WCET +
//                ", backup_WCET=" + backup_WCET +
                '}';
    }

    @Override
    public int compareTo(Task o) {
        int compare = Double.compare(this.period,o.period);
        return compare;
    }
}
