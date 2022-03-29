import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;

public class Test {
    public static void main(String[] args) {
        System.out.println(largestSquare(new int[]{6,13,15,28,22,30,24}));

    }

    private static int largestSquare(int [] heights)
    {
        Stack<Integer> stack = new Stack<>();
        int i=0, maxArea = 0;

        while (i < heights.length) {
            while (!stack.isEmpty() && heights[i] < heights[stack.peek()]) {
                int idx = stack.pop();
                int width = stack.isEmpty() ? i : i - stack.peek() - 1;
                int edge = Math.min(width, heights[idx]);
                maxArea = Math.max(maxArea, edge * edge);
            }

            stack.push(i++);
        }

        while (!stack.isEmpty()) {
            int idx = stack.pop();
            int width = stack.isEmpty() ? i : i - stack.peek() - 1;
            int edge = Math.min(width, heights[idx]);
            maxArea = Math.max(maxArea, edge * edge);
        }

        return (int)Math.sqrt(maxArea);
    }
}
