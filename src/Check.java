public class Check {
    public static void main(String[] args) {
        int start = 1;
        int end = 5;
        int perGroup = 5;
        while (end <= 15) {
            for (int i = start; i <= end; i++) {
                System.out.println(i);
            }
            start = end + 1;
            end = 2 * perGroup + end;
        }
    }
}
