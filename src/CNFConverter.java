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

    // Ton tai duy nhat = toi da + toi thieu

    // Cac o lien ke voi cac o dang xet
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
        int row = (int) Math.ceil(Math.sqrt(max_num));
        int col = (int) Math.ceil((double) max_num / row);
        int adding_vars = row + col;
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

                    rules.addAll(rule0);
                    rules.addAll(rule1);
                    rules.addAll(rule2);

                    clauses += rule0.size() + rule1.size() + rule2.size();

                    // blank cell
                } else if (inputs[i][j] == 0) {
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

        // Adding row and column constraints (additional rule)
        additionalRule = additionalRule(source, target, max_num, m_limit[DOWN], m_limit[RIGHT], inputs, numberLink);
        rules.addAll(additionalRule);
        clauses += additionalRule.size();
        Arrays.stream(source).forEach(x -> Arrays.fill(x, 0));
        Arrays.stream(target).forEach(x -> Arrays.fill(x, 0));

        // Phai xem xem cho nao dung bien thi moi cong bien
        variables = m_limit[DOWN] * m_limit[RIGHT] * max_num + adding_vars * (m_limit[DOWN] * m_limit[RIGHT] - 2 * max_num);
        // max_num * 2: tong so o co so trong bang
        // Do chi thuc hien Encoding cho blank cells trong truong hop onlyOneValue (moi o chi co 1 gia tri)

        // Convert string array to int array
//        List<String> tmp = new ArrayList<>();
//        for (String rule : rules) {
//            String[] arr = rule.split(" ");
//            for (String s : arr) {
//                tmp.add(s);
//            }
//        }
//        int[] res = new int[tmp.size()];
//        for (int i = 0; i < tmp.size(); i++) {
//            res[i] = Math.abs(Integer.parseInt(tmp.get(i)));
//        }
//
//        // Count distinct number in res
//        Set<Integer> set = new HashSet<>();
//        for (int i : res) {
//            if (i != 0) {
//                set.add(i);
//            }
//        }
//        System.out.println("Distinct number: " + set.size());

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
            // numCells == 2: � � v� tr� g�c --> (-Xijk v Xi(j+1)k) ^ (-Xijk v X(i+1)jk)
            // numCells == 3: � � v� tr� bi�n
            // numCells == 4: � � c�c v� tr� c�n l�i
            if (numCells == 2) {
                for (int z = 0; z <= numCells - 1; z++) {
                    String tmp2 = firstClause + adjacentCells.get(z) + " ";
                    tmp2 += "0";
                    resultStringList.add(tmp2);
                }
            } else if (numCells == 3) {
                // 2 trong 3 h��ng i: (-Xijk v Xi(j-1)k v Xi(j+1)k) ^ (-Xijk v Xi(j-1)k v X(i+1)jk) ^ (-Xijk v X(i+1)jk v Xi(j+1)k)
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

        // AT MOST 1 is TRUE --> t�i sao kh�ng d�ng c�ng th�c d�a tr�n bi�n m�i nh� trong slides th�y Kh�nh
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
        int row = (int) Math.ceil(Math.sqrt(maxNum));
        int col = (int) Math.ceil((double) maxNum / row);
        int X_vars = numberLink.getCol() * numberLink.getRow() * maxNum;
        int newVars = row + col; // k --> row --> col

        // ALO
//        String ALOclause = "";
//        for (int k = 1; k <= maxNum; k++) {
//            ALOclause += computePosition(i, j, k, numberLink) + " ";
//        }
//        ALOclause += "0";
//        resultStringList.add(ALOclause);

        // AMO for group of rows
        for (int r = 1; r <= row - 1; r++) {
            String tmp = -computePositionForBlankCell(i, j, X_vars, newVars, r, blankCells) + " ";
            for (int k = r + 1; k <= row; k++) {
                String clause = tmp + -computePositionForBlankCell(i, j, X_vars, newVars, k, blankCells) + " 0";
                resultStringList.add(clause);
            }
        }

        // AMO for group of columns
        for (int c = 1; c <= col - 1; c++) {
            String tmp = -computePositionForBlankCell(i, j, X_vars, newVars, c + row, blankCells) + " ";
            for (int k = c + 1; k <= col; k++) {
                String clause = tmp + -computePositionForBlankCell(i, j, X_vars, newVars, k + row, blankCells) + " 0";
                resultStringList.add(clause);
            }
        }

        // Xi � ru ' cv <=> (-Xi v ru) ' (-Xi v cv)
        int num = 1;
        int[][] value = new int[row][col];
        for (int r = 0; r < row; r++) {
            for (int c = 0; c < col; c++) {
                if (num <= maxNum) {
                    value[r][c] = num;
                    num++;
                } else {
                    break;
                }
            }
        }
        for (int k = 1; k <= maxNum; k++) {
            int r = findIndex(value, k)[0];
            int c = findIndex(value, k)[1];
            String tmp = -computePosition(i, j, k, numberLink) + " ";
            String rowClause = tmp + computePositionForBlankCell(i, j, X_vars, newVars, r, blankCells) + " 0";
            String colClause = tmp + computePositionForBlankCell(i, j, X_vars, newVars, c + row, blankCells) + " 0";
            resultStringList.add(rowClause);
            resultStringList.add(colClause);
        }
        return resultStringList;
    }


    // Find the index of 2d array
    private int[] findIndex(int[][] arr, int value) {
        int[] index = new int[2];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                if (arr[i][j] == value) {
                    index[0] = i + 1;
                    index[1] = j + 1;
                    break;
                }
            }
        }
        return index;
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
        int maxNum = numberLink.getMaxNum();
        int row = (int) Math.ceil(Math.sqrt(maxNum));
        int col = (int) Math.ceil((double) maxNum / row);
        int adding_vars = row + col;
        int X_vars = numberLink.getRow() * numberLink.getCol() * maxNum;
        if (value <= maxNum)
            return n * (i - 1) * maxNum + (j - 1) * maxNum + value;
        return X_vars + n * (i - 1) * adding_vars + (j - 1) * adding_vars + value;
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

    public int getValueOfY(int positionValue, int maxNum, NumberLink numberLink) {
        int rows = numberLink.getRow();
        int cols = numberLink.getCol();
        if (positionValue <= rows * cols * maxNum) {
            return (positionValue - 1) % maxNum + 1;
        }
        return -1;
    }
}