import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.Math;

public class CNFConverter {
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 3;
    public static final int DOWN = 4;
    //    public static final int[] DIR = new int[]{ -1000, -1, 1};
    public static int[] m_limit = new int[]{0, 1, 10, 1, 10};

    boolean isLUCornerCell(int i, int j) {
        return (i == 1 && j == 1);
    }

    boolean isLDCornerCell(int i, int j) {
        return (i == m_limit[DOWN] && j == 1);
    }

    boolean isRUCornerCell(int i, int j) {
        return (i == 1 && j == m_limit[RIGHT]);
    }

    boolean isRDCornerCell(int i, int j) {
        return (i == m_limit[DOWN] && j == m_limit[RIGHT]);
    }

    boolean isLEdgeCell(int i, int j) {
        return (j == 1 && (i > 1 && i < m_limit[DOWN]));
    }

    boolean isREdgeCell(int i, int j) {
        return (j == m_limit[RIGHT] && (i > 1 && i < m_limit[DOWN]));
    }

    boolean isDEdgeCell(int i, int j) {
        return (i == m_limit[DOWN] && (j > 1 && j < m_limit[RIGHT]));
    }

    boolean isUEdgeCell(int i, int j) {
        return (i == 1 && (j > 1 && j < m_limit[RIGHT]));
    }

    // Tồn tại duy nhất = tối đa + tối thiểu

    // Các ô liền kề với ô đang xét
    List<Integer> adjacentCells(int i, int j, int value, NumberLink numberLink) {
        List<Integer> res = new ArrayList<>();
        if (isLUCornerCell(i, j)) {
            res.add(computePosition(i + 1, j, value, numberLink));
            res.add(computePosition(i, j + 1, value, numberLink));
        } else if (isLDCornerCell(i, j)) {
            res.add(computePosition(i - 1, j, value, numberLink));
            res.add(computePosition(i, j + 1, value, numberLink));
        } else if (isRDCornerCell(i, j)) {
            res.add(computePosition(i - 1, j, value, numberLink));
            res.add(computePosition(i, j - 1, value, numberLink));
        } else if (isRUCornerCell(i, j)) {
            res.add(computePosition(i + 1, j, value, numberLink));
            res.add(computePosition(i, j - 1, value, numberLink));
        } else if (isLEdgeCell(i, j)) {
            res.add(computePosition(i + 1, j, value, numberLink));
            res.add(computePosition(i, j + 1, value, numberLink));
            res.add(computePosition(i - 1, j, value, numberLink));
        } else if (isREdgeCell(i, j)) {
            res.add(computePosition(i + 1, j, value, numberLink));
            res.add(computePosition(i, j - 1, value, numberLink));
            res.add(computePosition(i - 1, j, value, numberLink));
        } else if (isUEdgeCell(i, j)) {
            res.add(computePosition(i, j - 1, value, numberLink));
            res.add(computePosition(i, j + 1, value, numberLink));
            res.add(computePosition(i + 1, j, value, numberLink));
        } else if (isDEdgeCell(i, j)) {
            res.add(computePosition(i - 1, j, value, numberLink));
            res.add(computePosition(i, j + 1, value, numberLink));
            res.add(computePosition(i, j - 1, value, numberLink));
        } else {
            res.add(computePosition(i + 1, j, value, numberLink));
            res.add(computePosition(i, j + 1, value, numberLink));
            res.add(computePosition(i - 1, j, value, numberLink));
            res.add(computePosition(i, j - 1, value, numberLink));
        }
        return res;
    }

    /*
    return binary strings with fixed length.
    i.e n=2 [00, 01, 10, 11]
    n = [log2(num of old variables)]
    */
    public static List<String> generateBinaryStrings(int n) {
        List<String> stringPermutations = new ArrayList<>();
        // number of permutations is 2^n (represented in binary by 2 bits 0 and 1)
        int permutations = (int) Math.pow(2, n);


        // Sinh du "permutations" chuoi nhi phan
        for (int bits = 0; bits < permutations; bits++) {
            String permutation = convert(bits, n);
            stringPermutations.add(permutation);
        }
        Collections.sort(stringPermutations);
        return stringPermutations;
    }

    public static String convert(int bits, int n) {
        String conversion = "";
        while (n-- > 0) {
            int bit = bits & 1; // Retrieves the rightmost bit
            if (bit == 0) {
                conversion += "0";
            } else {
                conversion += "1";
            }

            // >> means signed right shift
            bits >>= 1; // Removes the rightmost bit.
        }
        return conversion;
    }

    /*
    * bits = 0 --> bit = 0 & 1 = 0 --> conversion = 0 --> bits = 0 >> 1 = 0 --> bit = 0 & 1 = 0 --> conversion = 00
    * bits = 1 --> bit = 1 & 1 = 1 --> conversion = 1 --> bits = 1 >> 1 = 0 --> bit = 0 & 1 = 0 --> conversion = 10
    * bits = 2 --> bit = 2 & 1 = 0 --> conversion = 0 --> bits = 2 >> 1 = 1 --> bit = 1 & 1 = 1 --> conversion = 01
    * bits = 3 --> bit = 3 & 1 = 1 --> conversion = 1 --> bits = 3 >> 1 = 1 --> bit = 1 & 1 = 1 --> conversion = 11
    * stringPermutations = [00, 01, 10, 11]
    * */
    // q: why the result of even number AND 1 is 0?
    // a: because the rightmost bit of even number is 0, so the result of AND 1 is 0
    // q: why we have to remove the rightmost bit in line 117?
    // a: because we have to check the next bit of the number



    // i.e.
    public static String reverseString(String str) {
        String nstr = "";
        char ch;
        for (int i = 0; i < str.length(); i++) {
            ch = str.charAt(i); //extracts each character
            nstr = ch + nstr; //adds each character in front of the existing string
        }
        return nstr;
    }

    public SatEncoding generateSat(NumberLink numberLink) {
        m_limit[DOWN] = numberLink.getRow();
        m_limit[RIGHT] = numberLink.getCol();
        int max_num = numberLink.getMaxNum();
        int adding_vars = (int) Math.ceil((Math.log(max_num) / Math.log(2)));
        // Math.log(max_num) / Math.log(2) = log2(max_num)
        int[][] inputs = numberLink.getInputs();
        int variables = 0;
        int clauses = 0;
        List<String> rules = new ArrayList<>();
        for (int i = 1; i < inputs.length; i++) {
            for (int j = 1; j < inputs[i].length; j++) {

                // cells have number
                if (inputs[i][j] != 0) {

                    List<String> rule0 = valueFromInput(i, j, inputs[i][j], numberLink);
                    List<String> rule1 = notValuesFromInput(i, j, inputs[i][j], numberLink);
                    clauses += rule1.size();
                    rules.addAll(rule1);
                    List<String> rule2 = exact_one_direction(i, j, numberLink);

                    clauses += rule0.size() + rule2.size();

                    rules.addAll(rule0);
                    rules.addAll(rule2);

                    // blank cell
                } else {
                    List<String> baseRule1 = onlyOneValue(i, j, numberLink);

                    clauses += baseRule1.size();
                    rules.addAll(baseRule1);

                    List<String> rule2 = has_two_directions(i, j, numberLink);

                    clauses += rule2.size();

                    rules.addAll(rule2);
                }
            }
        }
        variables = m_limit[DOWN] * m_limit[RIGHT] * max_num +
                adding_vars * (m_limit[DOWN] * m_limit[RIGHT] - max_num * 2);
        return new SatEncoding(rules, clauses, variables);
    }


    // Blank cells have two directions
    private List<String> has_two_directions(int i, int j, NumberLink numberLink) {
        List<String> resultStringList = new ArrayList<>();

        String firstClause = "";
        for (int k = 1; k <= numberLink.getMaxNum(); k++) {
            firstClause = -computePosition(i, j, k, numberLink) + " ";
            List<Integer> adjacentCells = adjacentCells(i, j, k, numberLink);
            int numCells = adjacentCells.size();
            // numCells == 2: ô ở vị trí góc --> (-Xijk v Xi(j+1)k) ^ (-Xijk v X(i+1)jk)
            // numCells == 3: ô ở vị trí biên
            // numCells == 4: ô ở các vị trí còn lại
            if (numCells == 2) {
                for (int z = 0; z <= numCells - 1; z++) {
                    String tmp2 = firstClause + adjacentCells.get(z) + " ";
                    tmp2 += "0";
                    resultStringList.add(tmp2);
                }
            } else if (numCells == 3) {
                // 2 trong 3 hướng đi: (-Xijk v Xi(j-1)k v Xi(j+1)k) ^ (-Xijk v Xi(j-1)k v X(i+1)jk) ^ (-Xijk v X(i+1)jk v Xi(j+1)k)
                // At least 2 in 3 are TRUE
                for (int t = 0; t <= numCells - 2; t++) {
                    String tmp1 = firstClause + adjacentCells.get(t) + " ";
                    for (int z = t + 1; z <= numCells - 1; z++) {
                        String tmp2 = tmp1 + adjacentCells.get(z) + " ";
                        tmp2 += "0";
                        resultStringList.add(tmp2);
                    }
                }
            } else if (numCells == 4) {
                // 2 trong 4 hướng đi: -Xijk v X(i+1)jk v Xi(j+1)k v
                // At least 2 in 4 are TRUE
                for (int q = 0; q <= numCells - 3; q++) {
                    String tmp0 = firstClause + adjacentCells.get(q) + " ";
                    // tmp0 = -Xijk v X(i+1)jk, q = 0
                    // tmp0 = -Xijk v Xi(j+1)k, q = 1
                    for (int t = q + 1; t <= numCells - 2; t++) {
                        String tmp1 = tmp0 + adjacentCells.get(t) + " ";
                        // tmp1 = -Xijk v X(i+1)jk v Xi(j+1)k, q = 0 t = 1
                        // tmp1 = -Xijk v X(i+1)jk v X(i-1)jk, q = 0 t = 2
                        // tmp1 = -Xijk v Xi(j+1)k v X(i-1)jk, q = 1 t = 2
                        for (int z = t + 1; z <= numCells - 1; z++) {
                            String tmp2 = tmp1 + adjacentCells.get(z) + " ";
                            tmp2 += "0";
                            // tmp2 = -Xijk v X(i+1)jk v Xi(j+1)k v X(i-1)jk, q = 0 t = 1 z = 2
                            // tmp2 = -Xijk v X(i+1)jk v Xi(j+1)k v Xi(j-1)k, q = 0 t = 1 z = 3
                            // tmp2 = -Xijk v X(i+1)jk v X(i-1)jk v Xi(j-1)k, q = 0 t = 2 z = 3
                            // tmp2 = -Xijk v Xi(j+1)k v X(i-1)jk v Xi(j-1)k, q = 1 t = 2 z = 3
                            resultStringList.add(tmp2);
                        }
                    }
                }
            }
        }


//        UEdge: ←, ↓, →
//        res.add(computePosition(i, j - 1, value, numberLink));
//        res.add(computePosition(i, j + 1, value, numberLink));
//        res.add(computePosition(i + 1, j, value, numberLink));

        for (int k = 1; k <= numberLink.getMaxNum(); k++) {
            firstClause = -computePosition(i, j, k, numberLink) + " ";
            List<Integer> adjacentCells = adjacentCells(i, j, k, numberLink);
            int numCells = adjacentCells.size();
            if (numCells == 3) {
//              At most 2 in 3 are TRUE
                String tmp2 = firstClause;
                for (int z = 0; z <= numCells - 1; z++) {
                    tmp2 += (-adjacentCells.get(z)) + " ";
                    // tmp2 = -Xijk v -Xi(j-1)k v -Xi(j+1)k v -X(i+1)jk
                }
                tmp2 += "0";
                resultStringList.add(tmp2);
            } else if (numCells == 4) {
//              At most 2 in 4 are TRUE
//              res.add(computePosition(i + 1, j, value, numberLink));   0
//              res.add(computePosition(i, j + 1, value, numberLink));   1
//              res.add(computePosition(i - 1, j, value, numberLink));   2
//              res.add(computePosition(i, j - 1, value, numberLink));   3
                for (int q = 0; q <= numCells - 3; q++) {
                    String tmp0 = firstClause + (-adjacentCells.get(q)) + " ";
                    // tmp0 = -Xijk v -X(i+1)jk, q = 0
                    // tmp0 = -Xijk v -Xi(j+1)k, q = 1
                    for (int t = q + 1; t <= numCells - 2; t++) {
                        String tmp1 = tmp0 + (-adjacentCells.get(t)) + " ";
                        // tmp1 = -Xijk v -X(i+1)jk v -Xi(j+1)k, q = 0 t = 1
                        // tmp1 = -Xijk v -X(i+1)jk v -X(i-1)jk, q = 0 t = 2
                        // tmp1 = -Xijk v -Xi(j+1)k v -X(i-1)jk, q = 1 t = 2
                        for (int z = t + 1; z <= numCells - 1; z++) {
                            String tmp2 = tmp1 + (-adjacentCells.get(z)) + " ";
                            // tmp2 = -Xijk v -X(i+1)jk v -Xi(j+1)k v -X(i-1)jk, q = 0 t = 1 z = 2
                            // tmp2 = -Xijk v -X(i+1)jk v -Xi(j+1)k v -Xi(j-1)k, q = 0 t = 1 z = 3
                            // tmp2 = -Xijk v -X(i+1)jk v -X(i-1)jk v -Xi(j-1)k, q = 0 t = 2 z = 3
                            // tmp2 = -Xijk v -Xi(j+1)k v -X(i-1)jk v -Xi(j-1)k, q = 1 t = 2 z = 3
                            tmp2 += "0";
                            resultStringList.add(tmp2);
                        }
                    }
                }
            }
        }
        return resultStringList;
    }


    // for numbered cells
    private List<String> exact_one_direction(int i, int j, NumberLink numberLink) {
        List<String> resultStringList = new ArrayList<>();
        List<Integer> adjacentCells = adjacentCells(i, j, numberLink.getInputs()[i][j], numberLink);
        String firstClause = "";
        // AT LEAST 1 is TRUE
        for (int value : adjacentCells) {
            firstClause += value + " ";
        }
        firstClause += "0";
        resultStringList.add(firstClause);

        // AT MOST 1 is TRUE --> tại sao không dùng công thức dựa trên biến mới như trong slides thầy Khánh
        int numCells = adjacentCells.size();
        for (int k = 0; k <= numCells - 2; k++) {
            String secondClause = -adjacentCells.get(k) + " ";
            // secondClause = -Xi(j-1)k, k = 0
            // secondClause = -Xi(j+1)k, k = 1
            for (int q = k + 1; q <= numCells - 1; q++) {
                String tmp = secondClause + (-adjacentCells.get(q)) + " 0";
                // tmp = -Xi(j-1)k v -Xi(j+1)k, k = 0 q = 1
                // tmp = -Xi(j-1)k v -X(i-1)jk, k = 0 q = 2
                // tmp = -Xi(j+1)k v -X(i-1)jk, k = 1 q = 2
                resultStringList.add(tmp);
            }
        }
        return resultStringList;
    }

    // For blank cells: at most one value is TRUE in each blank cell
    private List<String> onlyOneValue(int i, int j, NumberLink numberLink) {
        List<String> resultStringList = new ArrayList<>();
//        String tmpClause = "onlyOneValue";
//        resultStringList.add(tmpClause);
        int max_num = numberLink.getMaxNum(); // max_num = 4 (5x5 1.in)
        int adding_vars = (int) Math.ceil((Math.log(max_num) / Math.log(2))); // adding_vars = log2(4) = 2
        List<String> binaryStrings = generateBinaryStrings(adding_vars);
        // binaryStrings = ["00", "01", "10", "11"]
        // cắt bớt, chỉ lấy n = max_num xâu
        binaryStrings = binaryStrings.subList(0, max_num);
        // binaryStrings = ["00", "01", "10", "11"]

        for (int k = 1; k <= max_num; k++) { // k = 1 --> 4

            String binary = binaryStrings.get(k - 1);
            binary = reverseString(binary);
            // binary = "00"
            // Tại sao lại chạy q từ max_num + 1 đến max_num + adding_vars
            for (int q = max_num + 1; q <= max_num + adding_vars; q++) {  // q = 5 --> 6
                String clause = "";
                // -X v
                clause += -computePosition(i, j, k, numberLink) + " ";
                // clause = -Xijk
                char bit = binary.charAt(q - max_num - 1);
                if (bit == '0') {
                    // -Y
                    clause += -computePosition(i, j, q, numberLink) + " ";
                } else {
                    // Y
                    clause += computePosition(i, j, q, numberLink) + " ";
                }
                clause += "0";
                resultStringList.add(clause);
            }
        }

//        String tmpClause2 = "end";
//        resultStringList.add(tmpClause2);

        //        for (int k = 1; k <= numberLink.getMaxNum(); k++) {
//            exactNumLine += computePosition(i, j, k, numberLink) + " ";
//        }
//        exactNumLine += "0";
//        resultStringList.add(exactNumLine);

        return resultStringList;
    }

    // For numbered cells: only the existed value is TRUE
    private List<String> valueFromInput(int i, int j, int num, NumberLink numberLink) {
        int result = computePosition(i, j, num, numberLink);
        // result = Xijk (k represents for numbered cell's value)
        List<String> resultStringList = new ArrayList<>();
        //String tmpClause = "valueFromInput";
        //resultStringList.add(tmpClause);

        String exactNumLine = "";
        exactNumLine += result + " 0";
        resultStringList.add(exactNumLine);

        return resultStringList;
    }

    private List<String> notValuesFromInput(int i, int j, int num, NumberLink numberLink) {
        List<String> resultStringList = new ArrayList<>();
//        String firstClause = -computePosition(i, j, num, numberLink) + " ";
        for (int q = 1; q <= numberLink.getMaxNum(); q++) {
            if (q != num) {
                String secondClause = -computePosition(i, j, q, numberLink) + " ";
                secondClause += "0";
                resultStringList.add(secondClause);
            }
        }
        return resultStringList;
    }


    private int computePosition(int i, int j, int value, NumberLink numberLink) {
        // vd -9 <=> -X9, có đảm bảo được các biến là các số liên tiếp ko?
        int n = numberLink.getCol();
        int max_num = numberLink.getMaxNum();
        int adding_vars = (int) Math.ceil((Math.log(max_num) / Math.log(2)));
        int X_vars = numberLink.getRow() * numberLink.getCol() * max_num;
        if (value <= max_num)
            return n * (i - 1) * max_num + (j - 1) * max_num + value;
        return X_vars + n * (i - 1) * adding_vars + (j - 1) * adding_vars + value;
    }


    public int getValueOf(int row, int col, int positionValue, NumberLink numberLink) {
        int n = numberLink.getCol();
        if (positionValue <= n * (n - 1)) {
            int JValue = (positionValue - 1) % (n - 1) + 1;
            if (JValue == col) {
                return RIGHT;
            } else if (JValue + 1 == col) {
                return LEFT;
            } else return 100;
        } else if (positionValue <= 2 * n * (n - 1)) {
            int IValue = (positionValue - n * (n - 1) - 1) % (n - 1) + 1;
            if (IValue == row) {
                return DOWN;
            } else if (IValue + 1 == row) {
                return UP;
            } else return 100;
        } else {
            int tmp = (positionValue - 2 * n * (n - 1 - 1)) % numberLink.getMaxNum() + 1;
            return ((positionValue - 2 * n * (n - 1) - tmp) / numberLink.getMaxNum() + 1 - col) / n + 1;
        }
    }

    public int getValueOfY(int positionValue, int maxNum, NumberLink numberLink) {
        int rows = numberLink.getRow();
        int cols = numberLink.getCol();
        if (positionValue <= rows * cols * maxNum) {
            return (positionValue - 1) % maxNum + 1;
        }
        return -1;
    }

    public int getValueOfYJ(int positionValue, NumberLink numberLink) {
        return ((positionValue - 1) / numberLink.getMaxNum()) % numberLink.getCol() + 1;
    }

    public int getValueOfYI(int positionValue, NumberLink numberLink) {
        positionValue = Math.abs(positionValue);
        return (positionValue - 1) / (numberLink.getMaxNum() * numberLink.getCol()) + 1;
    }

}