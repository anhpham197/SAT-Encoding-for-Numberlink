import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.TimeoutException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Controller {
    public static File outFile = new File("./output/log_test.txt");
    static NumberLink numberLink = new NumberLink();
    static IProblem problem = null;
    static long clause = 0;
    static long vars = 0;
    static boolean satisfied = false;
    static String sat = "UNSAT";
    static String time = "";
    static String parameters = "";
    private static List<List<Integer>> paths = new ArrayList<>();
    private static CNFConverter cnfConverter = new CNFConverter();
    private static SATSolver satSolver;
    private static String result;


    public static List<String> inFoList() {
        List<String> res = new ArrayList<>();
        res.add(String.valueOf(numberLink.getRow()));
        res.add(String.valueOf(numberLink.getCol()));
        res.add(String.valueOf(numberLink.getMaxNum()));
        res.add(String.valueOf(vars));
        res.add(String.valueOf(clause));
        res.add(time);
        res.add(sat);
        return res;
    }

    public static void read(File file) throws FileNotFoundException {
        List<Long> res = new ArrayList<>();
        Scanner sc = new Scanner(file);

        List<List<String>> matrix = new ArrayList<>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            List<String> arr = Arrays.asList(line.split(" "));
            matrix.add(arr);
        }
        sc.close();
        int rows = matrix.size();
        int cols = matrix.get(0).size();
        numberLink.setRow(rows);
        numberLink.setCol(cols);

        int[][] input = new int[numberLink.getRow() + 1][numberLink.getCol() + 1];
        for (int i = 1; i < numberLink.getRow() + 1; i++) {
            for (int j = 1; j < numberLink.getCol() + 1; j++) {
                input[i][j] = Integer.parseInt(matrix.get(i - 1).get(j - 1));
            }
        }
        int maxNum = getMaxNum(input);
        numberLink.setMaxNum(maxNum);
        numberLink.setInputs(input);
        parameters = file.getName() + "\n" + "Kich thuoc ma tran: " + rows + "x" + cols + "\n"
                + "Gia tri lon nhat: " + maxNum + "\n";

        System.out.println(parameters);

        // in ra de bai
        System.out.println(numberLink);
    }

    // This method is to find the position of SOURCE and DESTINATION (2 same number cells in the matrix)
    public static int[][] findNum(int[][] input, int num) {
        int[][] arrNum = new int[2][2];
        int count = 0;
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[i].length; j++) {
                if (input[i][j] == num) {
                    arrNum[count][0] = i - 1;
                    arrNum[count][1] = j - 1;
                    count++;
                }
                if (count == 2) {
                    return arrNum;
                }
            }
        }
        return arrNum;
    }

    // This method is to get the position of the SOURCE for each pair of numbers
    public static int[] getSource(int[][] arrNum) {
        return arrNum[0];
    }

    // This method is to get the position of the DESTINATION for each pair of numbers
    public static int[] getDestination(int[][] arrNum) {
        return arrNum[1];
    }

    // Method for checking boundaries
    public static boolean isSafe(int i, int j, String matrix[][]) {
        return (i >= 0 && i < matrix.length && j >= 0 && j < matrix[0].length);
    }

    // Method for finding and printing whether the path exists or not
    public static void isPath(String[][] matrix, int sourceX, int sourceY, int desX, int desY, String num) {
        // Defining visited array to keep track of already visited indexes
        boolean visited[][] = new boolean[matrix.length][matrix[0].length];

        // Flag to indicate whether the path exists or not
        boolean flag = false;

        if (matrix[sourceX][sourceY].equals(num) && !visited[sourceX][sourceY]) {
            if (isPath(matrix, sourceX, sourceY, desX, desY, visited, num)) {
                flag = true;
            }
        }
    }

    // Returns true if there is a path from a source to a destination
    public static boolean isPath(String matrix[][], int i,
                                 int j, int desX, int desY, boolean visited[][], String num) {

        // Checking the boundaries, "-", and whether the cell is unvisited
        if (isSafe(i, j, matrix) && !matrix[i][j].equals("-") && !visited[i][j] && matrix[i][j].equals(num)) {
            // Make the cell visited
            visited[i][j] = true;

            // if the cell is the required destination then return true
            if (matrix[i][j].equals(num) && i == desX && j == desY) {
                List<Integer> pos = new ArrayList<>();
                pos.add(i);
                pos.add(j);
                paths.add(pos);
                return true;
            }

            // traverse up
            boolean up = isPath(matrix, i - 1, j, desX, desY, visited, num);

            // if path is found in up direction return true
            if (up) {
                List<Integer> pos = new ArrayList<>();
                pos.add(i);
                pos.add(j);
                paths.add(pos);
                return true;
            }

            // traverse left
            boolean left = isPath(matrix, i, j - 1, desX, desY, visited, num);

            // if path is found in left direction return true
            if (left) {
                List<Integer> pos = new ArrayList<>();
                pos.add(i);
                pos.add(j);
                paths.add(pos);
                return true;
            }
            // traverse down
            boolean down = isPath(matrix, i + 1, j, desX, desY, visited, num);

            // if path is found in down direction return true
            if (down) {
                List<Integer> pos = new ArrayList<>();
                pos.add(i);
                pos.add(j);
                paths.add(pos);
                return true;
            }

            // traverse right
            boolean right = isPath(matrix, i, j + 1, desX, desY, visited, num);

            // if path is found in right direction return true
            if (right) {
                List<Integer> pos = new ArrayList<>();
                pos.add(i);
                pos.add(j);
                paths.add(pos);
                return true;
            }
        }
        // no path has been found
        return false;
    }

    // Convert string to 2D String array
    public static String[][] convertToArray(String input, int r) {
        String[] rows = input.split("\n");
        String[][] res = new String[r][];
        for (int i = 0; i < r; i++) {
            rows[i] = rows[i].trim().replaceAll(" +", " ");
            String[] cols = rows[i].split(" ");
            res[i] = new String[cols.length];
            for (int j = 0; j < cols.length; j++) {
                res[i][j] = cols[j];
            }
        }
        return res;
    }

    // Method for storing the position of all cells in a 2D List
    public static List<List<Integer>> getAllPositionOfCells(String[][] matrix) {
        List<List<Integer>> pos = new ArrayList<>();
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                List<Integer> tmp = new ArrayList<>();
                tmp.add(i);
                tmp.add(j);
                pos.add(tmp);
            }
        }
        return pos;
    }

    public static List<List<Integer>> findAllLoopsPosition(List<List<Integer>> pos, List<List<Integer>> paths) {
        Set<List<Integer>> union = new HashSet<>(pos);
        union.addAll(paths);
        Set<List<Integer>> intersection = new HashSet<>(pos);
        intersection.retainAll(paths);
        union.removeAll(intersection);
        List<List<Integer>> loops = new ArrayList<>(union);
        return loops;
    }

    public static void replaceLoop(String[][] matrix, List<List<Integer>> loops) {
        for (int i = 0; i < loops.size(); i++) {
            List<Integer> pos = loops.get(i);
            int x = pos.get(0);
            int y = pos.get(1);
            matrix[x][y] = "-";
        }
    }

    public static void encode() throws IOException, TimeoutException, ParseFormatException, ContradictionException {
        long t1 = System.currentTimeMillis();
        // Ghi ra file CNF
        File fileCNF = new File("text.cnf");
        FileWriter writer = new FileWriter(fileCNF);

        //long t1 = System.currentTimeMillis();
        SatEncoding satEncoding = cnfConverter.generateSat(numberLink);
        clause = satEncoding.getClauses();
        vars = satEncoding.getVariables();
        String firstLine = "p cnf " + vars + " " + clause;
        System.out.println("So luong bien la: " + vars);
        System.out.println("So luong menh de la: " + clause);
        writer.write(firstLine + "\n");
        List<String> rules = satEncoding.getRules();
        for (int i = 0; i < rules.size(); i++) {
            // dong cuoi khong xuong dong
            if (i == rules.size() - 1) {
                writer.write(rules.get(i));
                continue;
            }
            writer.write(rules.get(i) + "\n");
        }
        writer.flush();
        writer.close();

        // SAT Solve
        NumberLinkResponse response = new NumberLinkResponse();
        DimacsReader reader = new DimacsReader(SolverFactory.newDefault());
        reader.parseInstance("text.cnf");
        satSolver = new SATSolver(reader);
        problem = satSolver.solve("text.cnf");
//        System.out.println(problem);
        satisfied = problem.isSatisfiable();
        result = parameters + "\nSo luong bien la: " + vars + "\n" +
                "So luong menh de la: " + clause + "\n" + numberLink + "\n";
        String solution = "";
        if (satisfied) {
            int[] model = problem.model();
            sat = "SAT";
            solution = printResult(model, numberLink);

            // Handle loops
            String[][] tmp = convertToArray(solution, numberLink.getRow());
            for (int i = 1; i <= numberLink.getMaxNum(); i++) {
                // do something
                int[][] arrNum = findNum(numberLink.getInputs(), i);
                int[] source = getSource(arrNum);
                int[] des = getDestination(arrNum);
                int sourceX = source[0];
                int sourceY = source[1];
                int desX = des[0];
                int desY = des[1];

                // Return 2D List of paths
                isPath(tmp, sourceX, sourceY, desX, desY, String.valueOf(i));
            }
//            int[][] pathArr = convert2DList(paths);
            // Find all position of cells
            List<List<Integer>> pos = getAllPositionOfCells(tmp);
            // Find all position of loops
            List<List<Integer>> loops = findAllLoopsPosition(pos, paths);
            // Replace loops with "-"
            if (loops.size() > 0) {
                replaceLoop(tmp, loops);
            }
            solution = convert2DStringArrToString(tmp);
            result += solution;
            System.out.println(solution);
        } else {
            sat = "UNSAT";
        }
        time = String.valueOf(System.currentTimeMillis() - t1);
        System.out.println(sat);
        result += "\n" + sat + "\n" + "Total time: " + time + "\n"
                + "--------------------------------";
        outputToTxt(result, outFile);
        paths = new ArrayList<>();
    }

    private static int getMaxNum(int[][] matrix) {
        int maxNum = 0;
        for (int i = 1; i < matrix.length; i++) {
            for (int j = 1; j < matrix[0].length; j++) {
                if (maxNum < matrix[i][j]) {
                    maxNum = matrix[i][j];
                }
            }
        }
        return maxNum;
    }


    private static String convert2DStringArrToString(String[][] matrix) {
        String res = "";
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j].equals("-") || Integer.parseInt(matrix[i][j]) < 10) {
                    res += " ";
                }
                res += matrix[i][j] + " ";
            }
            res += "\n";
        }
        return res;
    }

    private static String printResult(int[] model, NumberLink numberLink) {
        int maxNum = numberLink.getMaxNum();
        String res = "";
        List<List<Integer>> arr = new ArrayList<>();
        int count = 0;
        for (int i = 0; i < (model.length / maxNum); i++) {
            List<Integer> cell = new ArrayList<>();
            for (int j = 0; j < maxNum; j++) {
                cell.add(model[count]);
                count++;
            }
            arr.add(cell);
        }

        count = 0;
        for (int i = 0; i < arr.size(); i++) {
            count++;
            res += printValue(arr.get(i), maxNum);

            if (count == numberLink.getCol()) {
                /*System.out.println();*/
                res += "\n";
                count = 0;
            }
        }
        return res;
    }

    private static String printValue(List<Integer> cell, int maxNum) {
        String res = "";
        boolean hasPositiveValue = false;
        for (int i : cell) {
            if (i > 0) {
                hasPositiveValue = true;
                int value = cnfConverter.getValueOfY(i, maxNum, numberLink);
                if (value > 0) {
                    if (value < 10) {
                        /*System.out.print(" ");*/
                        res += " ";
                    }
                    /*System.out.print((value) + " ");*/
                    res += value + " ";
                }
            }
        }
        if (!hasPositiveValue) {
            /*System.out.print(" - ");*/
            res += " - ";
        }

        return res;
    }

    public static void outputToTxt(String result, File outFile) {
        try {
            FileWriter writer = new FileWriter(outFile, true);
            writer.write(result + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}