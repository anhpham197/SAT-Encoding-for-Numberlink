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
        int adding_vars = (int) Math.sqrt(max_num);
        int[][] inputs = numberLink.getInputs();
        int variables = 0;
        int clauses = 0;
        List<String> rules = new ArrayList<>();
        int count = 0;
        int[][] visited = new int[numberLink.getMaxNum() * 2][2];
        for (int i = 1; i < inputs.length; i++) {
            for (int j = 1; j < inputs[i].length; j++) {

                // cells have number
                if (inputs[i][j] != 0) {
                    // rule0: only the existed value is TRUE
                    // rule1: other value is FALSE
                    List<String> rule0 = valueFromInput(i, j, inputs[i][j], numberLink);
                    List<String> rule1 = notValuesFromInput(i, j, inputs[i][j], numberLink);
                    List<String> rule2 = exact_one_direction(i, j, numberLink);
                    List<String> rowConstraints = new ArrayList<>();
                    List<String> colConstraints = new ArrayList<>();

                    rules.addAll(rule1);
                    rules.addAll(rule0);
                    rules.addAll(rule2);

                    clauses += rule0.size() + rule1.size() + rule2.size();

                    if (!checkCellVisited(i, j, visited)) {
                        // Mark numbered cells as visited
                        int[] arrNum = findCellHasSameNumber(i, j, inputs[i][j], numberLink);
                        visited[count][0] = i;
                        visited[count][1] = j;
                        visited[count + 1][0] = arrNum[0];
                        visited[count + 1][1] = arrNum[1];
                        count += 2;

                        // Add constraints
                        rowConstraints = rowConstraints(i, j, arrNum[0], arrNum[1], inputs[i][j], numberLink);
                        colConstraints = colConstraints(i, j, arrNum[0], arrNum[1], inputs[i][j], numberLink);
                        rules.addAll(rowConstraints);
                        rules.addAll(colConstraints);
                        clauses += colConstraints.size() + rowConstraints.size();
                    }

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

    // For blank cells: AT MOST one value is TRUE in each blank cell
    private List<String> onlyOneValue(int i, int j, NumberLink numberLink) {
        List<String> resultStringList = new ArrayList<>();
        int maxNum = numberLink.getMaxNum();
        // newVars = groupSize
        int groupSize = (int) Math.sqrt(maxNum);
        int varsPerGroup = maxNum / groupSize;

        // ALO for groupSize groups
//        String ALOclause = "";
//        for (int k = 1; k <= groupSize; k++) {
//            ALOclause += computePosition(i, j, k + maxNum, numberLink) + " ";
//        }
//        ALOclause += "0";
//        resultStringList.add(ALOclause);

        // AMO on the set of all commander variables: (-c1 v -c2)  (-c1 v -c3)  (-c2 v -c3)
        for (int k = 1; k <= groupSize - 1; k++) {
            String tmp = -computePosition(i, j, k + maxNum, numberLink) + " ";
            for (int q = k + 1; q <= groupSize; q++) {
                String clause = tmp + -computePosition(i, j, q + maxNum, numberLink) + " 0";
                resultStringList.add(clause);
            }
        }

        // AMO for each group Gi
        int startOfGi = 1;
        int endOfGi = varsPerGroup;
        if (maxNum % groupSize == 0) {
            for (int k = 1; k <= groupSize; k++) {
                for (int q = startOfGi; q <= endOfGi - 1; q++) {
                    String tmp = -computePosition(i, j, q, numberLink) + " ";
                    for (int l = q + 1; l <= endOfGi; l++) {
                        String clause = tmp + -computePosition(i, j, l, numberLink) + " 0";
                        resultStringList.add(clause);
                    }
                }
                startOfGi = endOfGi + 1;
                endOfGi += varsPerGroup;
            }
        } else {
            for (int k = 1; k < groupSize; k++) {
                for (int q = startOfGi; q <= endOfGi - 1; q++) {
                    String tmp = -computePosition(i, j, q, numberLink) + " ";
                    for (int l = q + 1; l <= endOfGi; l++) {
                        String clause = tmp + -computePosition(i, j, l, numberLink) + " 0";
                        resultStringList.add(clause);
                    }
                }
                startOfGi = endOfGi + 1;
                endOfGi += varsPerGroup;
            }
            // last group
            for (int k = (groupSize - 1) * varsPerGroup + 1; k < maxNum; k++) {
                String tmp = -computePosition(i, j, k, numberLink) + " ";
                for (int l = k + 1; l <= maxNum; l++) {
                    String clause = tmp + -computePosition(i, j, l, numberLink) + " 0";
                    resultStringList.add(clause);
                }
            }
        }


        // If Ci is TRUE --> some variables in Gi are TRUE
        int startOfCi = 1;
        int endOfCi = varsPerGroup;
        if (maxNum % groupSize == 0) {
            for (int k = 1; k <= groupSize; k++) {
                String tmp = -computePosition(i, j, k + maxNum, numberLink) + " ";
                for (int q = startOfCi; q <= endOfCi; q++) {
                    tmp += computePosition(i, j, q, numberLink) + " ";
                }
                tmp += "0";
                resultStringList.add(tmp);
                startOfCi = endOfCi + 1;
                endOfCi += varsPerGroup;
            }
        } else {
            for (int k = 1; k < groupSize; k++) {
                String tmp = -computePosition(i, j, k + maxNum, numberLink) + " ";
                for (int q = startOfCi; q <= endOfCi; q++) {
                    tmp += computePosition(i, j, q, numberLink) + " ";
                }
                tmp += "0";
                resultStringList.add(tmp);
                startOfCi = endOfCi + 1;
                endOfCi += varsPerGroup;
            }
            // last group
            String tmp = -computePosition(i, j, groupSize + maxNum, numberLink) + " ";
            for (int k = (groupSize - 1) * varsPerGroup; k <= maxNum; k++) {
                tmp += computePosition(i, j, k, numberLink) + " 0";
            }
            resultStringList.add(tmp);
        }

        // If Ci is FALSE --> all variables in Gi is FALSE
        startOfCi = 1;
        endOfCi = varsPerGroup;
        if (maxNum % groupSize == 0) {
            for (int k = 1; k <= groupSize; k++) {
                String tmp = computePosition(i, j, k + maxNum, numberLink) + " ";
                for (int l = startOfCi; l <= endOfCi; l++) {
                    String clause = tmp + -computePosition(i, j, l, numberLink) + " 0";
                    resultStringList.add(clause);
                }
                startOfCi = endOfCi + 1;
                endOfCi += varsPerGroup;
            }
        } else {
            for (int k = 1; k < groupSize; k++) {
                String tmp = computePosition(i, j, k + maxNum, numberLink) + " ";
                for (int l = startOfCi; l <= endOfCi; l++) {
                    String clause = tmp + -computePosition(i, j, l, numberLink) + " 0";
                    resultStringList.add(clause);
                }
                startOfCi = endOfCi + 1;
                endOfCi += varsPerGroup;
            }
            // last group
            String tmp = computePosition(i, j, groupSize + maxNum, numberLink) + " ";
            for (int k = (groupSize - 1) * varsPerGroup + 1; k <= maxNum; k++) {
                String clause = tmp + -computePosition(i, j, k, numberLink) + " 0";
                resultStringList.add(clause);
            }
        }

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

    // Constraints of rows and columns between two cells have same number

    private int[] findCellHasSameNumber(int i, int j, int num, NumberLink numberlink) {
        int[] arrNum = new int[2];
        int[][] input = numberlink.getInputs();
        for (int p = 1; p < input.length; p++) {
            for (int q = 1; q < input[p].length; q++) {
                if (p == i && q == j) continue;
                else if (input[p][q] == num) {
                    arrNum[0] = p;
                    arrNum[1] = q;
                    return arrNum;
                }
            }
        }
        return null;
    }

    private boolean checkCellVisited(int i, int j, int[][] visited) {
        for (int p = 0; p < visited.length; p++)
            if (visited[p][0] == i && visited[p][1] == j)
                return true;
        return false;
    }

    private List<String> rowConstraints(int x1, int y1, int x2, int y2, int num, NumberLink numberlink) {
        List<String> resultStringList = new ArrayList<>();
        int startRow = x1 < x2 ? x1 : x2;
        int endRow = x1 > x2 ? x1 : x2;
        for (int i = startRow; i <= endRow; i++) {
            String clause = "";
            for (int j = 1; j <= numberlink.getCol(); j++) {
//                if ((i == x1 && j == y1) || (i == x2 && j == y2))
//                    continue;
                clause += computePosition(i, j, num, numberlink) + " ";
            }
            clause += "0";
            resultStringList.add(clause);
        }
        return resultStringList;
    }

    private List<String> colConstraints(int x1, int y1, int x2, int y2, int num, NumberLink numberlink) {
        List<String> resultStringList = new ArrayList<>();
        int startCol = y1 < y2 ? y1 : y2;
        int endCol = y1 > y2 ? y1 : y2;
        for (int i = startCol; i <= endCol; i++) {
            String clause = "";
            for (int j = 1; j <= numberlink.getRow(); j++) {
//                if ((j == x1 && i == y1) || (j == x2 && i == y2))
//                    continue;
                clause += computePosition(j, i, num, numberlink) + " ";
            }
            clause += "0";
            resultStringList.add(clause);
        }
        return resultStringList;
    }


    private int computePosition(int i, int j, int value, NumberLink numberLink) {
        int n = numberLink.getCol();
        int max_num = numberLink.getMaxNum();
        int adding_vars = (int) Math.sqrt(max_num);
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