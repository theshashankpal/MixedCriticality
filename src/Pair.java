public class Pair <T> implements Comparable<Pair<T>>{
    T first;
    T second;


    public Pair(T first, T second)
    {
        this.first = first;
        this.second = second;
    }



    @Override
    public int compareTo(Pair<T> o) {
        return (int)((double) this.first - (double)o.first);
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}

