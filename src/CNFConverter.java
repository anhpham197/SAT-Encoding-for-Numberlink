import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CNFConverter {
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int UP = 3;
    public static final int DOWN = 4;
    public static int[] m_limit = new int[]{0, 1, 10, 1, 10};
    int[][] source = new int[100][2];
    int[][] target = new int[100][2];
    int[][] blankCells;

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

    // Only = at least + at most

    // Find all adjacent cells of cell (i, j)
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
        int numOfBlankCells = m_limit[DOWN] * m_limit[RIGHT] - max_num * 2;
        blankCells = new int[numOfBlankCells][2];
        int indexBlankCell = 0;
        List<String> rules = new ArrayList<>();
        List<String> additionalRule = new ArrayList<>();
        for (int i = 1; i < inputs.length; i++) {
            for (int j = 1; j < inputs[i].length; j++) {

                // cells have number
                if (inputs[i][j] != 0) {
                    // rule0: only the existed value is TRUE
                    // rule1: other value is FALSE
                    List<String> rule0 = valueFromInput(i, j, inputs[i][j], numberLink);
                    List<String> rule1 = notValuesFromInput(i, j, inputs[i][j], numberLink);
                    List<String> rule2 = exact_one_direction(i, j, numberLink);

                    int index = inputs[i][j];
//                  Add index of numbered cells to source and target arrays
                    if (source[index][0] == 0 && source[index][1] == 0) {
                        source[index][0] = i;
                        source[index][1] = j;
                    } else {
                        target[index][0] = i;
                        target[index][1] = j;
                    }

                    rules.addAll(rule1);
                    rules.addAll(rule0);
                    rules.addAll(rule2);

                    clauses += rule0.size() + rule1.size() + rule2.size();

                    // blank cell
                } else {
                    blankCells[indexBlankCell][0] = i;
                    blankCells[indexBlankCell][1] = j;
                    indexBlankCell++;
                    List<String> rule1 = onlyOneValue(i, j, numberLink);
                    List<String> rule2 = has_two_directions(i, j, numberLink);

                    rules.addAll(rule1);
                    rules.addAll(rule2);

                    clauses += rule1.size() + rule2.size();
                }
            }
        }

        // Adding row and column contraints (addtional rule)
//        additionalRule = additionalRule(source, target, max_num, m_limit[DOWN], m_limit[RIGHT], inputs, numberLink);
//        rules.addAll(additionalRule);
//        clauses += additionalRule.size();
//        Arrays.stream(source).forEach(x -> Arrays.fill(x, 0));
//        Arrays.stream(target).forEach(x -> Arrays.fill(x, 0));

        // Cho nao dung bien thi moi cong bien
        variables = m_limit[DOWN] * m_limit[RIGHT] * max_num +
                adding_vars * numOfBlankCells;
        // max_num * 2: the number of numbered cells
        // Do chi thuc hien Encoding cho blank cells trong truong hop onlyOneValue (moi o chi co 1 gia tri)
//        for (int i = 0; i < blankCells.length; i++) {
//            for (int j = 0; j < 2; j++) {
//                System.out.print(blankCells[i][j] + " ");
//            }
//            System.out.println();
//        }
        return new SatEncoding(rules, clauses, variables);
    }

    public List<String> additionalRule(int[][] source, int[][] target, int maxNum, int row, int col, int[][] inputs, NumberLink numberlink) {
        List<String> res = new ArrayList<>();

        for (int i = 1; i <= maxNum; i++) {
            int startRow = source[i][0] > target[i][0] ? target[i][0] + 1 : source[i][0] + 1;
            int endRow = source[i][0] > target[i][0] ? source[i][0] - 1 : target[i][0] - 1;
            int startCol = source[i][1] > target[i][1] ? target[i][1] + 1 : source[i][1] + 1;
            int endCol = source[i][1] > target[i][1] ? source[i][1] - 1 : target[i][1] - 1;
            // Row constraints
            for (int j = startRow; j <= endRow; j++) {
                String rowConstraint = "";
                for (int k = 1; k <= col; k++) {
                    if (inputs[j][k] == 0) {
                        rowConstraint += computePosition(j, k, i, numberlink) + " ";
                    }
                }
                rowConstraint += "0";
                res.add(rowConstraint);
            }
            // Col constraints
            for (int j = startCol; j <= endCol; j++) {
                String colConstraint = "";
                for (int k = 1; k <= row; k++) {
                    if (inputs[k][j] == 0) {
                        colConstraint += computePosition(k, j, i, numberlink) + " ";
                    }
                }
                colConstraint += "0";
                res.add(colConstraint);
            }
        }
        return res;
    }

    // Blank cells have two directions
    private List<String> has_two_directions(int i, int j, NumberLink numberLink) {
        List<String> resultStringList = new ArrayList<>();

        String firstClause = "";
        for (int k = 1; k <= numberLink.getMaxNum(); k++) {
            firstClause = -computePosition(i, j, k, numberLink) + " ";
            List<Integer> adjacentCells = adjacentCells(i, j, k, numberLink);
            int numCells = adjacentCells.size();
            // numCells == 2: cac o o vi tri goc --> (-Xijk v Xi(j+1)k) ^ (-Xijk v X(i+1)jk)
            // numCells == 3: cac o o vi tri bien
            // numCells == 4: cac o con lai
            if (numCells == 2) {
                for (int z = 0; z <= numCells - 1; z++) {
                    String tmp2 = firstClause + adjacentCells.get(z) + " ";
                    tmp2 += "0";
                    resultStringList.add(tmp2);
                }
            } else if (numCells == 3) {
                // 2 trong 3 huong di: (-Xijk v Xi(j-1)k v Xi(j+1)k) ^ (-Xijk v Xi(j-1)k v X(i+1)jk) ^ (-Xijk v X(i+1)jk v Xi(j+1)k)
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
                // 2 trong 4 huong di: -Xijk v X(i+1)jk v Xi(j+1)k v
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


//        UEdge: �, �, �
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
        int X_vars = numberLink.getCol() * numberLink.getRow() * maxNum;
        int newVars = maxNum - 1;
        // ALO
        String ALOclauses = "";
        for (int k = 1; k <= maxNum; k++) {
            ALOclauses += computePosition(i, j, k, numberLink) + " ";
        }
        ALOclauses += "0";
        resultStringList.add(ALOclauses);

        // AMO
        String firstClause = "";
        firstClause += -computePosition(i, j, 1, numberLink) + " " + computePositionForBlankCell(i, j, X_vars, newVars, 1, blankCells) + " 0";
        resultStringList.add(firstClause);
        for (int k = 2; k < numberLink.getMaxNum(); k++) {
            String tmp1 = -computePosition(i, j, k, numberLink) + " " + computePositionForBlankCell(i, j, X_vars, newVars, k, blankCells) + " 0";
            String tmp2 = -computePositionForBlankCell(i, j, X_vars, newVars, k - 1, blankCells) + " " + computePositionForBlankCell(i, j, X_vars, newVars, k, blankCells) + " 0";
            String tmp3 = -computePositionForBlankCell(i, j, X_vars, newVars, k - 1, blankCells) + " " + -computePosition(i, j, k, numberLink) + " 0";
            resultStringList.add(tmp1);
            resultStringList.add(tmp2);
            resultStringList.add(tmp3);
        }
        String finalClause = "";
        finalClause += -computePositionForBlankCell(i, j, X_vars, newVars, newVars, blankCells) + " " + -computePosition(i, j, maxNum, numberLink) + " 0";
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

    private int computePositionForBlankCell(int i, int j, int X_vars, int newVars, int value, int[][] blankCells) {
        int[] subArr = {i, j};
        for (int k = 0; k < blankCells.length; k++) {
            if (Arrays.equals(blankCells[k], subArr)) {
                return X_vars + k * newVars + value;
            }
        }
        return -1;
    }

    private int computePosition(int i, int j, int value, NumberLink numberLink) {
        int n = numberLink.getCol();
        int max_num = numberLink.getMaxNum();
        return n * (i - 1) * max_num + (j - 1) * max_num + value;
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