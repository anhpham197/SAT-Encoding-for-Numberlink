import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NumberLinkResponse implements Serializable{
    private List<List<Cell>> cells;
    public NumberLinkResponse() {
        cells = new ArrayList<>();
    }
    public List<List<Cell>> getCells() {
        return cells;
    }
}
