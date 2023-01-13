import java.util.ArrayList;
import java.util.List;

public class Cell {
    int i;
    int j;
    int value;
    List<Integer> pattern;

    public Cell(int i, int j, int value) {
        this.i = i;
        this.j = j;
        this.value = value;
        this.pattern = new ArrayList<>();
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

    public int getValue() {
        return value;
    }

    public List<Integer> getPattern() {
        return pattern;
    }
}
