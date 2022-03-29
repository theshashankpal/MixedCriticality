public class Pair{
        int x;
        int y;

        public Pair(int x, int y){
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object temp)
        {
            Pair p = (Pair) temp;
            if(this.x == p.x && this.y == p.y)
                return true;
            return false;
        }
    }