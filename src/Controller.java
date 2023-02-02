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
    private static CNFConverter cnfConverter = new CNFConverter();
    private static SATSolver satSolver;
    static NumberLink numberLink = new NumberLink();
    static IProblem problem = null;
    static long clause = 0;
    static long vars = 0;
    static boolean satisfied = false;
    static String sat = "UNSAT";
    static String time = "";
    static String parameters = "";
    public static File outFile = new File("./output/log3548_ALO.txt");
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

        satisfied = problem.isSatisfiable();
        result = parameters + "\nSo luong bien la: " + vars + "\n" +
                "So luong menh de la: " + clause + "\n" + numberLink + "\n";
        String solution = "";
        if (satisfied) {
            int[] model = problem.model();
            sat = "SAT";
            solution = printResult(model, numberLink);
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