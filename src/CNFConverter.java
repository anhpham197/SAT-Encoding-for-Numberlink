import java.util.ArrayList;
import java.util.List;

public class CNFConverter {
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 3;
    public static final int DOWN = 4;
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


    public SatEncoding generateSat(NumberLink numberLink) {
        m_limit[DOWN] = numberLink.getRow();
        m_limit[RIGHT] = numberLink.getCol();
        int max_num = numberLink.getMaxNum();
        int adding_vars = max_num - 1;
        int[][] inputs = numberLink.getInputs();
        int variables = 0;
        int clauses = 0;
        List<String> rules = new ArrayList<>();
        for (int i = 1; i < inputs.length; i++) {
            for (int j = 1; j < inputs[i].length; j++) {

                // cells have number
                if (inputs[i][j] != 0) {
                    // rule0: only the existed value is TRUE
                    // rule1: other value is FALSE
                    List<String> rule0 = valueFromInput(i, j, inputs[i][j], numberLink);
                    List<String> rule1 = notValuesFromInput(i, j, inputs[i][j], numberLink);
                    List<String> rule2 = exact_one_direction(i, j, numberLink);

                    rules.addAll(rule1);
                    rules.addAll(rule0);
                    rules.addAll(rule2);

                    clauses += rule0.size() + rule1.size() + rule2.size();

                    // blank cell
                } else {
                    List<String> rule1 = onlyOneValue(i, j, numberLink);
                    List<String> rule2 = has_two_directions(i, j, numberLink);

                    rules.addAll(rule1);
                    rules.addAll(rule2);

                    clauses += rule1.size() + rule2.size();
                }
            }
        }
        // Phải xem xem chỗ nào dùng biến thì mới cộng biến
        variables = m_limit[DOWN] * m_limit[RIGHT] * max_num +
                adding_vars * (m_limit[DOWN] * m_limit[RIGHT] - max_num * 2);
        // max_num * 2: tổng số ô có số trong bảng
        // Do chỉ thực hiện Encoding cho blank cells trong trường hợp onlyOneValue (mỗi ô chỉ có 1 giá trị)
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
        // => Nếu dùng Sequential encounter encoding thì cần 5 mệnh đề để biểu diễn AT MOST ONE cho mỗi ô
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
        int maxNum = numberLink.getMaxNum();
        int newVars = maxNum - 1;
        // ALO
//        String ALOclauses = "";
//        for (int k = 1; k <= maxNum; k++) {
//            ALOclauses += computePosition(i, j, k, numberLink) + " ";
//        }
//        ALOclauses += "0";
//        resultStringList.add(ALOclauses);

        // AMO
        String firstClause = "";
        firstClause += -computePosition(i, j, 1, numberLink) + " " + computePosition(i, j, maxNum + 1, numberLink) + " 0";
        resultStringList.add(firstClause);
        for (int k = 2; k < numberLink.getMaxNum(); k++) {
            String tmp1 = -computePosition(i, j, k, numberLink) + " " + computePosition(i, j, k + maxNum, numberLink) + " 0";
            String tmp2 = -computePosition(i, j, k - 1 + maxNum, numberLink) + " " + computePosition(i, j, k + maxNum, numberLink) + " 0";
            String tmp3 = -computePosition(i, j, k - 1 + maxNum, numberLink) + " " + -computePosition(i, j, k, numberLink) + " 0";
            resultStringList.add(tmp1);
            resultStringList.add(tmp2);
            resultStringList.add(tmp3);
        }
        String finalClause = "";
        finalClause += -computePosition(i, j, newVars + maxNum, numberLink) + " " + -computePosition(i, j, maxNum, numberLink) + " 0";
        resultStringList.add(finalClause);
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
        int n = numberLink.getCol();
        int max_num = numberLink.getMaxNum();
        int adding_vars = max_num - 1;
        int X_vars = numberLink.getRow() * numberLink.getCol() * max_num;
        if (value <= max_num)
            return n * (i - 1) * max_num + (j - 1) * max_num + value;
        return X_vars + n * (i - 1) * adding_vars + (j - 1) * adding_vars + value;
    }

    public int getValueOfY(int positionValue, int maxNum, NumberLink numberLink) {
        int rows = numberLink.getRow();
        int cols = numberLink.getCol();
        if (positionValue <= rows * cols * maxNum) {
            return (positionValue - 1) % maxNum + 1;
        }
        return -1;
    }
}