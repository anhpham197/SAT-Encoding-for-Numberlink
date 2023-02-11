// Java program to find path between two cell in matrix

import java.util.ArrayList;
import java.util.List;

class Path {
    public static int[][] findNum(int[][] matrix, int num) {
        int[][] arrNum = new int[2][2];
        int count = 0;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] == num) {
                    arrNum[count][0] = i;
                    arrNum[count][1] = j;
                    count++;
                }
                if (count == 2) {
                    return arrNum;
                }
            }
        }
        return arrNum;
    }

    public static int[] getSource(int[][] arrNum) {
        return arrNum[0];
    }

    public static int[] getDestination(int[][] arrNum) {
        return arrNum[1];
    }

    // Method for finding and printing whether the path exists or not
    public static void isPath(int input[][], int matrix[][], int num) {
        // Defining visited array to keep track of already visited indexes
        boolean visited[][] = new boolean[matrix.length][matrix[0].length];

        // Flag to indicate whether the path exists or not
        boolean flag = false;

        int[][] arrNum = findNum(input, num);
        int[] source = getSource(arrNum);
        int[] des = getDestination(arrNum);
        int sourceX = source[0];
        int sourceY = source[1];
        int desX = des[0];
        int desY = des[1];

        if (matrix[sourceX][sourceY] == num && !visited[sourceX][sourceY]) {
            if (isPath(input, matrix, sourceX, sourceY, visited, num)) {
                flag = true;
            }
        }

//        for (int i = 0; i < n; i++) {
//            for (int j = 0; j < n; j++) {
//                // if matrix[i][j] is source
//                // and it is not visited
//                if (matrix[i][j] == 1 && !visited[i][j])
//
//                    // Starting from i, j and
//                    // then finding the path
//                    if (isPath(matrix, i, j, visited)) {
//                        // if path exists
//                        flag = true;
//                        break;
//                    }
//            }
//        }
        if (flag)
            System.out.println("YES");
        else
            System.out.println("NO");
    }

    // Method for checking boundaries
    public static boolean isSafe(int i, int j,
                                 int matrix[][]) {

        if (i >= 0 && i < matrix.length && j >= 0
                && j < matrix[0].length)
            return true;
        return false;
    }

    static List<List<Integer>> res = new ArrayList<>();

    // Returns true if there is a path from a source (a cell with value 1) to a destination (a cell with value 2)
    public static boolean isPath(int input[][], int matrix[][], int i,
                                 int j, boolean visited[][], int num) {
        int[][] arrNum = findNum(input, num);
        int[] source = getSource(arrNum);
        int[] des = getDestination(arrNum);
        int sourceX = source[0];
        int sourceY = source[1];
        int desX = des[0];
        int desY = des[1];


        // Checking the boundaries, walls and whether the cell is unvisited
        if (isSafe(i, j, matrix) && matrix[i][j] != -1 && !visited[i][j] && matrix[i][j] == num) {
            // Make the cell visited
            visited[i][j] = true;

            // if the cell is the required destination then return true
            if (matrix[i][j] == num && i == desX && j == desY) {
                List<Integer> pos = new ArrayList<>();
                pos.add(i);
                pos.add(j);
                res.add(pos);
                return true;
            }

            // traverse up
            boolean up = isPath(input, matrix, i - 1, j, visited, num);

            // if path is found in up direction return true
            if (up) {
                List<Integer> pos = new ArrayList<>();
                pos.add(i);
                pos.add(j);
                res.add(pos);
                return true;
            }

            // traverse left
            boolean left
                    = isPath(input, matrix, i, j - 1, visited, num);

            // if path is found in left direction return true
            if (left) {
                List<Integer> pos = new ArrayList<>();
                pos.add(i);
                pos.add(j);
                res.add(pos);
                return true;
            }
            // traverse down
            boolean down
                    = isPath(input, matrix, i + 1, j, visited, num);

            // if path is found in down direction return true
            if (down) {
                List<Integer> pos = new ArrayList<>();
                pos.add(i);
                pos.add(j);
                res.add(pos);
                return true;
            }

            // traverse right
            boolean right
                    = isPath(input, matrix, i, j + 1, visited, num);

            // if path is found in right direction return true
            if (right) {
                List<Integer> pos = new ArrayList<>();
                pos.add(i);
                pos.add(j);
                res.add(pos);
                return true;
            }
        }
        // no path has been found
        return false;
    }

    public static int[][] getInputs(String input) {
        String[] rows = input.split("\n");
        int[][] res = new int[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = rows[i].trim().replaceAll(" +", " ");
            String[] cols = rows[i].split(" ");
            res[i] = new int[cols.length];
            for (int j = 0; j < cols.length; j++) {
                res[i][j] = Integer.parseInt(cols[j]);
            }
        }
        return res;
    }

    // driver program to check above function
    public static void main(String[] args) {
//        int inp[][] = {
//                {0, 4, 0, 3},
//                {4, 0, 0, 0},
//                {3, 0, 0, 0},
//                {0, 0, 0, 0}
//        };
//
//        int matrix[][] = {
//                    {4, 4, -1, 3},
//                    {4, 4, 3, 3},
//                    {3, 3, -1, 3},
//                    {-1, 3, 3, 3}
//        };
//
//        // calling isPath method
//        isPath(inp, matrix, 3);
//    }
        int[][] res = getInputs(" 1  2  3\n14 15 16\n 7 18 19");
        for (int i = 0; i < res.length; i++) {
            for (int j = 0; j < res[0].length; j++) {
                System.out.print(res[i][j] + ",");
            }
            System.out.println();
        }
    }
}

/* This code is contributed by Madhu Priya */

